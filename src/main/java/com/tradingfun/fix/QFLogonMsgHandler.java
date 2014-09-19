package com.tradingfun.fix;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.StringField;

import com.tradingfun.fix.util.FIXMessageException;

public class QFLogonMsgHandler implements QFMsgHandler{
	
	private Map<String, Integer> fixValueMap;
	private QFFIXTemplate successFIX;
	private QFFIXTemplate failureFIX;
	private static final Logger logger = LoggerFactory.getLogger(QFLogonMsgHandler.class);

	public Map<String, Integer> getFixValueMap() {
		return fixValueMap;
	}

	public void setFixValueMap(Map<String, Integer> fixValueMap) {
		this.fixValueMap = fixValueMap;
	}

	public QFFIXTemplate getSuccessFIX() {
		return successFIX;
	}

	public void setSuccessFIX(QFFIXTemplate successFIX) {
		this.successFIX = successFIX;
	}

	public QFFIXTemplate getFailureFIX() {
		return failureFIX;
	}

	public void setFailureFIX(QFFIXTemplate failureFIX) {
		this.failureFIX = failureFIX;
	}

	@Override
	public void processMsg(Message message, SessionID sessionId) throws FIXMessageException {
		// TODO Auto-generated method stub
		
		Map<String, String> incomingDate = new HashMap<String, String>();
		
		if (fixValueMap != null && fixValueMap.size() > 0) {
			for (Map.Entry<String, Integer> entry : fixValueMap.entrySet()) {

				try {
					StringField field = new StringField(entry.getValue().intValue());
					message.getField(field);
					incomingDate.put(entry.getKey(), field == null ? null : field.getValue());
				} catch (FieldNotFound e) {
					logger.error("tag not found in the message " + message.toString());
				}
			}
		}
	}
}
