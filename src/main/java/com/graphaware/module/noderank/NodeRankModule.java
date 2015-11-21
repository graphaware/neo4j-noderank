/*
 * Copyright (c) 2013-2015 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.noderank;

import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.runtime.module.BaseRuntimeModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.walk.NodeSelector;
import com.graphaware.runtime.walk.RandomNodeSelector;
import com.graphaware.runtime.walk.RandomRelationshipSelector;
import com.graphaware.runtime.walk.RelationshipSelector;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * A {@link TimerDrivenModule} that perpetually walks the graph by randomly following relationships and increments
 * a configured node property called as it goes.
 * <p/>
 * Sooner or later, depending on the size and shape of the network, it will converge to values that would be computed
 * by PageRank algorithm (not normalised).
 */
public class NodeRankModule extends BaseRuntimeModule implements TimerDrivenModule<NodeRankContext> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRankModule.class);

    private final NodeRankModuleConfiguration config;
    private final NodeSelector nodeSelector;
    private final RelationshipSelector relationshipSelector;
    private final TopRankedNodes topNodes = new TopRankedNodes();
    private final Random random = new Random();

    /**
     * Constructs a new {@link NodeRankModule} with the given ID using the default module configuration.
     *
     * @param moduleId The unique identifier for this module instance in the {@link com.graphaware.runtime.GraphAwareRuntime}.
     */
    public NodeRankModule(String moduleId) {
        this(moduleId, NodeRankModuleConfiguration.defaultConfiguration());
    }

    /**
     * Constructs a new {@link NodeRankModule} with the given ID and configuration settings.
     *
     * @param moduleId The unique identifier for this module instance in the {@link com.graphaware.runtime.GraphAwareRuntime}.
     * @param config   The {@link NodeRankModuleConfiguration} to use.
     */
    public NodeRankModule(String moduleId, NodeRankModuleConfiguration config) {
        super(moduleId);
        this.config = config;
        this.nodeSelector = new RandomNodeSelector(config.getNodeInclusionPolicy());
        this.relationshipSelector = new RandomRelationshipSelector(config.getRelationshipInclusionPolicy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        //nothing needed for now
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRankContext createInitialContext(GraphDatabaseService database) {
        Node node;

        try (Transaction tx = database.beginTx()) {
            node = nodeSelector.selectNode(database);
            tx.success();
        }

        if (node == null) {
            LOG.warn("NodeRank did not find a node to start with. There are no nodes matching the configuration.");
            return null;
        }

        LOG.info("Starting node rank graph walker from random start node...");
        return new NodeRankContext(node.getId(), new Long[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRankContext doSomeWork(NodeRankContext lastContext, GraphDatabaseService database) {
        topNodes.initializeIfNeeded(lastContext, database, config);

        Node lastNode = determineLastNode(lastContext, database);
        Node nextNode = determineNextNode(lastNode, database);

        if (nextNode == null) {
            LOG.warn("NodeRank did not find a node to continue with. There are no nodes matching the configuration.");
            return lastContext;
        }

        int rankValue = (int) nextNode.getProperty(config.getRankPropertyKey(), 0) + 1;
        nextNode.setProperty(config.getRankPropertyKey(), rankValue);
        topNodes.addNode(nextNode, rankValue);

        return new NodeRankContext(nextNode, topNodes.getTopNodeIds());
    }

    private Node determineLastNode(NodeBasedContext lastContext, GraphDatabaseService database) {
        if (lastContext == null) {
            LOG.debug("No context found. Will start from a random node.");
            return null;
        }

        try {
            return lastContext.find(database);
        } catch (NotFoundException e) {
            LOG.warn("Node referenced in last context with ID {} was not found in the database.  Will start from a random node.", lastContext);
            return null;
        }
    }

    private Node determineNextNode(Node currentNode, GraphDatabaseService database) {
        if (currentNode == null) {
            return nodeSelector.selectNode(database);
        }

        //hyperjump
        if (random.nextDouble() > config.getDampingFactor()) {
            LOG.debug("Performing hyperjump");
            return nodeSelector.selectNode(database);
        }

        Relationship randomRelationship = relationshipSelector.selectRelationship(currentNode);
        if (randomRelationship == null) {
            LOG.debug("Dead end at {}, selecting a new random node", currentNode);
            return nodeSelector.selectNode(database);
        }

        Node result = randomRelationship.getOtherNode(currentNode);

        if (!config.getNodeInclusionPolicy().include(result)) {
            LOG.warn("Relationship Inclusion Policy allows for a relationship, which leads to a node that " +
                    "is not included by the Node Inclusion Policy. This is likely a mis-configuration");
        }

        return result;
    }

    public TopRankedNodes getTopNodes() {
        return topNodes;
    }
}
