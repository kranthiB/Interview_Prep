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
    * **Coherency model**
      * this model for a file system describes the data visibility of reads and writes for a file.
      * creating a file is visible in the filesystem namespace
        ```
         fs.create(new Path("p"))
        ```
      * writing to a file 
        * **out.flush()** 
          * is not guaranteed to be visible
          * once more than a block's worth of data has been written, the first block will be visible to new readers
          * it is always current block being written is not visible to other readers
        * **out.hflush()** - 
          * data written up to that point in the file has reached all the datanodes in the write pipeline and is visible to all new readers
          * it does not guarantee that the datanodes have written the data to the disk, only that it's in the datanode memory
        * **out.hsync()**
          * this stores the data to the datanode's disk
        * **out.close()**
          * peforms implicit hflush()
      * consequences for application design
        * an acceptable trade-off to use of *flush()* / *hflush()* / *hsync()*  is an application dependent which varies between data robustness and throughput
        * suitable values can be selected after measuring performance with different *hflush()* or *hsync()* frequencies.
  * **Parallel Copying with distcp**
    * Copy one file to another
      ```
       hadoop distcp file1 file2
      ```
    * Copy directories
      ```
       hadoop distcp dir1 dir2
      ```
      * If dir2 does not exist, it will be created 
      * If dir2 exists, dir1 will be copied under it - *dir2/dir1*
        * to overwrite and keep the same directory structure use option *-overwrite*
        * to update only the files changed use option *-update*
    * *distcp* is implemented as a MapReduce job
      * work of copying is done by maps that run in parallel across the cluster
      * no reducers
      * tries to give each map same amount of data
      * by default, up to 20 maps are used, this can be changed by specifying *-m* argument
      * hadoop distcp -update -delete -p hdfs://namenode1/foo hdfs://namenode2/foo
        *  *-delete* to remove the files or directories that are not present in the source
        * *-p* - file status attributes like permissions, block size, and replication are preserved
      * if two clusters are running incompatible versions of HDFS, we can use *webhdfs* protocol
        * hadoop distcp webhdfs://namenode1:50070/foo webhdfs://namenode2:50070/foo
      * use the *balancer* tool to subsequently even out the distributions across the cluster

### Map Reduce ###
 * **Anatomy of a MapReduce Job Run**
   * **Entities**
     * Client - submits MapReduce job
     * YARN Resource Manager - coordinates the allocation of compute resources on the cluster
     * YARN Node Managers - launch and monitor the compute containers on machines in the cluster
     * MR Application Master - coordinates the tasks running the MR job
       * Application master and MR tasks run in containers that are scheduled by resource manager and managed by node managers
     * DFS (normally HDFS) - used for sharing job files between the other entities.
   * ![anatomy of mr job run](https://user-images.githubusercontent.com/20100300/37778849-fc0add48-2e10-11e8-8784-3fc1e0c273ea.png)
   * Streaming
     * runs special map and reduce tasks
     * communicates with the process(whic may be written in any language) using standard input and output streams
     * During execution of task
       * Java process passes input key-value pairs to the external process which runs it through the user-defined map or reduce function and passes the output key-value pairs back to the Java process
   * Progress and status updates
     * a job and each of its tasks have a status which includes
       * state of job or task (running / successfully completed / failed)
       * progress of maps and reduces
         * For map tasks, this is the proportion of the input that has been processed
         * For reduce tasks, it does by dividing the total progress in to three parts corresponding to the three phases of the shuffle
         * what constitutes progress in MR?
           * reading an input record (in a mapper or reducer)
           * writing an output record(in a mapper or reducer)
           * setting the status description (via Reporter's or TaskAttemptContect's setStatus() method)
           * Incrementing a counter (via Reporter's incrCounter() method or Counter's increment() method)
           * Calling Reporter's or TaskAttemptContext's progress() method
       * values of the job's counters
       * status message or description
     * a task also have set of counters that count various events which are either built into or defined by users
     * using *umbilical* interface , task reports its progress and status(including counters) back to its application master for every three seconds
     * Resource manager web UI displays all the running application with
       * links to web UIs of thier respective application masters and each will diplay
         * details on the MR job including its progress
     * During the course of the job, client receives the latest status by polling application master every second (the interval is set via *mapreduce.client.progressmonitor.pollinterval*)
     * clients can also use Job's getStatus() method to obtain a JobStatus instance, which contains all of the status information for the job
   * Job Completion
     * applicaiton master changes the status to "successful" when the last task for a job is complete
     * application master also sends HTTP job notification via the *mapreduce.job.end-notificaion.url*
     * on job completion
       * application master and task containers clean up their working state
       * *OutputCommitter's* commitJob() method is called.
       * Job information is archived by the job history server
 * **Failures**
   * **Task Failure**
     * if an application(map or reduce task) throws a runtime exception, JVM reports error back to application master
       * applicatin master marks the task attempt as *failed* and frees up the container
     * if streaming task exists with a nonzero exit code, marked as failed. Governed by *stream.non.zero.exist.is.failure* property (default is true)
     * sudden exit of the task JVM - due to JVM bug then node manager notices this and informs the application master s it can mark attempt as failed
     * Hanging tasks
       * application master hasn't received a progress update and proceeds to mark the task as failed
       * timeout period is 10 minutes and can be configured on a per-job or cluster basis using *mapreduce.task.timeout* property in milliseconds
         * timeout to a value of zero disables the timeout, so long-running tasks are never marked as failed
     * If application master notified of a task attempt has failed
       * reschedule execution of a task
       * try to avoid rescheduling on a node manager where it previously failed
       * if task fails four times, it will not be retried again
         * value is configurable - *mapreduce.map.maxattempts* / *mapreduce.reduce.maxattempts*
       * if task fails maximum number of attempts, the whole job fails
         * if it is undesirable to abort a job if task fails then we can configure the maximum percentage of tasks that are allowed to fail without triggering job failure using the *mapreduce.map.failures.maxpercent* / *mapreduce.reduce.failures.maxpercent* 
       * task fail can also be possible  - speculative execution / node manager failed / application master failed then the maximum number of attempts for map / reduce won't effect
       * users may kill task using web UI or the command line(*mapred job*)
   * **Application Master Failure**
     * maximum number of times to run a MapReduce application master is controlled by *mapreduce.am.max-attempts* (default is 2). If it fails twice then the job will fail
     * Yarn imposes maximum number of attempts for any application in the cluster through *yarn.resourcemanager.am.max-attempts* (default is 2)
       * if want to increase the MR application master attempts , this has to be increased too
     * way of recovery
       * AM sends periodic hearbeats to resource manager and in the event of failure RM will delete the failure and start a new instance of AM in a new container
         * MR AM will use the job history to recover the state of the tasks
         * it is configurable(default is true) through *yarn.app.mapreduce.am.job.recovery.enable*
     * client will receive status through AM and the information is cached. On failure, it will experience a timeout and ask the RM for new instance of AM. It is transparent to user
   * **Node Manager Failure**
     * RM removes NM from its pool of nodes if it does not receive heartbeat for 10 minutes and this is configurable using *yarn.resourcemanager.nm.liveness-monitor.expiry-interval-ms* property
     * For incompleted jobs, map tasks that were run on the node manager will rerun as their intermediate output residing on NM local file system may not be accessible to reduce task
     * If number of failures for the application is high, NM is blacklisted (default is 3) and user can set the threshold with *mapreduce.job.maxtaskfailures.per.tracker* property.
       * AM from new job can select bad nodes even if it is blacklisted by another AM of another job.
   * **Resource Manager Failure**
     * have to run in active-standby configuration to achieve high availabilty(HA)
     * information of all the running applications is stored in highly available state store(backed by zookeeper or HDFS)
       * NM information is not stored in the state store
       * tasks are not part of RM's state since they are managed by AM
     * on new resource starts
       * reads application information from the state store
       * restarts the AMs (does not count to *yarn.resourcemanager.am.max-attempts*)
     * transition of a RM from standby to active is handled by failover controller
       * default is automatic one which uses ZooKeeper leader election to ensure that there is only a single active RM.
       * this does not have a standalone process, and is embeded in the RM by default. (Separate process is possible but not receommended)
     * clients and node managers tries to connect RM in a round-robin fashion until standby becomes active.
 * **Shuffle and Sort**
   * MR guarantees input to every reducer is sorted by key.
   * shuffle - process by which the system performs the sort and transfers the map outputs to the reducers as inputs
   * shuffle is the heart of MR
   * **Map Side**
     * map function do buffer writes in memory and do some presorting before it is written to disk
     * each map has a circular memory buffer
     * buffer is 100MB by default (can be tuned using *mapreduce.task.io.sort.mb*)
     * buffer reaches threshold size(*mapreduce.map.sort.spill.percent, default is 0.80 or 80%), a background thread will start to spill the contents to disk
     * map will continue to write the buffer while spill takes place but map blocks if buffer fills up.
     * spills are written in round-robin fashion to the directories specified by *mapreduce.cluster.local.dir* or in a job specifc subdirectory
     * before writes to disk,thread divides the data into partitions corresponding to reducers that they ultimately be sent to
     * within each partition, background thread performs as in-memory sort by key
     * if there is a combiner funtion, it is run on the output of the sort
     * combiner function makes more compact map output, so less data write to disk and to transfer to the reducer
     * on buffer reaches spill threshold, a new spill file is created
     * these spill files are merged into a single partioned and sorted output file (*mapreduce.task.io.sort.factor* controls this merge process, default is 10).
     * atleast 3 spill files (*mapreduce.map.combine.minspills*) , combiner is run again before output file is written
     * compress map output (default, output is not compressed) - enable by *mapreduce.map.output.compress* and the library is specified by *mapreduce.map.output.compress.codec*
     * output files are made available to the reducers over HTTP
     * maximum number of worker threads used to serve file partitions is controlled by *mapreduce.shuffle.max.threads* (this setting is per node manager, not per map task)
       * default of 0 sets the maximum number of threads to twice the number of processors on the machine.
   * **Reduce Side**
     * copy phase 
       * map task may finish at different times, so the reduce starts copying their outputs as soon as each completes.
       * reduce has small number of copier threads so that it can fetch map outputs in parallel.(default is five or *mapreduce.reduce.shuffle.parallelcopies*
       * this thread periodically asks AM for map hosts to know the machines where to fetch output
       * these map hosts do not delete ouputs from disk as soon as first reducer has retrieved them 
         * this will wait until told by AM which is after the job has completed.
       * map outputs are copied to reduce task JVM' if they are small enough controlled by *mapreduce.reduce.shuffle.input.buffer.percent* which specifies the proportion of the heap to use for this purpose
       * when in-memoruy reaches threshold size(*mapreduce.reduce.shuffle.merge.percent*) or threshold number of map outputs(*mapreduce.reduce.merge.inmem.threshold*), it is merged and spilled to disk.
       * If a combiner is specified, it will be run during the merge to reduce amount of data written to disk
       * Once copy to disk done. a background thread - merges them inyo larger, sorted files. If map outputs compressed by map tasks then they have to decompress before merging to sorted files.
       * when all map outputs have been copied, reducer task moves to sort phase
     * sort phase (merge phase)
     
    
     
       
         
       
      
            
      
        
