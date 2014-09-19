package com.financialogix.xstream.common.marketrate;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.financialogix.common.SystemException;
import com.financialogix.common.config.Configuration;
import com.financialogix.common.util.BlockPolicy;
import com.financialogix.common.util.QueueMap;
import com.financialogix.xstream.common.data.Rate;
import com.financialogix.xstream.common.data.XStreamIdentifier;

/**
 * FileMarketRateService plays canned rate from rate files
 */
public class EnhancedFileMarketRateService 
extends	AbstractMarketRateService
{
	private int		rateFrequency=200;
	private int		fileLoadFrequency = 300000;
	
	@SuppressWarnings("rawtypes")
	private Map<String, ScheduledFuture>	timerTaskLookup = new ConcurrentHashMap<String,ScheduledFuture>();
	private ScheduledThreadPoolExecutor taskTimer;
    private ExecutorService sendRatesService;
    
    private int rateUpdateThread = 2;
	private int rateGeneratorThread = 3;
	
	private AtomicInteger counter = new AtomicInteger();
	private final QueueMap<XStreamIdentifier, Rate> rmdsRateCache = new QueueMap<XStreamIdentifier, Rate>();
	private boolean isRunning;

    					
	/**
	 * subscribe to the rate
	 * @param anIdentifier identifier
	 * @param aMarketRateCode market rate code as subscription topic
	 * @exception SystemException thrown if there is a problem subscribing to the rate
	 */
	@Override
	public synchronized void	subscribe
				( XStreamIdentifier anIdentifier,
				  String			aMarketRateCode )
	throws	SystemException
	{
		
		if (anIdentifier == null)
		{
			return;
		}
		
		String key = anIdentifier.toString() +".load";

		if (timerTaskLookup.containsKey(key))
		{
			return;
		}
		
		Runnable loadRateTask = new LoadRateTask(anIdentifier );
		ScheduledFuture<?> futureTask = taskTimer.scheduleAtFixedRate(loadRateTask, 0, fileLoadFrequency, TimeUnit.MILLISECONDS);
		timerTaskLookup.put( anIdentifier.toString() +".load", futureTask );

	}

	/**
	 * unsubscribe to the rate
	 * @param anIdentifier identifier
	 * @param aMarketRateCode market rate code as subscription topic
	 * @exception SystemException thrown if there is a problem unsubscribing to the rate
	 */
	@Override
	public synchronized void unsubscribe
				( XStreamIdentifier anIdentifier,
				  String			aMarketRateCode )
	throws	SystemException
	{

		String[] taskKeys = {
				anIdentifier.toString() +".load",
				anIdentifier.toString() +".push"
		};

		for (int i = 0; i < taskKeys.length; i++)
		{
			ScheduledFuture<?> futureTask = timerTaskLookup.get(taskKeys[i]);
			if (futureTask != null) {
				futureTask.cancel(true);
				timerTaskLookup.remove(taskKeys[i]);
				futureTask = null;
			}
		}
	}
	
	/**
	 * unsubscribe all rates
	 * @exception SystemException thrown if there is a problem unsubscribing to the rate
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public synchronized void unsubscribeAll()
	throws	SystemException
	{
		for (Iterator<ScheduledFuture> iterator = timerTaskLookup.values()
				.iterator(); iterator.hasNext();) {
			iterator.next().cancel(true);
		}

		timerTaskLookup.clear();
		
	}
	

	/**
	 * initialize
	 * @exception SystemException thrown if there is an unexpected system error
	 */
	@Override
	public void	initialize()
	throws	SystemException
	{
		
		taskTimer = new ScheduledThreadPoolExecutor(rateGeneratorThread);
		
        sendRatesService = new ThreadPoolExecutor(rateUpdateThread, rateUpdateThread, 60, 
        		TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), 
        		new BlockPolicy());
        
        
        if (getAuditLogger().isDebugEnabled()) 
		{
        	taskTimer.scheduleAtFixedRate(new PrintCounter(), 5000, 5000, TimeUnit.MILLISECONDS);
		}
        
        
		isRunning = true;
		Thread marketRatePooler = new Thread( new PollMarketRateTask() );
		marketRatePooler.start();
        
        
	}
	
	/**
	 * configure
	 * @param aConfiguration configuration
	 * @exception SystemException thrown if there is a problem configuring
	 * the service
	 */
	@Override
	public void	configure
				( Configuration		aConfiguration )
	throws	SystemException
	{
		
		setSystemLogger( "xstream.marketrate.enhancedfile" );
        setAuditLogger( "xstream.marketrate.enhancedfile.audit" );
		
//		setSystemLogger( "xstream.marketrate.rmds" );
//        setAuditLogger( "xstream.marketrate.rmds.audit" );
        
		rateFrequency = aConfiguration.getInt( "xstream.marketRateService.file.rateFrequency", rateFrequency );
		fileLoadFrequency = aConfiguration.getInt( "xstream.marketRateService.file.fileLoadFrequency", fileLoadFrequency );
		
		rateUpdateThread = aConfiguration.getInt( "xstream.marketRateService.file.rateUpdateThread", rateUpdateThread );
		rateGeneratorThread = aConfiguration.getInt( "xstream.marketRateService.file.rateGeneratorThread", rateGeneratorThread );
		
	}

	/**
	 * dispose
	 * @exception SystemException thrown if there is an unexpected system error
	 */
	@Override
	public synchronized void dispose()
	throws	SystemException
	{
			isRunning = false;
			unsubscribeAll();
			sendRatesService.shutdown();
			taskTimer.shutdown();	
	}

	/**
	 * load rates
	 * @param aProduct1Symbol product 1 symbol
	 * @param aProduct2Symbol product 2 symbol
	 * @param aTerm term
	 */
	
	
	@SuppressWarnings("resource")
	private ArrayList<BigDecimal[]> loadRates
						( XStreamIdentifier anIdentifier )
	{
		// load all rates
		DataInputStream inputStream = null;
//		java.text.NumberFormat numberFormat = new DecimalFormat( "#########.#########" );
		ArrayList<BigDecimal[]> rates = new ArrayList<BigDecimal[]>();
		String data = null;
		int index = 0;
		Reader r = null; // cooked reader
		BufferedReader br = null; // buffered for readLine()
		try
		{
			// get stream id
			File file = new File( "rate/" + anIdentifier.getBaseProduct() + "." + anIdentifier.getQuoteProduct() + "." + anIdentifier.getTerm() );
			if ( file.exists() )
			{
				inputStream = new DataInputStream( new FileInputStream( file  ) );
				r = new InputStreamReader(inputStream, "UTF-8"); // leave charset out for default
			    br = new BufferedReader(r);
				while( ( data = br.readLine()) != null )
				{
					index = data.indexOf( "," );
//					double[] bidAskRate = new double[2];
					
					BigDecimal[] bidAskRate = new BigDecimal[2];
					bidAskRate[0] = new BigDecimal(data.substring( 0, index ));
					bidAskRate[1] = new BigDecimal(data.substring( index + 1, data.length()));
					
//					bidAskRate[0] = new BigDecimal(numberFormat.parse( data.substring( 0, index ) ).doubleValue());
//					bidAskRate[1] = numberFormat.parse( data.substring( index + 1, data.length() ) ).doubleValue();						
					rates.add( bidAskRate );
				}
			}
			else
			{
				getSystemLogger().error( "Rate file does not exist for " + anIdentifier );
			}
			return rates;
		}
		catch( Throwable t )
		{
			getSystemLogger().error( "Unable to load rates for " + anIdentifier );
			return new ArrayList<BigDecimal[]>();
		}
		finally
		{
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Throwable t) {
			}
			if (br != null) {
				try {
					br.close();
				} catch (Throwable t) { /* ensure close happens */
				}
			}
			if (r != null) {
				try {
					r.close();
				} catch (Throwable t) { /* ensure close happens */
				}
			}

		}
	}
	
	
	class PublishRateTask
	implements Runnable
	{
		private List<BigDecimal[]>		rates;
		private int				currentRateIndex;
		private XStreamIdentifier identifier;
		
		/**
		 * create publish rate task
		 * @param allRates rates
		 */
		public	PublishRateTask
				( List<BigDecimal[]>		allRates,
				  XStreamIdentifier anIdentifier )
		{
			rates = allRates;
			identifier = anIdentifier;
		}
		
		@Override
		public void	run()
		{
			if ( currentRateIndex >= rates.size() )
			{
				currentRateIndex = 0;
			}
			BigDecimal[] bidAskRates = rates.get( currentRateIndex );
			Rate rate = new Rate( identifier, null, bidAskRates[0], bidAskRates[1], MarketRateSourceType.FILE );
			
			rmdsRateCache.put(rate.getIdentifier(), rate);

			currentRateIndex++;
			
			if (getAuditLogger().isDebugEnabled())
			{
				counter.incrementAndGet();	
			}
		}
	}
	
	
	
	class LoadRateTask
	implements Runnable
	{
		
		private XStreamIdentifier anIdentifier;
		
		public LoadRateTask(XStreamIdentifier anIdentifier) {
			this.anIdentifier = anIdentifier;
		}


		@Override
		public void	run()
		{
			List<BigDecimal[]> rates = loadRates( anIdentifier );
			
			String key = anIdentifier.toString() + ".push";
			
			ScheduledFuture<?> futureTask = timerTaskLookup.get(key);

			if (futureTask != null) {
				futureTask.cancel(true);
				timerTaskLookup.remove(key);
				futureTask = null;
			}

			if (rates.size() > 0) {
				Runnable pushTask = new PublishRateTask(rates, anIdentifier);
				futureTask = taskTimer.scheduleWithFixedDelay(pushTask, 0, rateFrequency,TimeUnit.MILLISECONDS);
				timerTaskLookup.put(key, futureTask);
			}
		}
	}
	
	
	class PrintCounter
	implements Runnable
	{
		@Override
		public void	run()
		{
			if (getAuditLogger().isDebugEnabled())
			{
				getAuditLogger().debug ("Sent out rates from last update: "	+ counter.intValue());
				counter.set(0);				
			}
		}
	}

	
	class PollMarketRateTask implements Runnable {

		@Override
		public void run() {

			while (isRunning) {

				Rate rate;
				try {
					rate = rmdsRateCache.take();
					if (rate != null)
					{
						updateMarketRate(rate, MarketRateSourceType.FILE);	
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
//	class UpdateMarkateRateTask implements Runnable {
//		Rate rate;
//
//		public UpdateMarkateRateTask(Rate aRate) {
//			this.rate = aRate;
//		}
//
//		@Override
//		public void run() {
//			updateMarketRate(rate, MarketRateSourceType.FILE);
//		}
//
//	}

	@Override
	public MarketRateSourceType getMarketRateSourceType() {
		return MarketRateSourceType.FILE;
		
	}
	
	

}
