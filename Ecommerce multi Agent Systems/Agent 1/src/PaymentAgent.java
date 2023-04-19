import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
public class PaymentAgent extends Agent {

    protected void setup() {
        // Register the Payment agent with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Payment");
        sd.setName(getLocalName() + "-payment-agent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the Payment agent's behavior
        addBehaviour(new ReceivePaymentRequests());
    }

    private class ReceivePaymentRequests extends CyclicBehaviour {
        public void action() {
            // Wait for a payment request message
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage request = myAgent.receive(mt);
            if (request != null) {
                System.out.println("Received payment request: " + request.getContent());

                // Process the payment request and send a response
                ACLMessage response = request.createReply();
                response.setPerformative(ACLMessage.INFORM);
                response.setContent("Payment successful. Thank you for your purchase.");
                send(response);
            }
            else {
                block();
            }
        }
    }

    // Agent clean-up code here
    protected void takeDown() {
        // Deregister the Payment agent from the DF
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Payment agent " + getAID().getName() + " terminating.");
    }
}
