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
public class FtpToJMs {

  public static void main(String[] args) throws Exception {
    CamelContext camelContext = new DefaultCamelContext();

    ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");

    camelContext.addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("ftp://rider.com/orders?username=rider&password=secret")
            .process(new Processor() {
              public void process(Exchange exchange) throws Exception {
                System.out.println("we just downloaded: " + exchange.getIn().getHeader("CamelFileName"));
              }
            })
            .to("jms:incomingOrders");
      }
    });

    camelContext.start();
    Thread.sleep(10000L);
    camelContext.stop();
  }
}
