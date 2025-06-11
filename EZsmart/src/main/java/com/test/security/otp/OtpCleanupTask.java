package com.test.security.otp;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OtpCleanupTask {
    private final OtpTokenRepository otpTokenRepository;

    @Scheduled(fixedRate = 3600000) // every hour
    public void cleanupExpiredOtps() {
        otpTokenRepository.deleteAll(
            otpTokenRepository.findAll().stream()
                .filter(token -> token.getExpiryTime().isBefore(LocalDateTime.now()))
                .toList()
        );
    }
}
