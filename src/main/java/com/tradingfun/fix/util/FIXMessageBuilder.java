package com.tradingfun.fix.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cameronsystems.fix.message.FIXMessageAsIndexedByteArray;
import com.cameronsystems.fix.message.IFIXMessage;

public class FIXMessageBuilder {
	
	private static final String TAG_35="35";
	private static final String TAG_49="49";
	private static final String TAG_56="56";
	private static final String[] BUILT_IN_TAGS = {"10", "34", "8", "9", "52", "98"};

	private static final Set<String> BUILT_IN_TAGS_SET = new HashSet<String>();
	
	static 
	{
		BUILT_IN_TAGS_SET.add("10");
		BUILT_IN_TAGS_SET.add("34");
		BUILT_IN_TAGS_SET.add("8");
		BUILT_IN_TAGS_SET.add("9");
		BUILT_IN_TAGS_SET.add("52");
		BUILT_IN_TAGS_SET.add("98");
	}
			
	
	private static final Logger logger = LoggerFactory.getLogger(FIXMessageBuilder.class);
	
	public static IFIXMessage buildFIXMessage(String templateStr, String tagDelimeter, Map<String, String> parameterMap) throws FIXMessageException {
		
		if (isEmptyStr(templateStr) || isEmptyStr(tagDelimeter) || parameterMap == null )
		{
			throw new FIXMessageException("Invalid message");
		}
		
		logger.info("Message template:" + templateStr + ", delimeter:" + tagDelimeter + ", parameters: " + parameterMap );

		String newString = templateStr;
		Map<String, String> fixMsgMap = new HashMap<String, String>();
		
		if (parameterMap != null)
		{
			for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
				newString = newString.replaceAll("p\\{" + entry.getKey().trim() + "\\}", entry.getValue());
			}
		}

		logger.info("After applying parameters, the message is " + newString );
		
		String[] tagValues = newString.split("\\" + tagDelimeter);
		
		if (tagValues.length > 0) {
			for (String singleTag : tagValues) {
				String[] parsedTag = singleTag.split("=");
				if (parsedTag.length < 0) {
					logger.warn("Invalid tag and value:" + singleTag);
					throw new FIXMessageException("Invalid message");
				}
				
				try {
					// the tag must be integer
					Integer.parseInt(parsedTag[0]);
				}
				catch (NumberFormatException e) {
					throw new FIXMessageException("the tag must be integer. given value is " + parsedTag[0]);
				}

				fixMsgMap.put(parsedTag[0], parsedTag[1]);
			}
		}
		
		if (logger.isDebugEnabled())
		{
			logger.debug("FIX Message (Out): " + APIClientUtils.printMapToString(fixMsgMap));
		}
		
		String msgType = null;
		
		// validate the final map. There must be a key "35"
		if (!fixMsgMap.containsKey(TAG_35)) {
			logger.warn("Invalid FIX message. No 35 tag");
			throw new FIXMessageException("Invalid message");
		} else if (!fixMsgMap.containsKey(TAG_49)) {
			logger.warn("Invalid FIX message. No 49 tag");
			throw new FIXMessageException("Invalid message");
		} else if (!fixMsgMap.containsKey(TAG_56)) {
			logger.warn("Invalid FIX message. No 56 tag");
			throw new FIXMessageException("Invalid message");
		}
		
		IFIXMessage message = new FIXMessageAsIndexedByteArray();
		
		//setup message type
		msgType = fixMsgMap.remove(TAG_35);
		message.setMessageType(msgType);
		
		//remove some built-in tags in case they are added
		for (String tag: BUILT_IN_TAGS)
		{
			fixMsgMap.remove(tag);
		}
		
		for (Map.Entry<String, String> entry : fixMsgMap.entrySet()) {
			message.addField(Integer.parseInt(entry.getKey()), entry.getValue());
		}

		return message;
	}

	public static IFIXMessage buildFIXMessageWithRepeatingGroup(String templateStr, String tagDelimeter, Map<String, String> parameterMap) throws FIXMessageException {
		
		if (isEmptyStr(templateStr) || isEmptyStr(tagDelimeter) || parameterMap == null )
		{
			throw new FIXMessageException("Invalid message");
		}
		
		logger.info("Message template:" + templateStr + ", delimeter:" + tagDelimeter + ", parameters: " + parameterMap );

		String newString = templateStr;
	
		
		List<String> tagList = new ArrayList<String>();
		List<String> valueLIst = new ArrayList<String>();
		
		if (parameterMap != null)
		{
			for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
				newString = newString.replaceAll("p\\{" + entry.getKey().trim() + "\\}", entry.getValue());
			}
		}

		logger.info("After applying parameters, the message is " + newString );
		
		String[] tagValues = newString.split("\\" + tagDelimeter);
		
		String msgType = null;
		
		if (tagValues.length > 0) {
			for (String singleTag : tagValues) {
				String[] parsedTag = singleTag.split("=");
				if (parsedTag.length < 0) {
					logger.warn("Invalid tag and value:" + singleTag);
					throw new FIXMessageException("Invalid message");
				}
				
				try {
					// the tag must be integer
					Integer.parseInt(parsedTag[0]);
				}
				catch (NumberFormatException e) {
					throw new FIXMessageException("the tag must be integer. given value is " + parsedTag[0]);
				}
				
				if (TAG_35.equals(parsedTag[0]))
				{
					msgType = parsedTag[1]; 
					continue;
					
				}
				else if (BUILT_IN_TAGS_SET.contains(parsedTag[0]))
				{
					continue;
				}
				
				tagList.add(parsedTag[0]);
				
				if (parsedTag[1].matches("\\{.*\\}.*"))
				{
					throw new FIXMessageException("Value of the tag " + parsedTag[0] + " is not replaced. The value is: " + parsedTag[1]);
				}
				
				valueLIst.add(parsedTag[1]);
			}
		}
	
		// validate the final map. There must be a key "35"
		if (msgType == null) {
			logger.warn("Invalid FIX message. No 35 tag");
			throw new FIXMessageException("Invalid message");
		} else if (!tagList.contains(TAG_49)) {
			logger.warn("Invalid FIX message. No 49 tag");
			throw new FIXMessageException("Invalid message");
		} else if (!tagList.contains(TAG_56)) {
			logger.warn("Invalid FIX message. No 56 tag");
			throw new FIXMessageException("Invalid message");
		}
		
		IFIXMessage message = new FIXMessageAsIndexedByteArray();
		message.setMessageType(msgType);
		
		for (int i = 0; i<tagList.size(); i++)
		{
			message.addField(Integer.parseInt(tagList.get(i)), valueLIst.get(i));
		}

		return message;
	}
	
	private static boolean isEmptyStr(String str) {
		if (str == null || str.trim().length() == 0)
			return true;
		return false;
	}

}
