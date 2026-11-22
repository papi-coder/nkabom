package com.infobip.nkabom.approval.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicConfigurationController {

    private final String turnstileSiteKey;

    public PublicConfigurationController(
            @Value("${app.turnstile.site-key}") String turnstileSiteKey) {
        this.turnstileSiteKey = turnstileSiteKey;
    }

    @GetMapping("/api/config")
    public PublicConfiguration configuration() {
        return new PublicConfiguration(turnstileSiteKey);
    }

    public record PublicConfiguration(String turnstileSiteKey) {
    }
}
