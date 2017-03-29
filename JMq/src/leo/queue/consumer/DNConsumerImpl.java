package leo.queue.consumer;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
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
	private ThreadLocal<MessageProducer> tl2=new ThreadLocal<MessageProducer>();
	
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
            MessageProducer replyProducer;
			if (tl2.get() !=null) {
				replyProducer=tl2.get();
			} else {
				replyProducer = this.session.createProducer(null);  
	            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);  
				tl2.set(replyProducer);
			}

			while (true) {
				TextMessage message = (TextMessage) consumer.receive();
				Thread.sleep(1500);
				
				if (message != null) {
					message.acknowledge();
					System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date())+" "+queue+" "+ Thread.currentThread().getName()+ " Consumer:我是消费者：我接收的消息内容是：" + message.getText());
					
					try{
			            TextMessage response = this.session.createTextMessage();  
			            response.setJMSCorrelationID(message.getJMSCorrelationID());  
			            String xx = new SimpleDateFormat("HH:mm:ss").format(new Date())+" "+message.getJMSReplyTo()+ "回复了::"+message.getText();
						response.setText(xx);
			            replyProducer.send(message.getJMSReplyTo(), response);  
			            System.out.println(xx);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init(String url) {
		try {
			connectionFactory = new ActiveMQConnectionFactory(USERNAME,
					PASSWORD, url);
			connection = connectionFactory.createConnection();
			connection.start();

			session = connection
					.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
