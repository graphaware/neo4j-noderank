package com.graphaware.module.noderank;

import java.util.Map;

import com.graphaware.common.strategy.*;
import com.graphaware.runtime.strategy.IncludeAllBusinessNodes;
import com.graphaware.runtime.strategy.IncludeAllBusinessRelationships;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphaware.module.noderank.parser.ModuleConfigParameterParser;
import com.graphaware.module.noderank.parser.RegexModuleConfigParameterParser;
import com.graphaware.runtime.module.RuntimeModuleBootstrapper;

/**
 * {@link RuntimeModuleBootstrapper} used by the {@link com.graphaware.runtime.GraphAwareRuntime} to prepare the
 * {@link RandomWalkerPageRankModule}.
 */
public class RandomWalkerPageRankModuleBootstrapper implements RuntimeModuleBootstrapper {

	private static final Logger LOG = LoggerFactory.getLogger(RandomWalkerPageRankModuleBootstrapper.class);

	private final ModuleConfigParameterParser configParameterParser = new RegexModuleConfigParameterParser();

	@Override
	public RandomWalkerPageRankModule bootstrapModule(String moduleId, Map<String, String> configParams, GraphDatabaseService database) {
		LOG.info("Constructing new module with ID: {}", moduleId);
		LOG.trace("Configuration parameter map is: {}", configParams);

		// parse Cypher-like expressions to configure inclusion strategies
		NodeInclusionStrategy nodeInclusionStrategy = configParams.containsKey("inclusionStrategy.node")
				? this.configParameterParser.parseForNodeInclusionStrategy(configParams.get("inclusionStrategy.node"))
				: IncludeAllBusinessNodes.getInstance();
		NodeCentricRelationshipInclusionStrategy relationshipInclusionStrategy = configParams.containsKey("inclusionStrategy.relationship")
				? this.configParameterParser.parseForRelationshipInclusionStrategy(configParams.get("inclusionStrategy.relationship"))
				: IncludeAllBusinessRelationships.getInstance();

		return new RandomWalkerPageRankModule(moduleId,
				new PageRankModuleConfiguration(nodeInclusionStrategy, relationshipInclusionStrategy));
	}

}
