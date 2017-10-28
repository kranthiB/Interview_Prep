import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Created by prokarma on 28/10/17.
 */
public class Multicast {

  public static void main(String[] args) throws Exception {
    CamelContext camelContext = new DefaultCamelContext();

    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
    camelContext.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
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
            .multicast().stopOnException()
            .parallelProcessing().executorService(executorService)
            .to("jms:accounting", "jms:production");

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
      }
    });
  }

}
