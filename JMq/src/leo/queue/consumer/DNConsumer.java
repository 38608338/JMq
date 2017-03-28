package leo.queue.consumer;

public interface DNConsumer {
	void init();
	void init(String url);
	void getMessage(String disname);
}
