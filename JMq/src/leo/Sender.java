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
		// ConnectionFactory �����ӹ�����JMS ������������
		ConnectionFactory connectionFactory;
		// Connection ��JMS �ͻ��˵�JMS Provider ������
		Connection connection = null;
		// Session�� һ�����ͻ������Ϣ���߳�
		Session session;
		// Destination ����Ϣ��Ŀ�ĵ�;��Ϣ���͸�˭.
		Destination destination;
		// MessageProducer����Ϣ������
		MessageProducer producer;
		// ����ConnectionFactoryʵ�����󣬴˴�����ActiveMq��ʵ��jar
		connectionFactory = new ActiveMQConnectionFactory(
				ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");
		try {
			// ����ӹ����õ����Ӷ���
			connection = connectionFactory.createConnection();
			// ����
			connection.start();
			// ��ȡ��������
			session = connection.createSession(Boolean.TRUE,
					Session.AUTO_ACKNOWLEDGE);
			// ��ȡsessionע�����ֵxingbo.xu-queue��һ����������queue��������ActiveMq��console����
			destination = session.createQueue("QUEUE_LOCAL1");
			// �õ���Ϣ�����ߡ������ߡ�
			producer = session.createProducer(destination);
			// ���ò��־û����˴�ѧϰ��ʵ�ʸ�����Ŀ����
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.setPriority(1);
			TextMessage msg = session.createTextMessage(xmlData);
			// ������Ϣ��Ŀ�ĵط�
			// System.out.println("����������Ϣ��" +xmlData);
			String msgid = "12432";
			msg.setJMSCorrelationID(msgid);
			producer.send(msg);
			session.commit();

			// ��ȡ��������
			session = connection.createSession(Boolean.FALSE,
					Session.CLIENT_ACKNOWLEDGE);
			destination = session.createQueue("QUEUE_LOCAL2");
			MessageConsumer consumer = session.createConsumer(destination);
			// ���ý����߽�����Ϣ��ʱ�䣬Ϊ�˱��ڲ��ԣ�����˭��Ϊ1s
			Message message = consumer.receive(10000);
			if (null != message) {
				if (message instanceof TextMessage) {
					TextMessage txtMsg = (TextMessage) message;
					returnXml = txtMsg.getText();
					// System.out.println("�����յ���Ϣ:" + msg);

					// ȷ�Ͻ��գ����ɹ���������Ϣ
					//if (txtMsg.getJMSCorrelationID().equals(msgid)) {
						message.acknowledge();
					//}
				}
			}
			//returnXml = Receive(msgid);
			System.out.println("������Ϣ ���ͣ�" + xmlData + " ���أ�" + returnXml);
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
