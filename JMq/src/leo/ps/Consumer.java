package leo.ps;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class Consumer {
	private ActiveMQConnectionFactory factory;
	private Connection connection;
	private Session session;

	public Consumer() throws JMSException {
		factory = new ActiveMQConnectionFactory("failover:(tcp://localhost:61616?wireFormat.maxInactivityDuration=0,tcp://localhost:61617?wireFormat.maxInactivityDuration=0)");
		connection = factory.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	public static void main(String[] args) throws JMSException {
		Consumer consumer = new Consumer();
		String[] stocks =new String[]{"tom","jim","lily"};
		for (String stock : stocks) {
			Destination destination = consumer.getSession().createTopic(
					"STOCKS." + stock);
			MessageConsumer messageConsumer = consumer.getSession()
					.createConsumer(destination);
			messageConsumer.setMessageListener(new Listener());
		}
	}

	public Session getSession() {
		return session;
	}
}
