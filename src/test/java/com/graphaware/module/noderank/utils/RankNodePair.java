/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.noderank.utils;

import com.google.common.base.Objects;
import com.graphaware.common.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * IndexNode pair, used for sorting the results of Page Rank algorithm
 * <p/>
 * The two RNP are equal if their names are equal. The comparable is
 * implemented with respect to the rank algorithm result value however!
 */
public class RankNodePair extends Pair<Double, Long> implements Comparable<RankNodePair> {

    /**
     * Construct a new pair.
     *
     * @param rank rank of the node
     * @param node node data
     */
    public RankNodePair(double rank, Long node) {
        super(rank, node);
    }

    /**
     * Return rank stored in the INP
     *
     * @return returns rank of the node
     */
    public double rank() {
        return first();
    }

    /**
     * Returns node stored in the INP
     */
    public Long node() {
        return second();
    }

    /**
     * Converts RankNodePairs to ArrayList of nodes
     *
     * @param rankNodePairs list
     * @return converted list of nodes
     */
    public static List<Long> convertToRankedNodeList(List<RankNodePair> rankNodePairs) {
        List<Long> toReturn = new ArrayList<>();

        // I am sure there is a plenty of room for improvement here ;)
        for (RankNodePair indexNodePair : rankNodePairs) {
            toReturn.add(indexNodePair.node());
        }

        return toReturn;
    }

    /**
     * Compares two ranks in descending order
     *
     * @param o RankNodePair to be compared to
     * @return -1 if this > o.rank(), 1 if < and 0 if =
     */
    @Override
    public int compareTo(RankNodePair o) {
        if (rank() > o.rank()) {
            return -1;
        }
        if (rank() < o.rank()) {
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
        if (!(other instanceof RankNodePair)) return false;
        RankNodePair otherRNP = (RankNodePair) other;
        return otherRNP.second().equals(second());
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(first(), second());
    }
}
