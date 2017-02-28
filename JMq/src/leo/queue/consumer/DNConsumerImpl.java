package leo.queue.consumer;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class DNConsumerImpl implements DNConsumer {
	private static final String USERNAME = ActiveMQConnection.DEFAULT_USER;
	private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
	private static final String BROKERURL = ActiveMQConnection.DEFAULT_BROKER_URL;

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private ThreadLocal<MessageConsumer> tl=new ThreadLocal<MessageConsumer>();
	
	@Override
	public void init() {
		try {
			connectionFactory = new ActiveMQConnectionFactory(USERNAME,
					PASSWORD, BROKERURL);
			connection = connectionFactory.createConnection();
			connection.start();

			session = connection
					.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void getMessage(String disname) {
		try {
			Queue queue = session.createQueue(disname);
			//MessageConsumer consumer = session.createConsumer(queue);
			MessageConsumer consumer;
			if (tl.get() !=null) {
				consumer=tl.get();
			} else {
				consumer = session.createConsumer(queue);
				tl.set(consumer);
			}

			while (true) {
				TextMessage message = (TextMessage) consumer.receive();
				Thread.sleep(100);
				
				if (message != null) {
					message.acknowledge();
					System.out.println(Thread.currentThread().getName()+ " Consumer:我是消费者：我接收的消息内容是：" + message.getText());
				} else {
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
