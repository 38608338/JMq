package leo.queue;

import leo.queue.consumer.DNConsumer;
import leo.queue.consumer.DNConsumerImpl;

public class ConsumerTest2 {

	public static void main(String[] args) {
		DNConsumer consumer = new DNConsumerImpl();
		//consumer.init("failover:(tcp://localhost:61617,tcp://localhost:61616,tcp://localhost:61618)");
		consumer.init("tcp://localhost:61617");
		// consumer.getMessage("DN-JACK-20");
		
		System.out.println(Thread.currentThread().getName());
		
		ConsumerTest2 t=new ConsumerTest2();
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
			dnc.getMessage("DN-JACK-30");
		}

	}
}
