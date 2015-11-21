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

package com.graphaware.module.noderank;

import com.graphaware.module.algo.generator.GraphGenerator;
import com.graphaware.module.algo.generator.Neo4jGraphGenerator;
import com.graphaware.module.algo.generator.config.BarabasiAlbertConfig;
import com.graphaware.module.algo.generator.config.BasicGeneratorConfig;
import com.graphaware.module.algo.generator.node.SocialNetworkNodeCreator;
import com.graphaware.module.algo.generator.relationship.BarabasiAlbertRelationshipGenerator;
import com.graphaware.module.algo.generator.relationship.SocialNetworkRelationshipCreator;
import com.graphaware.module.noderank.utils.*;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.policy.all.IncludeAllBusinessNodes;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import com.graphaware.runtime.schedule.TimingStrategy;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.sort;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for page rank module.
 */
public class PageRankIntegration extends DatabaseIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(PageRankIntegration.class);

    private NodeRankModule nodeRankModule;

    public void setUp() throws Exception {
        super.setUp();
        this.nodeRankModule = new NodeRankModule("TEST");
    }

    @Test
    public void verifyRandomWalkerModuleGeneratesReasonablePageRank() {
        generateGraph();

        List<RankNodePair> pageRank = computePageRank(new NetworkMatrixFactory(getDatabase()));

        List<RankNodePair> nodeRank = computeNodeRank();

        analyseResults(pageRank, nodeRank);
    }

    private void generateGraph() {
        final int numberOfNodes = 50;
        GraphGenerator graphGenerator = new Neo4jGraphGenerator(getDatabase());

        LOG.info("Generating Barabasi-Albert social network graph with {} nodes...", numberOfNodes);

        graphGenerator.generateGraph(new BasicGeneratorConfig(new BarabasiAlbertRelationshipGenerator(
                new BarabasiAlbertConfig(numberOfNodes, 10)),
                SocialNetworkNodeCreator.getInstance(),
                SocialNetworkRelationshipCreator.getInstance()));
    }

    private List<RankNodePair> computePageRank(NetworkMatrixFactory networkMatrixFactory) {
        LOG.info("Computing page rank based on adjacency matrix...");

        List<RankNodePair> pageRankResult;
        PageRank pageRank = new PageRank();

        try (Transaction tx = getDatabase().beginTx()) {
            NetworkMatrix transitionMatrix = networkMatrixFactory.getTransitionMatrix();
            pageRankResult = pageRank.getPageRankPairs(transitionMatrix, 0.85); // Sergei's & Larry's suggestion is to use .85 to become rich;)
            LOG.info("The highest PageRank in the network is: " + getDatabase().getNodeById(pageRankResult.get(0).node()).getProperty("name").toString());

            tx.success();
        }

        return pageRankResult;
    }

    private ArrayList<RankNodePair> computeNodeRank() {
        letCrawlerDoItsJob();

        ArrayList<RankNodePair> nodeRank = new ArrayList<>();

        try (Transaction tx = getDatabase().beginTx()) {
            for (Node node : GlobalGraphOperations.at(getDatabase()).getAllNodes()) {
                if (IncludeAllBusinessNodes.getInstance().include(node)) {
                    nodeRank.add(new RankNodePair((int) node.getProperty("nodeRank", 0), node.getId()));
                }
            }

            sort(nodeRank);
            LOG.info("The highest NeoRank in the network is: " + getDatabase().getNodeById(nodeRank.get(0).node()).getProperty("name").toString());

            tx.success();
        }

        return nodeRank;
    }

    private void letCrawlerDoItsJob() {
        LOG.info("Applying random graph walker module to the graph");

        TimingStrategy timingStrategy = FixedDelayTimingStrategy.getInstance()
                .withInitialDelay(50)
                .withDelay(2);

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(getDatabase(),
                FluentRuntimeConfiguration
                        .defaultConfiguration()
                        .withTimingStrategy(timingStrategy));
        runtime.registerModule(nodeRankModule);
        runtime.start();

        LOG.info("Waiting for module walker to do its work");

        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Analyses and compares
     * the results of PageRank and NeoRank
     * <p/>
     * The input lists have to be
     * in descending order and have the same length
     */
    private void analyseResults(List<RankNodePair> pageRankPairs, List<RankNodePair> nodeRankPairs) {
        LOG.info("Analysing results:");

        List<Long> pageRank = RankNodePair.convertToRankedNodeList(pageRankPairs);
        List<Long> nodeRank = RankNodePair.convertToRankedNodeList(nodeRankPairs);

        SimilarityComparison similarityComparison = new SimilarityComparison();
        LOG.info("Similarity of all entries: " + similarityComparison.getHammingDistanceMeasure(pageRank, nodeRank));

        List<Long> pageRank20 = pageRank.subList(0, (int) (pageRank.size() * .2));
        List<Long> nodeRank20 = nodeRank.subList(0, (int) (nodeRank.size() * .2));
        LOG.info("Similarity of top 20% entries: " + similarityComparison.getHammingDistanceMeasure(pageRank20, nodeRank20));

        List<Long> pageRank5 = pageRank.subList(0, 5);
        List<Long> nodeRank5 = nodeRank.subList(0, 5);
        LOG.info("Unordered similarity of the top 5 entries: " + 100 * similarityComparison.unorderedComparisonOfEqualLengthLists(pageRank5, nodeRank5) + "%");


        /**
         * Measures the "Lehmer ratio" of the resulting list. The ratio is a percentage to which the new list is
         * completely permuted. The Lehmer code for nodeRank results, given pageRank results is calculated and
         * converted to decimal representation. This is the order-number of a permutation of nodeRank result,
         * given the pageRank result as a start. The Lehmer code is the normalised by maximum allowed LC (size!).
         *
         * 1.0 corresponds to a perfect match of the two algorithms.
         *
         * The log Lehmer ratio is a ratio of logarithms of the two numbers.
         */
        Permutation<RankNodePair> pageRankToNodeRankPermutation = new Permutation<>(pageRankPairs, nodeRankPairs);
        LOG.info("The un-normed Lehmer distance of pageRank to nodeRank is: " + pageRankToNodeRankPermutation.getPermutationIndex().toString());
        LOG.info("Lehmer distance ratio: {} ", pageRankToNodeRankPermutation.getNormedPermutationIndex());
        LOG.info("Lehmer log-distance ratio: {} ", pageRankToNodeRankPermutation.getLogNormedPermutationIndex());

        assertTrue(pageRankToNodeRankPermutation.getNormedPermutationIndex() * 100 > 90);
    }
}
