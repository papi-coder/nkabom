package com.infobip.nkabom.approval.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ApprovalRequest(
        @NotBlank @Size(max = 120) String campaignName,
        @NotBlank @Size(max = 320) String message,
        @NotBlank @Size(max = 120) String audience,
        @NotEmpty @Size(max = 3) List<@Pattern(regexp = "WhatsApp|SMS|Email") String> channels,
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String sendDate,
        @NotBlank @Pattern(regexp = "\\d{2}:\\d{2}") String sendTime,
        @Email @Size(max = 254) String recipientEmail,
        @Size(max = 30) String recipientPhone,
        @NotBlank @Size(max = 2048) String captchaToken) {
}
