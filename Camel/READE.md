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
      * DSL is unique as it offers multiple programming languages such as Java / Scala / Groovy.
      * Also offers to be specified in XML
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
  ** File Copy Sample  - https://github.com/kranthiB/Interview_Prep/tree/master/Camel/filecopy **
  
  CamelContext camelContext = new DefaultCamelContext();
      
  camelContext.addRoutes(new RouteBuilder() {
    @Override
    public void configure() throws Exception {
      from("file:data/inbox?noop=true).to("file:data/outbox");
    }
  });
```
      




https://github.com/camelinaction/camelinaction

https://github.com/apache/camel

