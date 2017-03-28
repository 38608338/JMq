package leo.queue;

import leo.queue.provider.DNProvider;
import leo.queue.provider.DNProviderImpl;

public class ProviderTest2 {

	public static void main(String[] args) {
		DNProvider provider=new DNProviderImpl();
		provider.init("failover:(tcp://localhost:61616,tcp://localhost:61617,tcp://localhost:61618)");
		//provider.sendMessage("DN-JACK-20");
		
		ProviderTest2 t=new ProviderTest2();
		new Thread(t.new MyThread(provider)).start();
		new Thread(t.new MyThread(provider)).start();
		new Thread(t.new MyThread(provider)).start();
		new Thread(t.new MyThread(provider)).start();
		new Thread(t.new MyThread(provider)).start();
		new Thread(t.new MyThread(provider)).start();
	}

	private class MyThread implements Runnable
	{
		DNProvider dnp;
		
		public MyThread(DNProvider dnp){
			this.dnp=dnp;
		}
		
		@Override
		public void run() {
			while (true) {
				dnp.sendMessage("DN-JACK-30");
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
