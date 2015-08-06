package com.graphaware.module.noderank;

import com.graphaware.test.integration.NeoServerIntegrationTest;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertTrue;


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
        httpClient.executeCypher(baseUrl(), "CREATE (m:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'})," +
                " (m)-[:FRIEND_OF]->(v:Person {name:'Vojta'})," +
                " (m)-[:FRIEND_OF]->(a:Person {name:'Adam'})," +
                " (m)-[:FRIEND_OF]->(vi:Person {name:'Vince'})," +
                " (m)-[:FRIEND_OF]->(:Person {name:'Luanne'})," +
                " (vi)-[:FRIEND_OF]->(a)," +
                " (d)-[:FRIEND_OF]->(a)," +
                " (d)-[:FRIEND_OF]->(vi)," +
                " (v)-[:FRIEND_OF]->(a)");

        Thread.sleep(30000);

        String s = httpClient.get(baseUrl() + "/graphaware/noderank/noderank/", HttpStatus.OK.value());
        System.out.println(s);
        assertTrue(s.contains("[{\"id\":0,\"properties\":{\"name\":\"Michal\",\"nodeRank\":"));
    }

    @Test
    public void requestToUnknownModuleShouldProduce404() throws InterruptedException {
        httpClient.get(baseUrl() + "/graphaware/noderank/unknown/",  HttpStatus.NOT_FOUND.value());
    }
}
