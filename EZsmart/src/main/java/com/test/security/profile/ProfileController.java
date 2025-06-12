package com.test.security.profile;

import com.test.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileDTO> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfileByUser(user));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ProfileDTO> getProfileByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<ProfileDTO> createProfile(
            @AuthenticationPrincipal User user,
            @RequestBody Profile profile) {
        return ResponseEntity.ok(profileService.createProfile(user, profile));
    }

    @PutMapping("/update")
    public ResponseEntity<ProfileDTO> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody Profile profile) {
        return ResponseEntity.ok(profileService.updateProfile(user, profile));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyProfile(@AuthenticationPrincipal User user) {
        profileService.deleteProfile(user);
        return ResponseEntity.ok().build();
    }
} 