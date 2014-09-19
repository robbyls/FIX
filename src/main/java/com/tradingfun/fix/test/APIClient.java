package com.tradingfun.fix.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cameronsystems.fix.configuration.Constants;
import com.cameronsystems.fix.message.FIXMessageAsIndexedByteArray;
import com.cameronsystems.fix.message.IFIXMessage;
import com.orcsoftware.util.data.ConversionException;
import com.orcsoftware.util.data.ITagValue;
import com.tradingfun.fix.util.APIClientUtils;
import com.tradingfun.fix.util.ISocketAdaptorListener;
import com.tradingfun.fix.util.SocketAdaptor;


public class APIClient implements ISocketAdaptorListener, APIClientMBean
{

	private static final Logger logger = LoggerFactory.getLogger(APIClient.class);
	
	private SocketAdaptor adaptor;
	private List<TestCase> testCaseList;
		
	private String logonMsg;
	private String logOutMsg;
	private String delimeter;
	
	private Map<String, String> parameterMap;
	private CountDownLatch sessionLatch = new CountDownLatch(1);
	private CountDownLatch finishLatch;
	private CountDownLatch adaptorLatch;

	
	private ScheduledExecutorService scheduler;

	public String getDelimeter() {
		return delimeter;
	}

	public void setDelimeter(String delimeter) {
		this.delimeter = delimeter;
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public void setScheduler(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}

	public SocketAdaptor getAdaptor() {
		return adaptor;
	}

	public void setAdaptor(SocketAdaptor adaptor) {
		this.adaptor = adaptor;
	}

	public List<TestCase> getTestCaseList() {
		return testCaseList;
	}

	public void setTestCaseList(List<TestCase> testCaseList) {
		this.testCaseList = testCaseList;
	}

	public String getLogonMsg() {
		return logonMsg;
	}

	public void setLogonMsg(String logonMsg) {
		this.logonMsg = logonMsg;
	}

	public String getLogOutMsg() {
		return logOutMsg;
	}

	public void setLogOutMsg(String logOut) {
		this.logOutMsg = logOut;
	}

	public Map<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

	protected void startup()
	{
		
		logger.info("Start up APIClient");
		
		adaptor.initialize();
		adaptor.registerListener(this);
		
		int sleepTime = 3000;
		int maxTry = 5;
		int retries = 0;
		
		while (!adaptor.isConnected() && retries < maxTry)	
		{
			try {
				Thread.sleep(sleepTime);
				retries++;
			} catch (InterruptedException e) {
				logger.warn("The main thread is interrupted");
				break;
			}
		}
		
		if (!adaptor.isConnected())
		{
			logger.info("Adaptor cannot connect to FIX engine. Shutting down the process.");
			adaptor.dispose();
			return;
		}
		
//		//Logon FIX session
//		try {
//			IFIXMessage message = FIXMessageBuilder.buildFIXMessage(logonMsg, delimeter, parameterMap);
//			
////			message.setValue(95, "L1_user1/qwe".length());
//			message.addField(96, "L1_user1/qwe");
//			
//			adaptor.sendMessage(message);
//		} catch (FIXMessageException e) {
//			logger.info("Failed to build a valid Logon message. Shuttding down the process");
//			adaptor.dispose();
//			return;
//		} catch (Exception e) {
//			logger.info("Failed to send out Logon message. Shuttding down the process");
//			adaptor.dispose();
//			return;
//		}
//		
//		
//		//waiting for logon completes
//		try {
//			sessionLatch.await();
//		} catch (InterruptedException e) {
//			logger.warn("The main thread is interrupted");
//			return;
//		}
		
		
		try {
			IFIXMessage message = new FIXMessageAsIndexedByteArray();
			message.setMessageType("h");
			message.addField(49, "CNX");
			message.addField(56, "i2tdmk1");
			message.addField(336, "123");
			message.addField(340, "2");
			message.addField(58, "Trading Session Begins");
			adaptor.sendMessage(message);
			
			
			message = new FIXMessageAsIndexedByteArray();
			message.setMessageType("h");
			message.addField(49, "CNX");
			message.addField(56, "i2tdmk2");
			message.addField(336, "123");
			message.addField(340, "2");
			message.addField(58, "Trading Session Begins");
			adaptor.sendMessage(message);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//set up the finish check point
		finishLatch = new CountDownLatch(testCaseList.size());
		
		//launch tests
		for (TestCase test: testCaseList){
			test.start();
		}

		APIClientUtils.waitOnLatch(finishLatch);
	
		logger.info("All test completed. Disposing adaptor and scheduler...");
		
		//dispose adaptor
//		adaptorLatch = new CountDownLatch(1);
		adaptor.dispose();
		
//		APIClientUtils.waitOnLatch(adaptorLatch);
		
		//dispose scheduler
		scheduler.shutdownNow();
		
		logger.info("Test completed");
		
	}
	
	public void notifyCompletion()
	{
		finishLatch.countDown();
		logger.info("One test completed.");
	}
	
	
	protected void stop() 
	{
		
		logger.info("Shutting down API Client");
		//launch tests
		for (TestCase test: testCaseList){
			test.stop();
		}
	}
	
	
	public static void main(String[] args)
	{
		
		if (args.length == 0)
		{
			logger.info("App context file(s) must be provided.");
			return;
		}
		
		// create and configure beans
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext(args);
		APIClient client = context.getBean("testClient", APIClient.class);
		client.startup();

	}


	@Override
	public void onMessage(IFIXMessage message) {

		String msgType = message.getMsgType();
		
		// if it is logon, enable the system
		if (msgType.equals(Constants.MSGLogon)) {
			sessionLatch.countDown();
		} else if (msgType.equals(Constants.MSGLogout)) {
			stop();
		}

		else if (msgType.equals(Constants.MSGExecutionReport)) {

			if (logger.isDebugEnabled()) {
				ITagValue[] tagVals = message.getTagValues();
				Map<String, String> map = new HashMap<String, String>();
				for (ITagValue tagVal : tagVals) {
					try {
						map.put(String.valueOf(tagVal.getTag()), tagVal.getValue().getValueAsString());
					} catch (ConversionException e) {
						logger.warn("Error encountered when converting execution report for the tag " + String.valueOf(tagVal.getTag()));
					}
				}

				logger.debug("----------Get one execution report--------------");
				logger.debug(APIClientUtils.printMapToString(map));
			}

		}
	}
		

	@Override
	public void onConnectionDown() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnectionUp() {
		// TODO Auto-generated method stub
	}
	
	public void onDisposed() {
		if (adaptorLatch != null) {
			adaptorLatch.countDown();
		}

	}

	@Override
	public void shutdown() {
		
		logger.info("Received shutdown instruction from JMX client");
		stop();
	}
	
}
