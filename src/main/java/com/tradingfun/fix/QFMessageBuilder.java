package com.tradingfun.fix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.taskdefs.email.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.StringField;
import quickfix.field.BeginString;
import quickfix.field.MsgType;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

public class QFMessageBuilder {

	private static final String TAG_35 = "35";
	private static final String TAG_49 = "49";
	private static final String TAG_56 = "56";
	private static final String[] BUILT_IN_TAGS = { "10", "34", "8", "9", "52", "98" };

	private static final Set<String> BUILT_IN_TAGS_SET = new HashSet<String>();

	static {
		BUILT_IN_TAGS_SET.add("10");
		BUILT_IN_TAGS_SET.add("34");
		BUILT_IN_TAGS_SET.add("8");
		BUILT_IN_TAGS_SET.add("9");
		BUILT_IN_TAGS_SET.add("52");
		BUILT_IN_TAGS_SET.add("98");
	}

	private static final Logger logger = LoggerFactory.getLogger(QFMessageBuilder.class);

	public static Message generateFixMessage(Message incomingMsg, SessionID sessionId, Map<String, String> outcomeData, Map<String, String> parameterMap, QFFIXTemplate msgTemplate) {

		if (msgTemplate == null || msgTemplate.getTags().size() == 0)
			return null;
		
		Message returnMsg = new Message();

		for (QFFIXTemplateTag temp : msgTemplate.getTags()) {
			processMsgTemplate(incomingMsg, outcomeData, parameterMap, temp, returnMsg);
		}

		return returnMsg;
	}

	
	
	public static void processMsgTemplate(Message incomingMsg, Map<String, String> outcomeData, Map<String, String> parameterMap, QFFIXTemplateTag  msgTempTag, Message outputMsg) {

		if (msgTempTag == null)
			return;

		String key = msgTempTag.getKey();
		String value = msgTempTag.getValue();

		boolean resolved = true;
		
		Pattern p = Pattern.compile("p\\{(.*)\\}");
		Matcher m = p.matcher(value);
		if (m.find()) {
		
			resolved = false;
			String extractValue = m.group();
			
			//If it is in the parameter map
			String mappedValue = parameterMap.get(extractValue);
			if (mappedValue != null) {
				value = mappedValue;
				resolved = true;
			} else if ("systemTime".equals(extractValue))
			{
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS", Locale.getDefault());
				value = formatter.format(new Date());
				resolved = true;
			} else {
				
				//check if the value from in and out parameter
				p = Pattern.compile("p\\{[(in|out])\\.(.*)\\}");
				m = p.matcher(value);
				
				if (m.find()) {
					String source = m.group(0);
					String tag = m.group(1);
					
					if ("in".equals(source))
					{
						// find the value from incoming message
						StringField field = new StringField(Integer.parseInt(tag));
						try {
							incomingMsg.getField(field);
							value = field.getValue();
							resolved = true;

						} catch (FieldNotFound e) {
							logger.error("Cannot find tag " + tag + " from the incoming FIX message." );
						}
						
					} else {
						
						//get from process result data
						value = outcomeData.get(tag);
						if (value != null)
						{
							resolved = true;
						} else {
							logger.error("Cannot find value from the process result map. The key is " + tag );
						}
					}
				} 
			}
		}
		
		
		StringField field = new StringField(Integer.parseInt(key), value);
		
		if (msgTempTag.isHeader())
		{
			outputMsg.getHeader().setField(field);
		} else {
			outputMsg.setField(field);
		}
				
		//if it has repeating groups, recursively call the method to finish them
		if (msgTempTag.getRepeatGroup() != null)
		{
			for (QFFIXTemplateTag subTag : msgTempTag.getRepeatGroup()) {
				
				processMsgTemplate(incomingMsg, outcomeData, parameterMap, subTag, outputMsg);
			}	
		}
	}

}
