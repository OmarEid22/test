package com.test.security.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class RegisterRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private List<Map<String, Object>> addresses;
    private String mobile;

}
