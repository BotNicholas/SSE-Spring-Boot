package org.botnicholas.projects.serversentevents.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.botnicholas.projects.serversentevents.config.properties.OtpProperties;
import org.botnicholas.projects.serversentevents.config.properties.SseProperties;
import org.botnicholas.projects.serversentevents.services.dto.CreateOtpDto;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SseProperties sseProperties;
    private final OtpProperties otpProperties;
    private static CreateOtpDto OTP;

    @SneakyThrows
    public CreateOtpDto generateNewOtp() {
        if (OTP != null) {
            log.info("Closing the emitter for OTP {}", OTP.getOtp());
            OTP.getEmitter().complete();
            OTP.getExpiry().cancel(true);
        }

        var random = new SecureRandom();
        var number = random.nextInt(100000);

        var expiry = executor.submit(() -> {
            try {
                log.info("Starting new expiry job");
                Thread.sleep(otpProperties.getTtl());
                log.info("Sending EXPIRED event for OTP {}", OTP.getOtp());
                OTP.getEmitter().send(SseEmitter.event().name("expired").data("Expired"));
                OTP.getEmitter().complete();
                OTP = null;
            } catch (InterruptedException | IOException ex) {
                log.error("Expiry job for OTP {} canceled", OTP.getOtp());
            }
        });

        OTP = CreateOtpDto.builder()
                .otp(String.format("%06d", number))
                .emitter(new SseEmitter(sseProperties.getTimeout()))
                .expiry(expiry)
                .build();

        return OTP;
    }

    public SseEmitter getCurrentStream() {
        if(OTP != null) {
            return OTP.getEmitter();
        }
        return null;
    }

    @SneakyThrows
    public boolean validateOtp(final String otp) {
        log.info("Validating OTP {}", otp);
        if (OTP != null) {
            if (OTP.getOtp().equals(otp)) {
                log.info("Sending VALIDATED event for OTP {}", OTP.getOtp());
                OTP.getEmitter().send(SseEmitter.event().name("valid").data("OTP Valid"));
                OTP.getEmitter().complete();
                OTP.getExpiry().cancel(true);
                OTP = null;
                return true;
            } else {
                log.info("Sending INVALID event for OTP {}", OTP.getOtp());
                OTP.getEmitter().send(SseEmitter.event().name("invalid").data("OTP Invalid"));
            }
        }

        return false;
    }
}
