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
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.proc.Procedures;

import com.graphaware.common.policy.role.WritableRole;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.test.data.DatabasePopulator;
import com.graphaware.test.integration.cluster.CausalClusterDatabasesintegrationTest;

/**
 *
 */
public class NodeRankProcedureTestCausalCluster extends CausalClusterDatabasesintegrationTest {
	
	private static boolean initialized = false;
	
	@Override
	protected boolean shouldRegisterModules() {
		return true;
	}

	@Override
	protected boolean shouldRegisterProcedures() {
		return true;
	}
	
	@Override
	protected void registerModule(GraphDatabaseService db) throws Exception {
		GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(db);
        NodeRankModuleConfiguration config = NodeRankModuleConfiguration.defaultConfiguration()
        .with(WritableRole.getInstance())
        .withMaxTopRankNodes(3);
		runtime.registerModule(new NodeRankModule("noderank",config));
        runtime.start();
	}

	@Override
	protected void registerProcedures(Procedures procedures) throws Exception {
		procedures.registerProcedure(NodeRankProcedure.class);
	}
	
	@Override
	protected DatabasePopulator databasePopulator() {
		return new DatabasePopulator() {
			
			@Override
			public void populate(GraphDatabaseService database) {
				try(Transaction tx = database.beginTx();){
	            database.execute("CREATE (m:Person {name:'Michal'})-[:FRIEND_OF]->(d:Person {name:'Daniela'})," +
	                    " (m)-[:FRIEND_OF]->(v:Person {name:'Vojta'})," +
	                    " (m)-[:FRIEND_OF]->(a:Person {name:'Adam'})," +
	                    " (m)-[:FRIEND_OF]->(vi:Person {name:'Vince'})," +
	                    " (m)-[:FRIEND_OF]->(:Person {name:'Luanne'})," +
	                    " (vi)-[:FRIEND_OF]->(a)," +
	                    " (d)-[:FRIEND_OF]->(a)," +
	                    " (d)-[:FRIEND_OF]->(vi)," +
	                    " (v)-[:FRIEND_OF]->(a)");
	            tx.success();
				}
				
			}
		};
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		if(! initialized){
			
			GraphDatabaseService database = getOneReplicaDatabase();
			//wait for ranking synch to replica
			int count = 0;
			do{
				count = 0;
		        try (Transaction tx = database.beginTx()) {
		            Result result = database.execute("MATCH (node:Person) RETURN node");
//		            System.out.println("======================");
		            while(result.hasNext()){
		            	Node node = (Node) result.next().get("node");
		            	if(node.hasProperty("nodeRank")){
		            		Integer rank = (Integer) node.getProperty("nodeRank");
//		            		System.out.println(node.getProperty("name")+": "+node.getProperty("nodeRank"));
		            		if(rank > 2){
		            			count++;			            			
		            		}
		            	}
		            }
		            tx.failure();
		        }catch(Exception e){e.printStackTrace();}
		        
		        try {
		        	TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}while( count < 6 );
			
			initialized = true;
		}
	}
	
    @Test
    public void testProcedureCall_LEADER() throws InterruptedException, IOException {
        GraphDatabaseService database = getLeaderDatabase();
        try (Transaction tx = database.beginTx()) {
            Result result = database.execute("CALL ga.noderank.getTopRanked('noderank', 10) YIELD node RETURN node");
            List<Node> ranked = new LinkedList<>();
            while (result.hasNext()) {
                Map<String, Object> record = result.next();
                ranked.add((Node) record.get("node"));
            }
            assertFalse("No ranked nodes found",ranked.isEmpty());
            assertEquals("Michal", ranked.get(0).getProperty("name"));
            tx.failure();
        }
    }
	
    @Test
    public void testProcedureCall_FOLLOWER() throws InterruptedException, IOException {
        GraphDatabaseService database = getOneFollowerDatabase();
        try (Transaction tx = database.beginTx()) {
            Result result = database.execute("CALL ga.noderank.getTopRanked('noderank', 10) YIELD node RETURN node");
            List<Node> ranked = new LinkedList<>();
            while (result.hasNext()) {
                Map<String, Object> record = result.next();
                ranked.add((Node) record.get("node"));
            }
            assertFalse("No ranked nodes found",ranked.isEmpty());
            assertEquals("Michal", ranked.get(0).getProperty("name"));
            tx.failure();
        }
    }
    
	
    @Test
    public void testProcedureCall_REPLICA() throws InterruptedException, IOException {
        GraphDatabaseService database = getOneReplicaDatabase();
        try (Transaction tx = database.beginTx()) {
            Result result = database.execute("CALL ga.noderank.getTopRanked('noderank', 10) YIELD node RETURN node");
            List<Node> ranked = new LinkedList<>();
            while (result.hasNext()) {
                Map<String, Object> record = result.next();
                ranked.add((Node) record.get("node"));
            }
            assertFalse("No ranked nodes found",ranked.isEmpty());
            assertEquals("Michal", ranked.get(0).getProperty("name"));
            tx.failure();
        }
    }
}
