package com.graphaware.module.noderank.utils;

import com.graphaware.common.util.Pair;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Objects;

/**
 * IndexNode pair, used for sorting the results of Page Rank algorithm
 *
 * The two RNP are equal if their names are equal. The comparable is
 * implemented with respect to the rank algorithm result value however!
 */
public class RankNodePair extends Pair<Double, Node> implements Comparable<RankNodePair>{

    /**
     * Construct a new pair.
     *
     * @param  rank rank of the node
     * @param node node data
     */
    public RankNodePair(double rank, Node node) {
        super(rank, node);
    }

    /**
     * Return rank stored in the INP
     * @return returns rank of the node
     */
    public double rank() {
        return first();
    }

    /**
     * Returns node stored in the INP
     */
    public Node node() {
        return second();
    }

    /**
     * Converts RankNodePairs to ArrayList of nodes
     * @param rankNodePairs list
     * @return converted list of nodes
     */
    public static List<Node> convertToRankedNodeList(List<RankNodePair> rankNodePairs) {
        List<Node> toReturn = new ArrayList<>();

        // I am sure there is a plenty of room for improvement here ;)
        for (RankNodePair indexNodePair : rankNodePairs) {
            toReturn.add(indexNodePair.node());
        }

        return toReturn;
    }

    /**
     * Compares two ranks in descending order
     * @param o RankNodePair to be compared to
     * @return -1 if this > o.rank(), 1 if < and 0 if =
     */
    @Override
    public int compareTo(RankNodePair o) {
        if(rank() > o.rank()){
            return -1;
        }
        if(rank() < o.rank()){
            return 1;
        }
        return 0;
    }

    /**
     * Two rank node pairs are equal iff their name is equal.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof RankNodePair))return false;
        RankNodePair otherRNP = (RankNodePair) other;
        return otherRNP.second().equals(second());
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(first(), second());
    }
}
