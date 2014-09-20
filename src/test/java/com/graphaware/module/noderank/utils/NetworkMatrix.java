package com.graphaware.module.noderank.utils;


import org.la4j.matrix.Matrix;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * An object returned by {@link NetworkMatrixFactory} matrix methods. Contains list of the node IDs and an adjacency matrix.
 * <p/>
 * The list of nodes is an ordered array of node IDs with indices corresponding to rows/columns in the adjacency matrix (and derivates).
 */
public class NetworkMatrix {

    private final List<Long> nodeList;
    private final Matrix matrix;

    public NetworkMatrix(Matrix matrix, List<Long> nodeList) {
        this.matrix = matrix;
        this.nodeList = nodeList;
    }

    /**
     * Returns an ordered array of node IDs. Node indices correspond to rows/columns of the adjacency matrix.
     *
     * @return ordered list of node IDs.
     */
    public List<Long> getNodeList() {
        return nodeList;
    }

    /**
     * Returns the stored matrix.
     *
     * @return matrix corresponding to the network.
     */
    public Matrix getMatrix() {
        return matrix;
    }
}
