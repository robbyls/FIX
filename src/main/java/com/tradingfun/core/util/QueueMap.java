package com.tradingfun.core.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This utility class is used to keep latest update of a data in a map and 
 * keep the key in a queue. In the extreme situation that update for a same data
 * happens so frequently, the consumers of the data can escape the interim changes 
 * and pick up the latest directly.
 * @author lirc
 *
 * @param <K>
 * @param <V>
 */
public class QueueMap<K, V> {

	private ConcurrentHashMap<K, V> valueMap;
	private BlockingQueue<K> keyQueue;

	public QueueMap() {
		this.valueMap = new ConcurrentHashMap<K, V>();
		this.keyQueue = new LinkedBlockingQueue<K>();
	}

	public void put(K key, V value) {
		valueMap.put(key, value);
		if (!keyQueue.contains(key)) {
			keyQueue.add(key);
		}
	}
	
	public void remove(K key) {
		keyQueue.remove(key);
		valueMap.remove(key);
	}
	
	public V take() throws InterruptedException {
		K key = keyQueue.take();
		return valueMap.remove(key);
	}

	public V poll() throws InterruptedException {
		K key = keyQueue.poll();
		if (key != null) {
			return valueMap.remove(key);
		}
		return null;
	}

}
