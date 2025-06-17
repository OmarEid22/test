package com.test.security.cartItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.test.security.user.User;
import com.test.security.product.Product;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Product product;
    private Integer quantity;
    private LocalDateTime createdAt;
    private User user;
    
    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.product = cartItem.getProduct();
        this.quantity = cartItem.getQuantity();
        this.createdAt = cartItem.getCreatedAt();
        this.user = cartItem.getUser();
    }
} 