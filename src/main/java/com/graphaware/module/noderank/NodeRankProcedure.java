package com.graphaware.module.noderank;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class NodeRankProcedure {

    @Context
    public GraphDatabaseService database;

    @Procedure("ga.noderank.getTopRanked")
    public Stream<NodeResult> getTopRankedNodes(@Name("moduleId") String moduleId, @Name("limit") Number limit) {
        List<NodeResult> result = new LinkedList<>();
        new NodeRankApi(database).getTopRankedNodes(moduleId, limit.intValue()).stream().forEach((node) -> {
            result.add(new NodeResult(node));
        });

        return result.stream();
    }

    public class NodeResult {

        public final Node node;

        public NodeResult(Node node) {
            this.node = node;
        }
    }

}
