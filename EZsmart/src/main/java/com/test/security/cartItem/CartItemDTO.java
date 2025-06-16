package com.test.security.cartItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.test.security.user.User;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Double price;
    private Integer quantity;
    private LocalDateTime createdAt;
    private User user;
    
    public CartItemDTO(CartItem cartItem) {
        this.id = cartItem.getId();
        this.productId = cartItem.getProduct().getId();
        this.productName = cartItem.getProduct().getName();
        this.productImage = cartItem.getProduct().getImage();
        this.price = cartItem.getProduct().getPrice();
        this.quantity = cartItem.getQuantity();
        this.createdAt = cartItem.getCreatedAt();
        this.user = cartItem.getUser();
    }
} 