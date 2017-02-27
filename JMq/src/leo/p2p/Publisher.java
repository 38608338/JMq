package leo.p2p;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class Publisher {
	private ActiveMQConnectionFactory factory;
	private Connection connection;
	private Session session;
	private MessageProducer producer;
	private String[] jobs;

	public Publisher() throws JMSException {
		jobs=new String[]{"jim","tom","cat"};
		factory = new ActiveMQConnectionFactory("failover:(tcp://localhost:61616?wireFormat.maxInactivityDuration=0,tcp://localhost:61617?wireFormat.maxInactivityDuration=0)");
		connection = factory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(null);
	}

	public void sendMessage() throws JMSException {
		for(int i = 0; i < jobs.length; i++)  
	    {  
	        String job = jobs[i];  
	        Destination destination = session.createQueue("JOBS." + job);  
	        Message message = session.createObjectMessage(i);  
	        System.out.println("Sending: id: " + ((ObjectMessage)message).getObject() + " on queue: " + destination);  
	        producer.send(destination, message);  
	    }
	}

	public static void main(String[] args) throws JMSException {
		Publisher publisher = new Publisher();
		for (int i = 0; i < 10; i++) {
			publisher.sendMessage();
			System.out.println("Published " + i + " job messages");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		publisher.close();
	}

	private void close() {
		try {
			if (null != connection)
				connection.close();
		} catch (Throwable ignore) {
		}
	}
}
