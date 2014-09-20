package com.graphaware.module.noderank;

import com.graphaware.common.policy.RelationshipInclusionPolicy;
import com.graphaware.common.policy.fluent.IncludeNodes;
import com.graphaware.runtime.metadata.NodeBasedContext;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NodeRankModuleTest extends DatabaseIntegrationTest {

    private NodeRankModule module;
    private ExecutionEngine executionEngine;

    public void setUp() throws Exception {
        super.setUp();
        this.executionEngine = new ExecutionEngine(getDatabase());
        this.module = new NodeRankModule("TEST");
    }

    @Test
    public void shouldTolerateEmptyContextGivenIfNoPreviousStepsHaveBeenMade() {
        executionEngine.execute("CREATE (arbitraryNode)-[:RELATES_TO]->(otherNode);");

        try (Transaction tx = getDatabase().beginTx()) {
            module.doSomeWork(module.createInitialContext(getDatabase()), getDatabase());
        }
    }

    @Test
    public void shouldExecuteSingleStepTowardsConvergenceAndUpdateNodePropertiesAccordingly() {
        ExecutionResult executionResult = executionEngine.execute(
                "CREATE (p:Person{name:'Gary'})-[:KNOWS]->(q:Person{name:'Sheila'}) RETURN p, q");

        Map<String, Object> insertionResults = executionResult.iterator().next();

        try (Transaction tx = getDatabase().beginTx()) {
            Node startNode = (Node) insertionResults.get("p");
            NodeBasedContext lastContext = new NodeBasedContext(startNode);

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
                .with(new RelationshipInclusionPolicy() {
                    @Override
                    public boolean include(Relationship relationship) {
                        return relationship.isType(DynamicRelationshipType.withName("OWNS"));
                    }

                    @Override
                    public boolean include(Relationship relationship, Node pointOfView) {
                        return include(relationship) && relationship.getOtherNode(pointOfView).hasLabel(DynamicLabel.label("Car"));
                    }
                }));

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

        try (Transaction tx = getDatabase().beginTx()) {
            Node person = (Node) insertionResults.get("p");

            NodeBasedContext newContext = module.doSomeWork(new NodeBasedContext(person), getDatabase());
            assertNotNull("The new context shouldn't be null", newContext);
            Node nextNode = newContext.find(getDatabase());
            assertNotNull("The next node in the new context shouldn't be null", nextNode);
            assertEquals("The wrong next node was selected", "Car", nextNode.getLabels().iterator().next().name());
        }
    }

    @Test
    public void shouldChooseLegitimateRandomStartNodeInAccordanceWithInclusionStrategy() {
        module = new NodeRankModule("TEST3", NodeRankModuleConfiguration.defaultConfiguration().with(IncludeNodes.all().with("Vegan")));

        executionEngine.execute("CREATE (:Meat{name:'Chicken'}), (:Meat{name:'Mutton'}), (:Vegan{name:'Potato'}), "
                + "(:Vegetarian{name:'Milk'}), (:Vegetarian{name:'Cheese'}), (:Meat{name:'Pork'})");

        try (Transaction tx = getDatabase().beginTx()) {
            NodeBasedContext initialContext = module.createInitialContext(getDatabase());
            assertNotNull("The initial context shouldn't be null", initialContext);
            Node startNode = initialContext.find(getDatabase());
            assertEquals("The wrong start node was selected", "Potato", startNode.getProperty("name"));
        }
    }
}
