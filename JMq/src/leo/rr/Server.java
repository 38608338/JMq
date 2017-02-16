package leo.rr;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

public class Server implements MessageListener {
	private MessageProtocol messageProtocol;
	private String messageBrokerUrl="tcp://localhost:61616";
	private Session session;
	private MessageProducer replyProducer;

	public Server() {  
          
    }  
	
	public void start(){
		//Æô¶¯MQ·þÎñ
		/*try {  
            //This message broker is embedded  
            BrokerService broker = new BrokerService();  
            broker.setPersistent(false);  
            broker.setUseJmx(false);  
            broker.addConnector(messageBrokerUrl);  
            broker.start();  
        } catch (Exception e) {  
            //Handle the exception appropriately 
        	e.printStackTrace();
        } */ 
  
        //Delegating the handling of messages to another class, instantiate it before setting up JMS so it  
        //is ready to handle messages  
        this.messageProtocol = new MessageProtocol();  
        this.setupMessageQueueConsumer();
	}
  
    private void setupMessageQueueConsumer() {  
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(messageBrokerUrl);  
        Connection connection;  
        try {  
            connection = connectionFactory.createConnection();  
            connection.start();  
            this.session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);  
            Destination adminQueue = this.session.createQueue("Request_Response_Queue");  
  
            //Setup a message producer to respond to messages from clients, we will get the destination  
            //to send to from the JMSReplyTo header field from a Message  
            this.replyProducer = this.session.createProducer(null);  
            this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);  
  
            //Set up a consumer to consume messages off of the admin queue  
            MessageConsumer consumer = this.session.createConsumer(adminQueue);  
            consumer.setMessageListener(this);  
        } catch (JMSException e) {  
            //Handle the exception appropriately 
        	e.printStackTrace();
        }  
    }

	@Override
	public void onMessage(Message message) {
		 try {  
	            TextMessage response = this.session.createTextMessage();  
	            if (message instanceof TextMessage) {  
	                TextMessage txtMsg = (TextMessage) message;  
	                String messageText = txtMsg.getText();  
	                response.setText(this.messageProtocol.handleProtocolMessage(messageText));  
	            }
	  
	            //Set the correlation ID from the received message to be the correlation id of the response message  
	            //this lets the client identify which message this is a response to if it has more than  
	            //one outstanding message to the server  
	            response.setJMSCorrelationID(message.getJMSCorrelationID());  
	  
	            //Send the response to the Destination specified by the JMSReplyTo field of the received message,  
	            //this is presumably a temporary queue created by the client  
	            this.replyProducer.send(message.getJMSReplyTo(), response);  
	        } catch (JMSException e) {  
	            //Handle the exception appropriately  
	        	e.printStackTrace();
	        }
	}  
	
	public static void main(String[] args) {
		Server server=new Server();
		server.start();
		System.out.println("Server is ready!");
	}
}
