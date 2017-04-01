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

import com.graphaware.runtime.RuntimeRegistry;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;

import java.util.LinkedList;
import java.util.List;

public class NodeRankApi {

    private final GraphDatabaseService database;

    public NodeRankApi(GraphDatabaseService database) {
        this.database = database;
    }

    public List<Node> getTopRankedNodes(String moduleId, int limit) {
        List<Node> result = new LinkedList<>();
        NodeRankModule module = RuntimeRegistry.getStartedRuntime(database).getModule(moduleId, NodeRankModule.class);

        try (Transaction tx = database.beginTx()) {
            for (Node node : module.getTopNodes().getTopNodes()) {
                try {
                    result.add(node);

                    if (result.size() >= limit) {
                        break;
                    }

                } catch (NotFoundException e) {
                    //oh well, deleted in the meantime
                }
            }

            tx.success();
        }

        return result;
    }

}
