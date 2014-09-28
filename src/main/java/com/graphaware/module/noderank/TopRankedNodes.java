package com.graphaware.module.noderank;

import com.graphaware.common.util.BoundedSortedList;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A container for top ranked nodes.
 */
public class TopRankedNodes {

    private static final Logger LOG = LoggerFactory.getLogger(TopRankedNodes.class);

    private BoundedSortedList<Node, Integer> topNodes;

    public List<Node> getTopNodes() {
        if (topNodes == null) {
            return Collections.emptyList();
        }

        return topNodes.getItems();
    }

    public void addNode(Node node, int rank) {
        if (topNodes == null) {
            throw new IllegalStateException("Please initialize top ranked nodes first");
        }

        topNodes.add(node, rank);
    }

    public void initializeIfNeeded(NodeRankContext context, GraphDatabaseService database, NodeRankModuleConfiguration config) {
        if (topNodes != null) {
            return;
        }

        topNodes = new BoundedSortedList<>(config.getMaxTopRankNodes(), Collections.<Integer>reverseOrder());

        if (context == null) {
            return;
        }

        for (long nodeId : context.getTopNodes()) {
            try {
                topNodes.add(database.getNodeById(nodeId), (int) database.getNodeById(nodeId).getProperty(config.getRankPropertyKey(), 0));
            } catch (Exception e) {
                LOG.warn("Exception while adding ranked node " + nodeId + " to the collection of top ranked nodes. Will ignore...", e);
            }
        }
    }

    public Long[] getTopNodeIds() {
        List<Long> result = new LinkedList<>();

        for (Node node : getTopNodes()) {
            result.add(node.getId());
        }

        return result.toArray(new Long[result.size()]);
    }
}
