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

package com.graphaware.module.noderank.utils;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import static org.junit.Assert.assertEquals;


public class NetworkMatrixFactoryTest extends DatabaseIntegrationTest {

    @Override
    public void populateDatabase(GraphDatabaseService database) {
        ExecutionEngine engine = new ExecutionEngine(database);
        engine.execute( "CREATE " +
                " (m:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'}),"+
                " (m)-[:FRIEND_OF]->(v:Person {name:'Vojta'}),"+
                " (m)-[:FRIEND_OF]->(a:Person {name:'Adam'}),"+
                " (m)-[:FRIEND_OF]->(vi:Person {name:'Vince'}),"+
                " (m)-[:FRIEND_OF]->(:Person {name:'Luanne'}),"+
                " (vi)-[:FRIEND_OF]->(a),"+
                " (d)-[:FRIEND_OF]->(a),"+
                " (d)-[:FRIEND_OF]->(vi),"+
                " (v)-[:FRIEND_OF]->(a)");
    }

    @Test
    public void shouldCalculateCorrectPageRank() {
        try (Transaction tx = getDatabase().beginTx()) {
            NetworkMatrixFactory networkMatrixFactory = new NetworkMatrixFactory(getDatabase());
            PageRank pageRank = new PageRank();

            NetworkMatrix adjacencyMatrix = networkMatrixFactory.getAdjacencyMatrix();
            NetworkMatrix transitionMatrix = networkMatrixFactory.getTransitionMatrix();

            System.out.println(adjacencyMatrix.getMatrix().toString());
            System.out.println(transitionMatrix.getMatrix().toString());

            System.out.println(pageRank.getPageRankVector(transitionMatrix, 0.85));

            Object name = getDatabase().getNodeById(pageRank.getPageRank(transitionMatrix, 0.85).get(0)).getProperty("name");
            System.out.println("The highest PageRank in the network is: " + name);

            assertEquals("Michal", name);

            tx.success();
        }
    }
}