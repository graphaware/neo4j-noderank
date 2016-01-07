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


import org.la4j.matrix.Matrix;

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
