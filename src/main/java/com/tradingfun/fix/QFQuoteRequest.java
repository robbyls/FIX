package com.tradingfun.fix;

import quickfix.Message;
import quickfix.SessionID;

import com.cameronsystems.fix.message.IFIXMessage;

public class QFQuoteRequest {
	
	//fields from the original request
	private String sender;
	private String target;
	private String strSymbol;
	private String quoteReqId;
	private String tenure;
	private String reqType;
	private String quantity;
	private String settlementDate;
	private IFIXMessage fixRequestMsg;
	
	//field derived
	private String newTermValue;
	private String userId;
	private String baseProduct;
	private String quoteroduct;
	
	private SessionID sessionId;
	private Message fixMsg;
	
	
	public QFQuoteRequest(String sender, String target, String strSymbol, String quoteReqId, String tenure) {
		super();
		this.sender = sender;
		this.target = target;
		this.strSymbol = strSymbol;
		this.quoteReqId = quoteReqId;
		this.tenure = tenure;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getStrSymbol() {
		return strSymbol;
	}

	public void setStrSymbol(String strSymbol) {
		this.strSymbol = strSymbol;
	}

	public String getQuoteReqId() {
		return quoteReqId;
	}

	public void setQuoteReqId(String quoteReqId) {
		this.quoteReqId = quoteReqId;
	}

	public String getTenure() {
		return tenure;
	}

	public void setTenure(String tenure) {
		this.tenure = tenure;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getSettlementDate() {
		return settlementDate;
	}

	public void setSettlementDate(String settlementDate) {
		this.settlementDate = settlementDate;
	}

	public IFIXMessage getFixRequestMsg() {
		return fixRequestMsg;
	}

	public void setFixRequestMsg(IFIXMessage fixRequestMsg) {
		this.fixRequestMsg = fixRequestMsg;
	}

	public String getNewTermValue() {
		return newTermValue;
	}

	public void setNewTermValue(String newTermValue) {
		this.newTermValue = newTermValue;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBaseProduct() {
		return baseProduct;
	}

	public void setBaseProduct(String baseProduct) {
		this.baseProduct = baseProduct;
	}

	public String getQuoteroduct() {
		return quoteroduct;
	}

	public void setQuoteroduct(String quoteroduct) {
		this.quoteroduct = quoteroduct;
	}

	public SessionID getSessionId() {
		return sessionId;
	}

	public void setSessionId(SessionID sessionId) {
		this.sessionId = sessionId;
	}

	public Message getFixMsg() {
		return fixMsg;
	}

	public void setFixMsg(Message fixMsg) {
		this.fixMsg = fixMsg;
	}
	
}
