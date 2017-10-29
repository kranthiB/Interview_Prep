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
      
### Data Transformation
  * Data Format Transformation
  * Data Type Transformation
  * Features
      * Data Transformation in routes
          * Message Translator EIP
              * equivalent to *Adapter Pattern* from Gang of Four
              * using a *processor*
                ```
                 from("quartz://report?cron=0+0+6+*+*+?")
                    .to("http://riders.com/orders/cmd=received&date=yesterday")
                    .process(new OrderToCsvProcessor())
                    .to("file://riders/orders?fileName=report-${header.Date}.csv");
                    
                 class OrderToCSVProcessor implements Processor {
                    public void process(Exchange exchange) throws Exception {
                      String custom = exchange.getIn().getBody(String.class);
                      String id = custom.substring(0,9);
                      String date = custom.substring(10,19);
                      String items = custom.substring(30);
                      String[] itemIds = items.split("@");
                      
                      StringBuilder csv = new StringBuilder();
                      csv.append(id.trim());
                      csv.append(",").append(date.trim());
                      csv.append(",").append(customerId.trim());
                      for(String item : itemIds) {
                        csv.append(",").append(item.trim());
                      }
                      exchange.getIn().setBody(csv.toString());
                    }
                 }
                ```
                  * using getIn / getOut methods on exchanges
                      * using *getOut*, incoming message headers and attachments will be lost
                          * to overcome we need to copy the headers and attachments from incoming to outgoing message, which can be tedious
                      * so, set the changes directly on incoming message using *getIn*, and not to use *getOut* at all
                  * disadvantage is required to use Camel API           
              * using *beans*
                ```
                from("quartz://report?cron=0+0+6+*+*+?")
                    .to("http://riders.com/orders/cmd=received&date=yesterday")
                    .bean(OrderToCsvBean())
                    .to("file://riders/orders?fileName=report-${header.Date}.csv");
                    
                class OrderToCSVBean {
                  public static string map(String custom) {
                      String id = custom.substring(0,9);
                      String date = custom.substring(10,19);
                      String items = custom.substring(30);
                      String[] itemIds = items.split("@");
                      
                      StringBuilder csv = new StringBuilder();
                      csv.append(id.trim());
                      csv.append(",").append(date.trim());
                      csv.append(",").append(customerId.trim());
                      for(String item : itemIds) {
                        csv.append(",").append(item.trim());
                      }
                      exchange.getIn().setBody(csv.toString());
                  }
                }
                ```
              * using *<transform>*
                  * Camel is known for *Builder Pattern*
                    ``` 
                    from("direct:start")
                        .transform(body().regexReplaceAll("\n", "<br/>"))
                        .to("mock:result");
                    ```
                  * *Direct* Component
                      * http://camel.apache.org/direct
                      * used to do things such as link routes together or for testing
                        ```
                        from("direct:start")
                            .transform(new Expression() {
                                public <T> T evaluate(Exchange exchange, Class<T> type) {
                                    String body = exchange.getIn().getBody(String.class);
                                    body = body.replaceAll("\n", "<br/>");
                                    body = "<body>" + body + "</body>";
                                    return (T) body;
                                }
                            })
                            .to("mock:result");
                        ```
          * Content Enricher EIP
              * *pollEnrich*
                  * merge data retrieved from another source using a consumer
              * *enrich*
                  * merge data retrieved from another source using a producer
              * *file component* can be used in both
                  * In enrich, will write the message content as file
                  * In pollEnrich, read the file as source
              * *HTTP component* only works as enrich
              * Camel uses *org.apache.camel.processor.AggregationStrategy* to merge the result
                ```
                Exchange aggregate(Exchange oldExchange, Exchange newExchange);
                
                ```
                
                ```
                from("quartz://report?cron=0+0+6+*+*+?")
                    .to("http://riders.com/orders/cmd=received&date=yesterday")
                    .bean(OrderToCsvBean())
                    .pollEnrich("ftp://riders.com/orders?username=rider&password=secret",
                          new AggregationStrategy() {
                              public Exchange aggregate(Exchange oldExchange, Exchange new Exchange) {
                                  if(newExchange == null) {
                                      return oldExchange;
                                  }
                                  String http = oldExchange.getIn().getBody(String.class);
                                  String ftp = newExchange.getIn().getBody(String.class);
                                  String body = http + "\n" + ftp;
                                  oldExchange.getIn().setBody(body);
                                  return oldExchange;
                              }
                          }
                    )
                    .to("file://riders/orders");
                ```
              * pollEnrich timeout modes
                  * pollEnrich(timeout = -1)
                      * waits until message arrives
                  * pollEnrich(timeout = 0)
                      * immediately polls if message exists
                      * otherwise null is returned
                      * never wait for the message
                  * pollEnrich(timeout = >0)
              * Neither *enrich* nor *pollEnrich* can't access any information in the current exchange
                  * for instance, you can't store a filename header on the exchange for *pollEnrich* to use to select a particular file
      * Data Transformation using components
          * Transforming XML
              * XSLT component
                  * XSLT
                      * declarative XML-based language used to transform XML documents to other documents
                      * for instance, XSLT can be used to transform XML to HTML / to another XML with different structure.
                  * this component available in *camel-spring.jar*
                  ```
                  from("file://rider/inbox")
                      .to("xslt://folder/transform.xsl")
                      .to("activemq:queue:transformed")
                  ```
                  * **xslt://folder/transform.xsl** - *none prefix* - loads from the classpath
                  * **xslt://classpath:com/camel/transform.xsl** - *classpath prefix- loads from the classpath
                  * **xslt://file:/folder/transform.xsl** - *file prefix- loads from the filesystem
                  * **xslt://http://url/transform.xsl** - *http prefix- loads from the URL   
              * XML Marshaling
                  * XStream
                      * library for serializing objects to XML and back again
                      * available in *camle-xstream.jar*
                      ```
                      from("direct:foo").marshal().xstream().to("uri:activemq:queue:foo");
                      from("uri:activemq:queue:foo").unmarshal().sxstream().to("direct:handleFoo");
                      ```
                  * JAXB
                      * Java Architecutre for XML Bindings
                      * serializing objects to XML and back again
                      * No special JAR required as it's distributed in Java
                      * *@XmlRootElement*
                      * *@XmlAccessorType*
                      * *@XmlAttribute*
      * Data Transformation using data formats
          * *org.apache.camel.spi.DataFormat*
              * *marshal*
              * *unmarshal*
          * *Data formats* 
              * *camel-bindy* - *CSV/FIX, fixed length* 
                  * *@CsvRecord*
                  * *@DataField* - *pos, precision, pattern, length, trim*
                  * *Data Types*
                      * *marshal - List<Map<String, Object>> - OutputStream*
                      * *unmarshal - InputStream - List<Map<String, Object>>
                  ```
                  from("direct:toCsv").marshal().bindy(BindyType.Csv, "<package>").to("mock:result")
                  ```
              * *camel-castor* - *XML*
              * *camel-crypto* - *Any*
              * *camel-csv* - *CSV*
                  ```
                  from("file://rider/csvfiles").unmarshal().csv().split(body()).to(activemq:queue.csv.record")
                  ```
                  * *Splitter EIP*  
                      * break the java.util.List<List<String>> in to rows(List<String>)
                  * *Data Types*
                      * *marshal - Map<String, Object> - OutputStream*
                      * *marshal - List<Map<String, Object>> - OutputStream*
                      * *unmarshal - InputStream - List<List<String>>*
                  * problem is it uses generic data types such as Maps/ Lists to represent CSV records
                  * old library which will not take advantage of annotations and generics in java
              * *camel-flatpack* - *CSV*
                  * old library which will not take advantage of annotations and generics in java
              * *camel-gzip* - *Any*
              * *camel-hl7* - *HL7* - well-known format in health care industry
              * *camel-jaxb* - *XML*
              * *camel-jackson* - *JSON*
              * *camel-protobuf* - *XML*
              * *camel-soap* - *XML*
              * *camel-core* - *Object* - java object serialization
              * *camel-tagsoup* - *HTML*
              * *camel-xmlbeans* - *XML*
              * *camel-xmlsecurity*  - *XML*
              * *camel-xstream* - *XML/JSON*
          * http://camel.apache.org/data-format.html 
      * Data Transformation using templates
          * Apache Velocity
              * http://camel.apache.org/velocity.html
          * FreeMarker
              * http://camel.apache.org/freemarker.html)
      * Data Type Transformation using Camel's type converter mechanism
          * *TypeConverterRegistry* 0..n *TypeConverter
            ```
            <T> T convertTo(Class<T> type, Object value)
            ```
          * Loading Type Convertes into Registry
              * *org.apache.camel.impl.converter.AnnotationTypeConverterLoader*
                  * to avoid scanning zillions of classes
                      * reads a service discovery file in the META-INF folder: META-INF/services/org/apache/camel/TypeConverter
                          * this is plain text file that has all list of packages that contain Camel type converters
                  * also searches for classes and public methods that are annotated with @Converter
          ```
          from("file://riders/inbox").convertBodyTo(String.class).to("activemq:queue:inbox");
          ```
      * Message Transformation in component adapters
   
   
  




https://github.com/camelinaction/camelinaction

https://github.com/apache/camel

