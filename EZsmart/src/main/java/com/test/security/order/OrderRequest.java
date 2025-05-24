package com.test.security.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String shippingAddress;
    private String paymentMethod;
    private String couponCode;
    private List<OrderItemRequest> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

class OrderItemRequest {
    private Long productId;
    private Integer quantity;
} 