package com.tradingfun.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;



public class QFApplication extends MessageCracker implements Application{
	
	private static final Logger logger = LoggerFactory.getLogger(QFApplication.class);
	
	private IFIXMarketMaker taker;
	
	public IFIXMarketMaker getTaker() {
		return taker;
	}

	public void setTaker(IFIXMarketMaker taker) {
		this.taker = taker;
	}

	@Override
	public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		// TODO Auto-generated method stub
		logger.info("got message", arg0.toString());
		
		
		
	}

	@Override
	public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		// TODO Auto-generated method stub
		logger.info("got message", arg0.toString());
		
	}

	@Override
	public void onCreate(SessionID arg0) {
		// TODO Auto-generated method stub
		logger.info("onCreate");
	}

	@Override
	public void onLogon(SessionID arg0) {
		// TODO Auto-generated method stub
		logger.info("onLogon");
		
	}

	@Override
	public void onLogout(SessionID arg0) {
		// TODO Auto-generated method stub
		logger.info("onLogout");
	}

	@Override
	public void toAdmin(Message arg0, SessionID arg1) {
		// TODO Auto-generated method stub
		logger.info("toAdmin message", arg0.toString());
	}

	@Override
	public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
		// TODO Auto-generated method stub
		logger.info("toApp message", arg0.toString());
	}

}
