package com.test.security.coupon;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Double minOrderValue;
    
    private Integer discountPercentage;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private String description;
    
    // Method to check if coupon is valid based on date
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               now.isAfter(startDate) && 
               now.isBefore(endDate);
    }
    
    // Method to check if coupon is applicable for a given order total
    public boolean isApplicable(Double orderTotal) {
        return isValid() && orderTotal >= minOrderValue;
    }
    
    // Method to calculate discount amount
    public Double calculateDiscount(Double orderTotal) {
        if (!isApplicable(orderTotal)) {
            return 0.0;
        }
        return (orderTotal * discountPercentage) / 100.0;
    }
} 