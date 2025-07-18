package com.test.security.orderItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
    private Long orderId;
}
