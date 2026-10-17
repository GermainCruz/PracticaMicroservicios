package com.example.Pasarela.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    @Value("${gateway.rate-limit.max-requests:2}")
    private int maxRequests;

    @Value("${gateway.rate-limit.window-seconds:60}")
    private long windowSeconds;

    private final ConcurrentHashMap<String, Deque<Long>> requestHistory = new ConcurrentHashMap<>();

    public synchronized boolean isAllowed(String clientKey) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000);

        Deque<Long> timestamps = requestHistory.computeIfAbsent(clientKey, k -> new ArrayDeque<>());

        // Remover solicitudes fuera de la ventana de tiempo
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequests) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public synchronized long getSecondsRemaining(String clientKey) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000);
        Deque<Long> timestamps = requestHistory.get(clientKey);
        if (timestamps == null || timestamps.isEmpty()) {
            return 0;
        }
        Long oldest = timestamps.peekFirst();
        if (oldest == null) {
            return 0;
        }
        long elapsed = now - oldest;
        long remaining = (windowSeconds * 1000) - elapsed;
        return Math.max(1, remaining / 1000);
    }

    public void reset() {
        requestHistory.clear();
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public int getTrackedClientsCount() {
        return requestHistory.size();
    }
}
