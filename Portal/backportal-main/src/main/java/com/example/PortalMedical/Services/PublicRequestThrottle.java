package com.example.PortalMedical.Services;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Small in-memory limiter for the public appointment-request form: at most N requests
 * per key (an IP address) in a sliding time window. Resets when the server restarts.
 */
@Component
public class PublicRequestThrottle {

	private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

	/** Records a hit and returns false once the key went over the limit in the window. */
	public boolean allow(String key, int max, long windowMillis) {
		long now = System.currentTimeMillis();
		Deque<Long> times = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
		synchronized (times) {
			while (!times.isEmpty() && now - times.peekFirst() > windowMillis) {
				times.pollFirst();
			}
			if (times.size() >= max) {
				return false;
			}
			times.addLast(now);
		}
		if (hits.size() > 10000) {
			hits.entrySet().removeIf(e -> e.getValue().isEmpty());
		}
		return true;
	}
}
