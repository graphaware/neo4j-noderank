package com.graphaware.module.noderank;

import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link NodeRankContext}.
 */
public class TopRankedNodesTest extends DatabaseIntegrationTest {

    @Test
    public void emptyTopNodesShouldProduceEmptyList() {
        TopRankedNodes topNodes = new TopRankedNodes();
        topNodes.initializeIfNeeded(null, getDatabase(), NodeRankModuleConfiguration.defaultConfiguration().withMaxTopRankNodes(3));
        assertTrue(topNodes.getTopNodes().isEmpty());
    }

    @Test
    public void nodeRanksShouldBeCorrectlySorted() {
        Node node1 = mock(Node.class);
        Node node2 = mock(Node.class);
        Node node3 = mock(Node.class);
        Node node4 = mock(Node.class);
        Node node5 = mock(Node.class);
        when(node1.getId()).thenReturn(1L);
        when(node2.getId()).thenReturn(2L);
        when(node3.getId()).thenReturn(3L);
        when(node4.getId()).thenReturn(4L);
        when(node5.getId()).thenReturn(5L);

        TopRankedNodes topNodes = new TopRankedNodes();
        topNodes.initializeIfNeeded(null, getDatabase(), NodeRankModuleConfiguration.defaultConfiguration().withMaxTopRankNodes(3));

        topNodes.addNode(node1, 10);
        topNodes.addNode(node2, 1);
        topNodes.addNode(node3, 2);
        topNodes.addNode(node4, 4);

        List<Node> result = topNodes.getTopNodes();
        assertEquals(3, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals(4L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());
        assertArrayEquals(new Long[]{1L, 4L, 3L}, topNodes.getTopNodeIds());

        topNodes.addNode(node5, 1);
        topNodes.addNode(node2, 3);
        topNodes.addNode(node3, 5);
        topNodes.addNode(node2, 6);
        topNodes.addNode(node2, 7);

        result = topNodes.getTopNodes();
        assertEquals(3, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());
        assertArrayEquals(new Long[]{1L, 2L, 3L}, topNodes.getTopNodeIds());
    }

    @Test
    public void nodeRanksShouldBeCorrectlyInitialized() {
        try (Transaction tx = getDatabase().beginTx()) {
            Node node1 = getDatabase().createNode();
            node1.setProperty("nodeRank", 10);
            Node node2 = getDatabase().createNode();
            node2.setProperty("nodeRank", 5);
            Node node3 = getDatabase().createNode();
            node3.setProperty("nodeRank", 3);

            tx.success();
        }

        TopRankedNodes topNodes = new TopRankedNodes();
        try (Transaction tx = getDatabase().beginTx()) {
            //10L doesn't exist and should be ignored:
            topNodes.initializeIfNeeded(new NodeRankContext(0L, new Long[]{0L, 10L, 1L, 2L}), getDatabase(), NodeRankModuleConfiguration.defaultConfiguration().withMaxTopRankNodes(3));
            tx.success();
        }

        List<Node> result = topNodes.getTopNodes();
        assertEquals(3, result.size());

        try (Transaction tx = getDatabase().beginTx()) {
            assertEquals(0L, result.get(0).getId());
            assertEquals(10, result.get(0).getProperty("nodeRank"));
            assertEquals(1L, result.get(1).getId());
            assertEquals(5, result.get(1).getProperty("nodeRank"));
            assertEquals(2L, result.get(2).getId());
            assertEquals(3, result.get(2).getProperty("nodeRank"));
            assertArrayEquals(new Long[]{0L, 1L, 2L}, topNodes.getTopNodeIds());
            tx.success();
        }
    }
}
