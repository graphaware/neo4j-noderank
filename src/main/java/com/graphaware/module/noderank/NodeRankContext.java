package com.graphaware.module.noderank;

import com.graphaware.runtime.metadata.NodeBasedContext;
import org.neo4j.graphdb.Node;

/**
 * Context for the {@link NodeRankModule} that extends {@link NodeBasedContext} and also remembers a
 * number of nodes with highest node ranks.
 */
public class NodeRankContext extends NodeBasedContext {

    private Long[] topNodes;

    public NodeRankContext(long nodeId, Long[] topNodes) {
        super(nodeId);
        this.topNodes = topNodes;
    }

    public NodeRankContext(Node node, Long[] topNodes) {
        super(node);
        this.topNodes = topNodes;
    }

    public NodeRankContext(long nodeId, long earliestNextCall, Long[] topNodes) {
        super(nodeId, earliestNextCall);
        this.topNodes = topNodes;
    }

    public NodeRankContext(Node node, long earliestNextCall, Long[] topNodes) {
        super(node, earliestNextCall);
        this.topNodes = topNodes;
    }

    public Long[] getTopNodes() {
        return topNodes;
    }
}
