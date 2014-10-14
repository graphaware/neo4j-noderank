package com.graphaware.module.noderank;

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.RuntimeRegistry;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class EmbeddedDatabaseIntegration2 {

    @Test
    public void shouldSuccessfullyInitialiseAndRunModuleWhenDatabaseIsStarted() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);  //where database is an instance of GraphDatabaseService
        NodeRankModule module = new NodeRankModule("NR");
        runtime.registerModule(module);
        runtime.start();

        populateDatabase(database);

        Thread.sleep(2000);

        ExecutionResult executionResult = new ExecutionEngine(database).execute("MATCH (p:Person) WHERE p.nodeRank > 0 RETURN p");

        assertTrue("The page rank module didn't run on startup", executionResult.iterator().hasNext());

        NodeRankModule nodeRankModule = RuntimeRegistry.getStartedRuntime(database).getModule("NR", NodeRankModule.class);
        List<Node> topNodes = nodeRankModule.getTopNodes().getTopNodes();
        assertTrue(topNodes.size() > 0);
    }

    private void populateDatabase(GraphDatabaseService database) {
        ExecutionEngine engine = new ExecutionEngine(database);
        engine.execute("CREATE " +
                " (m:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'})," +
                " (m)-[:FRIEND_OF]->(v:Person {name:'Vojta'})," +
                " (m)-[:FRIEND_OF]->(a:Person {name:'Adam'})," +
                " (m)-[:FRIEND_OF]->(vi:Person {name:'Vince'})," +
                " (m)-[:FRIEND_OF]->(:Person {name:'Luanne'})," +
                " (vi)-[:FRIEND_OF]->(a)," +
                " (d)-[:FRIEND_OF]->(a)," +
                " (d)-[:FRIEND_OF]->(vi)," +
                " (v)-[:FRIEND_OF]->(a)");
    }
}
