package com.graphaware.module.noderank;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A container for top ranked nodes.
 */
public class TopRankedNodes {

    private static final Logger LOG = LoggerFactory.getLogger(TopRankedNodes.class);

    private List<RankedNode> topRankedNodes;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public List<RankedNode> getTopRankedNodes() {
        if (topRankedNodes == null) {
            throw new IllegalStateException("Please initialize top ranked nodes first");
        }

        lock.readLock().lock();
        try {
            return new LinkedList<>(topRankedNodes);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addRankedNode(Node node, int rank, int maxTopRankedNodes) {
        if (topRankedNodes == null) {
            throw new IllegalStateException("Please initialize top ranked nodes first");
        }

        lock.readLock().lock();
        try {
            if (topRankedNodes.size() >= maxTopRankedNodes && topRankedNodes.get(topRankedNodes.size() - 1).getRank() > rank) {
                return;
            }
        } finally {
            lock.readLock().unlock();
        }

        RankedNode rankedNode = new RankedNode(node.getId(), rank);

        lock.writeLock().lock();
        try {
            if (topRankedNodes.contains(rankedNode)) {
                topRankedNodes.remove(rankedNode);
            }

            topRankedNodes.add(rankedNode);

            Collections.sort(topRankedNodes, Collections.reverseOrder());

            while (topRankedNodes.size() > maxTopRankedNodes) {
                topRankedNodes.remove(topRankedNodes.size() - 1);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void initializeTopRankedNodesIfNeeded(NodeRankContext context, GraphDatabaseService database, NodeRankModuleConfiguration config) {
        if (topRankedNodes != null) {
            return;
        }

        lock.writeLock().lock();
        try {
            topRankedNodes = new ArrayList<>(config.getMaxTopRankNodes());

            if (context == null) {
                return;
            }

            for (long nodeId : context.getTopRankedNodes()) {
                try {
                    topRankedNodes.add(new RankedNode(nodeId, (int) database.getNodeById(nodeId).getProperty(config.getRankPropertyKey(), 0)));
                } catch (Exception e) {
                    LOG.warn("Exception while adding ranked node " + nodeId + " to the collection of top ranked nodes. Will ignore...", e);
                }
            }

            Collections.sort(topRankedNodes, Collections.reverseOrder());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Long[] produceTopRankedNodes() {
        List<Long> result = new LinkedList<>();

        for (RankedNode rankedNode : getTopRankedNodes()) {
            result.add(rankedNode.getNodeId());
        }

        return result.toArray(new Long[result.size()]);
    }
}
