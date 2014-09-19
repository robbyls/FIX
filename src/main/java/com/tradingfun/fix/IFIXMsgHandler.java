package com.tradingfun.fix;

import com.tradingfun.fix.util.FIXMessageException;

public interface IFIXMsgHandler {
	
	public void execute() throws FIXMessageException;

}
