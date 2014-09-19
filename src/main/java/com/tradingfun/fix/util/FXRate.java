package com.tradingfun.fix.util;

import java.math.BigDecimal;

public class FXRate {
	
	private String identifier;
	private BigDecimal bidRate;
	private BigDecimal askRate;
	private BigDecimal limit;
	private boolean cancelled;
	
	public FXRate(String identifier, BigDecimal bidRate, BigDecimal askRate, BigDecimal limit, boolean cancelled) {
		super();
		this.identifier = identifier;
		this.bidRate = bidRate;
		this.askRate = askRate;
		this.limit = limit;
		this.cancelled = cancelled;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public BigDecimal getBidRate() {
		return bidRate;
	}

	public void setBidRate(BigDecimal bidRate) {
		this.bidRate = bidRate;
	}

	public BigDecimal getAskRate() {
		return askRate;
	}

	public void setAskRate(BigDecimal askRate) {
		this.askRate = askRate;
	}

	public BigDecimal getLimit() {
		return limit;
	}

	public void setLimit(BigDecimal limit) {
		this.limit = limit;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
