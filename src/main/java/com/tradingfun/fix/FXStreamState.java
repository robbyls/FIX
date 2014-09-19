package com.tradingfun.fix;

import com.tradingfun.fix.util.FXRate;
import com.tradingfun.fix.util.FXStreamIdentifier;

public class FXStreamState {

	private FXRate lastRate;
	
	private FXStreamIdentifier identifier;
	
	private boolean isStreaming = true;
	
	private boolean isLocked = false;
	
	private boolean isDisable = false;
	
	private String settlmentDateStr;
	
	private int updateFrequency = 500;

	public FXStreamState(FXStreamIdentifier identifier, String settlmentDateStr) {
		super();
		this.identifier = identifier;
		this.settlmentDateStr = settlmentDateStr;
	}

	public FXRate getLastRate() {
		return lastRate;
	}

	public void setLastRate(FXRate lastRate) {
		this.lastRate = lastRate;
	}

	public FXStreamIdentifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(FXStreamIdentifier identifier) {
		this.identifier = identifier;
	}

	public boolean isStreaming() {
		return isStreaming;
	}

	public void setStreaming(boolean isStreaming) {
		this.isStreaming = isStreaming;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public boolean isDisable() {
		return isDisable;
	}

	public void setDisable(boolean isDisable) {
		this.isDisable = isDisable;
	}

	public String getSettlmentDateStr() {
		return settlmentDateStr;
	}

	public void setSettlmentDateStr(String settlmentDateStr) {
		this.settlmentDateStr = settlmentDateStr;
	}

	public int getUpdateFrequency() {
		return updateFrequency;
	}

	public void setUpdateFrequency(int updateFrequency) {
		this.updateFrequency = updateFrequency;
	}
	
}
