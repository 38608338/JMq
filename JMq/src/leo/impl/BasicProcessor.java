package leo.impl;

import leo.Processor;

public class BasicProcessor implements Processor {

	@Override
	public String Operate(String xmlData) {
		return "done::"+xmlData;
	}

}
