package com.tradingfun.fix;

import java.util.Map;

public class FIXMsgTemplate {

	private String msgTemplate;
	private String delimeter;
	private Map<String, String> parameterMap;

	public FIXMsgTemplate(String msgTemplate, String delimeter, Map<String, String> parameterMap) {
		super();
		this.msgTemplate = msgTemplate;
		this.delimeter = delimeter;
		this.parameterMap = parameterMap;
	}

	public String getMsgTemplate() {
		return msgTemplate;
	}

	public void setMsgTemplate(String msgTemplate) {
		this.msgTemplate = msgTemplate;
	}

	public String getDelimeter() {
		return delimeter;
	}

	public void setDelimeter(String delimeter) {
		this.delimeter = delimeter;
	}

	public Map<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

}
