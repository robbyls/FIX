package com.tradingfun.fix;

public class ValueUtils {
	
	public static boolean isInteger(String value) {

		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

}
