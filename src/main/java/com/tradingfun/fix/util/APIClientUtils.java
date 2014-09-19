package com.tradingfun.fix.util;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class APIClientUtils {
	
	
	public static void waitOnLatch(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
	}
	
	public static String printMapToString(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
			sb.append(";");
		}
		return sb.toString();
	}

}
