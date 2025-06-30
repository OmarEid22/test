package com.test.security.otp;

import com.test.security.auth.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    @Transactional
    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        otpTokenRepository.deleteByEmail(email);

        OtpToken token = OtpToken.builder()
                .email(email)
                .otp(otp)
                .expiryTime(expiry)
                .build();
        otpTokenRepository.save(token);

        emailService.sendOtpEmail(email, otp);
        return otp;
    }
    

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        return otpTokenRepository.findByEmail(email)
                .map(token -> {
                    if (token.getOtp().equals(otp) && !token.getExpiryTime().isBefore(LocalDateTime.now())) {
                        otpTokenRepository.deleteByEmail(email);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public boolean isOtpExpired(String email) {
        return otpTokenRepository.findByEmail(email)
                .map(token -> token.getExpiryTime().isBefore(LocalDateTime.now()))
                .orElse(true);
    }
}
