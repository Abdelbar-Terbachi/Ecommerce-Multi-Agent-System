import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.domain.DFService;
import java.sql.*;

public class ProductAgent extends Agent {
    Connection connection;

    protected void setup() {
        // Connect to the database
        String url = "jdbc:mysql://localhost:3306/Product";
        String user = "root";
        String password = "anbar2000";
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Register the Product agent with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Product");
        sd.setName(getLocalName() + "-product-agent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the Product agent's behavior
        addBehaviour(new RequestHandler());
    }

    private class RequestHandler extends CyclicBehaviour {
        public void action() {
            // Wait for a message requesting product information
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println("Received product information request: " + msg.getContent());

                // Generate the requested product information
                String productInfo = "The leather shoes are made from high-quality Italian leather and come in a range of sizes and colors.";

                // Send the product information back to the User agent
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(productInfo);
                send(reply);
            }
            else {
                block();
            }
        }
    }

    private class RequestProductInformation extends CyclicBehaviour {
        public void action() {
            // Retrieve product information from the database
            String query = "SELECT * FROM products WHERE name = 'leather shoes'";
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while (resultSet.next()) {
                    String productName = resultSet.getString("name");
                    double price = resultSet.getDouble("price");
                    int quantity = resultSet.getInt("quantity");
                    String description = resultSet.getString("description");

                    // Send product information to the user agent
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setContent("Product name: " + productName + ", price: " + price + ", quantity: " + quantity + ", description: " + description);
                    msg.addReceiver(new AID("UserAgent", AID.ISLOCALNAME));
                    send(msg);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Agent clean-up code here
    protected void takeDown() {
        // Deregister the Product agent from the DF
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Disconnect from the database
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Product agent " + getAID().getName() + " terminating.");
    }
}
