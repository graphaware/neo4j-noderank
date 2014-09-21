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
        topNodes.initializeTopRankedNodesIfNeeded(null, getDatabase(), NodeRankModuleConfiguration.defaultConfiguration().withMaxTopRankNodes(3));
        assertTrue(topNodes.getTopRankedNodes().isEmpty());
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
        topNodes.initializeTopRankedNodesIfNeeded(null, getDatabase(), NodeRankModuleConfiguration.defaultConfiguration().withMaxTopRankNodes(3));

        topNodes.addRankedNode(node1, 10, 3);
        topNodes.addRankedNode(node2, 1, 3);
        topNodes.addRankedNode(node3, 2, 3);
        topNodes.addRankedNode(node4, 4, 3);

        List<RankedNode> result = topNodes.getTopRankedNodes();
        assertEquals(3, result.size());

        assertEquals(1L, result.get(0).getNodeId());
        assertEquals(4L, result.get(1).getNodeId());
        assertEquals(3L, result.get(2).getNodeId());
        assertArrayEquals(new Long[]{1L, 4L, 3L}, topNodes.produceTopRankedNodes());

        topNodes.addRankedNode(node5, 1, 3);
        topNodes.addRankedNode(node2, 3, 3);
        topNodes.addRankedNode(node3, 5, 3);
        topNodes.addRankedNode(node2, 6, 3);
        topNodes.addRankedNode(node2, 7, 3);

        result = topNodes.getTopRankedNodes();
        assertEquals(3, result.size());

        assertEquals(1L, result.get(0).getNodeId());
        assertEquals(2L, result.get(1).getNodeId());
        assertEquals(3L, result.get(2).getNodeId());
        assertArrayEquals(new Long[]{1L, 2L, 3L}, topNodes.produceTopRankedNodes());
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
            topNodes.initializeTopRankedNodesIfNeeded(new NodeRankContext(0L, new Long[]{0L, 10L, 1L, 2L}), getDatabase(), NodeRankModuleConfiguration.defaultConfiguration().withMaxTopRankNodes(3));
            tx.success();
        }

        List<RankedNode> result = topNodes.getTopRankedNodes();
        assertEquals(3, result.size());

        assertEquals(0L, result.get(0).getNodeId());
        assertEquals(10, result.get(0).getRank());
        assertEquals(1L, result.get(1).getNodeId());
        assertEquals(5, result.get(1).getRank());
        assertEquals(2L, result.get(2).getNodeId());
        assertEquals(3, result.get(2).getRank());
        assertArrayEquals(new Long[]{0L, 1L, 2L}, topNodes.produceTopRankedNodes());
    }
}
