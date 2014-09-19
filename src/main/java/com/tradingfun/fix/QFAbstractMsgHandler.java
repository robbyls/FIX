package com.tradingfun.fix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.StringField;

import com.tradingfun.fix.util.BusinessException;
import com.tradingfun.fix.util.FIXMessageException;

public abstract class QFAbstractMsgHandler implements QFMsgHandler{
	
	protected Map<String, Integer> incomingTagMap;
	protected QFFIXTemplate successFIX;
	protected QFFIXTemplate failureFIX;
	private Map<String, String> parameterMap;
	protected static final Logger logger = LoggerFactory.getLogger(QFAbstractMsgHandler.class);
			
	public Map<String, String> getParameterMap() {
		return parameterMap;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

	public Map<String, Integer> getIncomingTagMap() {
		return incomingTagMap;
	}

	public void setIncomingTagMap(Map<String, Integer> incomingTagMap) {
		this.incomingTagMap = incomingTagMap;
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

		boolean resultFlag = true;
		
		Map<String, String> incomingData = new HashMap<String, String>();
		
		
		if (incomingTagMap != null && incomingTagMap.size() > 0) {
			for (Map.Entry<String, Integer> entry : incomingTagMap.entrySet()) {

				try {
					StringField field = new StringField(entry.getValue().intValue());
					message.getField(field);
					incomingData.put(entry.getKey(), field == null ? null : field.getValue());
				} catch (FieldNotFound e) {
					logger.error("tag not found in the message " + message.toString());
					resultFlag = false;
					break;
				}
			}
		}
		
		
		Map<String, String> returnData;
		try 
		{
			returnData = processEvent(message, sessionId, incomingData);
		} catch (BusinessException e)
		{
			logger.error("encounter exception ", e);
			resultFlag = false;
		}
		
		
		if (resultFlag)
		{
			//send out success message
			
		} else 
		{
			//send out failure message
			
		}
		
	}
	
	
	protected Message generateFixMessage(Message incomingMsg, SessionID sessionId,Map<String, String> returnData, QFFIXTemplate msgTemplate)
	{
		
		if (msgTemplate == null || msgTemplate.getTags().size()==0)
			return null;
		
		
		for (QFFIXTemplateTag temp: msgTemplate.getTags())
		{
			String key = temp.getKey();
			String value = temp.getValue();

			//handle parameter
			if ("p{systemTime}".equals(value))
			{
				 SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMdd-HH:mm:ss.SSS", Locale.getDefault() );
				 value=formatter.format(new Date());
			} else if ("p{senderCompId}".equals(value))
			{
		
			}
	
			
		}
		
		
		return null;
	}
	
	
	
	abstract public Map<String, String> processEvent(Message message, SessionID sessionId, Map<String, String> incomingDate) throws BusinessException;
	
	
}
