### Install and Configure(Pseudodistributed mode) Apache Hadoop
  * Prerequisite - Java 
  * Download the appropriate version from  http://hadoop.apache.org/releases.html#Download
  * Set HADOOP_HOME environment variable and add it to PATH
  * Check the hadoop by executing the command "hadoop version"
  * Each component in hadoop is configured by using an XML File (all the below files will exists in the "etc/hadoop" directory)
      * "core-site.xml" - common properties
      * "hdfs-site.xml" - hdfs properties
      * "mapred-site.xml" - mapreduce properties
      * "yarn-site.xml" - yarn properties
  * Default settings (core-default.xml, hdfs-default.xml, mapred-deafult.xml, yarn-default.xml) can be found under "share/doc" directory
  * Hadoop can be run in one of three modes
      * Standalone(or local) mode
          * Runs everything in single JVM
      * Pseudodistributed mode
          * Hadoop daemons run on the single machine
      * Fully distributed mode
          * Hadop daemons run on a cluster of machines
  * Key configuration properties for different modes
  
      | Component | Property | Standalone | Pseudodisributed | Fully Distributed |
      | --------- | -------- | ---------- | ---------------- | ----------------- |
      |   Common  | fs.defaultFS | file:/// (default) | hdfs://localhost/ | hdfs://namenode/ |
      |   HDFS    | dfs.replication | N/A | 1 | 3 |
      | MapReduce | mapreduce.framework.name | local | yarn | yarn |
      |   YARN    | yarn.resourcemanager.hostname | N/A | localhost | resourcemanager |
      |   YARN    | yarn.nodemanager.aux-services | N/A | mapreduce_shuffe | mapreduce_shuffle |
  
  * The default config directory is etc/hadoop. If want can change using the environment variable - **HADOOP_CONF_DIR**
  * **start-dfs.sh** - starts Namenode / Datanode / SecondaryNamenode
    (Note: have to set JAVA_HOME in hadoop-env.sh file)
    * Check the status by
      * jps (or)
      * http://localhost:50070/ or http://localhost:8042/
  * **start-yarn.sh** - Starts Nodemanager / Resourcemanager
    * Check the status by
      * jps (or)
      * http://localhost:8088/
  * **mr-jobhistory-daemon.sh start historyserver**  - Starts history server
    * Check the status by
      * jps (or)
      * http://localhost:19888/
  * **mr-jobhistory-daemon.sh stop historyserver** - Stops history server
  * **stop-yarn.sh** - Stops Nodemanager / ResourceManager
  * **stop-dfs.sh** - Stops Namenode / Datanode / SecondaryNamenode
  
### Meet Hadoop ###
 * 1 Zettabyte = 10^21 bytes = 1000 exabytes = 1M petabytes = 1B terabytes
 * Hadoop enables reliable, scalable platform for storage(replication) and analysis(MapReduce framework)
 * MapReduce = brite-force approach - batch processing system
 * YARN (Yet Another Resource negotiator) - realtime distributed computations (like HBase - interactive analysis) not just MapReduce to run on data in a Hadoop cluster
 * **Processing Patterns (run on Hadoop)**
   * **Interactive SQL** - to achieve low latency reponses for SQL queries on Hadoop
     * "always-on" daemon like Impala
     * Container reuse like Hive on Tez
   * **Iterative Processing**
     * Spark - enables a hihgly exploratory style of working with datasets
   * **Spark Streaming** - real-time distributed computations
     * Storm, Spark Streaming , Samza
   * **Search**
     * Solr - indexing documents that are added to HDFS, serving search queries from indexes stored in HDFS

### Hadoop Distributed File System ###
 * **Design**
   * for storing **very large files** with **streaming data access patterns**(write once, read many times), running on clusters of **commodity hardware**.
   * **Not Fit** in case of
     * Low Latency responses
     * Lots of small files
       * limit to number of files is governed by amount of memory on the namenode.
     * Multiple writers, arbitrary file modifications
 * **Concepts**
   * **Blocks**
     * Seek Time
       * is the process of moving the disk's head to a particular place on the disk to read or write data. 
       * it characterizes the latency of disk operation
     * Transfer rate
       * amount of data that can be transmitted in fixed amount of time
       * it characterizes the disk bandwidth
     * Default block size is 128MB
     * Files in HDFS are broken into block-sized chunks
     * Reason for large blocks in HDFS is to minimize the cost of seeks
     * **Benefits of block abstraction**
       * file can be larger than any single disk on the network
       * Simplifies the storage subsystem
          * as blocks are fixed sizes , it is easy to calculate how many can be stored on a given disk
          * eliminates the metadata concern
            * file metadata such as permissions information does not need to be stored with the blocks
       * blocks fit well with replication for providing fault tolerance and avaiiability
     * Command to list the blocks - **hdfs fsck / -files -blocks**
   * **Namenodes and Datanodes**
     * HDFS cluster has 2 types of nodes (operates in master-worker pattern)
       * a Namenode (master)
         * manages the filesystem namespace
         * maintains the filesystem tree and the metadata for all the files and directories in the tree.
         * information is stored on the local disk in the form of two files
           * the namespace image
           * the edit log
         * also knows the datanodes
           * but it does not store block locations persistently
             * this information is reconstructed from the datanodes when system starts
         * Resilient to failure
           * Hadoop provide two mechanisms
             * persist state to multiple filesystems
               * to write to local disk as well as remote NFS mount
             * secondary namenode
               * periodically merge the namespace image with the edit log to prevent the edit log from becoming too large
               * keeps a copy of merged namespace image
       * a number of datanodes (workers)
         * workhorses of the file system
           * store and retrieve te blocks when they are told to (by clients or namenode)
         * periodically, they report back to Namenode with the list of blocks that they are storing
    * **Block Caching**
      * For frequent access, blocks explicitly cached in an off-heap *block cache*
        * By default, a block is cached in only one datanode's memory, although number is configurable on a per-file basis
      * Can instruct namenode which files to cache(and for how long) by adding *cache directive* to a *cache pool*
        * Cache pools are an administrative grouping for managing cache permissions and resource usage.
    * **HDFS Federation**
      * Allows cluster to scale by adding namenodes, each of which manages a portion of the filesystem namespace
        * For example, one namenode might manage all the files rooted under /user, say, and a second namenode might handle files under /share
      * Under federation , namenode manages
        * *namespace volume* - metadata for the namenode
        * *block pool* - all the blocks for the files in the namespace
          * block pool storage is not paritioned
            * however, datanodes register with each namenode in the cluster can store blocks from multiple block pools
      * One namenode do not communication with another in the cluster
      * Access federated HDFS cluster
        * Clients use client-side mount tables to map file paths to namenodes
          * this is managed in configuration using *ViewFileSystem* and the *viewfs://* URIs
    * **HDFS High Availability (HA)**
      * In this there are two pair of namenodes in an active-standby configuration. A few architectural changes are needed to allow this to happen
        * shared storage to share the edit log
        * datanodes must send block reports to both namenodes because the block mapping are stored in a namenode's memory, and not on disk
        * clients must be configured to handle namenode failover
        * secondary namenode's role is subsumed by the standby
      * Two choices for HA shared storage
        * NFS filer
        * Quorum journal manager (QJM)  - its recommended - runs a group of journal nodes
