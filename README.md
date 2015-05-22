<a name="top"/>
GraphAware Neo4j NodeRank
==========================

[![Build Status](https://travis-ci.org/graphaware/neo4j-noderank.png)](https://travis-ci.org/graphaware/neo4j-noderank) | <a href="http://graphaware.com/downloads/" target="_blank">Downloads</a> | <a href="http://graphaware.com/site/noderank/latest/apidocs/" target="_blank">Javadoc</a> | Latest Release: 2.2.2.31.2

GraphAware NodeRank is a [GraphAware](https://github.com/graphaware/neo4j-framework) Runtime Module that executes a configurable
Page Rank-like algorithm on the Neo4j graph. It is a reference implementation of a [Timer-Driven GraphAware Runtime Module](https://github.com/graphaware/neo4j-framework/tree/master/runtime#building-a-timer-driven-graphaware-runtime-module).

The module "crawls" the graph continuously behind the scenes, slowing down as the database gets busier and speeding up
in quiet periods. It starts by selecting a node at random (obeying configured node inclusion policy). At each step of the
algorithm, it follows a random relationship (obeying configured relationship inclusion policy) and increments a property
(with configurable key) of the other node of the relationship. With a probability 1-p, where p is the configurable damping
factor, it selects another random node rather than following a relationship. Also, if there are no suitable relationships
to follow from a node, a jump to another suitable random node is performed.

Over time, the node ranks written to the nodes in the graph converge to the results of Page Rank, has it been computed
analytically. The amount of time it takes to converge greatly depends on the size of the graph, the load on the database,
and the [Timer-Driven Module Scheduling Configuration](https://github.com/graphaware/neo4j-framework/tree/master/runtime#building-a-timer-driven-graphaware-runtime-module).
With default settings and 100 nodes in the database, the top 10 nodes are identical for NodeRank and Page Rank within a
few seconds of running the module.

Getting the Software
--------------------

### Server Mode

When using Neo4j in the <a href="http://docs.neo4j.org/chunked/stable/server-installation.html" target="_blank">standalone server</a> mode,
you will need the <a href="https://github.com/graphaware/neo4j-framework" target="_blank">GraphAware Neo4j Framework</a> and GraphAware Neo4j NodeRank .jar files (both of which you can <a href="http://graphaware.com/downloads/" target="_blank">download here</a>) dropped
into the `plugins` directory of your Neo4j installation. After a change in neo4.properties (described later) and Neo4j restart, you will be able to use the REST APIs of the NodeRank
and the computation will take place continuously.

### Embedded Mode / Java Development

Java developers that use Neo4j in <a href="http://docs.neo4j.org/chunked/stable/tutorials-java-embedded.html" target="_blank">embedded mode</a>
and those developing Neo4j <a href="http://docs.neo4j.org/chunked/stable/server-plugins.html" target="_blank">server plugins</a>,
<a href="http://docs.neo4j.org/chunked/stable/server-unmanaged-extensions.html" target="_blank">unmanaged extensions</a>,
GraphAware Runtime Modules, or Spring MVC Controllers can include use the NodeRank as a dependency for their Java project.

#### Releases

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22changefeed%22" target="_blank">Maven Central repository</a>. When using Maven for dependency management, include the following dependency in your pom.xml.

    <dependencies>
        ...
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>noderank</artifactId>
            <version>2.2.2.31.2</version>
        </dependency>
        ...
    </dependencies>

#### Snapshots

To use the latest development version, just clone this repository, run `mvn clean install` and change the version in the
dependency above to 2.2.2.31.3-SNAPSHOT.

#### Note on Versioning Scheme

The version number has two parts. The first four numbers indicate compatibility with Neo4j GraphAware Framework.
 The last number is the version of the NodeRank library. For example, version 2.1.4.19.1 is version 1 of the NodeRank
 compatible with GraphAware Neo4j Framework 2.1.4.19.

Setup and Configuration
=======================

### Server Mode

Edit neo4j.properties to register the NodeRank module:

```
com.graphaware.runtime.enabled=true

#NR becomes the module ID:
com.graphaware.module.NR.1=com.graphaware.module.noderank.NodeRankModuleBootstrapper

#optional number of top ranked nodes to remember, the default is 10
com.graphaware.module.NR.maxTopRankNodes=10

#optional daming factor, which is a number p such that a random node will be selected at any step of the algorithm
#with the probability 1-p (as opposed to following a random relationship). The default is 0.85
com.graphaware.module.NR.dampingFactor=0.85

#optional key of the property that gets written to the ranked nodes, default is "nodeRank"
com.graphaware.module.NR.propertyKey=nodeRank

#optionally specify nodes to rank using an expression-based node inclusion policy, default is all business (i.e. non-framework-internal) nodes
com.graphaware.module.NR.node=hasLabel('Person')

#optionally specify relationships to follow using an expression-based relationship inclusion policy, default is all business (i.e. non-framework-internal) relationships
com.graphaware.module.NR.relationship=isType('FRIEND_OF')
```

Note that "NR" becomes the module ID. It is possible to register the NodeRank module multiple times with different
configurations, provided that their IDs are different. This ID is important for querying the top ranked nodes (read on).

### Embedded Mode / Java Development

To use the NodeRank programmatically, register the module like this

```java
GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);  //where database is an instance of GraphDatabaseService
NodeRankModule module = new NodeRankModule("NR");
runtime.registerModule(module);
runtime.start();
```

Alternatively:
```java
GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabaseBuilder(pathToDb)
    .loadPropertiesFromFile(this.getClass().getClassLoader().getResource("neo4j.properties").getPath())
    .newGraphDatabase();

RuntimeRegistry.getStartedRuntime(database);
//make sure neo4j.properties contain the lines mentioned in previous section
```

Using GraphAware NodeRank
=========================

### Server Mode

In Server Mode, the NodeRank is accessible via the REST API.

You can issue GET requests to `http://your-server-address:7474/graphaware/noderank/{moduleId}` to get a list of top ranked
nodes, ordered by decreasing rank. The maximum size of the list is determined by the `maxTopRankNodes` parameter configured
earlier. You can further limit the size of the results by provising a `limit` request parameter, e.g.
`http://your-server-address:7474/graphaware/noderank/{moduleId}?limit=10`.

The REST API returns a JSON array of nodes, e.g.

```json
[
    {
        "id": 5,
        "labels": ["Person", "Male"],
        "nodeRank": 3022,
        "name": "Brandon Morley"
    },
    {
        "id": 9,
        "labels": ["Person", "Male"],
        "nodeRank": 2656,
        "name": "Liam Rees"
    },
    {
        "id": 8,
        "labels": ["Person", "Female"],
        "nodeRank": 2280,
        "name": "Amelie Green"
    }
]
```

### Java API

To use the Java API, obtain the module and ask for top nodes. You can also just interrogate the node properties as usual,
the property that NodeRank writes to the nodes is configured using the `propertyKey` parameter described above.

```
NodeRankModule nodeRankModule = ProductionRuntime.getStartedRuntime(database).getModule("NR", NodeRankModule.class);
List<Node> topNodes = nodeRankModule.getTopNodes().getTopNodes();
```

Please refer to Javadoc for more detail.

License
-------

Copyright (c) 2014 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
