package com.test.security.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {
    private String code;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double minOrderValue;
    private Integer discountPercentage;
    private Boolean isActive;
}