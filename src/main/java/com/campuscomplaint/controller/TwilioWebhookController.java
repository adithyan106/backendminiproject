package com.campuscomplaint.controller;

import com.campuscomplaint.service.TwilioWebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/twilio")
public class TwilioWebhookController {

    private final TwilioWebhookService twilioWebhookService;

    public TwilioWebhookController(TwilioWebhookService twilioWebhookService) {
        this.twilioWebhookService = twilioWebhookService;
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receiveSmsReply(
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
            @RequestParam Map<String, String> formParams) {

        if (!twilioWebhookService.isValidRequest(signature, formParams)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(toTwiml("Invalid webhook signature."));
        }

        String fromPhoneNumber = formParams.getOrDefault("From", "");
        String body = formParams.getOrDefault("Body", "");

        String replyMessage = twilioWebhookService.processIncomingMessage(fromPhoneNumber, body);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(toTwiml(replyMessage));
    }

    private String toTwiml(String message) {
        String escaped = message
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Message>" + escaped + "</Message></Response>";
    }
}
