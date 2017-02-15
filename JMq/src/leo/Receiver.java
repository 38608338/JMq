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
	// ConnectionFactory ：连接工厂，JMS 用它创建连接
    ConnectionFactory connectionFactory;
    // Connection ：JMS 客户端到JMS Provider 的连接
    Connection connection = null;
    // Session： 一个发送或接收消息的线程
    Session session;
    // Destination ：消息的目的地;消息发送给谁.
    Destination destination;
    // 消费者，消息接收者
    MessageConsumer consumer;
    Processor processor;
    
	public void start(){
	    processor =new BasicProcessor();
	    
		connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                "tcp://localhost:61616");
        try {
            // 构造从工厂得到连接对象
            connection = connectionFactory.createConnection();
            // 启动
            connection.start();
            // 获取操作连接
            session = connection.createSession(Boolean.FALSE,
                    Session.CLIENT_ACKNOWLEDGE);
            // 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
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
            // 获取操作连接
            session = connection.createSession(Boolean.TRUE,
                    Session.AUTO_ACKNOWLEDGE);
            // 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
            destination = session.createQueue("QUEUE_LOCAL2");
            // 得到消息生成者【发送者】
            producer = session.createProducer(destination);
            // 设置不持久化，此处学习，实际根据项目决定
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.setPriority(4);
            TextMessage message = session
                    .createTextMessage(xmlData);
            // 发送消息到目的地方
            System.out.println("内网发送消息：" +xmlData);
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
            //获取到接收的数据
            String text = ((TextMessage)message).getText();
            System.out.println("内网发送消息：" +text);
            Send(processor.Operate(text), message.getJMSCorrelationID());
            //确认接收，并成功处理了消息
            message.acknowledge();
        } catch (JMSException e) {
            e.printStackTrace();
        }
		
	}
}
