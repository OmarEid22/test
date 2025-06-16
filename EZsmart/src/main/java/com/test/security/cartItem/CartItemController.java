package com.test.security.cartItem;

import com.test.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getUserCartItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartItemService.getCartItemsByUser(user));
    }

    @PostMapping
    public ResponseEntity<CartItemDTO> addCartItem(
            @AuthenticationPrincipal User user,
            @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartItemService.addItemToCart(user, request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemDTO> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartItemService.updateCartItem(user, cartItemId, request.getQuantity()));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Boolean> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartItemService.removeCartItem(user, cartItemId));
    }

    @DeleteMapping("/clearAll")
    public ResponseEntity<Boolean> clearCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartItemService.clearCart(user));
    }
} 