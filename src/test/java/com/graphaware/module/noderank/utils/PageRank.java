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

import org.la4j.LinearAlgebra;
import org.la4j.factory.Basic1DFactory;
import org.la4j.factory.CRSFactory;
import org.la4j.factory.Factory;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.sort;

/**
 * Testing implementation of PageRank. As PR is a global operation which requires a full adjacency matrix, this class
 * is meant to be used for TESTING PURPOSES only and mainly as a benchmark against the WEAKER NeoRank.
 */
public class PageRank {

    /**
     * Returns PageRank of the nodes from the network.
     * <p/>
     * WARNING: The stored graph must be single-component (the
     * matrix must be irreducible for the algorithm to
     * succeed)
     *
     * @param transitionMatrix transition mtx of the system
     * @return pageRank vector
     */
    public Vector getPageRankVector(NetworkMatrix transitionMatrix, double damping) {
        validateArguments(transitionMatrix, damping);

        Factory vectorFactory = new Basic1DFactory();
        Factory matrixFactory = new CRSFactory();

        int size;

        // Calculates the pageRank. The convergence to PageRank is guaranteed
        // by picking the vector which converges to Perron Vector by
        // Perron-Frobenius theorem

        size = transitionMatrix.getMatrix().rows();

        Matrix identityMatrix = matrixFactory.createIdentityMatrix(size);
        Vector testVector = vectorFactory.createConstantVector(size, 1);
        Matrix inverse = identityMatrix.add(transitionMatrix.getMatrix().multiply(-damping)).withInverter(LinearAlgebra.InverterFactory.SMART).inverse();
        Matrix pageRankOperator = identityMatrix.multiply(1 - damping).multiply(inverse);
        Vector pageRank = pageRankOperator.multiply(testVector);

        return pageRank;
    }

    /**
     * Returns a pageRanked array list of nodes contained in the network.
     *
     * @return returns a list of nodes
     */
    public List<Long> getPageRank(NetworkMatrix transitionMatrix, double damping) {
        return RankNodePair.convertToRankedNodeList(getPageRankPairs(transitionMatrix, damping));
    }

    /**
     * Returns (rank, node) pairs sorted in descending order by pageRank.
     *
     * @param transitionMatrix tr. matrix
     * @param damping          damping factor
     * @return rankNodePair list
     */
    public List<RankNodePair> getPageRankPairs(NetworkMatrix transitionMatrix, double damping) {
        Vector pageRankVector = getPageRankVector(transitionMatrix, damping);
        List<Long> nodeList = transitionMatrix.getNodeList();
        List<RankNodePair> rankNodePairs = new ArrayList<>(pageRankVector.length());

        for (int i = 0; i < pageRankVector.length(); ++i) {
            rankNodePairs.add(new RankNodePair(pageRankVector.get(i), nodeList.get(i)));
        }

        sort(rankNodePairs);

        return rankNodePairs;
    }

    /**
     * Throws an exception if the argument set is invalid.
     *
     * @param transitionMatrix tr. matrix
     * @param damping          damping factor
     */
    private void validateArguments(NetworkMatrix transitionMatrix, double damping) {
        if (damping > 1.0 || damping < 0 || transitionMatrix == null) {
            throw new IllegalArgumentException("Wrong arguments passed on input");
        }
    }
}
