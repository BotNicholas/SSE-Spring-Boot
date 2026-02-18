package org.botnicholas.projects.serversentevents.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.botnicholas.projects.serversentevents.config.properties.SseProperties;
import org.botnicholas.projects.serversentevents.controller.dto.OtpValidationRequestDto;
import org.botnicholas.projects.serversentevents.services.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/otp")
public class OtpController {
    private final OtpService otpService;

    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Void> generateOtp() {
        var otp = otpService.generateNewOtp();
        log.info("Generated new OTP: {}", otp.getOtp());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/stream")
    public ResponseEntity<SseEmitter> getStreamForCurrentOtp() {
        return ResponseEntity.ok(otpService.getCurrentStream());
    }

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> validateOtp(@RequestParam("otp") String otp) {
        otpService.validateOtp(otp);
        return ResponseEntity.ok().build();
    }
}

