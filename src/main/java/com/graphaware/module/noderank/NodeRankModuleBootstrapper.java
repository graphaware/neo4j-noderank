package com.graphaware.module.noderank;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.common.policy.RelationshipInclusionPolicy;
import com.graphaware.runtime.config.function.StringToNodeInclusionPolicy;
import com.graphaware.runtime.config.function.StringToRelationshipInclusionPolicy;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * {@link RuntimeModuleBootstrapper} for {@link NodeRankModule}.
 */
public class NodeRankModuleBootstrapper implements RuntimeModuleBootstrapper {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRankModuleBootstrapper.class);

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
        LOG.info("Constructing new module with ID: {}", moduleId);
        LOG.trace("Configuration parameter map is: {}", config);

        NodeRankModuleConfiguration configuration = NodeRankModuleConfiguration.defaultConfiguration();

        if (config.get(PROPERTY_KEY) != null) {
            LOG.info("Property key set to {}", config.get(PROPERTY_KEY));
            configuration = configuration.withRankPropertyKey(config.get(PROPERTY_KEY));
        }

        if (config.get(MAX_TOP_RANK_NODES) != null) {
            LOG.info("Max top rank nodes set to {}", config.get(MAX_TOP_RANK_NODES));
            configuration = configuration.withMaxTopRankNodes(Integer.valueOf(config.get(MAX_TOP_RANK_NODES)));
        }

        if (config.get(DAMPING) != null) {
            LOG.info("Damping factor set to {}", config.get(DAMPING));
            configuration = configuration.withDampingFactor(Double.valueOf(config.get(DAMPING)));
        }

        if (config.get(NODE) != null) {
            NodeInclusionPolicy policy = StringToNodeInclusionPolicy.getInstance().apply(config.get(NODE));
            LOG.info("Node Inclusion Policy set to {}", policy);
            configuration = configuration.with(policy);
        }

        if (config.get(RELATIONSHIP) != null) {
            RelationshipInclusionPolicy policy = StringToRelationshipInclusionPolicy.getInstance().apply(config.get(RELATIONSHIP));
            LOG.info("Relationship Inclusion Policy set to {}", policy);
            configuration = configuration.with(policy);
        }

        return new NodeRankModule(moduleId, configuration);
    }

}
