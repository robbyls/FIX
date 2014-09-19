package com.tradingfun.fix.util;
//package com.tdsecurities.api.test.util;
//
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.ScheduledThreadPoolExecutor;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
///**
// * FileMarketRateService plays canned rate from rate files
// */
//public class FileRateGenerator 
//
//{
//	private int rateFrequency = 200;
//	private int fileLoadFrequency = 300000;
//	private String ratesFileLocation = "rate";
//
//	private Map<String, ScheduledFuture> timerTaskLookup = new ConcurrentHashMap<String, ScheduledFuture>();
//	private ScheduledThreadPoolExecutor taskTimer;
//	private ExecutorService sendRatesService;
//
//	private int rateUpdateThread = 2;
//	private int rateQueueSize = 100;
//	private int rateGeneratorThread = 3;
//
//	private String provider;
//
//	private AtomicInteger genCounter = new AtomicInteger();
//	private AtomicInteger sendCounter = new AtomicInteger();
//
//	private String defaultLimit = "2000000";
//	private String prefix = "xstream.taker.file.taker.";
//	private Map<FXStreamIdentifier, FXRate> rateCache = new ConcurrentHashMap<FXStreamIdentifier, FXRate>();
//
//	private static final Logger logger = LoggerFactory.getLogger(FileRateGenerator.class);
//
//
//
//	/**
//	 * subscribe to the rate
//	 * 
//	 * @param anIdentifier
//	 *            identifier
//	 * @param aMarketRateCode
//	 *            market rate code as subscription topic
//	 * @exception Exception
//	 *                thrown if there is a problem subscribing to the rate
//	 */
//	public synchronized void subscribe(FXStreamIdentifier anIdentifier) throws Exception {
//
//		if (anIdentifier == null) {
//			return;
//		}
//
//		String key = anIdentifier.toString() + ".load";
//
//		if (timerTaskLookup.containsKey(key)) {
//			return;
//		}
//
//		Runnable loadRateTask = new LoadRateTask(anIdentifier);
//		ScheduledFuture futureTask = taskTimer.scheduleAtFixedRate(loadRateTask, 0, fileLoadFrequency, TimeUnit.MILLISECONDS);
//		timerTaskLookup.put(anIdentifier.toString() + ".load", futureTask);
//	}
//
//	public synchronized void unSubscribe(String anIdentifier) throws Exception {
//
//		if (anIdentifier == null) {
//			return;
//		}
//
//		String loadTaskKey=anIdentifier.toString() + ".load";
//		String pushTaskKey=anIdentifier.toString() + ".push";
//		
//		ScheduledFuture futureTask = timerTaskLookup.get(loadTaskKey);
//		if (futureTask != null) {
//			futureTask.cancel(true);
//			timerTaskLookup.remove(loadTaskKey);
//		}
//
//		futureTask = timerTaskLookup.get(pushTaskKey);
//		if (futureTask != null) {
//			futureTask.cancel(true);
//			timerTaskLookup.remove(pushTaskKey);
//		}
//
//	}
//
//
//	/**
//	 * initialize
//	 * 
//	 * @exception Exception
//	 *                thrown if there is an unexpected system error
//	 */
//	public void initialize() throws Exception {
//
//		taskTimer = new ScheduledThreadPoolExecutor(rateGeneratorThread);
//
//		sendRatesService = new ThreadPoolExecutor(rateUpdateThread, rateUpdateThread, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(rateQueueSize),
//				new ThreadPoolExecutor.DiscardOldestPolicy());
//
//		if (logger.isDebugEnabled()) {
//			taskTimer.scheduleAtFixedRate(new PrintCounter(), 5000, 5000, TimeUnit.MILLISECONDS);
//		}
//	}
//
//
//	/**
//	 * dispose
//	 * 
//	 * @exception Exception
//	 *                thrown if there is an unexpected system error
//	 */
//	public synchronized void dispose() throws Exception {
//		sendRatesService.shutdown();
//		taskTimer.shutdown();
//	}
//	
//	private List<BigDecimal[]> loadRates(FXStreamIdentifier anIdentifier) {
//		// load all rates
//		List<BigDecimal[]> rates = new ArrayList<BigDecimal[]>();
//		String data = null;
//		BufferedReader br = null;
//		int index = 0;
//
//		// get stream id
//		File file = new File(ratesFileLocation + "/" + anIdentifier.getBaseCurrency() + "." + anIdentifier.getQuoteCurrency() + "." + anIdentifier.getTerm());
//		if (file.exists()) {
//			try {
//				br = new BufferedReader(new FileReader(file));
//				while ((data = br.readLine()) != null) {
//					String[] strArray = data.split(",");
//
//					//ignore invalid data
//					if (strArray == null || strArray.length < 2)
//					{
//						continue;
//					}
//					
//					BigDecimal[] bidAskRate = new BigDecimal[3];
//										
//					
//					bidAskRate[0] = new BigDecimal(strArray[0]);
//					bidAskRate[1] = new BigDecimal(strArray[1]);
//					
//					if (strArray.length > 2)
//					{
//						bidAskRate[2] = new BigDecimal(strArray[2]);
//					} else
//					{
//						bidAskRate[2] = new BigDecimal(defaultLimit);
//					}
//
//					rates.add(bidAskRate);
//				}
//			} catch (IOException t) {
//				logger.error("Unable to load rates for " + anIdentifier);
//			}
//
//			finally {
//
//				try {
//					if (br != null)
//						br.close();
//				} catch (IOException ex) {
//					logger.error("Error when close rate file for " + anIdentifier);
//				}
//			}
//
//		} else {
//			logger.error("Rate file does not exist for " + anIdentifier);
//		}
//
//		return rates;
//	}
//	
//
//	class PushRate2ESRTask implements Runnable {
//
//		Liquidity rate;
//
//		public PushRate2ESRTask(Liquidity rate) {
//			super();
//			this.rate = rate;
//		}
//
//		public void run() {
//
//			try {
//				long rawTimestamp = System.currentTimeMillis();
//
//				XStreamRate xstreamBidRate = new XStreamRate(getProvider(), null, // sourceId
//						rate.getIdentifier(), // XstreamID
//						null, // settlementDate
//						Long.toString(rawTimestamp), // rateId
//						true, // isBid
//						BigDecimal.ZERO.equals(rate.getBidRate())?false:true,
//						rate.getLimit(), // Limit
//						rate.getBidRate(), // Rate
//						false, // isCancelled
//						null, // cancelReason
//						new Date());
//
//				XStreamRate xstreamOfferRate = new XStreamRate(getProvider(), null, // sourceId
//						rate.getIdentifier(), null, // settlementDate
//						Long.toString(rawTimestamp), // rateId
//						false, // isBid
//						BigDecimal.ZERO.equals(rate.getBidRate())?false:true,
//						rate.getLimit(), // Limit
//						rate.getAskRate(), // Rate
//						false, // isCancelled
//						null, // cancelReason
//						new Date());
//
//				// create XStreamRates
//				XStreamRate[] individualBidRates = new XStreamRate[1];
//				individualBidRates[0] = xstreamBidRate;
//
//				XStreamRate[] individualAskRates = new XStreamRate[1];
//				individualAskRates[0] = xstreamOfferRate;
//
//				XStreamRates rateUpdate = new XStreamRates(Long.toString(rawTimestamp), // rateId
//						rate.getIdentifier(), null, // aSettlementDate
//						individualBidRates, individualAskRates, new Date(), false /* shouldProcessRates */);
//
//				rateCache.put(rate.getIdentifier().toString(), rateUpdate);
//				
////				taker.rateUpdated(getProvider(), rateUpdate, true, true, false);
//				taker.notifyRateUpdate(rateUpdate);
//
//				logger.info(rateUpdate.getRatesId() + ", " + rateUpdate.getIdentifier() + ", " 
//						+ rate.getBidRate().toPlainString() + "/" + rate.getAskRate() + ", " + rate.getLimit().toPlainString());
//				
//				if (logger.isDebugEnabled()) {
//					sendCounter.incrementAndGet();
//				}
//
//
//			} catch (Throwable t) {
//				getSystemLogger().error("Could not publish Rate " + rate + ". The error details is " + ExceptionHelper.converStackTraceToString(t));
//			}
//
//		}
//	}
//
//	class PublishRateTask implements Runnable {
//		private List<BigDecimal[]> rates;
//		private int currentRateIndex;
//		private FXStreamIdentifier identifier;
//
//		/**
//		 * create publish rate task
//		 * 
//		 * @param allRates
//		 *            rates
//		 */
//		public PublishRateTask(List<BigDecimal[]> allRates, FXStreamIdentifier anIdentifier) {
//			rates = allRates;
//			identifier = anIdentifier;
//		}
//
//		public void run() {
//			if (currentRateIndex >= rates.size()) {
//				currentRateIndex = 0;
//			}
//			BigDecimal[] bidAskRates = rates.get(currentRateIndex);
//
//			Liquidity rate = new Liquidity(identifier,bidAskRates[0],  bidAskRates[1],  bidAskRates[2]);
//			sendRatesService.execute(new PushRate2ESRTask(rate));
//
//			currentRateIndex++;
//
//			if (logger.isDebugEnabled()) {
//				genCounter.incrementAndGet();
//			}
//		}
//	}
//
//	class LoadRateTask implements Runnable {
//
//		private FXStreamIdentifier anIdentifier;
//
//		public LoadRateTask(FXStreamIdentifier anIdentifier) {
//			this.anIdentifier = anIdentifier;
//		}
//
//		public void run() {
//			List<BigDecimal[]> rates = loadRates(anIdentifier);
//
//			String key = anIdentifier.toString() + ".push";
//
//			ScheduledFuture futureTask = timerTaskLookup.get(key);
//
//			if (futureTask != null) {
//				futureTask.cancel(true);
//				timerTaskLookup.remove(key);
//				futureTask = null;
//			}
//
//			if (rates.size() > 0) {
//				Runnable pushTask = new PublishRateTask(rates, anIdentifier);
//				futureTask = taskTimer.scheduleAtFixedRate(pushTask, 0, rateFrequency, TimeUnit.MILLISECONDS);
//				timerTaskLookup.put(key, futureTask);
//			}
//		}
//	}
//
//	class PrintCounter implements Runnable {
//		public void run() {
//			if (logger.isDebugEnabled()) {
//				logger.debug("Generated rates from last update: " + genCounter.intValue());
//				genCounter.set(0);
//
//				logger.debug("Sent out rates from last update: " + sendCounter.intValue());
//				sendCounter.set(0);
//			}
//		}
//	}
//	
//	public FXRate getCurrentRate( FXStreamIdentifier identifier)
//	{
//		return  rateCache.get(identifier.toString());
//	}
//	
//	
//
//}
