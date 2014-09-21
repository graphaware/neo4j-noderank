package com.graphaware.module.noderank;

import com.graphaware.api.JsonInput;
import com.graphaware.api.JsonPropertyContainer;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Hacky workaround - framework doesn't allow returning all properties without knowing what they are.
 * <p/>
 * Will be fixed in next release.
 */
public class HackyJsonNode extends JsonPropertyContainer {

    private String[] labels;

    public HackyJsonNode(Node node) {
        super(node.getId());

        for (String property : node.getPropertyKeys()) {
            putProperty(property, node.getProperty(property));
        }

        setLabels(labelsToStringArray(node.getLabels()));
    }

    private String[] labelsToStringArray(Iterable<Label> labels) {
        List<String> labelsAsList = new LinkedList<>();
        for (Label label : labels) {
            labelsAsList.add(label.name());
        }
        return labelsAsList.toArray(new String[labelsAsList.size()]);
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }
}