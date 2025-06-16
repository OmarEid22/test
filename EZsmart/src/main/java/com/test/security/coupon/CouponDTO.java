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
public class CouponDTO {
    private Long id;
    private String code;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double minOrderValue;
    private Integer discountPercentage;
    private Boolean isActive;
    private String description;
    private Boolean isCurrentlyValid;
    
    public CouponDTO(Coupon coupon) {
        this.id = coupon.getId();
        this.code = coupon.getCode();
        this.startDate = coupon.getStartDate();
        this.endDate = coupon.getEndDate();
        this.minOrderValue = coupon.getMinOrderValue();
        this.discountPercentage = coupon.getDiscountPercentage();
        this.isActive = coupon.getIsActive();
        this.description = coupon.getDescription();
        this.isCurrentlyValid = coupon.isValid();
    }
} 