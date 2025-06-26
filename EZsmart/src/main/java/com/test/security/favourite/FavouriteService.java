package com.test.security.favourite;

import com.test.security.user.User;
import com.test.security.product.Product;
import com.test.security.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.List;

@Service
public class FavouriteService {
    private final FavouriteRepository favouriteRepository;
    private final ProductRepository productRepository;

    public FavouriteService(FavouriteRepository favouriteRepository , ProductRepository productRepository) {

        this.favouriteRepository = favouriteRepository;
        this.productRepository = productRepository;

    }

    public List<Product> getFavouritesByUser(User user) {
         List<Favourite> favourites = favouriteRepository.findByUser(user);
         return favourites.stream()
                .map(Favourite::getProduct)
                .collect(Collectors.toList());
    }

    public Favourite addFavourite(User user, Long productId) {
        Favourite favourite = Favourite.builder()
                .user(user)
                .product(productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found")))
                .build();
        return favouriteRepository.save(favourite);
    }

    public void deleteFavourite(User user, Long productId) {
        favouriteRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    public boolean isFavourite(User user, Long productId) {
        return favouriteRepository.existsByUserIdAndProductId(user.getId(), productId);
    }
}
