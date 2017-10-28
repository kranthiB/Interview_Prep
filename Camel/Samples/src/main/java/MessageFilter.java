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
public class MessageFilter {

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
            .when(header("CamelFileName").endsWith("Xml"))
            .to("jms:xmlOrders")
            .when(header("CamelFileName").regex("^.*(csv|csl)$"))
            .to("jms:csvOrders")
            .otherwise()
            .to("jms:badOrders");

        from("jms:xmlOrders").filter(xpath("/order[not(@test)]"))
            .process(new Processor() {
              public void process(Exchange exchange) throws Exception {
                System.out.println("Received XML order: "
                    + exchange.getIn().getHeader("CamelFileName"));
              }
            });
      }
    });
  }

}
