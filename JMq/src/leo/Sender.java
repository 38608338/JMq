package leo;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class Sender {
	String returnXml = null;

	public static void main(String[] args) {
		singlethread();
	}

	public static void multithread() {
		final Sender ss = new Sender();
		for (int i = 1; i < 10; i++) {
			Thread tt=new Thread(new Runnable() {
				public void run() {
					for (int j = 0; j < 10; j++) {
						ss.Send("work" + j);
					}
				}
			});
			tt.start();
			/*try {
				tt.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
	}

	public static void singlethread() {
		final Sender ss = new Sender();
		for (int i = 1; i < 100; i++) {
			ss.Send("work" + i);
		}
	}

	public String Send(String xmlData) {
		// ConnectionFactory ：连接工厂，JMS 用它创建连接
		ConnectionFactory connectionFactory;
		// Connection ：JMS 客户端到JMS Provider 的连接
		Connection connection = null;
		// Session： 一个发送或接收消息的线程
		Session session;
		// Destination ：消息的目的地;消息发送给谁.
		Destination destination;
		// MessageProducer：消息发送者
		MessageProducer producer;
		// 构造ConnectionFactory实例对象，此处采用ActiveMq的实现jar
		connectionFactory = new ActiveMQConnectionFactory(
				ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");
		try {
			// 构造从工厂得到连接对象
			connection = connectionFactory.createConnection();
			// 启动
			connection.start();
			// 获取操作连接
			session = connection.createSession(Boolean.TRUE,
					Session.AUTO_ACKNOWLEDGE);
			// 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
			destination = session.createQueue("QUEUE_LOCAL1");
			// 得到消息生成者【发送者】
			producer = session.createProducer(destination);
			// 设置不持久化，此处学习，实际根据项目决定
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.setPriority(1);
			TextMessage msg = session.createTextMessage(xmlData);
			// 发送消息到目的地方
			// System.out.println("外网发送消息：" +xmlData);
			String msgid = "12432";
			msg.setJMSCorrelationID(msgid);
			producer.send(msg);
			session.commit();

			// 获取操作连接
			session = connection.createSession(Boolean.FALSE,
					Session.CLIENT_ACKNOWLEDGE);
			destination = session.createQueue("QUEUE_LOCAL2");
			MessageConsumer consumer = session.createConsumer(destination);
			// 设置接收者接收消息的时间，为了便于测试，这里谁定为1s
			Message message = consumer.receive(10000);
			if (null != message) {
				if (message instanceof TextMessage) {
					TextMessage txtMsg = (TextMessage) message;
					returnXml = txtMsg.getText();
					// System.out.println("外网收到消息:" + msg);

					// 确认接收，并成功处理了消息
					//if (txtMsg.getJMSCorrelationID().equals(msgid)) {
						message.acknowledge();
					//}
				}
			}
			//returnXml = Receive(msgid);
			System.out.println("外网消息 发送：" + xmlData + " 返回：" + returnXml);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != connection)
					connection.close();
			} catch (Throwable ignore) {
			}
		}
		return returnXml;
	}
}
