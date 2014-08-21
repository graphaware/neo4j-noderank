package com.graphaware.module.noderank;

import com.graphaware.common.strategy.*;
import com.graphaware.runtime.strategy.IncludeAllBusinessNodes;
import com.graphaware.runtime.strategy.IncludeAllBusinessRelationships;

/**
 * Contains configuration settings for the page rank module.
 */
public class NodeRankModuleConfiguration {

	private final NodeInclusionStrategy nodeInclusionStrategy;
	private final NodeCentricRelationshipInclusionStrategy relationshipInclusionStrategy;

	/**
	 * Retrieves the default {@link NodeRankModuleConfiguration}, which includes all (non-internal) nodes and relationships.
	 *
	 * @return The default {@link NodeRankModuleConfiguration}
	 */
	public static NodeRankModuleConfiguration defaultConfiguration() {
		return new NodeRankModuleConfiguration(IncludeAllBusinessNodes.getInstance(), IncludeAllBusinessRelationships.getInstance());
	}

	/**
	 * Constructs a new {@link NodeRankModuleConfiguration} based on the given inclusion strategies.
	 *
	 * @param nodeInclusionStrategy The {@link InclusionStrategy} to use for selecting nodes to include in the page rank
	 *        algorithm
	 * @param relationshipInclusionStrategy The {@link InclusionStrategy} for selecting which relationships to follow when
	 *        crawling the graph
	 */
	public NodeRankModuleConfiguration(NodeInclusionStrategy nodeInclusionStrategy,
                                       NodeCentricRelationshipInclusionStrategy relationshipInclusionStrategy) {

		this.nodeInclusionStrategy = nodeInclusionStrategy;
		this.relationshipInclusionStrategy = relationshipInclusionStrategy;
	}

    public NodeInclusionStrategy getNodeInclusionStrategy() {
        return nodeInclusionStrategy;
    }

    public NodeCentricRelationshipInclusionStrategy getRelationshipInclusionStrategy() {
        return relationshipInclusionStrategy;
    }
}
