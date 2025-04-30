package com.test.security.auth;
import  com.test.security.user.User;
import com.test.security.config.JwtService;
import com.test.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
//        Role role = request.getRole() != null
//                ? Role.valueOf(request.getRole().toUpperCase())
//                : Role.USER;

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
//                .role(role)
                .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user, Collections.singleton(user.getRole()));
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .roles(Collections.singleton(user.getRole()))
                .build();
    }

    public AuthenticationResponse authenticate(authenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user, Collections.singleton(user.getRole()));
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .roles(Collections.singleton(user.getRole()))
                .build();
    }
}
