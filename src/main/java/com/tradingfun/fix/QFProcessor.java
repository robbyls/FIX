package com.tradingfun.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Acceptor;
import quickfix.ConfigError;
import quickfix.RuntimeError;

public class QFProcessor implements IFIXProcessor {

	private IFIXMarketMaker maker;
	private Acceptor acceptor;
	private static final Logger logger = LoggerFactory.getLogger(QFProcessor.class);
	private int numberOfSessions;
	
	
	public QFProcessor(Acceptor acceptor) {
		super();
		this.acceptor = acceptor;
	}

	@Override
	public void setTaker(IFIXMarketMaker maker) {
		this.maker = maker;
	}
		
	public int getNumberOfSessions() {
		return numberOfSessions;
	}

	public void setNumberOfSessions(int numberOfSessions) {
		this.numberOfSessions = numberOfSessions;
	}

	@Override
	public void initialize() {
		try {
			acceptor.start();
		} catch (RuntimeError e) {
			logger.error("QuickFX session cannot be established.", e);
		} catch (ConfigError e) {
			logger.error("QuickFX session cannot be established due to incorrect configuration.", e);
		}
	}

	@Override
	public void dispose() {
		logger.error("Disposing QuickFX session.");
		acceptor.stop();
	}

}
