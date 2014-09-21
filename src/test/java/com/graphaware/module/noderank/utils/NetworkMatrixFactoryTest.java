package com.graphaware.module.noderank.utils;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.*;


import static org.junit.Assert.*;


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