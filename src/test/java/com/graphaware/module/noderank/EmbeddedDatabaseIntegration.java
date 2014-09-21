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

        Thread.sleep(2000);

        ExecutionResult executionResult = new ExecutionEngine(database).execute("MATCH (p:Person) WHERE p.nodeRank > 0 RETURN p");

        assertTrue("The page rank module didn't run on startup", executionResult.iterator().hasNext());
    }

    private void populateDatabase(GraphDatabaseService database) {
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
}
