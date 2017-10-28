import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.language.XPath;

/**
 * Created by prokarma on 28/10/17.
 */
public class RecipientList {

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

        // from("jms:xmlOrders").bean(RecipientListBean.class);

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


  private class RecipientListBean {

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
}
