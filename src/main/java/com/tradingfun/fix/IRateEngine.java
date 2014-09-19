package com.tradingfun.fix;

import com.tradingfun.fix.util.FXStreamIdentifier;

public interface IRateEngine {
	
	public void subscribe(FXStreamIdentifier anIdentifier, long frequency) throws Exception;
	public void unSubscribe(FXStreamIdentifier anIdentifier) throws Exception;
	public void initialize() throws Exception; 
	public void dispose() throws Exception;
	

}
