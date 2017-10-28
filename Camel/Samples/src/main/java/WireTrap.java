import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * Created by prokarma on 28/10/17.
 */
public class WireTrap {

  public static void main(String[] args) throws Exception {
    CamelContext context = new DefaultCamelContext();
    ConnectionFactory connectionFactory =
        new ActiveMQConnectionFactory("vm://localhost");
    context.addComponent("jms",
        JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

    context.addRoutes(new RouteBuilder() {
      @Override
      public void configure() {

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
      }
    });

  }

}
