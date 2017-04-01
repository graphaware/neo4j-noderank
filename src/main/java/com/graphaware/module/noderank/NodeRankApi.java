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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.kernel.impl.core.GraphProperties;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.runtime.metadata.DefaultTimerDrivenModuleMetadata;

public class NodeRankApi {

	private final GraphDatabaseService database;

	public NodeRankApi(GraphDatabaseService database) {
		this.database = database;
	}

	/**
	 * Need to be call during a transaction
	 * 
	 * @param moduleId
	 * @param limit
	 * @return
	 */
	public List<Node> getTopRankedNodes(String moduleId, int limit) {
		List<Node> result = new LinkedList<>();

		DependencyResolver dependencyResolver = ((GraphDatabaseAPI) database).getDependencyResolver();
		NodeManager resolveDependency = dependencyResolver.resolveDependency(NodeManager.class);		
		GraphProperties properties = resolveDependency.newGraphProperties();
		Map<String, Object> allProperties = properties.getAllProperties();
		List<String> collect = allProperties.keySet().stream().filter(k -> k.contains(moduleId)).collect(Collectors.toList());
		
		// not synchronized yet or don't exists
		if(collect.isEmpty()){
			throw new NotFoundException("No module with ID " + moduleId + " has been registered");
		}
		
		String key = collect.get(0);
		Object v = allProperties.get(key);
		byte[] array = (byte[]) v;
		DefaultTimerDrivenModuleMetadata metadata = Serializer.fromByteArray(array);
		NodeRankContext lastContext = (NodeRankContext) metadata.lastContext();
		if (lastContext != null) {
			Long[] topNodes = lastContext.getTopNodes();
			for (Long id : topNodes) {
				Node node = database.getNodeById(id);
				result.add(node);
			}
		}
		return result;
	}

}
