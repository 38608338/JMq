package leo.queue.provider;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

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
				producer = tl.get();
			} else {
				producer = session.createProducer(queue);
				tl.set(producer);
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
				//String msg = Thread.currentThread().getName()+ " Provider:我是生产者：这是我产生的内容"+count;
				String msg = Thread.currentThread().getName()+ " Provider:我是生产者：这是我产生的内容"+counttl.get();
				Message message = session.createTextMessage(msg);
				System.out.println(msg);

				producer.send(message);
				session.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
