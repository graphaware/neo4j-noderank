package com.graphaware.module.noderank;

import com.graphaware.common.strategy.*;
import com.graphaware.runtime.strategy.IncludeAllBusinessNodes;
import com.graphaware.runtime.strategy.IncludeAllBusinessRelationships;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Contains configuration settings for the page rank module.
 */
public class PageRankModuleConfiguration {

	private final NodeInclusionStrategy nodeInclusionStrategy;
	private final NodeCentricRelationshipInclusionStrategy relationshipInclusionStrategy;

	/**
	 * Retrieves the default {@link PageRankModuleConfiguration}, which includes all (non-internal) nodes and relationships.
	 *
	 * @return The default {@link PageRankModuleConfiguration}
	 */
	public static PageRankModuleConfiguration defaultConfiguration() {
		return new PageRankModuleConfiguration(IncludeAllBusinessNodes.getInstance(), IncludeAllBusinessRelationships.getInstance());
	}

	/**
	 * Constructs a new {@link PageRankModuleConfiguration} based on the given inclusion strategies.
	 *
	 * @param nodeInclusionStrategy The {@link InclusionStrategy} to use for selecting nodes to include in the page rank
	 *        algorithm
	 * @param relationshipInclusionStrategy The {@link InclusionStrategy} for selecting which relationships to follow when
	 *        crawling the graph
	 */
	public PageRankModuleConfiguration(NodeInclusionStrategy nodeInclusionStrategy,
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
