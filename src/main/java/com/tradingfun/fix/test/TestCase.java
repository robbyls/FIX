package com.tradingfun.fix.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cameronsystems.fix.configuration.Constants;
import com.cameronsystems.fix.message.IFIXMessage;
import com.orcsoftware.util.data.ConversionException;
import com.orcsoftware.util.data.ITagValue;
import com.tradingfun.fix.util.APIClientUtils;
import com.tradingfun.fix.util.FIXMessage;
import com.tradingfun.fix.util.FIXMessageBuilder;
import com.tradingfun.fix.util.FIXMessageException;
import com.tradingfun.fix.util.ISocketAdaptorListener;
import com.tradingfun.fix.util.SocketAdaptor;

public class TestCase implements ISocketAdaptorListener {
	
	private List<FIXMessage> messageList;
	private Map<String, String> parameterMap;
	private String testCaseId;
	private String idPrefix;
	private List<Map<String,String>> stopConditionMap;
	private SocketAdaptor adaoptor;
	private ScheduledExecutorService scheduler;
	private static final Logger logger = LoggerFactory.getLogger(TestCase.class);
	private APIClient apiClient;
	private int repeat;
	
	private volatile boolean stopFlag = false;
	private volatile int runTimes = 0;
	private CountDownLatch adaptorLatch;
	private static final String REQUEST_ID_TAG="131";
	
	private String tag35ForDeal="D";  //default to D
	private String orderIdTag="11";   //default to 11
	
	private ScheduledFuture scheduledTask;
	
	private Map<String, String> requestMap = new HashMap<String, String>();
	private Map<String, String> quotetMap = new HashMap<String, String>();
	private Set<String> OrderIdSet = new HashSet<String>();
	
	private List<FIXMessage> runMessageList;
	
	public String getTag35ForDeal() {
		return tag35ForDeal;
	}
	public void setTag35ForDeal(String tag35ForDeal) {
		this.tag35ForDeal = tag35ForDeal;
	}
	public String getOrderIdTag() {
		return orderIdTag;
	}
	public void setOrderIdTag(String orderIdTag) {
		this.orderIdTag = orderIdTag;
	}
	public APIClient getApiClient() {
		return apiClient;
	}
	public void setApiClient(APIClient apiClient) {
		this.apiClient = apiClient;
	}
	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}
	public void setScheduler(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	public List<FIXMessage> getMessageList() {
		return messageList;
	}
	public void setMessageList(List<FIXMessage> messageList) {
		this.messageList = messageList;
	}
	public Map<String, String> getParameterMap() {
		return parameterMap;
	}
	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public int getRepeat() {
		return repeat;
	}
	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
	public String getIdPrefix() {
		return idPrefix;
	}
	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}
	
	public List<Map<String, String>> getStopConditionMap() {
		return stopConditionMap;
	}
	public void setStopConditionMap(List<Map<String, String>> stopConditionMap) {
		this.stopConditionMap = stopConditionMap;
	}
	
	public SocketAdaptor getAdaoptor() {
		return adaoptor;
	}
	public void setAdaoptor(SocketAdaptor adaoptor) {
		this.adaoptor = adaoptor;
	}
	
	
	public void start() {
		
		//initially allow
		adaptorLatch = new CountDownLatch(0);
		
		//register itself as the listener
		adaoptor.registerListener(this);
		
		stopFlag = false;
		new Thread(new KickOffTest()).start();
	}
	
	public void stop() {
		stopFlag = true;
	}
	
	
	private void dispose()
	{
		//register Test Case
		adaoptor.deRegisterListener(TestCase.this);
		apiClient.notifyCompletion();
	}
	
	
	private synchronized void runNext() {

		if (runMessageList.size() == 0) {
			
			//try next complete run
			new Thread(new KickOffTest()).start();
			return;
		}

		FIXMessage msgTemplate = runMessageList.get(0);
		if (msgTemplate == null) {
			runMessageList.remove(0);
			runNext();
			
		} else {
			
			msgTemplate.encreaseCounter();
			
			if (msgTemplate.finishedAllRuns()) {
				runMessageList.remove(0);
			} 
			
			// schedule the task
			scheduledTask = scheduler.schedule(new SingleStep(msgTemplate), msgTemplate.getDeplay(), TimeUnit.MILLISECONDS);
		}
	}
	
	
	private class KickOffTest implements Runnable
	{

		@Override
		public void run() {

			//clean up request and quote map
			requestMap.clear();
			quotetMap.clear();
			
			boolean exitFlag = false;
			if (stopFlag)
			{
				logger.info("Test has been stopped. TestCase prefix: " + idPrefix);
				exitFlag = true;
			}
			
			if (messageList == null || messageList.size() ==0)
			{
				logger.info("No FIX message defined this test. Exit. TestCase prefix: " + idPrefix);
				exitFlag = true;
			}
			
			if (!adaoptor.isConnected())
			{
				logger.info("Adaptor has not connected. Exit. TestCase prefix: " + idPrefix);
				exitFlag = true;
			}
			
			//if the test has repeated the 
			if (runTimes > repeat)
			{
				logger.info("Test has been executed for " +runTimes + " times. Exit. TestCase prefix: " + idPrefix);
				exitFlag = true;
			}
			
			if (exitFlag)
			{
				
				dispose();
				return;
			}
			
			runTimes++;
			
			//generate new testCaseId/requestId
			testCaseId =idPrefix + "_" + String.valueOf(runTimes);
			parameterMap.put("requestid", testCaseId);
			
			runMessageList = Collections.synchronizedList(new ArrayList<FIXMessage>());
			runMessageList.addAll(messageList);
			
			runNext();
			
		}
		
	}
	
	private class SingleStep implements Runnable {

		FIXMessage msgTemplate;

		public SingleStep(final FIXMessage msgTemplate) {
			this.msgTemplate = msgTemplate;
		}

		@Override
		public void run() {
			
			//if the connection is down, wait
			APIClientUtils.waitOnLatch(adaptorLatch);
			
			if (stopFlag)
			{
				logger.info("Test is stopped. Exit.");
				dispose();
				return;
			}

			msgTemplate.getMsgTemplate();

			// prepare API level parameters
			Map<String, String> parameters = new HashMap<String, String>();
			
			if (apiClient.getParameterMap() != null)
			{
				parameters.putAll(apiClient.getParameterMap());
			}
			
			//add the test case level parameters
			if (parameterMap != null)
			{
				parameters.putAll(parameterMap);
			}
		
			if (requestMap != null)
			{
				parameters.putAll(buildParamFromMap(requestMap, "request"));
			}
			
			if (quotetMap != null)
			{
				parameters.putAll(buildParamFromMap(quotetMap, "quote"));
			}
		
			if (msgTemplate.getParameterMap() != null)
			{
				parameters.putAll(msgTemplate.getParameterMap());
			}

			try {
				IFIXMessage message = FIXMessageBuilder.buildFIXMessageWithRepeatingGroup(msgTemplate.getMsgTemplate(), msgTemplate.getDelimeter(), parameters);
				String msgType = message.getMsgType();
				
				//replace the request map
				Map<String, String> map = loadFIXMsgToMap(message);
				
				//special process for request and deal request
				if (msgType.equals(Constants.MSGQuoteRequest)) {
					
					requestMap= map;
					
					//if it is the deal request
				} else	if (msgType.equals(tag35ForDeal)) {
					
					if (map.containsKey(orderIdTag))
					{
						//get the order id
						OrderIdSet.add(map.get(orderIdTag));
					}
				}
				
				if (logger.isDebugEnabled())
				{
					logger.debug("FIX Message (Out): " + APIClientUtils.printMapToString(map));
				}
				
				adaoptor.sendMessage(message);
			} catch (FIXMessageException e) {
				logger.warn("Failed to send out FIX Message." + e.toString() + msgTemplate.getMsgTemplate() + ". parameters:" + APIClientUtils.printMapToString(parameters));
			} catch (Exception e) {
				logger.warn("Failed to send out FIX Message." + e.toString() + msgTemplate.getMsgTemplate() + ". parameters:" + APIClientUtils.printMapToString(parameters));
			}

			//call the next step
			runNext();
		}
	}
	
	
	private Map<String, String> buildParamFromMap(Map<String, String>inputMap, String suffix) {

		Map<String, String> parameters = new HashMap<String, String>();

		for (Map.Entry<String, String> innerEntry : inputMap.entrySet()) {
			String tag = innerEntry.getKey();
			String value = innerEntry.getValue();
			parameters.put(suffix + "." + tag, value);
		}

		return parameters;

	}
	

	@Override
	public void onMessage(IFIXMessage message) {

		String msgType = message.getMsgType();

		//parse the message to map first
		Map<String, String> map = loadFIXMsgToMap(message);
		
		if (checkStopCondition(map)) {

			// stop the current run of the test
			// however if the test has some unfinished repeat, it will continue
			// on the left repeats.
			
			if (logger.isDebugEnabled())
			{
				logger.debug("FIX Message (In): " + APIClientUtils.printMapToString(map));
			}
			
			
			if (scheduledTask != null) {
				scheduledTask.cancel(true);
			}

			runMessageList.clear();
			runNext();
			return;
		}

		// update quote map
		if (msgType.equals(Constants.MSGQuote)) {

			// get requestID
			try {
				String keyID = message.getValueAsString(Constants.TAGiQuoteReqID, null);

				// if no quote request included, check symbol
				if (keyID == null) {
					keyID = message.getValueAsString(Constants.TAGiSymbol, null);
				}

				if (keyID == null) {
					logger.warn("Cannot process quote. There is no QuoteRequestId, neither Symbol");
					return;
				}

				if (!testCaseId.equals(keyID)) {
					// by pass
					return;
				}
				
				if (logger.isDebugEnabled())
				{
					logger.debug("FIX Message (In) - Quote: " + APIClientUtils.printMapToString(map));
				}
				

				//replace the map
				quotetMap=map;

			} catch (ConversionException e) {
				logger.warn("Encountered exception when reading Quote", e);
			}

		} else if (msgType.equals(Constants.MSGExecutionReport)) {
			
			String orderId = map.get(orderIdTag);
			
			if (orderId != null && OrderIdSet.contains(orderId))
			{
				
				if (logger.isDebugEnabled())
				{
					logger.debug("FIX Message (In) - Execution report for " + orderId  + APIClientUtils.printMapToString(map));
				}
				
				OrderIdSet.remove(orderId);
			}
		}
	}
	
	/**
	 * If any condition defined in the stop criteria is met, stop the current run of the test case
	 * @param map
	 * @return
	 */
	private boolean checkStopCondition(Map<String, String> map)
	{
		boolean shouldStop = false;
		
		if (stopConditionMap != null && stopConditionMap.size() > 0) {
			
			// check stop condition
			if (map.containsKey(REQUEST_ID_TAG)) {
				String requestId = map.get(REQUEST_ID_TAG);

				if (testCaseId.equals(requestId)) {

					for (Map<String, String> stopCriteria : stopConditionMap) {

						if (stopCriteria == null || stopCriteria.size() == 0) {
							continue;
						}

						boolean condMet = true;
						for (Map.Entry<String, String> oneCondition : stopCriteria.entrySet()) {

							String key = oneCondition.getKey();
							String value = oneCondition.getValue();

							if (!value.equals(map.get(key))) {

								// once one condition is not met, set the flag
								// and exit from the inner loop
								condMet = false;
								break;
							}
						}
						
						if (condMet) {
							shouldStop = true;
							break;
						}
					}
				}
			}
		}
		
		return shouldStop;
	}
	
	private Map<String, String> loadFIXMsgToMap(IFIXMessage message) {
		ITagValue[] tagVals = message.getTagValues();
		Map<String, String> map = new HashMap<String, String>();

		try {
			for (ITagValue tagVal : tagVals) {
				
				String tag = String.valueOf(tagVal.getTag());
				
				//to handle repeating group
				while (map.containsKey(tag))
				{
					tag = tag+"-";
				}
				
				map.put(tag, tagVal.getValue().getValueAsString());
			}

		} catch (ConversionException e) {
			logger.warn("Encountered exception when reading Quote", e);
		}
		return map;
	}
	
	@Override
	public void onConnectionDown() {
		
		if (adaptorLatch.getCount() == 0)
		{
			adaptorLatch = new CountDownLatch(1);	
		}
		
	}
	@Override
	public void onConnectionUp() {
		
		if (adaptorLatch != null)
		{
			adaptorLatch.countDown();
		}
		
	}
	
	@Override
	public void onDisposed() {
		// TODO Auto-generated method stub
		
	}

}
