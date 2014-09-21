package com.graphaware.module.noderank;

import com.graphaware.runtime.metadata.NodeBasedContext;
import org.neo4j.graphdb.Node;

import javax.swing.*;
import java.util.*;

/**
 * Context for the {@link NodeRankModule} that extends {@link NodeBasedContext} and also remembers a configurable
 * number of nodes with highest node ranks.
 */
public class NodeRankContext extends NodeBasedContext {

    private List<RankedNode> topRankedNodes;

    public NodeRankContext(long nodeId) {
        super(nodeId);
    }

    public NodeRankContext(Node node) {
        super(node);
    }

    public NodeRankContext(long nodeId, long earliestNextCall) {
        super(nodeId, earliestNextCall);
    }

    public NodeRankContext(Node node, long earliestNextCall) {
        super(node, earliestNextCall);
    }

    public NodeRankContext(long nodeId, List<RankedNode> topRankedNodes) {
        super(nodeId);
        this.topRankedNodes = topRankedNodes;
    }

    public NodeRankContext(Node node, List<RankedNode> topRankedNodes) {
        super(node);
        this.topRankedNodes = topRankedNodes;
    }

    public NodeRankContext(long nodeId, long earliestNextCall, List<RankedNode> topRankedNodes) {
        super(nodeId, earliestNextCall);
        this.topRankedNodes = topRankedNodes;
    }

    public NodeRankContext(Node node, long earliestNextCall, List<RankedNode> topRankedNodes) {
        super(node, earliestNextCall);
        this.topRankedNodes = topRankedNodes;
    }

    public List<RankedNode> getTopRankedNodes() {
        if (topRankedNodes == null) {
            topRankedNodes = new LinkedList<>();
        }

        return topRankedNodes;
    }

    public void addRankedNode(Node node, int rank, int maxTopRankedNodes) {
        if (topRankedNodes == null) {
            topRankedNodes = new LinkedList<>();
        }

        RankedNode rankedNode = new RankedNode(node.getId(), rank);

        if (topRankedNodes.contains(rankedNode)) {
            topRankedNodes.remove(rankedNode);
        }

        topRankedNodes.add(rankedNode);

        Collections.sort(topRankedNodes, Collections.reverseOrder());

        while (topRankedNodes.size() > maxTopRankedNodes) {
            topRankedNodes.remove(topRankedNodes.size()-1);
        }
    }
}
