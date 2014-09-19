package com.tradingfun.fix;


public interface IFIXProcessor {

	public void setTaker(IFIXMarketMaker maker);

	public void initialize();

	public void dispose();
	
}
