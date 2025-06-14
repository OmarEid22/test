package com.test.security.order;

import com.test.security.orderItem.OrderItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Integer userId;
    private String userEmail;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private String paymentMethod;
    @Builder.Default
    private List<OrderItemDTO> orderItems = new ArrayList<>();
    
    public OrderDTO(Order order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.userEmail = order.getUser().getEmail();
        this.orderDate = order.getOrderDate();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.shippingAddress = order.getShippingAddress();
        this.paymentMethod = order.getPaymentMethod();


        if (order.getOrderItems() != null) {
            this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDTO::new)
                .toList();
        }
    }
} 