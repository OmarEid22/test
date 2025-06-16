package com.test.security.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        List<CouponDTO> coupons = couponService.getAllCoupons().stream()
                .map(CouponDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<CouponDTO>> getActiveCoupons() {
        List<CouponDTO> coupons = couponService.getActiveCoupons().stream()
                .map(CouponDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CouponDTO> getCouponById(@PathVariable Long id) {
        return couponService.getCouponById(id)
                .map(CouponDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CouponDTO> getCouponByCode(@PathVariable String code) {
        return couponService.getCouponByCode(code)
                .map(CouponDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CouponDTO> createCoupon(@RequestBody CouponRequest couponRequest) {
        Coupon coupon = Coupon.builder()
                .code(couponRequest.getCode())
                .startDate(couponRequest.getStartDate())
                .endDate(couponRequest.getEndDate())
                .minOrderValue(couponRequest.getMinOrderValue())
                .discountPercentage(couponRequest.getDiscountPercentage())
                .isActive(couponRequest.getIsActive() != null ? couponRequest.getIsActive() : true)
                .build();
        
        Coupon savedCoupon = couponService.createCoupon(coupon);
        return ResponseEntity.ok(new CouponDTO(savedCoupon));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CouponDTO> updateCoupon(
            @PathVariable Long id,
            @RequestBody CouponRequest couponRequest) {
        
        Coupon couponDetails = Coupon.builder()
                .code(couponRequest.getCode())
                .startDate(couponRequest.getStartDate())
                .endDate(couponRequest.getEndDate())
                .minOrderValue(couponRequest.getMinOrderValue())
                .discountPercentage(couponRequest.getDiscountPercentage())
                .isActive(couponRequest.getIsActive())
                .build();
        
        return couponService.updateCoupon(id, couponDetails)
                .map(CouponDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CouponDTO> updateCouponStatus(
            @PathVariable Long id,
            @RequestBody Boolean active) {
        
        return couponService.updateCouponStatus(id, active)
                .map(CouponDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @RequestBody CouponValidationRequest request) {
        
        boolean isApplicable = couponService.isCouponApplicable(request.getCode(), request.getOrderTotal());
        
        if (isApplicable) {
            double discountAmount = couponService.calculateDiscount(request.getCode(), request.getOrderTotal());
            return ResponseEntity.ok(
                    new CouponValidationResponse(true, discountAmount, "Coupon is valid and applied")
            );
        } else {
            return ResponseEntity.ok(
                    new CouponValidationResponse(false, 0.0, "Coupon is not applicable")
            );
        }
    }
    
    // A simple inner class for coupon validation responses
    @Data
    @AllArgsConstructor
    public static class CouponValidationResponse {
        private boolean valid;
        private double discountAmount;
        private String message;
    }
} 