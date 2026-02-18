package org.botnicholas.projects.serversentevents.controller.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OtpValidationRequestDto {
    private String otp;
}
