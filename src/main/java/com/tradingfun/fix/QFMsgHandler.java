package com.tradingfun.fix;

import quickfix.Message;
import quickfix.SessionID;

import com.tradingfun.fix.util.FIXMessageException;

public interface QFMsgHandler {

	public void processMsg(Message arg0, SessionID arg1) throws FIXMessageException;
	
}
