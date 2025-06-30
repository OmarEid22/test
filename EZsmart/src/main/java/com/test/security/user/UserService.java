package com.test.security.user;

import com.test.security.seller.Seller;
import com.test.security.seller.SellerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    UserService(UserRepository userRepository , SellerRepository sellerRepository) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    //update user role and seller_id
    @Transactional
    public void updateUserRoleAndSeller(User user, Role role, Seller seller) {
        Seller existingSeller = sellerRepository.findById(seller.getId())
                .orElseThrow(() -> new IllegalArgumentException("Seller does not exist"));

        user.setRole(role);
        user.setSeller(existingSeller);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }
}
