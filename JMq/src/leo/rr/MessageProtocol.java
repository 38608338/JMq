package leo.rr;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageProtocol {

	public String handleProtocolMessage(String messageText) {
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(new Date())+ " Server�յ���Ϣ��"+messageText);
		return "done::"+messageText;
	}

}
