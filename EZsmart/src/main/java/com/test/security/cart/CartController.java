package com.test.security.cart;

import com.test.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getUserCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCartByUser(user));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(
            @AuthenticationPrincipal User user,
            @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(user, request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(user, cartItemId, request.getQuantity()));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(user, cartItemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartDTO> clearCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.clearCart(user));
    }
} 