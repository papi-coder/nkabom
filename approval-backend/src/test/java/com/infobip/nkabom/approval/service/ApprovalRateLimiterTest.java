package com.infobip.nkabom.approval.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalRateLimiterTest {

    @Test
    void limitsEachAddressToFiveRequestsPerWindow() {
        ApprovalRateLimiter limiter = new ApprovalRateLimiter();

        for (int request = 0; request < 5; request++) {
            assertTrue(limiter.allowRequest("192.0.2.10"));
        }
        assertFalse(limiter.allowRequest("192.0.2.10"));
        assertTrue(limiter.allowRequest("192.0.2.11"));
    }
}
