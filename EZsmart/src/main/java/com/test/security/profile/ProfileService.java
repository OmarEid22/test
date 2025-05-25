package com.test.security.profile;

import com.test.security.user.User;
import com.test.security.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileDTO getProfileByUser(User user) {
        Optional<User> existingUser = userRepository.findById(user.getId());
        if(existingUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        Optional<Profile> existingProfile = profileRepository.findByUser(user);
        if(existingProfile.isPresent()) {
            return ProfileDTO.fromProfile(existingProfile.get());
        }

        Profile profile = Profile.builder()
                .user(user)
                .build();
        Profile savedProfile = profileRepository.save(profile);
        return ProfileDTO.fromProfile(savedProfile);
    }

    public ProfileDTO getProfileByUserId(Integer userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        if(existingUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        Optional<Profile> existingProfile = profileRepository.findByUserId(userId);
        if(existingProfile.isPresent()) {
            return ProfileDTO.fromProfile(existingProfile.get());
        }

        Profile profile = Profile.builder()
                .user(existingUser.get())
                .build();
        Profile savedProfile = profileRepository.save(profile);
        return ProfileDTO.fromProfile(savedProfile);
    }

    @Transactional
    public ProfileDTO createProfile(User user, Profile profile) {
        if (profileRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException("Profile already exists for user: " + user.getId());
        }
        profile.setUser(user);
        Profile savedProfile = profileRepository.save(profile);
        return ProfileDTO.fromProfile(savedProfile);
    }

    @Transactional
    public ProfileDTO updateProfile(User user, Profile updatedProfile) {
        Optional<User> existingUser = userRepository.findById(user.getId());
        if(existingUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        Profile existingProfile = profileRepository.findByUser(user)
                .orElseGet(() -> {
                    Profile newProfile = Profile.builder()
                            .user(existingUser.get())
                            .build();
                    return profileRepository.save(newProfile);
                });

        // Update only the image
        existingProfile.setImage(updatedProfile.getImage());
        Profile savedProfile = profileRepository.save(existingProfile);
        return ProfileDTO.fromProfile(savedProfile);
    }

    @Transactional
    public void deleteProfile(User user) {
        Optional<User> existingUser = userRepository.findById(user.getId());
        if(existingUser.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));
        profileRepository.delete(profile);
    }
} 