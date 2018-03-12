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
  
  * **start-dfs.sh** - starts Namenode / Datanode / SecondaryNamenode
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
  * **mr-jobhistory-daemon.sh stop historyserver** - Stops history server\
  * **stop-yarn.sh** - Stops Nodemanager / ResourceManager
  * **stop-dfs.sh** - Stops Namenode / Datanode / SecondaryNamenode
  
  
