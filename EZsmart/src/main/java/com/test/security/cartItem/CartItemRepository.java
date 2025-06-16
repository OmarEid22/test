package com.test.security.cartItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<Optional<CartItem>> findByUserId(Integer userId);
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Long productId);
    void deleteByUserId(Integer userId);
}
