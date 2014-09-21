package com.graphaware.module.noderank;

import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *  Unit test for {@link NodeRankContext}.
 */
public class NodeRankContextTest {

    @Test
    public void emptyTopNodesShouldProduceEmptyList() {
         assertTrue(new NodeRankContext(0L).getTopRankedNodes().isEmpty());
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

        NodeRankContext context = new NodeRankContext(0L);

        context.addRankedNode(node1, 10, 3);
        context.addRankedNode(node2, 1, 3);
        context.addRankedNode(node3, 2, 3);
        context.addRankedNode(node4, 4, 3);

        List<RankedNode> result = context.getTopRankedNodes();
        assertEquals(3, result.size());

        assertEquals(1L, result.get(0).getNodeId());
        assertEquals(4L, result.get(1).getNodeId());
        assertEquals(3L, result.get(2).getNodeId());

        context.addRankedNode(node5, 1, 3);
        context.addRankedNode(node2, 3, 3);
        context.addRankedNode(node3, 5, 3);
        context.addRankedNode(node2, 6, 3);
        context.addRankedNode(node2, 7, 3);

        result = context.getTopRankedNodes();
        assertEquals(3, result.size());

        assertEquals(1L, result.get(0).getNodeId());
        assertEquals(2L, result.get(1).getNodeId());
        assertEquals(3L, result.get(2).getNodeId());
    }
}
