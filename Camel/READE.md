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
              * using *transform*
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

### Using Beans
  * in easy way
      ```
      from("direct:hello").beanRef("helloBean","hello");
                             (OR)
      from("direct:hello").beanRef("helloBean");  // if only one method then can remove second parameter
                             (OR)
      from("direct:hello").bean(HelloBean.class); //don't have to preregister the bean in the registry
      
      class HelloBean { 
        public String hello(String name) {
           return "Hello " + name;
        }
      }
      ```
  * **Service Activator Pattern**
      * It describes a service that can be invoked easily from both messaging and non-messaging services
      * *Camel Bean Component*, which eventually uses *org.apache.camel.component.bean.BeanProcessor*
  * **Camel's Bean Registry**
      * Serivce Provider Interface (**SPI**) defined in *org.apache.camel.spi.Registry*
          ```
          Object lookup(String name);
          <T> T lookup(String name, Class<T> type);
          <T> Map<String, T> lookupByType(Class<T> type)
          ```
          
          ```
          HelloBean hello = (HelloBean) context.getRegistry().lookup("helloBean");
                                    (OR)
          HelloBean hello = context.getRegistry().lookup("helloBean", HelloBean.class);
          ```
          * Last method is mostly used internally by camel to support convention over configuration
      * Registry Implementations shipped with Camel
          * *SimpleRegistry* - is a map based registry, used when unit testing or in the Google App engine; where only a limited number of JDK classes are available
           
            ```
            class SimpleRegistryTest extends TestCase {
              private CamelContext context;
              private ProducerTemplate template;
              
              protected void setUp() throws Exception {
                  SimpleRegistry registry = new SimpleRegistry();
                  registry.put("helloBean", new HelloBean());
                  
                  context = new DefaultCamelContext(registry);
                  template = context.createProducerTemplate();
                  
                  context.addRoutes(new RouteBuilder() {
                      public void configure() throws Exception {
                          from("direct:hello").beanRef("helloBean");
                      }
                  });
                  
                  context.start();
              }
              
              protected void tearDown() throws Exception {
                template.stop();
                context.stop();
              }
              
              public void testHello() throws Exception {
                Object reply = template.requestBody("direct:hello", "World");
                assertEquals("Hello World", reply);
              }
            }
            ```
                
          * *JndiRegistry* - uses an existing Java Naming and Directory Interface registry to look up beans
              * is the default registry
              ```
              protected CamelContext createCamelContext() throws Exception {
                Hashtable env = new Hashtable();
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.ibm.websphere.naming.WsnInitialContextFactory");
                env.put(Context.PROVIDER_URL, "corbaloc:iiop:myhost.mycompany.com:2809");
                env.put(Context.SECURITY_PRINCIPAL, "username");
                env.put(Context.SECURITY_CREDENTIALS, "password");
                
                Context context = new InitialContext(env);
                JndiRegistry jndi = new JndiRegistry(context);
                
                return new DefaultCamelContext(jndi);
              }
              ```
          * *ApplicationContextRegistry* - works with Spring to look up beans in the Spring ApplicationContext
          * *OsgiServiceRegistry* - looking up beans in the OSGi service interface registry
  * **Selecting Bean Methods**
      * Look up the bean in the registry
      * Selects the method to invoke on the bean
      * Binds to the parameters of the selected method
      * Invokes the method
      * Handles any invocation erros that occur
      * Sets the method's reply as the body on the output message on the camel exchange
      * **Camel's method-selection algorithm**
          1. Camel message contains a header with key *CamelBeanMethodName* , go to step-e
          2. If method is explicitly defined, go to step-e
          3. Can message body be converted to a processor? If so, invoke the processor and stop, else, go to step-d
          4. Is message body a *BeanInvocation* instance? yes : invoke bean and stop, no : go to step-e
          5. Does at least one method exist with that name? yes : go to step-f, no: throw **MethodNotFoundException**
          6. Is there only one method marked with @Handler annotation? yes : go to step-j, no : go to step-g
          7. Is there only one method marked with other kinds of camel annotations? yes go to step-j: , no : go to step-h
          8. Is there only one method with a single parameter? yes : go to step-j, no : go to step-i
          9. Find best matching method and is there a single best matching method? yes : go to step-j, no : throw **AmbigiousMethodCallException**
          10. Return selected method and stop 
          * Other possible exception is **NoTypeConversionAvailableException**
      * **Bean Parameter Binding**
          * Binding with multiple parameters
              * Binding using built-in types
                  * Exchange
                  * Message
                  * CamelContext
                  * TypeConverter
                  * Registry
                  * Exception
              ```
                public String echo(String echo, CamelContext context)
                                    (OR)
                public String echo(String echo, Registry registry) 
                                    (OR)
                public String echo(String echo, CamelContext context, Registry registry)                     
              ```
              * Binding using Camel annotations
                  * @Attachments
                  * @Body
                  * @Header(name)
                  * @Headers - java.util.Map
                  * @OutHeaders - will be sed in **InOut** MEP style
                  * @Property(name)
                  * @Properties - java.util.Map
                  ```
                    public String orderStatus(@Body Integer orderId, @Headers Map headers)
                  ```
              * Binding using Camel language annotations
                  * @Bean - *camel-core* - Invokes method on a bean
                  * @BeanShell - *camel-script* - Evaluates a bean shell script
                  * @EL - *camel-juel* - Evaluates an EL script(unifie JSP and JSF scripts)
                  * @Groovy - *camel-script* - Evaluates a groovy script
                  * @JavaScript - *camel-script* - Evaluates a JavaScript script
                  * @MVEL - *camel-mvel* - Evaluates a MVEL script
                  * @OGNL - *camel-ognl* - Evaluates a OGNL script
                  * @PHP - *camel-script* - Evaluates a PHP script
                  * @Python - *camel-script* - Evaluates a Python script
                  * @Ruby - *camel-script* - Evaluates a Ruby script
                  * @Simple - *camel-core* - Evaluates a simple expression
                  * @XPath - *camel-core*- Evaluates an XPath expression
                  * @XQuery - *camel-saxon* - Evaluates and XQuery expression
              * **Rules**
                  * Camel annotations
                  * Camel built-in types
                  * first parameter assumed as message IN body
                  * all remaining parameters will be unbounded and will pass in empty values
                  
### Understanding Error Handling
  * Recoverable and irrecoverable errors
      * recoverable error is represented as plain *Throwable* or *Exception*
          * *org.apache.camel.Exchange*
              ```
               void setException(Throwable cause);
               Exception getException();
              ```
      * irrecoverable error is represented as a message with fault flag
          ```
          Message out = exchange.getOut();
          out.setFault(true);
          out.setBody("Unknown customer");
          ```
  * Where Camel's error handling applies
      * Camel's error handling does not apply everywhere.
      * It's handlied during the lifecycle of the exchange.
      * Before the exchange instantiation, its the component-specific on how to deal with errors
          * Components like *File, FTP, Mail, iBATIS, RSS, Atom, JPA and SNMP* offers minor error-handling features
              * these are based on *ScheduledPollConsumer* class, which offers a pluggable *PollingConsulerPollStrategy* that we can use to create our own error-handling strategy.
                  * http://camel.apache.org/polling-consumer.html
  * Error handling in Camel
      * Camel only trigger error handlers when *exchange.getException() != null* 
      * this won't react to irrecoverable errors
      * *Error Handlers*
          * DefaultErrorHandler - automatically enabled if not specifed
              * Configuration Settings
                  * No redelivery
                  * Exceptions are propagated back to the caller
          * DeadLetterChannel - implements *Dead Letter Channel* EIP
              * move the failed messages to the dead letter queue
              * Handling exception by default
                  * Camel handles exceptions by supression them, it removes the exceptions from the exchange and stores them as properties on the exchange
                  ```
                    errorHandler(deadLetterChannel("log:dead?level=ERROR"));
                    Exception e = exchange.getProperty(Exchange.CAUSED_EXCEPTION,
                                                       Exception.class);
                  ```
              * Using original message with Dead Letter Channel
                  ```
                    errorHandler(deadLetterChannel("jms:queue:dead").useOriginalMessage());
                  ```
          * TransactionErrorHandler - transaction aware handler
          * NoErrorHandler - disable error handling
          * LoggingErrorHandler - logs the exception
              * defaultly, log the failed message and exception using *org.apache.camel.processor.LoggingErrorHandler* at **ERROR** level
          * First three error handlers extend the *RedeliveryErrorHandler* class
      * Features of error handlers
          * Redelivery policies
              * Options
                  * MaximumRedeliveries - int - 0
                  * RedeliveryDelay - long - 1000
                  * MaximumRedeliveryDelay - long - 60000
                  * AsyncDelayedRedelivery - boolean -false
                  * BackOffMultiplier - double - 2.0 - Exponential backoff multiplier used to multiply each consequent delay. Disabled by default.
                  * CollisionAvoidanceFactor - double - 0.15 - A percentage to use when calculating a random delay offset. Disabled by default.
                  * DelayPattern - String - - For instance "0:1000, 5:5000, 10:30000"
                  * RetryAttemptedLogLevel - LoggingLevel - DEBUG
                  * RetryExhaustedLogLevel - LoggingLevel - ERROR - used when all redelivery attempts have failed
                  * LogStackTrace - boolean - true - should be logged when all delivery attempts have failed
                  * LogRetryStackTrance - boolean - false  - should be logged when a delivery attempt failed
                  * LogExhausted - boolean - true - specifies exhaustion of redelivery attempts (when all have failed)
                  * LogHandled - boolean - false - handled exceptions should be logged
                  ```
                  errorHandler(defaultErrorHandler()
                     .maximumRedeliveries(5)
                     .backOffMultiplier(2)
                     .retryAttemptedLogLevel(LoggingLevel.WARN)
                  ```
              * How does camel know this?
                  * Stores information in Exchange
                      * Exchange.REDELIVERY_COUNTER - int
                      * Exchange.REDELIVERED - boolean
                      * Exchange.REDELIVERY_EXHAUSTED - boolean
          * Scope - two possible scopes
              * context(high level) - this is default
              ```
              errorHandler(defaultErrorHandler()
                  .maximumRedeliveries(5)
                  .backOffMultiplier(2)
                  .retryAttemptedLogLevel(LoggingLevel.WARN)
                  
              from("direct:hello").beanRef("helloBean","hello");
              ```
              * route(low level)
              ```
              errorHandler(defaultErrorHandler()
                  .maximumRedeliveries(5)
                  .backOffMultiplier(2)
                  .retryAttemptedLogLevel(LoggingLevel.WARN)
                  
              from("direct:hello").beanRef("helloBean","hello");
              
              from("file:data/inbox?noop=true)
                  .errorHandler(deadLetterChannel("log:DLC")
                      .maximumRedeliveries(5)
                      .backOffMultiplier(2)
                      .redeliveryDelay(250)
                      .retryAttemptedLogLevel(LoggingLevel.WARN)
                  .to("file:data/outbox");
              ```
          * Handling faults
              * Normal situations, Camel error handler won't react when the fault occurs
                  * Possible components are *CXF, SOAP, JBI, NMR*
              * To enable fault handling,
                  ```
                    getContext().setHandleFault(true);
                                  (OR)
                    from("seda:queue.inbox").handleFault()
                        .beanRef("orderService", "toSoap")
                       .to("mock:queue.order");              
                  ```
          * Exception policies
              * policies are specified with the *onException* method in the route
              * how Camel understands hierarchy
                  ```
                  org.apache.camel.RuntimeCamelException (wrapper by Camel)
                  + com.mycompany.OrderFailedException
                    + java.net.ConnectException
                  ```
                  * it follows bottom-up approach
                  ```
                    onException(OrderFailedException.class).maximumRedeliveries(3);
                  ```
                  * *onException and Gap Detection*
                      * with lowest gap as the winner
                      ```
                      onException(ConnectException.class)
                          .maximumRedeliveries(5);
                      onException(IOException.class)
                          .maximumRedeliveries(3).redeliveryDelay(1000);
                      onException(Exception.class)
                          .maximumRedeliveries(1).redeliveryDelay(5000);
                      ```
                      * imagine exception is thrown
                          ```
                          org.apache.camel.OrderFailedException
                          + java.io.FileNotFoundException
                          ```
                      * *FileNotFoundException* hierarchy
                          ```
                          java.lang.Exception
                          + java.io.IOException
                            + java.io.FileNotFoundException
                          ```
                          * Gap with IOException is 1 and with Exception is 2 . So *IOException* wins
                      * *OrderNotFoundException* hierarchy
                          ```
                          java.lang.Exception
                          + OrderNotFoundException
                          ```
                          * Gap with Exception is 1. So it wins
                      * Incase of tie, Camel always pick the first match
                  * Multiple Exceptions per onException
                      ```
                      onException(IOException.class, SQLException.class, JMSException.class)
                          .maximumRedeliveries(5).redeliveryDelay(3000);
                      ```
              * peculiar cases
                  * default redelivery delay is 1 second
                  ```
                  errorHandler(defaultErrorHandler().maximumRedeliveries(3).delay(3000));
                  
                  onException(IOException.class).maximumRedeliveries(5);
                  
                  from("jetty:http://0.0.0.0/orderservice")
                      .to("mina:tcp://erp.rider.com:4444?textline=true")
                      .beanRef("orderBean", "prepareReply");
                  ```
                      * In above, delay will be set to 3 seconds as onException we set maximumRedeliveries
                  ```
                  errorHandler(defaultErrorHandler().maximumRedeliveries(3).delay(3000));
                  
                  onException(IOException.class);
                  
                  from("jetty:http://0.0.0.0/orderservice")
                      .to("mina:tcp://erp.rider.com:4444?textline=true")
                      .beanRef("orderBean", "prepareReply");
                  ```
                      * In above, delay is 0. Camel won't attempt to redelivery
              * Handling an exception with onException is similar to exception handling in Java(try...catch block)
                  * regular try..catch block , code won't compile
                  * doTry...doCatch...doFinally
                  ```
                  from("mina:tcp://0.0.0.0:4444?textline=true")
                      .doTry()
                          .process(new ValidateOrderId())
                          .to("jms:queue:order.status")
                          .process(new GenerateResponse());
                      .doCatch(JmsException.class)
                          .process(new GenerateFailureResponse())
                      .end();
                  ```
                  * Using onException to Handle Exceptions
                  ```
                  onException(JMSException.class).handled(true).process(new GenerateFailureResponse())
                  
                  from("mina:tcp://0.0.0.0:4444?textline=true")
                      .process(new ValidateOrderId())
                      .to("jms:queue:order.status")
                      .process(new GenerateResponse());
                  ```
              * Properties on the Exchange related to error handling
                  * Exchange.EXCEPTION_CAUGHT - Exception
                  * Exchange.FAILURE_ENDPOINT - String
                  * Exchange.ERRORHANDLER_HANDLED - boolean
                  * Exchange.FAILURE_HANDLED - boolean
                  ```
                  Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)
                  ```
              * Ignoring exceptions
                  ```
                  onException(ValidationException.class).continued(true)
                  ```
              * even alter the route on Exception
                  ```
                  onException(JMSException.class).handled(true).to("ftp://user:password@ftp.com")
                  ```
          * Error handling
              * *onWhen* - to dictate when an exception policy is in use
                  ```
                  onException(HttpOperationFailedException.class)
                      .onWhen(bean(MyHttpUtil.class, "isIllegalData"))
                      .handled(true)
                      .to("file:/data/illegal")
                      
                  class MyHttpUtil {
                    public static boolean isIllegalDataError(HttpOperationFailedException cause) {
                      int code = cause.getStatusCode();
                      if (code != 500) {
                        return false;
                      }
                      return "ILLEGAL DATA".equals(cause.getResponseBody().toString())
                    }
                  }
                  ```
              * *onRedeliver* - to execute some code before the message is redelivered
                  * can be configured on the error handler or on onException
                  ```
                  errorHandler(defaultErrorHandler()
                      .maximumRedeliveries(3)
                      .onRedeliver(new MyOnRedeliveryProcessor());
                  
                  onException(IOException.class)
                      .maximumRedeliveries(5)
                      .onRedeliver(new MyOtherOnRedeliveryProcessor());
                  ```
              * *retryWhile* - at runtime, determine whether or not to continue redelivery or to give up
                  ```
                   class MyRetryRuleset {
                   
                   public boolean shouldRetry(
                                      @Header(Exchange.REDELIVERY_COUNTER) Integer counter,
                                      Exception causedBy) {
                       ...
                   }
                   
                   onException(IOException.class).retryWhile(bean(MyRetryRuletset.class));
                  ```
      
          


https://github.com/camelinaction/camelinaction

https://github.com/apache/camel

