package com.graphaware.module.noderank.parser;

import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import org.junit.Test;
import org.neo4j.graphdb.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class RegexModuleConfigParameterParserTest extends DatabaseIntegrationTest {

    private RegexModuleConfigParameterParser configParameterParser = new RegexModuleConfigParameterParser();

    @Test
    public void shouldCreateInclusionStrategyForNodeExpressionWithLabelAndProperty() {
        String nodeExpression = "Person {name:'Gary'}";

        InclusionStrategy<Node> inclusionStrategy = this.configParameterParser.parseForNodeInclusionStrategy(nodeExpression);

        Node wrongPropertyNode = proxyNode("Person", Collections.singletonMap("name", "Sheila"));
        Node wrongLabelNode = proxyNode("Elephant", Collections.singletonMap("name", "Gary"));
        Node totallyWrongNode = proxyNode("Horse");
        Node validNode = proxyNode("Person", Collections.singletonMap("name", "Gary"));

        try (Transaction tx = getDatabase().beginTx()) {
            assertNotNull("The resultant inclusion strategy shouldn't be null", inclusionStrategy);
            assertFalse("A node with the wrong property was included", inclusionStrategy.include(wrongPropertyNode));
            assertFalse("A node with the wrong label was included", inclusionStrategy.include(wrongLabelNode));
            assertFalse("An incorrect node was included", inclusionStrategy.include(totallyWrongNode));
            assertTrue("The correct node was not included", inclusionStrategy.include(validNode));
        }

    }

    @Test
    public void shouldCreateInclusionStrategyForNodeExpressionWithoutSpecificProperties() {
        InclusionStrategy<Node> inclusionStrategy = this.configParameterParser.parseForNodeInclusionStrategy("Camel");

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse("A node with the wrong label was included", inclusionStrategy.include(proxyNode("Donkey")));
            assertTrue("The correct node wasn't included", inclusionStrategy.include(proxyNode("Camel")));
            Node correctNodeWithProperties = proxyNode("Camel", Collections.singletonMap("age", 5));
            assertTrue("The presence of properties shouldn't affect the inclusion", inclusionStrategy.include(correctNodeWithProperties));
        }
    }

    @Test
    public void shouldCreateInclusionStrategyForNodeExpressionWithoutLabels() {
        InclusionStrategy<Node> inclusionStrategy = this.configParameterParser.parseForNodeInclusionStrategy("{name:\"Jeff\"}");

        Node matchingNode = proxyNode(null, Collections.singletonMap("name", "Jeff"));
        Node nodeWithWrongValue = proxyNode(null, Collections.singletonMap("name", "Colin"));

        try (Transaction tx = getDatabase().beginTx()) {
            assertTrue("The correct node wasn't included", inclusionStrategy.include(matchingNode));
            assertFalse("A node with the wrong property value was included", inclusionStrategy.include(nodeWithWrongValue));
            Node matchingLabelledNode = proxyNode("Bloke", Collections.singletonMap("name", "Jeff"));
            assertTrue("The presence of a label shouldn't affect the inclusion", inclusionStrategy.include(matchingLabelledNode));
        }
    }

    @Test
    public void shouldCreateInclusionStrategyForNodeExpressionWithMultipleProperties() {
        InclusionStrategy<Node> inclusionStrategy = this.configParameterParser.parseForNodeInclusionStrategy(
                "PieAndChips { mushyPeas: true, gravy: true, salt: true, vinegar: false}");

        Map<String, Boolean> options = new HashMap<>();
        options.put("mushyPeas", true);
        options.put("gravy", true);
        Node notEnoughProperties = proxyNode("PieAndChips", new HashMap<>(options));

        options.put("salt", false);
        options.put("vinegar", true);
        Node nonMatchingProperties = proxyNode("PieAndChips", new HashMap<>(options));

        options.put("salt", true);
        options.put("vinegar", false);
        Node exactMatch = proxyNode("PieAndChips", new HashMap<>(options));

        options.put("currySauce", false);
        Node matchWithExtraProperties = proxyNode("PieAndChips", new HashMap<>(options));

        try (Transaction tx = getDatabase().beginTx()) {
            assertFalse("A node with too few properties shouldn't be included", inclusionStrategy.include(notEnoughProperties));
            assertFalse("A node with non-matching properties shouldn't be included", inclusionStrategy.include(nonMatchingProperties));
            assertTrue("The exact match wasn't included", inclusionStrategy.include(exactMatch));
            assertTrue("Additional properties shouldn't affect the inclusion", inclusionStrategy.include(matchWithExtraProperties));
        }
    }

    @Test
    public void shouldCreateInclusionStrategyForRelationshipsFromSingleTypeExpression() {
        InclusionStrategy<Relationship> inclusionStrategy = this.configParameterParser.parseForRelationshipInclusionStrategy("FRIEND_OF");

        try (Transaction tx = getDatabase().beginTx()) {
            assertNotNull("The resultant inclusion strategy shouldn't be null", inclusionStrategy);
            assertTrue(inclusionStrategy.include(proxyRelationship("FRIEND_OF")));
            assertFalse(inclusionStrategy.include(proxyRelationship("FOE_OF")));
        }
    }

    @Test
    public void shouldCreateInclusionStrategyForRelationshipsFromMultiTypeExpression() {
        InclusionStrategy<Relationship> inclusionStrategy =
                this.configParameterParser.parseForRelationshipInclusionStrategy("NORTH|EAST|WEST");

        try (Transaction tx = getDatabase().beginTx()) {
            assertNotNull("The resultant inclusion strategy shouldn't be null", inclusionStrategy);
            assertTrue(inclusionStrategy.include(proxyRelationship("NORTH")));
            assertFalse(inclusionStrategy.include(proxyRelationship("SOUTH")));
            assertTrue(inclusionStrategy.include(proxyRelationship("EAST")));
            assertTrue(inclusionStrategy.include(proxyRelationship("WEST")));
        }
    }

    private Node proxyNode(String labelName) {
        return proxyNode(labelName, Collections.<String, Object>emptyMap());
    }

    private Node proxyNode(final String labelName, final Map<String, ?> properties) {
        Node node;
        try (Transaction tx = getDatabase().beginTx()) {
            if (labelName != null) {
                node = getDatabase().createNode(DynamicLabel.label(labelName));
            }
            else {
                node = getDatabase().createNode();
            }

            for (String key : properties.keySet()) {
                node.setProperty(key, properties.get(key));
            }
            tx.success();
        }
        return node;
    }

    private Relationship proxyRelationship(final String relationshipType) {
        Relationship r;
        try (Transaction tx = getDatabase().beginTx()) {
            Node n1 = getDatabase().createNode();
            Node n2 = getDatabase().createNode();
            r = n1.createRelationshipTo(n2, DynamicRelationshipType.withName(relationshipType));
            tx.success();
        }
        return r;
    }
}

