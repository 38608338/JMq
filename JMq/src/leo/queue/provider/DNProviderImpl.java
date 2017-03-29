package leo.queue.provider;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class DNProviderImpl implements DNProvider {
	private static final String USERNAME = ActiveMQConnection.DEFAULT_USER;
	private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;
	private static final String BROKERURL = ActiveMQConnection.DEFAULT_BROKER_URL;

	private ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private ThreadLocal<MessageProducer> tl = new ThreadLocal<MessageProducer>();
	private ThreadLocal<MessageConsumer> tl2 = new ThreadLocal<MessageConsumer>();
	private ThreadLocal<Integer> counttl = new ThreadLocal<Integer>(){  
        public Integer initialValue() {  
            return 0;  
        }
    };

	@Override
	public void init() {
		try {
			connectionFactory = new ActiveMQConnectionFactory(USERNAME,
					PASSWORD, BROKERURL);
			connection = connectionFactory.createConnection();
			connection.start();

			session = connection
					.createSession(true, Session.SESSION_TRANSACTED);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendMessage(String disname) {
		try {
			Queue queue = session.createQueue(disname);
			// MessageProducer producer=session.createProducer(queue);
			MessageProducer producer;
			if (tl.get() != null) {
				System.out.println("got MessageProducer.");
				producer = tl.get();
			} else {
				System.out.println("create MessageProducer.");
				producer = session.createProducer(queue);
				tl.set(producer);
			}
			MessageConsumer responseConsumer;
			Destination tempDest = session.createTemporaryQueue();  
			if (tl2.get() != null) {
				System.out.println("got MessageConsumer.");
				responseConsumer = tl2.get();
			} else {
				System.out.println("create MessageConsumer.");
				responseConsumer = session.createConsumer(tempDest);  
				tl2.set(responseConsumer);
			}
			/*Integer count;
			if (counttl.get() !=null) {
				count=counttl.get();
			} else {
				count=0;
			}*/
			
			while (true) {
				//count++;
				//counttl.set(count);
				counttl.set(counttl.get()+1);
				Thread.sleep(500);
				
				 synchronized(this) {
					String msgid =  java.util.UUID.randomUUID().toString();
					String msg = new SimpleDateFormat("HH:mm:ss").format(new Date())+" "+queue+" "+ Thread.currentThread().getName()+" "+msgid+ " Provider:我是生产者：这是我产生的内容"+counttl.get();
					Message message = session.createTextMessage(msg);
					
					//add reply
					message.setJMSCorrelationID(msgid);
					message.setJMSReplyTo(tempDest);
					//end reply
					
					System.out.println(msg);
	
					producer.send(message);
					session.commit();
				 
					//wait reply message
					Message msg2=responseConsumer.receive(10000);
		            String messageText = null;  
		            try {  
		                if (msg2 instanceof TextMessage) {  
		                    TextMessage textMessage = (TextMessage) msg2;  
		                    messageText = textMessage.getText();  
		                    System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date())+" "+tempDest+" "+ Thread.currentThread().getName()+" "+ textMessage.getJMSCorrelationID()+ " responseText = " + messageText);  
		                }  
		            } catch (JMSException e) {  
		                e.printStackTrace();
		            }  
		            //end wait reply message
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
					.createSession(true, Session.SESSION_TRANSACTED);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
