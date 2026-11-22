package com.infobip.nkabom.approval.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApprovalRateLimiter {

    private static final Duration WINDOW = Duration.ofMinutes(10);
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final int MAX_TRACKED_ADDRESSES = 10_000;

    private final Map<String, RequestWindow> requests = new HashMap<>();

    public synchronized boolean allowRequest(String clientAddress) {
        Instant now = Instant.now();
        RequestWindow window = requests.get(clientAddress);
        if (window == null) {
            if (requests.size() >= MAX_TRACKED_ADDRESSES) {
                requests.entrySet().removeIf(entry -> isExpired(entry.getValue(), now));
                if (requests.size() >= MAX_TRACKED_ADDRESSES) {
                    return false;
                }
            }
            window = new RequestWindow(now, 0);
        } else if (isExpired(window, now)) {
            window = new RequestWindow(now, 0);
        }

        if (window.count() >= MAX_REQUESTS_PER_WINDOW) {
            return false;
        }
        requests.put(clientAddress, new RequestWindow(window.startedAt(), window.count() + 1));
        return true;
    }

    private boolean isExpired(RequestWindow window, Instant now) {
        return !now.isBefore(window.startedAt().plus(WINDOW));
    }

    private record RequestWindow(Instant startedAt, int count) {
    }
}
