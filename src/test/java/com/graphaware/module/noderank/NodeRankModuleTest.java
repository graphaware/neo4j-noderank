package com.graphaware.module.noderank;

import static com.graphaware.module.noderank.NodeRankModule.*;
import static org.junit.Assert.*;

import java.util.Map;

import com.graphaware.common.strategy.*;

import com.graphaware.runtime.strategy.IncludeBusinessNodes;
import com.graphaware.runtime.strategy.IncludeBusinessRelationships;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import com.graphaware.runtime.metadata.NodeBasedContext;

public class NodeRankModuleTest {

    private NodeRankModule module;
    private GraphDatabaseService database;
    private ExecutionEngine executionEngine;

    /** */
    @Before
    public void setUp() {
        this.database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        this.executionEngine = new ExecutionEngine(database);
        this.module = new NodeRankModule("TEST");
    }

    @Test
    public void shouldTolerateEmptyContextGivenIfNoPreviousStepsHaveBeenMade() {
        executionEngine.execute("CREATE (arbitraryNode)-[:RELATES_TO]->(otherNode);");

        try (Transaction tx = database.beginTx()) {
            module.doSomeWork(module.createInitialContext(database), database);
        }
    }

    @Test
    public void shouldExecuteSingleStepTowardsConvergenceAndUpdateNodePropertiesAccordingly() {
        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (p:Person{name:'Gary'})-[:KNOWS]->(q:Person{name:'Sheila'}) RETURN p, q");
        Map<String, Object> insertionResults = executionResult.iterator().next();

        try (Transaction tx = database.beginTx()) {
            Node startNode = (Node) insertionResults.get("p");
            NodeBasedContext lastContext = new NodeBasedContext(startNode);

            Node expectedNextNode = (Node) insertionResults.get("q");

            NodeBasedContext newContext = module.doSomeWork(lastContext, database);
            assertNotNull("The new context shouldn't be null", newContext);
            Node nextNode = newContext.find(database);
            assertNotNull("The next node in the new context shouldn't be null", nextNode);
            assertEquals("The next node wasn't selected as expected", expectedNextNode, nextNode);
            assertEquals("The expected page rank property wasn't updated", 1, nextNode.getProperty(NODE_RANK_PROPERTY_KEY));
        }
    }

    @Test
    public void shouldHonourInclusionStrategiesForNodesAndRelationships() {
        NodeInclusionStrategy carNodesOnly = IncludeBusinessNodes.all().with("Car");
        NodeCentricRelationshipInclusionStrategy ownsRelationshipsOnly = IncludeBusinessRelationships.all().with("OWNS");

        module = new NodeRankModule("TEST2", new NodeRankModuleConfiguration(carNodesOnly, ownsRelationshipsOnly));

        // set up test data and run test
        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (p:Person{name:'Sanjiv'})-[:KNOWS]->(:Person{name:'Lakshmipathy'}),"
                        + " (p)-[:KNOWS]->(:Person{name:'Rajani'}), "
                        + " (p)-[:OWNS]->(:Laptop{manufacturer:'Dell'}), "
                        + " (p)-[:OWNS]->(:MobilePhone{manufacturer:'Nokia'}), "
                        + " (p)-[:OWNS]->(:Car{manufacturer:'Vauxhall'}), "
                        + " (p)-[:OWNS]->(:Tablet{manufacturer:'Samsung'}) "
                        + "RETURN p");

        Map<String, Object> insertionResults = executionResult.iterator().next();

        try (Transaction tx = database.beginTx()) {
            Node person = (Node) insertionResults.get("p");

            NodeBasedContext newContext = module.doSomeWork(new NodeBasedContext(person), database);
            assertNotNull("The new context shouldn't be null", newContext);
            Node nextNode = newContext.find(database);
            assertNotNull("The next node in the new context shouldn't be null", nextNode);
            assertEquals("The wrong next node was selected", "Car", nextNode.getLabels().iterator().next().name());
        }
    }

    @Test
    public void shouldChooseLegitimateRandomStartNodeInAccordanceWithInclusionStrategy() {
        NodeInclusionStrategy veganInclusionStrategy = IncludeBusinessNodes.all().with("Vegan");

        module = new NodeRankModule("TEST3",
                new NodeRankModuleConfiguration(veganInclusionStrategy, IncludeAllRelationships.getInstance()));

        executionEngine.execute("CREATE (:Meat{name:'Chicken'}), (:Meat{name:'Mutton'}), (:Vegan{name:'Potato'}), "
                + "(:Vegetarian{name:'Milk'}), (:Vegetarian{name:'Cheese'}), (:Meat{name:'Pork'})");

        try (Transaction tx = database.beginTx()) {
            NodeBasedContext initialContext = module.createInitialContext(database);
            assertNotNull("The initial context shouldn't be null", initialContext);
            Node startNode = initialContext.find(database);
            assertEquals("The wrong start node was selected", "Potato", startNode.getProperty("name"));
        }
    }
}
