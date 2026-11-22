package com.infobip.nkabom.approval.api;

import com.infobip.nkabom.approval.service.ApprovalEmailService;
import com.infobip.nkabom.approval.service.ApprovalRateLimiter;
import com.infobip.nkabom.approval.service.TurnstileVerificationService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalEmailService approvalEmailService;
    private final ApprovalRateLimiter rateLimiter;
    private final TurnstileVerificationService turnstileVerificationService;

    public ApprovalController(
            ApprovalEmailService approvalEmailService,
            ApprovalRateLimiter rateLimiter,
            TurnstileVerificationService turnstileVerificationService) {
        this.approvalEmailService = approvalEmailService;
        this.rateLimiter = rateLimiter;
        this.turnstileVerificationService = turnstileVerificationService;
    }

    @PostMapping
    public ResponseEntity<Void> requestApproval(
            @Valid @RequestBody ApprovalRequest request,
            HttpServletRequest servletRequest) {
        String clientAddress = servletRequest.getRemoteAddr();
        if (!rateLimiter.allowRequest(clientAddress)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many approval requests.");
        }
        turnstileVerificationService.verify(request.captchaToken(), clientAddress);
        approvalEmailService.sendApprovalRequest(request, "Public visitor");
        return ResponseEntity.noContent().build();
    }
}
