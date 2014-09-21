package com.graphaware.module.noderank;

import com.graphaware.runtime.metadata.NodeBasedContext;
import org.neo4j.graphdb.Node;

/**
 * Context for the {@link NodeRankModule} that extends {@link NodeBasedContext} and also remembers a
 * number of nodes with highest node ranks.
 */
public class NodeRankContext extends NodeBasedContext {

    private Long[] topRankedNodes;

    public NodeRankContext(long nodeId, Long[] topRankedNodes) {
        super(nodeId);
        this.topRankedNodes = topRankedNodes;
    }

    public NodeRankContext(Node node, Long[] topRankedNodes) {
        super(node);
        this.topRankedNodes = topRankedNodes;
    }

    public NodeRankContext(long nodeId, long earliestNextCall, Long[] topRankedNodes) {
        super(nodeId, earliestNextCall);
        this.topRankedNodes = topRankedNodes;
    }

    public NodeRankContext(Node node, long earliestNextCall, Long[] topRankedNodes) {
        super(node, earliestNextCall);
        this.topRankedNodes = topRankedNodes;
    }

    public Long[] getTopRankedNodes() {
        return topRankedNodes;
    }
}
