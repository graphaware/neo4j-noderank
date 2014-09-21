package com.graphaware.module.noderank;

import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import com.graphaware.runtime.metadata.ModuleMetadataRepository;
import com.graphaware.runtime.metadata.ProductionSingleNodeMetadataRepository;
import com.graphaware.runtime.metadata.TimerDrivenModuleMetadata;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * REST API for {@link NodeRankModule}.
 */
@Controller
@RequestMapping("/noderank")
public class NodeRankApi {

    private final GraphDatabaseService database;
    private final ModuleMetadataRepository repository;

    @Autowired
    public NodeRankApi(GraphDatabaseService database) {
        this.database = database;
        //todo: this is a hack - find a clean way of supporting this from the framework:
        this.repository = new ProductionSingleNodeMetadataRepository(database, FluentRuntimeConfiguration.defaultConfiguration(), RuntimeConfiguration.TIMER_MODULES_PROPERTY_PREFIX);
    }

    @RequestMapping(value = "/{moduleId}", method = RequestMethod.GET)
    @ResponseBody
    public List<HackyJsonNode> getChangeFeed(@PathVariable String moduleId, @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<HackyJsonNode> result = new LinkedList<>();

        try (Transaction tx = database.beginTx()) {
            TimerDrivenModuleMetadata moduleMetadata = repository.getModuleMetadata(moduleId);

            if (moduleMetadata == null) {
                throw new NotFoundException("Unknown module " + moduleId);
            }

            NodeRankContext context = (NodeRankContext) moduleMetadata.lastContext();

            if (context == null) {
                return Collections.emptyList();
            }

            for (RankedNode node : context.getTopRankedNodes()) {
                try {
                    result.add(new HackyJsonNode(database.getNodeById(node.getNodeId())));
                } catch (NotFoundException e) {
                    //oh well, deleted in the meantimg
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
