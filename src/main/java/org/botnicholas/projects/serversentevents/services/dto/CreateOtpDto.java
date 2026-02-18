package org.botnicholas.projects.serversentevents.services.dto;

import lombok.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Future;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreateOtpDto {
    private String otp;
    private SseEmitter emitter;
    private Future<?> expiry;
}
