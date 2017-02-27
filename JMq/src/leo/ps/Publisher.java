package leo.ps;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMapMessage;

public class Publisher {
	private ActiveMQConnectionFactory factory;
	private Connection connection;
	private Session session;
	private MessageProducer producer;
	private Destination[] destinations;

	public Publisher() throws JMSException {
		factory = new ActiveMQConnectionFactory("failover:(tcp://localhost:61616?wireFormat.maxInactivityDuration=0,tcp://localhost:61617?wireFormat.maxInactivityDuration=0)");
		connection = factory.createConnection();
		try {
			connection.start();
		} catch (JMSException jmse) {
			connection.close();
			throw jmse;
		}
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(null);
	}

	protected void setTopics(String[] stocks) throws JMSException {
		destinations = new Destination[stocks.length];
		for (int i = 0; i < stocks.length; i++) {
			destinations[i] = session.createTopic("STOCKS." + stocks[i]);
		}
	}

	protected void sendMessage(String[] stocks) throws JMSException {
		for (int i = 0; i < stocks.length; i++) {
			Message message = createStockMessage(stocks[i], session);
			System.out.println("Sending: "
					+ ((ActiveMQMapMessage) message).getContentMap()
					+ " on destination: " + destinations[i]);
			producer.send(destinations[i], message);
		}
	}

	protected Message createStockMessage(String stock, Session session)
			throws JMSException {
		MapMessage message = session.createMapMessage();
		message.setString("stock", stock);
		message.setDouble("price", 1.00);
		message.setDouble("offer", 0.01);
		message.setBoolean("up", true);

		return message;
	}

	public static void main(String[] args) throws JMSException {
		//if (args.length < 1)
		//	throw new IllegalArgumentException();

		String[] stocks =new String[]{"tom","jim","lily"};
		
		// Create publisher
		Publisher publisher = new Publisher();

		// Set topics
		publisher.setTopics(stocks);

		for (int i = 0; i < 10; i++) {
			publisher.sendMessage(stocks);
			System.out.println("Publisher '" + i + " price messages");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Close all resources
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
