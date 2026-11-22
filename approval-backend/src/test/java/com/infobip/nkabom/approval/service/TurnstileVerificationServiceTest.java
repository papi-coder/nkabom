package com.infobip.nkabom.approval.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TurnstileVerificationServiceTest {

    @Test
    void acceptsValidTokenForExpectedHostnameAndAction() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://challenges.cloudflare.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TurnstileVerificationService service =
                new TurnstileVerificationService(builder, "private-key", "127.0.0.1");
        server.expect(requestTo("https://challenges.cloudflare.com/turnstile/v0/siteverify"))
                .andExpect(method(POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("response=challenge-token")))
                .andRespond(withSuccess(
                        "{\"success\":true,\"hostname\":\"127.0.0.1\",\"action\":\"approval\"}",
                        MediaType.APPLICATION_JSON));

        service.verify("challenge-token", "127.0.0.1");

        server.verify();
    }

    @Test
    void rejectsSuccessfulChallengeForAnotherHostname() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://challenges.cloudflare.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TurnstileVerificationService service =
                new TurnstileVerificationService(builder, "private-key", "127.0.0.1");
        server.expect(requestTo("https://challenges.cloudflare.com/turnstile/v0/siteverify"))
                .andRespond(withSuccess(
                        "{\"success\":true,\"hostname\":\"attacker.example\",\"action\":\"approval\"}",
                        MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.verify("challenge-token", "127.0.0.1"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        server.verify();
    }
}
