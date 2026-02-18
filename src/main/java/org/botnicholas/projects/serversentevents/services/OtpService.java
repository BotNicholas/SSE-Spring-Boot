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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final SseProperties sseProperties;
    private final OtpProperties otpProperties;
    private static AtomicReference<CreateOtpDto> OTP = new AtomicReference<>();

    @SneakyThrows
    public CreateOtpDto generateNewOtp() {
        var oldOtp = OTP.getAndSet(null);
        if (oldOtp != null) {
            oldOtp.getExpiry().cancel(true);
            log.info("Closing the emitter for OTP {}", oldOtp.getOtp());
            oldOtp.getEmitter().complete();
        }

        var random = new SecureRandom();
        var number = random.nextInt(1000000);
        var otp = String.format("%06d", number);
        var emitter = new SseEmitter(sseProperties.getTimeout());
        var newOtp = CreateOtpDto.builder()
                .otp(otp)
                .emitter(emitter)
                .build();

        var expiry = scheduler.schedule(() -> {
            try {
                //Here we don't have to access the global OTP, because we could send the expiry for the last one OTP. We should use the one we were creating the expiry job for.
                log.info("Sending EXPIRED event for OTP {}", otp);
                emitter.send(SseEmitter.event().name("expired").data("Expired"));
            } catch (IOException ex) {
                log.error("Expiry job for OTP {} canceled", otp);
            } finally {
                emitter.complete();
                OTP.compareAndSet(newOtp, null); //if current OTP value is the one we were setting on this operation set it to null
            }
        }, otpProperties.getTtl(), TimeUnit.SECONDS);

        newOtp.setExpiry(expiry);

        OTP.set(newOtp);

        return newOtp;
    }

    public SseEmitter getCurrentStream() {
        var otp = OTP.get();
        if(otp != null) {
            return otp.getEmitter();
        }
        return null;
    }

    @SneakyThrows
    public boolean validateOtp(final String otp) {
        var currentOtp = OTP.get();

        log.info("Validating OTP {}", otp);
        if (currentOtp != null) {
            if (currentOtp.getOtp().equals(otp)) {
                log.info("Sending VALIDATED event for OTP {}", currentOtp.getOtp());
                currentOtp.getExpiry().cancel(true);
                currentOtp.getEmitter().send(SseEmitter.event().name("valid").data("OTP Valid"));
                currentOtp.getEmitter().complete();
                OTP.compareAndSet(currentOtp, null);
                return true;
            } else {
                log.info("Sending INVALID event for OTP {}", currentOtp.getOtp());
                currentOtp.getEmitter().send(SseEmitter.event().name("invalid").data("OTP Invalid"));
            }
        }
        return false;
    }
}
/**
 * Main rules during multitreading - use Tread save variables (atomic ones, for instance)
 * When you schedule a job that will do something with atomic variable prefer to work with it's copy and not the "global" value, because variable might change and this logic will be applied to the latest variable.
 * Prefer using compare and set methods to set something.
 * Get the value of atomic at the beginning, so during the processing you won't work with/change the new value.
 */
