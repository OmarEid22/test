package com.test.security.order;

import com.test.security.orderItem.OrderItemRequest;
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
    private Double discountAmount;
    private Double originalAmount;
    private String CouponCode;
}

