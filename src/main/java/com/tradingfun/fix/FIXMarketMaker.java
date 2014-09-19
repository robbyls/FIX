package com.tradingfun.fix;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tradingfun.fix.util.APIClientUtils;

public class FIXMarketMaker implements IFIXMarketMaker {

	private static final Logger logger = LoggerFactory.getLogger(FIXMarketMaker.class);
	private CountDownLatch finishLatch;
	private ScheduledExecutorService scheduler;
	private IFIXProcessor fixProcessor;
	private Map<String, String> parameterMap;
	private Date endOfDate;
	private Date businessDate;

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public void setScheduler(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	
	public Map<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public IFIXProcessor getFixProcessor() {
		return fixProcessor;
	}

	public void setFixProcessor(IFIXProcessor fixProcessor) {
		this.fixProcessor = fixProcessor;
	}

	public void startup() {

		logger.info("Start up FXMaker");

		finishLatch = new CountDownLatch(1);
		
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownTask()));

		if (fixProcessor == null ) {
			logger.info("No FIX Session is configured. Stop the program");
			System.exit(0);
			// return;
		} else {
			// initializing FIX Sessions
			fixProcessor.initialize();
			fixProcessor.setTaker(this);
		}

		APIClientUtils.waitOnLatch(finishLatch);
		stop();
	}

	public void notifyCompletion() {
		finishLatch.countDown();
		logger.info("One test completed.");
	}

	protected void stop() {

		logger.info("Shutting down FIXMarketMaker");

		//Dispose fix connections
		fixProcessor.dispose();
		
		// dispose scheduler
		scheduler.shutdownNow();
	}

	
	public static void main(String[] args) {

		if (args.length == 0) {
			logger.info("App context file(s) must be provided.");
			return;
		}

		// create and configure beans
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext(args);
		IFIXMarketMaker marker = context.getBean("testMaker", IFIXMarketMaker.class);
		marker.startup();
	}

	@Override
	public void shutdown() {

		logger.info("Received shutdown instruction from JMX client");
		stop();
	}

	private class ShutdownTask implements Runnable {

		@Override
		public void run() {

			// release countDown
			finishLatch.countDown();
		}

	}

	@Override
	public void onQuoteRequest(FIXQuoteRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOrderRequest(FIXOrder request) {
		// TODO Auto-generated method stub
		
	}
	
	
}
