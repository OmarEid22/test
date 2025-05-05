package com.test.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // admin >>> all sellers
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllSellers() {
        return userService.getAllSellers();
    }

    // anyone >>> seller by ID
    @GetMapping("/{sellerId}")
    public Optional<UserDto> getSellerById(@PathVariable Integer sellerId) {
        return userService.getSellerById(sellerId);
    }
}
