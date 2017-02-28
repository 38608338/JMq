package leo.queue;

import leo.queue.provider.DNProvider;
import leo.queue.provider.DNProviderImpl;

public class ProviderTest {

	public static void main(String[] args) {
		DNProvider provider=new DNProviderImpl();
		provider.init();
		//provider.sendMessage("DN-JACK-20");
		
		ProviderTest t=new ProviderTest();
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
