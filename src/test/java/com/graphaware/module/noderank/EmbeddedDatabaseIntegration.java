package com.graphaware.module.noderank;

import com.graphaware.runtime.RuntimeRegistry;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.junit.Assert.assertTrue;

public class EmbeddedDatabaseIntegration  {

    @Test
    public void shouldSuccessfullyInitialiseAndRunModuleWhenDatabaseIsStarted() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder()
                .loadPropertiesFromFile("src/test/resources/test-neo4j.properties")
                .newGraphDatabase();

        RuntimeRegistry.getStartedRuntime(database);

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
