package com.tradingfun.fix.util;

import com.cameronsystems.fix.message.IFIXMessage;

public interface ISocketAdaptorListener {
	
	public void onMessage(IFIXMessage message);
	
	public void onConnectionDown();
	
	public void onConnectionUp();
	
	public void onDisposed();

}
