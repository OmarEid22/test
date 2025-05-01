package com.test.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // admin >>> all sellers
    public List<UserDto> getAllSellers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.SELLER)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // anyone >>> seller by ID
    public Optional<UserDto> getSellerById(Integer sellerId) {
        return userRepository.findById(sellerId)
                .filter(user -> user.getRole() == Role.SELLER)
                .map(this::mapToDto);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail()
        );
    }
}
