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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Test;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import com.graphaware.common.policy.inclusion.BaseRelationshipInclusionPolicy;
import com.graphaware.common.policy.inclusion.fluent.IncludeNodes;
import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.test.integration.EmbeddedDatabaseIntegrationTest;

public class NodeRankModuleTest extends EmbeddedDatabaseIntegrationTest {

    private NodeRankModule module;

    public void setUp() throws Exception {
        super.setUp();
        this.module = new NodeRankModule("TEST");
    }

    @Test
    public void shouldTolerateEmptyContextGivenIfNoPreviousStepsHaveBeenMade() {
        getDatabase().execute("CREATE (arbitraryNode)-[:RELATES_TO]->(otherNode);");

        try (Transaction tx = getDatabase().beginTx()) {
            module.doSomeWork(module.createInitialContext(getDatabase()), getDatabase());
        }
    }

    @Test
    public void shouldExecuteSingleStepTowardsConvergenceAndUpdateNodePropertiesAccordingly() {
        Result executionResult = getDatabase().execute(
                "CREATE (p:Person{name:'Gary'})-[:KNOWS]->(q:Person{name:'Sheila'}) RETURN p, q");

        Map<String, Object> insertionResults = executionResult.next();

        try (Transaction tx = getDatabase().beginTx()) {
            Node startNode = (Node) insertionResults.get("p");
            NodeRankContext lastContext = new NodeRankContext(startNode, new Long[0]);

            Node expectedNextNode = (Node) insertionResults.get("q");

            NodeBasedContext newContext = module.doSomeWork(lastContext, getDatabase());
            assertNotNull("The new context shouldn't be null", newContext);
            Node nextNode = newContext.find(getDatabase());
            assertNotNull("The next node in the new context shouldn't be null", nextNode);
            assertEquals("The next node wasn't selected as expected", expectedNextNode, nextNode);
            assertEquals("The expected page rank property wasn't updated", 1, nextNode.getProperty("nodeRank"));
        }
    }

    @Test
    public void shouldHonourInclusionStrategiesForNodesAndRelationships() {

        module = new NodeRankModule("TEST2", NodeRankModuleConfiguration
                .defaultConfiguration()
                .with(IncludeNodes.all().with("Car"))
                .with(new BaseRelationshipInclusionPolicy() {
                    @Override
                    public boolean include(Relationship relationship) {
                        return relationship.isType(RelationshipType.withName("OWNS"));
                    }

                    @Override
                    public boolean include(Relationship relationship, Node pointOfView) {
                        return include(relationship) && relationship.getOtherNode(pointOfView).hasLabel(Label.label("Car"));
                    }
                }));

        // set up test data and run test
        Result executionResult = getDatabase().execute(
                "CREATE (p:Person{name:'Sanjiv'})-[:KNOWS]->(:Person{name:'Lakshmipathy'}),"
                        + " (p)-[:KNOWS]->(:Person{name:'Rajani'}), "
                        + " (p)-[:OWNS]->(:Laptop{manufacturer:'Dell'}), "
                        + " (p)-[:OWNS]->(:MobilePhone{manufacturer:'Nokia'}), "
                        + " (p)-[:OWNS]->(:Car{manufacturer:'Vauxhall'}), "
                        + " (p)-[:OWNS]->(:Tablet{manufacturer:'Samsung'}) "
                        + "RETURN p");

        Map<String, Object> insertionResults = executionResult.next();

        try (Transaction tx = getDatabase().beginTx()) {
            Node person = (Node) insertionResults.get("p");

            NodeRankContext newContext = module.doSomeWork(new NodeRankContext(person, new Long[0]), getDatabase());
            assertNotNull("The new context shouldn't be null", newContext);
            Node nextNode = newContext.find(getDatabase());
            assertNotNull("The next node in the new context shouldn't be null", nextNode);
            assertEquals("The wrong next node was selected", "Car", nextNode.getLabels().iterator().next().name());
        }
    }

    @Test
    public void shouldChooseLegitimateRandomStartNodeInAccordanceWithInclusionStrategy() {
        module = new NodeRankModule("TEST3", NodeRankModuleConfiguration.defaultConfiguration().with(IncludeNodes.all().with("Vegan")));

        getDatabase().execute("CREATE (:Meat{name:'Chicken'}), (:Meat{name:'Mutton'}), (:Vegan{name:'Potato'}), "
                + "(:Vegetarian{name:'Milk'}), (:Vegetarian{name:'Cheese'}), (:Meat{name:'Pork'})");

        try (Transaction tx = getDatabase().beginTx()) {
            NodeBasedContext initialContext = module.createInitialContext(getDatabase());
            assertNotNull("The initial context shouldn't be null", initialContext);
            Node startNode = initialContext.find(getDatabase());
            assertEquals("The wrong start node was selected", "Potato", startNode.getProperty("name"));
        }
    }
}
