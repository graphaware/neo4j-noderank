package com.graphaware.module.noderank;

import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.proc.Procedures;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NodeRankProcedureTest extends GraphAwareIntegrationTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        //this is a hack - Neo4j doesn't detect procedures on the classpath that aren't packaged up as .jars.
        //in the next release, the Framework will provide a better way of doing this
        Procedures procedures = ((GraphDatabaseFacade) getDatabase()).getDependencyResolver().resolveDependency(Procedures.class);
        procedures.register(NodeRankProcedure.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String configFile() {
        return "int-test-neo4j.conf";
    }

    @Test
    public void testProcedureCall() throws InterruptedException, IOException {
        try (Transaction tx = getDatabase().beginTx()) {
            getDatabase().execute("CREATE (m:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'})," +
                    " (m)-[:FRIEND_OF]->(v:Person {name:'Vojta'})," +
                    " (m)-[:FRIEND_OF]->(a:Person {name:'Adam'})," +
                    " (m)-[:FRIEND_OF]->(vi:Person {name:'Vince'})," +
                    " (m)-[:FRIEND_OF]->(:Person {name:'Luanne'})," +
                    " (vi)-[:FRIEND_OF]->(a)," +
                    " (d)-[:FRIEND_OF]->(a)," +
                    " (d)-[:FRIEND_OF]->(vi)," +
                    " (v)-[:FRIEND_OF]->(a)");

            tx.success();
        }

        Thread.sleep(30000);

        try (Transaction tx = getDatabase().beginTx()) {
            Result result = getDatabase().execute("CALL ga.noderank.getTopRanked('noderank', 10) YIELD node RETURN node");
            List<Node> ranked = new LinkedList<>();
            while (result.hasNext()) {
                Map<String, Object> record = result.next();
                ranked.add((Node) record.get("node"));
            }
            assertEquals(0, ranked.get(0).getId());
            assertEquals("Michal", ranked.get(0).getProperty("name"));
            tx.success();
        }
    }
}
