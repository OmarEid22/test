package com.test.security.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
    private List<Map<String, Object>> addresses;

}
