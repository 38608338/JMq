package leo;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import leo.impl.BasicProcessor;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Receiver implements MessageListener {
	// ConnectionFactory �����ӹ�����JMS ������������
    ConnectionFactory connectionFactory;
    // Connection ��JMS �ͻ��˵�JMS Provider ������
    Connection connection = null;
    // Session�� һ�����ͻ������Ϣ���߳�
    Session session;
    // Destination ����Ϣ��Ŀ�ĵ�;��Ϣ���͸�˭.
    Destination destination;
    // �����ߣ���Ϣ������
    MessageConsumer consumer;
    Processor processor;
    
	public void start(){
	    processor =new BasicProcessor();
	    
		connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                "tcp://localhost:61616");
        try {
            // ����ӹ����õ����Ӷ���
            connection = connectionFactory.createConnection();
            // ����
            connection.start();
            // ��ȡ��������
            session = connection.createSession(Boolean.FALSE,
                    Session.CLIENT_ACKNOWLEDGE);
            // ��ȡsessionע�����ֵxingbo.xu-queue��һ����������queue��������ActiveMq��console����
            destination = session.createQueue("QUEUE_LOCAL1");
            consumer = session.createConsumer(destination);
            consumer.setMessageListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
        }
	}
	
	public void stop(){
		
	}
	
	public static void main(String[] args) {
    	Receiver rr=new Receiver();
    	rr.start();
    }
	
	private void Send(String xmlData, String msgid){
        MessageProducer producer;
        try {
            // ��ȡ��������
            session = connection.createSession(Boolean.TRUE,
                    Session.AUTO_ACKNOWLEDGE);
            // ��ȡsessionע�����ֵxingbo.xu-queue��һ����������queue��������ActiveMq��console����
            destination = session.createQueue("QUEUE_LOCAL2");
            // �õ���Ϣ�����ߡ������ߡ�
            producer = session.createProducer(destination);
            // ���ò��־û����˴�ѧϰ��ʵ�ʸ�����Ŀ����
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.setPriority(4);
            TextMessage message = session
                    .createTextMessage(xmlData);
            // ������Ϣ��Ŀ�ĵط�
            System.out.println("����������Ϣ��" +xmlData);
            message.setJMSCorrelationID(msgid);
            producer.send(message);
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            
        }
	}

	@Override
	public void onMessage(Message message) {
		try {
            //��ȡ�����յ�����
            String text = ((TextMessage)message).getText();
            System.out.println("����������Ϣ��" +text);
            Send(processor.Operate(text), message.getJMSCorrelationID());
            //ȷ�Ͻ��գ����ɹ���������Ϣ
            message.acknowledge();
        } catch (JMSException e) {
            e.printStackTrace();
        }
		
	}
}
