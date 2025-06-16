package com.test.security.cartItem;

import com.test.security.product.Product;
import com.test.security.product.ProductRepository;
import com.test.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public List<CartItemDTO> getCartItemsByUser(User user) {
        List<Optional<CartItem>> CartItems = cartItemRepository.findByUserId(user.getId());

        return CartItems.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CartItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemDTO addItemToCart(User user, Long productId, Integer quantity) {

        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(user.getId(), productId);

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            return new CartItemDTO(item);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newItem);
            return new CartItemDTO(newItem);
        }
    }

    @Transactional
    public CartItemDTO updateCartItem(User user, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            // Remove item if quantity is 0 or negative
            cartItemRepository.delete(cartItem);
        } else {
            // Update quantity
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
        return new CartItemDTO(cartItem);
    }

    @Transactional
    public Boolean removeCartItem(User user, Long cartItemId) {


        // Find the cart item
        CartItem itemToRemove = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItemRepository.delete(itemToRemove);
        return true;
    }

    @Transactional
    public Boolean clearCart(User user) {
        cartItemRepository.deleteByUserId(user.getId());
        return true;
    }
} 