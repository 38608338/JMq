package leo.queue;

import leo.queue.consumer.DNConsumer;
import leo.queue.consumer.DNConsumerImpl;

public class ConsumerTest {

	public static void main(String[] args) {
		DNConsumer consumer = new DNConsumerImpl();
		consumer.init();
		// consumer.getMessage("DN-JACK-20");
		
		System.out.println(Thread.currentThread().getName());
		
		ConsumerTest t=new ConsumerTest();
		new Thread(t.new MyThread(consumer)).start();
		new Thread(t.new MyThread(consumer)).start();
		new Thread(t.new MyThread(consumer)).start();
		new Thread(t.new MyThread(consumer)).start();
		new Thread(t.new MyThread(consumer)).start();
		new Thread(t.new MyThread(consumer)).start();
	}

	private class MyThread implements Runnable {
		DNConsumer dnc;

		public MyThread(DNConsumer dnc) {
			this.dnc = dnc;
		}

		@Override
		public void run() {
			while (true) {
				dnc.getMessage("DN-JACK-30");
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
