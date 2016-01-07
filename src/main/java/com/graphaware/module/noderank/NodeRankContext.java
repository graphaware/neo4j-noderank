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
