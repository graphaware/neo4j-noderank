/*
 * Copyright (c) 2013-2015 GraphAware
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

import org.la4j.factory.CRSFactory;
import org.la4j.factory.Factory;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.sparse.CRSMatrix;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.graphaware.common.util.IterableUtils.count;
import static org.neo4j.tooling.GlobalGraphOperations.at;

/**
 * Exports the entire graph as adjacency matrix.
 * <p/>
 * WARNING: This is for testing purposes only.
 */
public class NetworkMatrixFactory {

    private final GraphDatabaseService database;

    public NetworkMatrixFactory(GraphDatabaseService database) {
        this.database = database;
    }

    /**
     * Returns an adjacency matrix in a raw List<List<Integer>> form.
     *
     * @return la4j sparse matrix.
     */
    public NetworkMatrix getAdjacencyMatrix() {
        return getMatrix(new MatrixPopulator() {
            @Override
            public void populate(CRSMatrix matrix, Map<Long, Integer> indices, Node origin, Node target) {
                matrix.set(indices.get(origin.getId()), indices.get(target.getId()), 1);
                matrix.set(indices.get(target.getId()), indices.get(origin.getId()), 1);
            }
        });
    }

    /**
     * Returns a Markov transition matrix (all entries are weighted by their out degree).
     * The matrix is in format (i <- j), the sum of any column is 1.
     */
    public NetworkMatrix getTransitionMatrix() {
        return getMatrix(new MatrixPopulator() {
            @Override
            public void populate(CRSMatrix matrix, Map<Long, Integer> indices, Node origin, Node target) {
                matrix.set(indices.get(origin.getId()), indices.get(target.getId()), 1.0 / ((float) target.getDegree()));
                matrix.set(indices.get(target.getId()), indices.get(origin.getId()), 1.0 / ((float) origin.getDegree()));
            }
        });
    }

    /**
     * Produce matrix by looking at all relationships.
     *
     * @param populator that populates the matrix for each relationship.
     * @return matrix.
     */
    private NetworkMatrix getMatrix(MatrixPopulator populator) {
        int length = countNodes();
        CRSMatrix adjacency = new CRSMatrix(length, length);

        Map<Long, Integer> indices = matrixNodeIndices(database);

        for (Relationship r : at(database).getAllRelationships()) {
            populator.populate(adjacency, indices, r.getStartNode(), r.getEndNode());
        }

        return new NetworkMatrix(adjacency, new LinkedList<>(indices.keySet()));
    }

    /**
     * Get a mapping of node IDs to indices in the matrix.
     *
     * @param database in which to find mappings.
     * @return mapping.
     */
    private Map<Long, Integer> matrixNodeIndices(GraphDatabaseService database) {
        int count = 0;
        Map<Long, Integer> result = new LinkedHashMap<>();
        for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
            result.put(node.getId(), count++);
        }
        return result;
    }

    interface MatrixPopulator {
        void populate(CRSMatrix matrix, Map<Long, Integer> indices, Node origin, Node target);
    }

    /**
     * Returns a google matrix given the specified damping constant.
     * The Google matrix is an iterative mtx for the pageRank algorithm.
     * <p/>
     * See.: The Anatomy of a Large-Scale Hypertextual Web Search Engine by Brin & Page
     *
     * @return Google matrix of the database, given the damping
     */
    public NetworkMatrix getGoogleMatrix(double damping) {
        Factory matrixFactory = new CRSFactory();

        NetworkMatrix transitionMatrixData = getTransitionMatrix();
        List<Long> nodeList = transitionMatrixData.getNodeList();
        Matrix transitionMatrix = getTransitionMatrix().getMatrix();

        int size = transitionMatrix.rows();
        Matrix identityMatrix = matrixFactory.createIdentityMatrix(size);
        Matrix googleMatrix = identityMatrix.multiply((1 - damping) / ((float) size)).add(transitionMatrix.multiply(damping));

        return new NetworkMatrix(googleMatrix, nodeList);

    }

    private int countNodes() {
        long length = count(at(database).getAllNodes());

        if (length > Integer.MAX_VALUE) {
            throw new IllegalStateException("Too many nodes in the database");
        }

        return Long.valueOf(length).intValue();
    }
}
