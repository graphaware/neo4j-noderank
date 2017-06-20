/*
 * Copyright (c) 2013-2016 GraphAware
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

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.util.ReservoirSampler;
import com.graphaware.runtime.config.TimerDrivenModuleConfiguration;
import com.graphaware.runtime.config.util.InstanceRoleUtils;
import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.runtime.module.BaseTimerDrivenModule;
import com.graphaware.runtime.module.TimerDrivenModule;
import com.graphaware.runtime.walk.NodeSelector;
import com.graphaware.runtime.walk.RandomNodeSelector;
import com.graphaware.runtime.walk.RandomRelationshipSelector;
import com.graphaware.runtime.walk.RelationshipSelector;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;

import java.util.Random;

/**
 * A {@link TimerDrivenModule} that perpetually walks the graph by randomly following relationships and increments
 * a configured node property called as it goes.
 * <p/>
 * Sooner or later, depending on the size and shape of the network, it will converge to values that would be computed
 * by PageRank algorithm (not normalised).
 */
public class NodeRankModule extends BaseTimerDrivenModule<NodeRankContext> implements TimerDrivenModule<NodeRankContext> {

    private static final Log LOG = LoggerFactory.getLogger(NodeRankModule.class);

    private final NodeRankModuleConfiguration config;
    private final NodeSelector nodeSelector;
    private final RelationshipSelector relationshipSelector;
    private final TopRankedNodes topNodes = new TopRankedNodes();
    private final Random random = new Random();
    private long counter;

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
        this.counter = 0L; //config.getInitialCounter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerDrivenModuleConfiguration getConfiguration() {
        return config;
    }

    /**
     * Initialize 'counter' which is needed for NodeRank percentage calculation
     */
    private void initializeCounter(GraphDatabaseService database) {
        if (database == null)
          return;

        // attempt to recover NodeRank counter
        try {
          Result resSum = database.execute("MATCH (n) WHERE exists(n.nodeRankCounter) RETURN sum(n.nodeRankCounter) AS sum");
          if (resSum.hasNext())
            this.counter = (Long)(resSum.next().get("sum"));
        } catch (Exception e) {
          LOG.error("Exception thrown while getting sum of nodeRank counters:", e);
          this.counter = 0L;
        }

        LOG.info("NodeRank counter initialized to %d", this.counter);
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
            LOG.debug("NodeRank did not find a node to start with. There are no nodes matching the configuration.");
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
            LOG.debug("NodeRank did not find a node to continue with. There are no nodes matching the configuration.");
            return lastContext;
        }

        // important to check the existing state when this module is starting (better would be in the constructor but how to access 'database' there?)
        if (this.counter==0L)
          initializeCounter(database);

        /*int rankValue = (int) nextNode.getProperty(config.getRankPropertyKey(), 0) + 1;
        nextNode.setProperty(config.getRankPropertyKey(), rankValue);
        topNodes.addNode(nextNode, rankValue);*/
        int rankCounterValue = (int) nextNode.getProperty(config.getRankPropertyCounterKey(), 0);
        if (rankCounterValue==0) {
          rankCounterValue = setToOneOrEstimate(nextNode, database);
          this.counter += rankCounterValue; // since we added (estimated) number of hops to a new node, we also need to update overall counter
        } else {
          rankCounterValue += 1;
          this.counter += 1L;
        }
        double rankValue = 100.*rankCounterValue/counter;
        nextNode.setProperty(config.getRankPropertyCounterKey(), rankCounterValue);
        nextNode.setProperty(config.getRankPropertyKey(), rankValue);
        topNodes.addNode(nextNode, rankValue);

        if (counter%100000==0)
          LOG.debug("NodeRank counter = %d", counter);

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
            LOG.debug("Node referenced in last context with ID %s was not found in the database.  Will start from a random node.", lastContext);
            return null;
        }
    }

    private Node determineNextNode(Node currentNode, GraphDatabaseService database) {
        if (currentNode == null) {
            return nodeSelector.selectNode(database);
        }

        // hyperjump
        if (random.nextDouble() > config.getDampingFactor()) {
            LOG.debug("Performing hyperjump");
            return nodeSelector.selectNode(database);
        }

        Relationship randomRelationship;
        if (!config.getDirections()) { // directions don't matter
          randomRelationship = relationshipSelector.selectRelationship(currentNode);
          if (randomRelationship == null) {
            LOG.debug("Dead end at %s, selecting a new random node", currentNode);
            return nodeSelector.selectNode(database);
          }
        } else { // directions do matter (would be better to change RandomRelationshipSelector in the Framework)
          ReservoirSampler<Relationship> randomSampler = new ReservoirSampler<>(1);
          for (Relationship r : currentNode.getRelationships(Direction.OUTGOING)) {
            if (config.getRelationshipInclusionPolicy().include(r, currentNode))
              randomSampler.sample(r);
          }
          if (randomSampler.isEmpty()) {
            LOG.debug("Dead end at %s, selecting a new random node", currentNode);
            return nodeSelector.selectNode(database);
          }
          randomRelationship = randomSampler.getSamples().iterator().next();
        }

        Node result = randomRelationship.getOtherNode(currentNode);

        if (!config.getNodeInclusionPolicy().include(result)) {
            LOG.info("Relationship Inclusion Policy allows for a relationship, which leads to a node that " +
                    "is not included by the Node Inclusion Policy. This is likely a mis-configuration");
        }

        return result;
    }

    public TopRankedNodes getTopNodes() {
        return topNodes;
    }

    private int setToOneOrEstimate(Node node, GraphDatabaseService database) {
      int NR = 1;
      if (node==null || database==null)
        return NR;

      // Retrieve number of nodes after inclusion policy
      int nNodes = 0;
      for (Node n : database.getAllNodes()) {
        if (config.getNodeInclusionPolicy().include(n))
          nNodes += 1;
      }

      // If the counter is in advanced stage (meaning this node should have been already visited many times),
      //   estimate it's NodeRank based on standard PageRank equation to converge to the right value as quickly as possible
      double d = config.getDampingFactor();
      if (d>0.95) d = 0.95;
      if (this.counter > 3*nNodes/(1-d)) { // i.e. high probability that the current node was added later (purely based on (1-d), i.e. ignoring relationships)
        LOG.debug("Estimating NodeRank for node %s", node.getId());
        // Get total # nodes that pass inclusion policy
        int totNodes = 0;
        for (Node n : database.getAllNodes()) {
          if (config.getNodeInclusionPolicy().include(n))
            totNodes++;
        }

        double estimate = this.counter*(1-d)/totNodes;
        for (Relationship r : node.getRelationships()) {
          if (!config.getRelationshipInclusionPolicy().include(r, node))
            continue;
          if (config.getDirections() && r.getEndNode().getId()!=node.getId()) // i.e. only incoming relationships (when directions matter)
            continue;
          int nRels = 0;
          Node newNode = r.getOtherNode(node);
          if (!config.getNodeInclusionPolicy().include(newNode)) {
            LOG.debug("Relationship Inclusion Policy allows for a relationship, which leads to a node that " +
                  "is not included by the Node Inclusion Policy. This is likely a mis-configuration");
          }
          if (!newNode.hasProperty(config.getRankPropertyCounterKey()))
            continue;
          for (Relationship r2 : newNode.getRelationships()) {
            if (!config.getRelationshipInclusionPolicy().include(r2, newNode))
              continue;
            if (config.getDirections() && r2.getEndNode().getId()==newNode.getId()) // i.e. only outgoing relationships (when directions matter)
              continue;
            nRels++;
          }
          if (nRels>0)
            estimate += d * (int)newNode.getProperty(config.getRankPropertyCounterKey())/nRels; // based on PageRank equation
        }
        NR = (int)estimate;
        LOG.debug("NodeRank estimated to %d (total counter is %d)", NR, this.counter);
      }

      return NR;
    }
}
