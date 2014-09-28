package com.graphaware.module.noderank;

import com.graphaware.api.JsonNode;
import com.graphaware.runtime.ProductionRuntime;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * REST API for {@link NodeRankModule}.
 */
@Controller
@RequestMapping("/noderank")
public class NodeRankApi {

    private final GraphDatabaseService database;

    @Autowired
    public NodeRankApi(GraphDatabaseService database) {
        this.database = database;
    }

    @RequestMapping(value = "/{moduleId}", method = RequestMethod.GET)
    @ResponseBody
    public List<JsonNode> getChangeFeed(@PathVariable String moduleId, @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<JsonNode> result = new LinkedList<>();

        NodeRankModule module = ProductionRuntime.getRuntime(database).getModule(moduleId, NodeRankModule.class);

        try (Transaction tx = database.beginTx()) {
            for (Node node : module.getTopNodes().getTopNodes()) {
                try {
                    result.add(new JsonNode(node));

                    if (result.size() >= limit) {
                        break;
                    }

                } catch (NotFoundException e) {
                    //oh well, deleted in the meantime
                }
            }

            tx.success();
        }

        return result;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleIllegalArguments() {
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound() {
    }
}
