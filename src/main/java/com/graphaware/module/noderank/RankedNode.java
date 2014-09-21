package com.graphaware.module.noderank;

/**
 * A node ID with a rank.
 */
public class RankedNode implements Comparable<RankedNode> {

    private final long nodeId;
    private final int rank;

    public RankedNode(long nodeId, int rank) {
        this.nodeId = nodeId;
        this.rank = rank;
    }

    public long getNodeId() {
        return nodeId;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public int compareTo(RankedNode o) {
        return Integer.valueOf(getRank()).compareTo(o.getRank());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RankedNode that = (RankedNode) o;

        if (nodeId != that.nodeId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (nodeId ^ (nodeId >>> 32));
    }
}
