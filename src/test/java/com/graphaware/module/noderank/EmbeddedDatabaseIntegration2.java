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

import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.RuntimeRegistry;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class EmbeddedDatabaseIntegration2 {

    @Test
    public void shouldSuccessfullyInitialiseAndRunModuleWhenDatabaseIsStarted() throws InterruptedException {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);  //where database is an instance of GraphDatabaseService
        NodeRankModule module = new NodeRankModule("NR");
        runtime.registerModule(module);
        runtime.start();

        populateDatabase(database);

        Thread.sleep(2000);

        Result executionResult = database.execute("MATCH (p:Person) WHERE p.nodeRank > 0 RETURN p");

        assertTrue("The page rank module didn't run on startup", executionResult.hasNext());

        NodeRankModule nodeRankModule = RuntimeRegistry.getStartedRuntime(database).getModule("NR", NodeRankModule.class);
        List<Node> topNodes = nodeRankModule.getTopNodes().getTopNodes();
        assertTrue(topNodes.size() > 0);

        database.shutdown();
    }

    private void populateDatabase(GraphDatabaseService database) {
        database.execute("CREATE " +
                " (m:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'})," +
                " (m)-[:FRIEND_OF]->(v:Person {name:'Vojta'})," +
                " (m)-[:FRIEND_OF]->(a:Person {name:'Adam'})," +
                " (m)-[:FRIEND_OF]->(vi:Person {name:'Vince'})," +
                " (m)-[:FRIEND_OF]->(:Person {name:'Luanne'})," +
                " (vi)-[:FRIEND_OF]->(a)," +
                " (d)-[:FRIEND_OF]->(a)," +
                " (d)-[:FRIEND_OF]->(vi)," +
                " (v)-[:FRIEND_OF]->(a)");
    }
}
