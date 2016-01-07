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

import com.graphaware.api.json.JsonNode;
import com.graphaware.api.json.LongIdJsonNode;
import com.graphaware.runtime.RuntimeRegistry;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * REST API for {@link NodeRankModule}.
 */
@Controller
@RequestMapping("/noderank")
public class NodeRankApi {

    private final GraphDatabaseService database;

    @Autowired
    public NodeRankApi(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(value = "/{moduleId}", method = RequestMethod.GET)
    @ResponseBody
    public List<JsonNode> topRankedNodes(@PathVariable String moduleId, @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<JsonNode> result = new LinkedList<>();

        NodeRankModule module = RuntimeRegistry.getStartedRuntime(database).getModule(moduleId, NodeRankModule.class);

        try (Transaction tx = database.beginTx()) {
            for (Node node : module.getTopNodes().getTopNodes()) {
                try {
                    result.add(new LongIdJsonNode(node));

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

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArguments() {
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound() {
    }
}
