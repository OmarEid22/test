package com.test.security.orderItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.test.security.product.Product;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Product product;
    private Double subtotal;
    private OrderItemStatus status;
    private LocalDateTime createdAt;
    
    // User information
    private Integer userId;
    private String userEmail;
    private String userName;
    
    public OrderItemDTO(OrderItem orderItem) {
        this.id = orderItem.getId();
        this.orderId = orderItem.getOrder().getId();
        this.product = orderItem.getProduct();
        this.subtotal = orderItem.getSubtotal();
        this.status = orderItem.getStatus();
        this.createdAt = orderItem.getCreatedAt();
        
        // Add user information
        this.userId = orderItem.getOrder().getUser().getId();
        this.userEmail = orderItem.getOrder().getUser().getEmail();
        this.userName = orderItem.getOrder().getUser().getFirstname() + " " + 
                       orderItem.getOrder().getUser().getLastname();
    }
} 