package leo.queue;

import leo.queue.provider.DNProvider;
import leo.queue.provider.DNProviderImpl;

public class ProviderTest {

	public static void main(String[] args) {
		DNProvider provider=new DNProviderImpl();
		provider.init("failover:(tcp://localhost:61616,tcp://localhost:61617,tcp://localhost:61618)");
		//provider.init("tcp://localhost:61616");
		//provider.sendMessage("DN-JACK-20");
		
		ProviderTest t=new ProviderTest();
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
			dnp.sendMessage("DN-JACK-30");
		}
		
	}
}
