package com.tradingfun.fix.util;

import java.util.Map;

public class FIXMessage {
	
	private String msgTemplate;
	private int deplay;
	private int repeat;
	private String delimeter;
	
	private volatile int runCounter;
	
	private Map<String, String> parameterMap;
	
	public String getMsgTemplate() {
		return msgTemplate;
	}
	public void setMsgTemplate(String msgTemplate) {
		this.msgTemplate = msgTemplate;
	}
	public int getDeplay() {
		return deplay;
	}
	public void setDeplay(int deplay) {
		this.deplay = deplay;
	}
	public int getRepeat() {
		return repeat;
	}
	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
	public Map<String, String> getParameterMap() {
		return parameterMap;
	}
	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}
	public String getDelimeter() {
		return delimeter;
	}
	public void setDelimeter(String delimeter) {
		this.delimeter = delimeter;
	}
	
	public void resetCounter()
	{
		runCounter = 0;
	}
	
	public void encreaseCounter()
	{
		runCounter++;
	}
	
	public boolean finishedAllRuns()
	{
		return runCounter > repeat;
	}

}
