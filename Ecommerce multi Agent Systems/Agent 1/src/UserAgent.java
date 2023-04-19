import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.domain.DFService;

public class UserAgent extends Agent {

    protected void setup() {
        // Register the User agent with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("User");
        sd.setName(getLocalName() + "-user-agent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the User agent's behavior
        addBehaviour(new RequestProductInformation());
    }

    private class RequestProductInformation extends CyclicBehaviour {
        public void action() {
            // Create a message to request product information
            ACLMessage productInfoRequest = new ACLMessage(ACLMessage.REQUEST);
            productInfoRequest.setContent("Please provide information about the leather shoes.");
            productInfoRequest.addReceiver(new AID("ProductAgent", AID.ISLOCALNAME));
            send(productInfoRequest);

            // Wait for a response with the requested product information
            MessageTemplate productInfoMT = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage productInfoResponse = myAgent.receive(productInfoMT);
            if (productInfoResponse != null) {
                System.out.println("Received product information: " + productInfoResponse.getContent());

                // Create a message to request payment
                ACLMessage paymentRequest = new ACLMessage(ACLMessage.REQUEST);
                paymentRequest.setContent("Please process payment for the leather shoes.");
                paymentRequest.addReceiver(new AID("PaymentAgent", AID.ISLOCALNAME));
                send(paymentRequest);

                // Wait for a response to the payment request
                MessageTemplate paymentMT = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage paymentResponse = myAgent.receive(paymentMT);
                if (paymentResponse != null) {
                    System.out.println("Received payment confirmation: " + paymentResponse.getContent());
                }
                else {
                    System.out.println("No payment confirmation received.");
                }
            }
            else {
                block();
            }
        }
    }


    // Agent clean-up code here
    protected void takeDown() {
        // Deregister the User agent from the DF
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("User agent " + getAID().getName() + " terminating.");
    }
}
