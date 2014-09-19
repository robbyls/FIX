package com.tradingfun.fix;

import java.io.Serializable;
import java.math.BigDecimal;

import com.cameronsystems.fix.message.IFIXMessage;

public class FIXOrder implements Cloneable, Serializable {
	
	// general information
	private String orderId;
	private String clientId;

	// execution instruction
	private String baseProduct;
	private String quoteProduct;
	private String term;
	private String settlementDate;
	private String side;
	private String priceReferenceId;
	private BigDecimal priceLimit;
	private BigDecimal spotPrice;
	private BigDecimal quantity;
	private String clientTradingProduct;
	private boolean isRejected;
	private String reason;
	private FIXSessionObject fixSessionObj;
	private IFIXMessage fixRequestMsg;
	

}
