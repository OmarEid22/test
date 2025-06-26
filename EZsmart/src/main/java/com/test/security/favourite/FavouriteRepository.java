package com.test.security.favourite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.security.user.User;

import java.util.List;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    boolean existsByUserIdAndProductId(Integer userId, Long productId);
    void deleteByUserIdAndProductId(Integer userId, Long productId);
    List<Favourite> findByUser(User user);
}
