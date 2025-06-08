package com.test.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.test.security.otp.OtpRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

     @PostMapping("/authenticate")
     public ResponseEntity<AuthenticationResponse> authenticate(
             @RequestBody authenticationRequest request
     ) {
         return ResponseEntity.ok(service.authenticate(request));
     }

    @PostMapping("/request-otp")
    public ResponseEntity<Void> requestOtp(@RequestBody OtpRequest request) {
        service.requestOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verifyOtp(@RequestBody OtpRequest request) {
        return ResponseEntity.ok(service.verifyOtpAndAuthenticate(request));
    }
}