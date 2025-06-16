package com.test.security.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long id;
    private Integer userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double totalAmount;
    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();
    
    public CartDTO(Cart cart) {
        this.id = cart.getId();
        this.userId = cart.getUser().getId();
        this.createdAt = cart.getCreatedAt();
        this.updatedAt = cart.getUpdatedAt();
        
        if (cart.getCartItems() != null) {
            this.items = cart.getCartItems().stream()
                .map(CartItemDTO::new)
                .collect(Collectors.toList());
        }
        
        this.totalAmount = this.items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
} 