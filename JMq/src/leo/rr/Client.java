package leo.rr;

import java.text.SimpleDateFormat;
import java.util.Date;

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

public class Client implements MessageListener {
	private MessageProducer producer;

	public Client() {  
        
    }

	public void send(String data) {
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("failover:(tcp://localhost:61616?wireFormat.maxInactivityDuration=0,tcp://localhost:61617?wireFormat.maxInactivityDuration=0)");  
        Connection connection = null;  
        try {  
            connection = connectionFactory.createConnection();  
            connection.start();  
            Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);  
            Destination adminQueue = session.createQueue("Request_Response_Queue");  
  
            //Setup a message producer to send message to the queue the server is consuming from  
            this.producer = session.createProducer(adminQueue);  
            this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);  
  
            //Create a temporary queue that this client will listen for responses on then create a consumer  
            //that consumes message from this temporary queue...for a real application a client should reuse  
            //the same temp queue for each message to the server...one temp queue per client  
            Destination tempDest = session.createTemporaryQueue();  
            MessageConsumer responseConsumer = session.createConsumer(tempDest);  
  
            //This class will handle the messages to the temp queue as well  
            //responseConsumer.setMessageListener(this);  
  
            //Now create the actual message you want to send  
            TextMessage txtMessage = session.createTextMessage();  
            txtMessage.setText(data);  
  
            //Set the reply to field to the temp queue you created above, this is the queue the server  
            //will respond to  
            txtMessage.setJMSReplyTo(tempDest);  
  
            //Set a correlation ID so when you get a response you know which sent message the response is for  
            //If there is never more than one outstanding message to the server then the  
            //same correlation ID can be used for all the messages...if there is more than one outstanding  
            //message to the server you would presumably want to associate the correlation ID with this  
            //message somehow...a Map works good  
            String correlationId = this.createRandomString();  
            txtMessage.setJMSCorrelationID(correlationId);  
            this.producer.send(txtMessage);  
            
            Message message=responseConsumer.receive(1000);
            String messageText = null;  
            try {  
                if (message instanceof TextMessage) {  
                    TextMessage textMessage = (TextMessage) message;  
                    messageText = textMessage.getText();  
                    System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date())+ " responseText = " + messageText);  
                }  
            } catch (JMSException e) {  
                //Handle the exception appropriately  
            }  
        } catch (JMSException e) {  
            //Handle the exception appropriately  
        } finally {
			try {
				if (null != connection)
					connection.close();
			} catch (Throwable ignore) {
			}
		}
	}

	private String createRandomString() {
		return  java.util.UUID.randomUUID().toString();
	}

	@Override
	public void onMessage(Message message) {
		String messageText = null;  
        try {  
            if (message instanceof TextMessage) {  
                TextMessage textMessage = (TextMessage) message;  
                messageText = textMessage.getText();  
                System.out.println("responseText = " + messageText);  
            }  
        } catch (JMSException e) {  
            //Handle the exception appropriately  
        }  
	}  

	public static void main(String[] args) {
		single();
		final Client client=new Client();
		for (int i = 1; i < 10; i++) {
			Thread tt=new Thread(new Runnable() {
				public void run() {
					for (int j = 0; j < 10; j++) {
						client.send("work" + j);
					}
				}
			});
			tt.start();
			/*try {
				tt.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
	}

	private static void single() {
		Client client=new Client();
		for (int i = 0; i < 10; i++) {
			client.send("request-response mode test "+i);
		}
	}
}
