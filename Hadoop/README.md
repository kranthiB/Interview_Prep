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
      * Failover and fencing
        * Transition from active to standby is managed by a new entity called the *failover controller*
          * it uses Zookeeper
          * types of failover
            * Graceful failover
            * Ungraceful failover
        * By using method called *fencing*, it ensures that the previously active namenode is prevented from doing any damage and causing corruption
  * **Command line interface**
    * **Basic Filesystem operations**
      * Copy file from local filesystem to HDFS
        * hadoop fs -copyFromLocal <local-file-path> hdfs://namenode/<hdfs-file-path>
        * hadoop fs -copyFromLocal <local-file-path> <hdfs-file-path>
        * hadoop fs -copyFromLocal <local-file-path> <file-name> (copies the file to user home directory)
      * Copy file to local filesystem
        * hadoop fs -copyToLocal <hdfs-filepath> <local-file-path>
        * (check MD5 digest same or not using md5 <original-file> <copied-file>) 
      * hadoop fs -mkdir books
      * hadop fs -ls .
        * In output
          * column-1 = File mode 
          * column-2 = Replication factor of the file (for directories, value will be empty)
          * column-3 = owner
          * column-4 = group
          * column-5 = size of file in bytes or zero for directories
          * column-6 = last modification date
          * column-7 = last modification time
          * column-8 = name of the file or directory
    * File permissions in HDFS
      * much like POSIX model
      * three types of permissions
        * r - read
        * w - write
        * x - execute
          * this is ignored for file because can't execute a file on HDFS (unlike POSIX)
          * required for directory to access its children
        * *dfs.permissions.enabled* 
          * whether to check permissions or not
          * superuser - identity of the namenode process - permissions check not performed for superuser
  * **Hadoop Filesystems**
    * Java abstract class - *org.apache.hadoop.fs.FileSystem*
      
      | Filesystem | URI scheme | Javaimplemetation (all under *org.apache.hadoop*) | Description |
      | ---------- | ---------- | ----------------- | ----------- |
      | Local | file | fs.LocalFileSystem |  with client-side checksums. *RawLocalFileSystem* with no checksums |
      | HDFS | hdfs | hdfs.DistributedFileSystem | designed to work efficiently in conjunction with MapReduce |
      | WebHDFS | webhdfs | hdfs.web.WebHdfsFileSystem | authenticated read/write access to HDFS over HTTP |
      | Secure WebHDFS | swebhdfs | hdfs.web.SWebHdfsFileSystem | HTTPS version of WebHDFS |
      | HAR | har | fs.HarFileSystem | A filesystem layered on another filesystem for archiving files. Hadoop archives lot of files in to a single archive file to reduce namenode's memory usage. Command - *hadoop archive* |
      | View | viewfs | viewfs.ViewFileSystem | used to create mount points for federated namenodes |
      | FTP | ftp | fs.ftp.FTPFileSystem | backed by an FTP server |
      | S3 | s3 | fs.s3a.S3AFileSystem | backed by Amazon S3 |
      | Azure | wasb | fs.azure.NativeAzureFileSystem | backed by Microsoft Azure |
      | Swift | swift | fs.swift.snative.SwiftNativeFileSystem | backed by OpenStack Swift |
      
      (Hint: To process large volumes of data while running MapReduce programs , its good to choose a distributed filesystem that has data locality optimization, notably HDFS)
  * **Interfaces**
    * **HTTP**
      * HTTP interface is slower than the native Java client
      * Two ways of accessing HDFS over HTTP (both use the WebHDFS protocol)
        * Directly
          * HDFS daemons serve HTTP requests to client
          * embedded webservers in the namenodes and datanodes act as WebHDFS endpoints
          * enabled by default - *dfs.webhdfs.enabled*
          * File metadata operations are handled by the namenode
          * File read and write operations
            * sent first to namenode which sends and HTTP redirect to the the client indicating the datanode to stream file data from or to.
        * via a proxy(or proxies)
          * access HDFS on the client's behalf using the usual *DistributedFileSystem* API
          * relies on one or more standalone proxy servers
            * proxies are stateless
            * can rubn behind a standalone load balancer
          * client never access the namenode or datanode directly as all traffic passes through the proxy
          * HttpFs proxy
            * exposes same HTTP and HTTPS interface as WebHDFS so clients can access both using webhdfs or swebhdfs
            * its started independently of the namenode and datanode daemons
            * defaultly listen on port - 14000
    * **C**
    * **NFS**
    * **FUSE** 
  * **Java Interface**
    * **Reading data from Hadoop URL**
      ```
          static {
             URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory());
          }
          InputStream in = new URL("hdfs://<host>/<path>").openStream();
      ```
    * **Reading data using the FILESYSTEM API**
  * **Data Flow**
    * **Anatomy of a File Read**
      ![anatomy of a file read](https://user-images.githubusercontent.com/20100300/37658582-e25d53a6-2c73-11e8-9c6b-3112b50f960a.png)
      * Datanodes are sorted according to their proximity of the client(i.e topology of the cluster's network)
      * If client itself a datanode then client will read from the local datanode if it hosts a copy of the block - *Short-circuit local reads*
      * This design allows HDFS to scale a large number of concurrent clients 
    * **Network Topology and Hadoop**
      * Network is represened as a tree and the distance between two nodes is the sum of their distances to the closest common ancestor
        * distance(/d1/r1/n1, /d1/r1/n1) = 0 (processes on the same node)
        * distance(/d1/r1/n1, /d1/r1/n2) = 2 (different nodes on the same rack)
        * distance(/d1/r1/n1, /d1/r2/n3) = 4 (nodes on different racks in the same data center)
        * distance(/d1/r1/n1, /d3/r3/n4) = 6 (nodes in different data centers)
    * **Anatomy of a File Write**
      ![anatomy of file write](https://user-images.githubusercontent.com/20100300/37704102-e5e6778c-2d1d-11e8-8002-ad2909da0963.png)
      * If datanode fails while data is being written
        * pipeline is closed 
        * any packets in the *ack queue* are added to the *data queue*
        * current block on the good datanode is given a new identity, which is communicated to the namenode
        * failed datanode is removed from the pipeline
        * new pipeline is constructed from the good datanodes
        * remainder of block's data is written to good datanodes in the pipeline
      * if multiple datanodes fail while a block is being written
        * write will succeed as long as *dfs.namenode.replication.min.replicas*(which defaults to 1) are written
        * block will be asyncronously replicated across the cluster until its target repliation factor(*dfs.replication*) is reached
      * Replica placement strategy
        * first replica on the same node
        * second replica is placed on a different rack from the first (off-rack), chosen at random
        * third replica is placed on the same rack as the second, but on a different node chosen at random
        * further replicas are placed on random nodes in the cluster
        * this strategy gives a good balance among
          * reliability(blocks are stored on different racks)
          * write bandwidth(writes only have to traverse a single network switch)
          * read performance(there's a choice of two racks to read from)
          * block distribution across the cluster(clients only write a single block on the local rack)

      
      
            
      
        
