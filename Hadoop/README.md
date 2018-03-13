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
