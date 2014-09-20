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
        engine.execute("MERGE (:Person {name:'John'})-[:BOSS_OF]-(:Person {name:'Carmack'});");
        engine.execute("MERGE (:Person {name:'John'})-[:BOSS_OF]-(:Person {name:'Paul'});");
        engine.execute("MERGE (:Person {name:'Romero'})-[:BOSS_OF]-(:Person {name:'Carmack'});");
        engine.execute("MERGE (:Person {name:'Adrian'})-[:BOSS_OF]-(:Person {name:'Carmack'});");
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

            assertEquals("John", name);

            tx.success();
        }
    }
}