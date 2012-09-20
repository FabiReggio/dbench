This is an exploratory documentation on the document oriented database called MongoDB. Here we discuss the features Mongo claims to have implemented, and go further into its deployment, and performance.




#MongoDB
MongoDB is a new database technology that is has no concept of tables, schemes, SQL or rows. It does not have transactions, ACID compliance, joins, foreign keys, or many other features that tend to cause headaches in the early hours of the morning.





#MongoDB Features
- **Document-oriented storage**: JSON-style documents with dynamic schemas offer simplicity and power.

- **Full Index Support**: Index on any attribute, just like you're used to.

- **Replication & High Availability**: Mirror across LANs and WANs for scale and peace of mind.

- **Auto-Sharding**: Scale horizontally without compromising functionality.

- **Querying**: Rich, document-based queries.

- **Fast In-Place Updates**: Atomic modifiers for contention-free performance.

- **Map/Reduce**: Flexible aggregation and data processing.

- **GridFS**: Store files of any size without complicating your stack.

- **Commercial Support**: Enterprise class support, training, and consulting available.



## Lack of Innate Support for Transactions
To improve traditional RDBMS performance, one would normally purchase bigger more powerful machine, this **vertical scaling** however is limited. Another type of scaling **horizontal scaling** is to have multiple machines running the same database. 

There are several different types of horizontal scaling:

- **Active - Passive**: Is where two database instances exists on different machines, one is the active instance, the other is the passive. All queries are directed to the active. Write operations are replicated to the passive instance to jeep it in sync. In the event of failure of the active and update distributes traffic is redirected to the passive instance.[[1]][scaling]

- **Master - Slave**: Using two or more database instances, one instance is the master while the others are slaves. All writes are routed to the master instance, then replicated to the slaves. Read operations are routed to the slave instances. In the event of failure of the master, one of the slave instances must be designated as the new master and update operations rerouted.[[1]][scaling]

- **Cluster**: Scaling by clustering distributes query processing and data storage across multiple nodes to eliminate single points of failure and allow almost unlimited scaling. The cluster is made up of three types of nodes: storage nodes, query processing nodes, and management nodes. To realize the full high-availability potential, a cluster should contain at least two of each type of node.[[1]][scaling]

- **Sharding**: Sharing comes in many forms. In the most basic sense, it describes breaking up a large database into many smaller databases. Sharding can include strategies like carving off tables, or cutting up tables vertically (by columns). [[1]][scaling]



## Document Oriented Storage via BSON
BSON is a binary-encoded serialisation of JSON-like documents. BSON is designed to be lightweight, traversable, and efficient. BSON, like JSON, supports the embedding of objects and arrays within other objects and arrays. 

MongoDB uses BSON as the data storage and network transfer format for "documents". BSON at first seems BLOB-like, but there exists an important difference: the Mongo database understands the BSON internals. This means that MongoDB can "reach inside" BSON objects, even nested ones. Among other things, this allows MongoDB to build indexes and match objects against query expressions on both top level and nested BSON keys.[[2]][BSON]

## Storing Binary Data
GridFS is MongoDB's solution to storing binary data in the database. BSON supports saving up to 4MB of binary data in a document, and this could well be enough for your needs. For example, if you want to store a profile picture or a sound clip, then 4MB might be more space than you need. On the other hand, if you want to store movie clips, high-quality audio clips or even files are are several hundred megabytes in size, then MongoDB has you covered here, too.

GridFS works by storing the information about the file (called metadata) in the `files` collection. The data itself is broken down into pieces called `chunks` that are stored in the `chunks` collection. This approach makes storing data both easy and scalable; it also makes range operations (such as retrieving specific parts of a file) much easier to use.[[3]][MongoDB Book]


# NUMA machine
Depending on the machine you are running, if the machine is a NUMA machine or Non-Uniform Memeory Access, MongoDB complains the following:

    Thu Sep 20 12:49:58 [initandlisten] ** WARNING: You are running on a NUMA machine.
    Thu Sep 20 12:49:58 [initandlisten] **          We suggest launching mongod like this to avoid performance problems:
    Thu Sep 20 12:49:58 [initandlisten] **              numactl --interleave=all mongod [other options]

To solve this problem if on a NUMA machine is to run the following:

    sudo numactl --interleave=all /usr/local/bin/mongod

Note: This is slightly different from the command given by the warning message, since numactl has to be run with root permission and the full path of mongodb has to be given.

NUMA machines provide a linear address space, allowing all processors to directly address all memory. This feature exploits the 64-bit addressing available in modern scientific computers. The advantages over distributed memory machines include:

- Faster movement of data
- Less replication of data
- Easier programming

The disadvantages:

- Cost of hardware routers
- Lack of programming standards for large configurations

The fundamental building block of a NUMA machine is a Uniform Memory Access (UMA ) region that we will call a "node". Within this region, the processors share a common memory. This local emory provides the fastest memeory access for each of hte processors on the node. The number of processors on a node is limited by the speed of the switch that couples the processors with their local memroy. 

For larger configurations, multiple nodes are combined to form a NUMA machine. When a processor on one node references data that is stored on another node, hardware routers automatically send the data from the node where it is stored to the node where it is being requested. This extra step in memory access results in delays, which can degrade performance.

Small to medium NUMA machines have only one level of memory hierarchy; data is either local or remote. Larger NUMA machines use a routing topology, where delays are greater for nodes further away.[[4]][NUMA]



[scaling]: http://www.oshyn.com/_blog/General/post/A_Summary_of_Scaling_Options_for_MySQL/ "A Summary of Scaling Options for MySQL"
[BSON]: http://www.mongodb.org/display/DOCS/BSON "BSON"
[MongoDB book]: http://www.apress.com/9781430230519 "The Definitive Guide To MongoDB"
[NUMA]: http://www.fmslib.com/fmsman/doc/numa.html "NUMA Machines"
