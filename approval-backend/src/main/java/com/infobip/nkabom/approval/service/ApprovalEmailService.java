package com.infobip.nkabom.approval.service;

import com.infobip.nkabom.approval.api.ApprovalRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

@Service
public class ApprovalEmailService {

    static final String APPROVER_EMAIL = "solopappy76@gmail.com";

    private final SesV2Client sesClient;
    private final String fromEmail;

    public ApprovalEmailService(
            SesV2Client sesClient,
            @Value("${approval.from-email}") String fromEmail) {
        this.sesClient = sesClient;
        this.fromEmail = fromEmail;
    }

    public void sendApprovalRequest(ApprovalRequest request, String submitter) {
        String subject = "Message approval requested: " + request.campaignName().replaceAll("[\\r\\n]+", " ").trim();
        String body = """
                A message is waiting for your approval.

                Submitted by: %s
                Campaign: %s
                Audience: %s
                Channels: %s
                Scheduled send: %s at %s
                Direct recipient email: %s
                Direct recipient phone: %s

                Message:
                %s
                """.formatted(
                submitter,
                request.campaignName(),
                request.audience(),
                String.join(", ", request.channels()),
                request.sendDate(),
                request.sendTime(),
                blankAsNone(request.recipientEmail()),
                blankAsNone(request.recipientPhone()),
                request.message());

        SendEmailRequest email = SendEmailRequest.builder()
                .fromEmailAddress(fromEmail)
                .destination(Destination.builder().toAddresses(APPROVER_EMAIL).build())
                .content(EmailContent.builder()
                        .simple(Message.builder()
                                .subject(textContent(subject))
                                .body(Body.builder().text(textContent(body)).build())
                                .build())
                        .build())
                .build();
        sesClient.sendEmail(email);
    }

    private static Content textContent(String text) {
        return Content.builder().data(text).charset("UTF-8").build();
    }

    private static String blankAsNone(String value) {
        return value == null || value.isBlank() ? "None" : value;
    }
}
