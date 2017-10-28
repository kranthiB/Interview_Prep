### What is Camel?
  * *is an integration framework*
      * routing engine builder - own routing rules
          * decide where to accept messages
          * how to process them
          * send processed ones to different destinations
  * *high level abstractions*
      * support multiple protocols and data types
          * no need to convert to canonical format
          * can built proprietory protocols if needed
  * *is not an ~~Enterprise Service Bus (ESB)~~*
      * even though it's support routing / transformation / monitoring / orchestration and so forth
      * it doesn't have a container or a reliable message bus
      * it can be deployed on Open ESB 
      
### Why use Camel?
  * *Routing and Mediation Framework*
  * *Enterprise Integration Patterns (EIPs)*
      * EIPs describe problems and solutions and also provide a common vocabulary, but the vocabulary isn't formalized
      * To fill this gap , camel came with language (DSL) to describe solutions
  * *Domain Specific Language (DSL)*  
      * These are computer languages that target specific problem domain, rather than a general purpose domain like most programming languages
      * DSL is unique as it offers multiple programming languages such as Java / Scala / Groovy.
      * Also offers to be specified in XML
      * External DSLs
          * Custom Syntax
          * Requires Separate Compiler or Interpreter to execute
      * Internal DSLs
          * Also referred as Embedded DSLs / Fluent Interfaces (aka Fluent Builders)
      * Camel's domain is enterprise integration
          * Java DSL is set of fluent interfaces which contain methods named after terms from EIP book
  * *Extensive Component Library*
      * Components enable Camel to connect over transports / use APIs / understand data formats
  * *Payload-Agnostic Router*
  * *Modular and Pluggable Architecture*
      * Any Component can be loaded whether it ships from camel / third party / its custom creation
  * *POJO Model*
  * *Easy Configuration*
  * *Automatic Type Converters*
  * *Lightweight core*
      * Depends on Apache Commons Logging and FuseSource Commons Management
      * It can be deployed into a/an
          * Standalone applicatin
          * Web application
          * Spring application
          * Java EE application
          * OSGI bundle
          * JBI container
          * Java Web Start
          * Google App Engine
  * *Test kits*
  * *Vibrant community*

### Camel's Message Model
  * *org.apache.camel.Message*
      * Elements
          * Unique Identifier(UID)
              * of type **"java.lang.String"**
              * Enforced and Guaranteed by message creator
              * Protocol Dependent
              * Camel use its own UID generator if protocol don't define.
          * Headers
              * name-value pairs
                  * name is unique
                  * name is case-insensitive string
                  * value is of type **"java.lang.Object"**
          * Attachments
              * It's optional
              * used for web service and email components
          * Body
              * of type **"java.lang.Object"**
              * automatic type converters will come into picture if sender and receiver use different formats
          * Fault flag
              * To distinguish between output and fault messages in few protocols and specifications such as WSDL and JBI
  * *org.apache.camel.Exchange*
      * Message's container during routing
      * supports various types of interactions between systems known as Message Exchange Patterns(MEPs)
          * one-way message style
          * request-response message style
      * Elements
          * Exchange ID
              * camel will generate if we don't specify
          * MEP
              * InOnly
              * InOut
          * Exception
          * Properties
              * Similar to headers
              * this will last for the duration of entire exchange
          * In Message
              * it's mandatory
              * request message
          * Out Message
              * it's optional
              * exists only if MEP is InOut
              * reply message

### Camel Architecture
  * **CamelContext**
      * Camel's runtime
      * Services
          * Components
          * Endpoints
          * Routes
          * Type Converters
          * Data Formats
          * Registry
              * allows you to look up for beans
              * default is **JNDI** registry
              * Spring **ApplicationContext**
              * **OSGI** registry
          * Languages
  * **Routing Engine**
      * is what actually moves messages under the hood
      * ensures messages are routed properly
  * **Routes**
  * **DSL**
  * **Processors**
      * a node capable of using / creating / modifying an incoming message
      * Message filter processor
      * Content-based router processor
      * Handle things in between endpoints like
          * EIPs
          * Routing
          * Transformation
          * Mediation
          * Enrichment
          * Validation
          * Interception
  * **Components**
      * provides a uniform endpoint interface
      * connect to other systems
  * **Endpoints**
      * acts as a factory for creating consumers and producers that are capable of receiving and sending messages to a particular endpoint
  * **Producers**
  * **Consumers**
      * two kinds
          * event-driven (asynchronous receiver in the EIP world)
          * polling (synchronous receiver in the EIP world)
   
```
  **File Copy Sample**

  camelContext.addRoutes(new RouteBuilder() {
    @Override
    public void configure() throws Exception {
      from("file:data/inbox?noop=true).to("file:data/outbox");
    }
  });
```



```
  **FTP to JMS**
  **http://camel.apache.org/jms.html**
  **http://camel.apache.org/ftp2.html**
  
  camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("ftp://rider.com/orders?username=rider&password=secret")
        .process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                System.out.println("we just downloaded: " + exchange.getIn().getHeader("FileName"));
            }
          })
        .to("jms:incomingOrders");
      }
  });
  
```

### Java DSL vs Spring DSL
  * Java DSL is slightly richer language to work with as will have full power of language at your fingertips.
      * Also, features like value builders(for building expressions and predicates) are not available in Spring DSL
  * Spring DSL provides
      * Object Construction capabilites
      * Abstractions for things like database connections / JMS integration
      * Finding Route Builders
          * bit more dynamic with 
              * **packageScan**(*includes / excludes*) (will use **RouteBuilder** class)
              * **contextScan**(*@Component stereotype) (here have to use **SpringRouteBuilder** class)
      * Importing Configuration and Routes
      * Advanced Configuration Options
          * Pluggable bean registries
          * Tracer and Delay mechanisms
          * Custom class resolvers, tracing, fault handling and startup
          * Configuration of interceptors
          
### Routing and EIPs

  * **Content-Based Router**
    ```
    from("file:data/input?noop=true").to("jms:incomingOrders");
    
    from("jms:incomingOrders")
        .choice()
            .when(header("CamelFileName").endsWith("Xml"))
                .to("jms:xmlOrders")
            .when(header("CamelFileName").regex("^.*(csv|csl)$")) ## used regular expression
                .to("jms:csvOrders")
            .otherwise() ## used otherwise
                .to("jms:badOrders").stop() ## stopped when its bad order 
            .end()
            .to("jms:continuedProcessing"); # Proceed to this after done through one of the parallel process
    
    from("jms:xmlOrders").process(new Processor() {
        public void process(Exchange exchange) throws Exception {
            System.out.println("Received XML Order:" + exchange.getIn().getHeader("CamelFileName"));
        }
    });
    
    from("jms:csvOrders").process(new Processor() {
        public void process(Exchange exchange) throws Exception {
            System.out.println("Received CSV Order:" + exchange.getIn().getHeader("CamelFileName"));
        }
    });
    
    from("jms:badOrders").process(new Processor() {
        public void process(Exchange exchange) throws Exception {
            System.out.println("Received Bad Order:" + exchange.getIn().getHeader("CamelFileName"));
        }
    });
    
    from("jms:continuedProcessing").process(new Processor() {
        public void process(Exchange exchange) throws Exception {
            System.out.println("Received Continued Order:" + exchange.getIn().getHeader("CamelFileName"));
        }
    });
    ```
    * Features
        * can use regular expression
        * otherwise
        * to separate process from the output content routing process 
        * can stop the flow 
   * **Message Filter**
     ```
      from("file:data/input?noop=true").to("jms:incomingOrders");
      
      from("jms:incomingOrders")
          .choice()
              .when(header("CamelFileName").endsWith("Xml"))
                  .to("jms:xmlOrders")
              .when(header("CamelFileName").regex("^.*(csv|csl)$"))
                  .to("jms:csvOrders")
              .otherwise()
                  .to("jms:badOrders");
      
      from("jms:xmlOrders").filter(xpath("/order[not(@test)]")) ## Applied xpath filter
          .process(new Processor() {
              public void process(Exchange exchange) throws Exception {
                  System.out.println("Received XML order: " + exchange.getIn().getHeader("CamelFileName"));
              }
          });
     ```
   * **Multicast**
     ```
     from("file:data/input?noop=true").to("jms:incomingOrders");
     
     from("jms:incomingOrders")
         .choice()
            .when(header("CamelFileName").endsWith(".xml"))
                .to("jms:xmlOrders")
            .when(header("CamelFileName").regex("^.*(csv|csl)$"))
                .to("jms:csvOrders")
            .otherwise()
                .to("jms:badOrders");
     
     ExecutorService executorService = Executors.newFixedThreadPool(16);
     from("jms:xmlOrders")
         .multicast()
              .stopOnException()  ## will stop on exception
              .parallelProcessing()  ## Initiated Parallel Processing
                  .executorService(executorService)  ## specfied threads max to 16 instead of 10(default)
         .to("jms:accounting", "jms:production");
     
     from("jms:accounting").process(new Processor() {
       public void process(Exchange exchange) throws Exception {
         System.out.println("Accounting received order: " + exchange.getIn().getHeader("CamelFileName"));
       }
     });
     from("jms:production").process(new Processor() {
       public void process(Exchange exchange) throws Exception {
         System.out.println("Production received order: " + exchange.getIn().getHeader("CamelFileName"));
       }
     });
     ```
     *  Features -
        * stop on exception
        * can specify number of threads to start 
   * RecipientList
     ```
     from("file:data/input?noop=true").to("jms:incomingOrders");
     
     from("jms:incomingOrders")
              .choice()
                 .when(header("CamelFileName").endsWith(".xml"))
                     .to("jms:xmlOrders")
                 .when(header("CamelFileName").regex("^.*(csv|csl)$"))
                     .to("jms:csvOrders")
                 .otherwise()
                     .to("jms:badOrders");
                     
     from("jms:xmlOrders")
         .setHeader("customer", xpath("/order/@customer"))
         .process(new Processor() {
           public void process(Exchange exchange) throws Exception {
             String recipients = "jms:accounting";
             String customer = exchange.getIn().getHeader("customer", String.class);
             if (customer.equals("honda")) {
               recipients = ",jms:production";
             }
             exchange.getIn().setHeader("recipients", recipients);
           }
         })
         .recipientList(header("recipients"));
     
     from("jms:accounting").process(new Processor() {
       public void process(Exchange exchange) throws Exception {
         System.out.println("Accounting received order: "
             + exchange.getIn().getHeader("CamelFileName"));
       }
     });
     
     from("jms:production").process(new Processor() {
       public void process(Exchange exchange) throws Exception {
         System.out.println("Production received order: "
             + exchange.getIn().getHeader("CamelFileName"));
       }
     });
     ```
      * Through multi-cast, we can't implement priority
      * Expression result must be iterable. Values that will work are
          * java.util.Collection
          * java.util.Iterator
          * Java arrays
          * org.w3c.dom.NodeList
          * String with comma separated
          
      * Recipient List annotation
        ```
          from("jms:xmlOrders").bean(RecipientListBean.class);
          
          class RecipientListBean {
          
              @org.apache.camel.RecipientList
              private String[] route(@XPath("/order/@customer") String customer) {
                if (isGoldCustomer(customer)) {
                  return new String[]{"jms:accounting", "jms:production"};
                } else {
                  return new String[]{"jms:accounting"};
                }
              }
          
              private boolean isGoldCustomer(String customer) {
                return customer.equals("honda");
              }
            }
          ```
   * WireTap
     ```
      from("file:src/data?noop=true").to("jms:incomingOrders");
      
      from("jms:incomingOrders")
          .wireTap("jms:orderAudit")
          .choice()
              .when(header("CamelFileName").endsWith(".xml"))
                  .to("jms:xmlOrders")
              .when(header("CamelFileName").regex("^.*(csv|csl)$"))
                  .to("jms:csvOrders")
              .otherwise()
                  .to("jms:badOrders");
      ```
      * **wireTap** use *InOnly MEP* pattern
   
   
  




https://github.com/camelinaction/camelinaction

https://github.com/apache/camel

