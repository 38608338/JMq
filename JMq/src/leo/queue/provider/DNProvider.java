package leo.queue.provider;

public interface DNProvider {
	void init();
	void init(String url);
	void sendMessage(String disname);
}
