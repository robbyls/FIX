package com.tradingfun.fix;

import quickfix.Message;
import quickfix.SessionID;

public interface IFIXMarketMaker {
	
	public void startup();
	public void shutdown();
	
	public void onQuoteRequest(FIXQuoteRequest request);
	public void onOrderRequest(FIXOrder request);

}
