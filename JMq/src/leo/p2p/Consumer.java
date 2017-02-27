package leo.p2p;

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
	private String[] jobs;
	
	public Consumer() throws JMSException {  
		jobs=new String[]{"jim","tom","cat"};
        factory = new ActiveMQConnectionFactory("failover:(tcp://localhost:61616?wireFormat.maxInactivityDuration=0,tcp://localhost:61617?wireFormat.maxInactivityDuration=0)");  
        connection = factory.createConnection();  
        connection.start();  
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);  
    }  
	public static void main(String[] args) throws JMSException {  
        Consumer consumer = new Consumer();  
        for (String job : consumer.jobs) {  
            Destination destination = consumer.getSession().createQueue("JOBS." + job);  
            MessageConsumer messageConsumer = consumer.getSession().createConsumer(destination);  
            messageConsumer.setMessageListener(new Listener(job));  
        }  
    }  
      
    public Session getSession() {  
        return session;  
    }  
}
