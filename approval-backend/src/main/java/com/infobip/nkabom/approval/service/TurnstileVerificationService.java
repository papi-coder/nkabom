package com.infobip.nkabom.approval.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TurnstileVerificationService {

    private final RestClient restClient;
    private final String secretKey;
    private final String expectedHostname;

    public TurnstileVerificationService(
            RestClient.Builder restClientBuilder,
            @Value("${turnstile.secret-key}") String secretKey,
            @Value("${turnstile.expected-hostname}") String expectedHostname) {
        this.restClient = restClientBuilder.baseUrl("https://challenges.cloudflare.com").build();
        this.secretKey = secretKey;
        this.expectedHostname = expectedHostname;
    }

    public void verify(String token, String clientAddress) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secretKey);
        form.add("response", token);
        form.add("remoteip", clientAddress);

        TurnstileResponse response;
        try {
            response = restClient.post()
                    .uri("/turnstile/v0/siteverify")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TurnstileResponse.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The security verification service is unavailable.",
                    exception);
        }

        if (response == null
                || !response.success()
                || !expectedHostname.equalsIgnoreCase(response.hostname())
                || !"approval".equals(response.action())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Security verification failed.");
        }
    }

    private record TurnstileResponse(boolean success, String hostname, String action) {
    }
}
