package com.graphaware.module.noderank;

import com.graphaware.test.integration.NeoServerIntegrationTest;
import static org.junit.Assert.*;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static com.graphaware.test.util.TestUtils.executeCypher;
import static com.graphaware.test.util.TestUtils.get;

public class NodeRankApiTest extends NeoServerIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String neo4jConfigFile() {
        return "int-test-neo4j.properties";
    }

    @Test
    public void shouldRetrieveTopNodes() throws InterruptedException {
        executeCypher(baseUrl(), "CREATE (m:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'}),"+
        " (m)-[:FRIEND_OF]->(v:Person {name:'Vojta'}),"+
        " (m)-[:FRIEND_OF]->(a:Person {name:'Adam'}),"+
        " (m)-[:FRIEND_OF]->(vi:Person {name:'Vince'}),"+
        " (m)-[:FRIEND_OF]->(:Person {name:'Luanne'}),"+
        " (vi)-[:FRIEND_OF]->(a),"+
        " (d)-[:FRIEND_OF]->(a),"+
        " (d)-[:FRIEND_OF]->(vi),"+
        " (v)-[:FRIEND_OF]->(a)");

        Thread.sleep(10000);

        String s = get(baseUrl() + "/graphaware/noderank/noderank/", HttpStatus.OK.value());
        assertTrue(s.contains("{\"id\":1,\"labels\":[\"Person\"],\"name\":\"Michal\",\"nodeRank\""));
    }

    @Test
    public void requestToUnknownModuleShouldProduce404() throws InterruptedException {
        get(baseUrl() + "/graphaware/noderank/unknown/",  HttpStatus.NOT_FOUND.value());
    }
}
