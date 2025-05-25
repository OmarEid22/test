package com.test.security.cart;

import com.test.security.product.Product;
import com.test.security.product.ProductRepository;
import com.test.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartDTO getCartByUser(User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });

        return new CartDTO(cart);
    }

    @Transactional
    public CartDTO addItemToCart(User user, Long productId, Integer quantity) {
        // Get or create user's cart
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });

        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getCartItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart updatedCart = cartRepository.save(cart);
        
        return new CartDTO(updatedCart);
    }

    @Transactional
    public CartDTO updateCartItem(User user, Long cartItemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Find the cart item
        CartItem itemToUpdate = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            cart.getCartItems().remove(itemToUpdate);
        } else {
            // Update quantity
            itemToUpdate.setQuantity(quantity);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart updatedCart = cartRepository.save(cart);
        
        return new CartDTO(updatedCart);
    }

    @Transactional
    public CartDTO removeItemFromCart(User user, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Find the cart item
        CartItem itemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cart.getCartItems().remove(itemToRemove);
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart updatedCart = cartRepository.save(cart);
        return new CartDTO(updatedCart);
    }

    @Transactional
    public CartDTO clearCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        
        Cart updatedCart = cartRepository.save(cart);
        return new CartDTO(updatedCart);
    }
} 