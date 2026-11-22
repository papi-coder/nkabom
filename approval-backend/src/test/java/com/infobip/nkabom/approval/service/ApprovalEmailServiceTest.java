package com.infobip.nkabom.approval.service;

import com.infobip.nkabom.approval.api.ApprovalRequest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApprovalEmailServiceTest {

    @Test
    void sendsCampaignDetailsToTheConfiguredApprover() {
        SesV2Client sesClient = mock(SesV2Client.class);
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenReturn(null);
        ApprovalEmailService service = new ApprovalEmailService(sesClient, "verified-sender@example.com");
        ApprovalRequest approval = new ApprovalRequest(
                "Parent meeting",
                "Please attend Friday.",
                "Accra Academy — Parents",
                List.of("WhatsApp", "SMS"),
                "2026-11-24",
                "09:30",
                null,
                null,
                "test-captcha-token");

        service.sendApprovalRequest(approval, "papi");

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(requestCaptor.capture());
        SendEmailRequest sentEmail = requestCaptor.getValue();
        assertEquals("verified-sender@example.com", sentEmail.fromEmailAddress());
        assertEquals(List.of("solopappy76@gmail.com"), sentEmail.destination().toAddresses());
        assertTrue(sentEmail.content().simple().subject().data().contains("Parent meeting"));
        assertTrue(sentEmail.content().simple().body().text().data().contains("Please attend Friday."));
        assertTrue(sentEmail.content().simple().body().text().data().contains("Submitted by: papi"));
    }

    @Test
    void removesLineBreaksFromTheEmailSubject() {
        SesV2Client sesClient = mock(SesV2Client.class);
        ApprovalEmailService service = new ApprovalEmailService(sesClient, "verified-sender@example.com");
        ApprovalRequest approval = new ApprovalRequest(
                "Meeting\r\nInjected header",
                "Please attend Friday.",
                "Parents",
                List.of("Email"),
                "2026-11-24",
                "09:30",
                null,
                null,
                "test-captcha-token");

        service.sendApprovalRequest(approval, "papi");

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(requestCaptor.capture());
        assertFalse(requestCaptor.getValue().content().simple().subject().data().contains("\r"));
        assertFalse(requestCaptor.getValue().content().simple().subject().data().contains("\n"));
    }
}
