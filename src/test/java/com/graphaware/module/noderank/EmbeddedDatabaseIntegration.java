package com.graphaware.module.noderank;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import com.graphaware.runtime.ProductionRuntime;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.Pair;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.test.impl.EphemeralFileSystemAbstraction;

public class EmbeddedDatabaseIntegration  {

    @Test
    public void shouldSuccessfullyInitialiseAndRunModuleWhenDatabaseIsStarted() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
                .loadPropertiesFromFile("src/test/resources/test-neo4j.properties")
                .newGraphDatabase();

        ProductionRuntime.getRuntime(database).waitUntilStarted();

        populateDatabase(database);

        Thread.sleep(500);

        ExecutionResult executionResult = new ExecutionEngine(database).execute("MATCH (p:Person) WHERE p.nodeRank > 0 RETURN p");

        assertTrue("The page rank module didn't run on startup", executionResult.iterator().hasNext());
    }

    private void populateDatabase(GraphDatabaseService database) {
        ExecutionEngine engine = new ExecutionEngine(database);
        engine.execute("MERGE (:Person {name:'Jeff'})-[:BOSS_OF]-(:Person {name:'Chris'});");
        engine.execute("MERGE (:Person {name:'Jeff'})-[:BOSS_OF]-(:Person {name:'Paul'});");
        engine.execute("MERGE (:Person {name:'Jeff'})-[:BOSS_OF]-(:Person {name:'Matthew'});");
        engine.execute("MERGE (:Person {name:'Gary'})-[:BOSS_OF]-(:Person {name:'Alan'});");
        engine.execute("MERGE (:Person {name:'Gary'})-[:BOSS_OF]-(:Person {name:'Robbie'});");
        engine.execute("MERGE (:Person {name:'Gary'})-[:BOSS_OF]-(:Person {name:'Mark'});");
        engine.execute("MERGE (:Person {name:'Gary'})-[:BOSS_OF]-(:Person {name:'Sue'});");
        engine.execute("MERGE (:Person {name:'John'})-[:BOSS_OF]-(:Person {name:'Matthew'});");
        engine.execute("MERGE (:Person {name:'John'})-[:BOSS_OF]-(:Person {name:'Sue'});");
    }
}
