package com.test.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //get all addresses of a user
    @GetMapping("/addresses")
    public List<Map<String, Object>> getAddresses(@AuthenticationPrincipal User user) {
        return user.getAddresses();
    }

    //user adds address
    @PostMapping("/addresses")
    public List<Map<String, Object>> addAddress(@AuthenticationPrincipal User user, @RequestBody Map<String, Object> address) {
        user.getAddresses().add(address);
        userService.updateUser(user);
        return user.getAddresses();
    }

}
