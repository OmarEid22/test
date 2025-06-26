package com.test.security.favourite;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import com.test.security.user.User;
import com.test.security.product.Product;
import java.util.List;


@RestController
@RequestMapping("/api/favourites")
public class FavouriteController {
    private final FavouriteService favouriteService;

    @Autowired
    public FavouriteController(FavouriteService favouriteService) {
        this.favouriteService = favouriteService;
    }

    @GetMapping
    public List<Product> getFavourites(@AuthenticationPrincipal User user) {
        return favouriteService.getFavouritesByUser(user);
    }

    @PostMapping
    public Favourite addFavourite(@AuthenticationPrincipal User user, @RequestBody Long productId) {
        return favouriteService.addFavourite(user, productId);
    }

    @DeleteMapping("/{id}")
    public void deleteFavourite(@AuthenticationPrincipal User user, @RequestBody Long productId) {
        favouriteService.deleteFavourite(user, productId);
    }

    @GetMapping("/check")
    public boolean isFavourite(@AuthenticationPrincipal User user, @RequestParam Long productId) {
        return favouriteService.isFavourite(user, productId);
    }
}
