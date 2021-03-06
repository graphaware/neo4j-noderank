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

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import com.graphaware.runtime.config.function.StringToNodeInclusionPolicy;
import com.graphaware.runtime.config.function.StringToRelationshipInclusionPolicy;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;

/**
 * {@link RuntimeModuleBootstrapper} for {@link NodeRankModule}.
 */
public class NodeRankModuleBootstrapper implements RuntimeModuleBootstrapper {

    private static final Log LOG = LoggerFactory.getLogger(NodeRankModuleBootstrapper.class);

    private static final String MAX_TOP_RANK_NODES = "maxTopRankNodes";
    private static final String DAMPING = "dampingFactor";
    private static final String PROPERTY_KEY = "propertyKey";
    private static final String NODE = "node";
    private static final String RELATIONSHIP = "relationship";

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRankModule bootstrapModule(String moduleId, Map<String, String> config, GraphDatabaseService database) {
        LOG.info("Constructing new module with ID: %s", moduleId);
        LOG.debug("Configuration parameter map is: %s", config);

        NodeRankModuleConfiguration configuration = NodeRankModuleConfiguration.defaultConfiguration();

        if (config.get(PROPERTY_KEY) != null) {
            LOG.info("Property key set to %s", config.get(PROPERTY_KEY));
            configuration = configuration.withRankPropertyKey(config.get(PROPERTY_KEY));
        }

        if (config.get(MAX_TOP_RANK_NODES) != null) {
            LOG.info("Max top rank nodes set to %s", config.get(MAX_TOP_RANK_NODES));
            configuration = configuration.withMaxTopRankNodes(Integer.valueOf(config.get(MAX_TOP_RANK_NODES)));
        }

        if (config.get(DAMPING) != null) {
            LOG.info("Damping factor set to %s", config.get(DAMPING));
            configuration = configuration.withDampingFactor(Double.valueOf(config.get(DAMPING)));
        }

        if (config.get(NODE) != null) {
            NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply(config.get(NODE));
            LOG.info("Node Inclusion Policy set to %s", policy);
            configuration = configuration.with(policy);
        }

        if (config.get(RELATIONSHIP) != null) {
            RelationshipInclusionPolicy policy = StringToRelationshipInclusionPolicy.getInstance().apply(config.get(RELATIONSHIP));
            LOG.info("Relationship Inclusion Policy set to %s", policy);
            configuration = configuration.with(policy);
        }

        return new NodeRankModule(moduleId, configuration);
    }

}
