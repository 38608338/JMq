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
		final Sender ss = new Sender();
		ss.start();
		for (int i = 1; i < 100; i++) {
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
		//ss.stop();
	}

	public void start() {

	}

	public void stop() {
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
			TextMessage message = session.createTextMessage(xmlData);
			// ������Ϣ��Ŀ�ĵط�
			// System.out.println("����������Ϣ��" +xmlData);
			String msgid = "12432";
			message.setJMSCorrelationID(msgid);
			producer.send(message);
			session.commit();

			returnXml = Receive(msgid);
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

	private String Receive(String msgid) {
		// ConnectionFactory �����ӹ�����JMS ������������
		ConnectionFactory connectionFactory;
		// Connection ��JMS �ͻ��˵�JMS Provider ������
		Connection connection = null;
		// Session�� һ�����ͻ������Ϣ���߳�
		Session session;
		// Destination ����Ϣ��Ŀ�ĵ�;��Ϣ���͸�˭.
		Destination destination;
		// MessageProducer����Ϣ������
		MessageConsumer consumer;
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
			session = connection.createSession(Boolean.FALSE,
					Session.AUTO_ACKNOWLEDGE);
			// ��ȡsessionע�����ֵxingbo.xu-queue��һ����������queue��������ActiveMq��console����
			destination = session.createQueue("QUEUE_LOCAL2");
			consumer = session.createConsumer(destination);
			while (true) {
				// ���ý����߽�����Ϣ��ʱ�䣬Ϊ�˱��ڲ��ԣ�����˭��Ϊ1s
				Message message = consumer.receive(1000);
				if (null != message) {
					if (message instanceof TextMessage) {
						TextMessage txtMsg = (TextMessage) message;
						String msg = txtMsg.getText();
						// System.out.println("�����յ���Ϣ:" + msg);

						// ȷ�Ͻ��գ����ɹ���������Ϣ
						if (txtMsg.getJMSCorrelationID().equals(msgid)) {
							message.acknowledge();
							return msg;
						}
					}
				} else {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != connection)
					connection.close();
			} catch (Throwable ignore) {
			}
		}
		return "��ʱ";
	}
}
