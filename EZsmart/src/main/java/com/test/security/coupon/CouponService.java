package com.test.security.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }
    
    public List<Coupon> getActiveCoupons() {
        return couponRepository.findByIsActive(true);
    }
    
    public List<Coupon> getValidCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByEndDateAfterAndStartDateBeforeAndIsActiveTrue(now, now);
    }
    
    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }
    
    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCode(code);
    }
    
    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        // Validation logic could go here
        return couponRepository.save(coupon);
    }
    
    @Transactional
    public Optional<Coupon> updateCoupon(Long id, Coupon couponDetails) {
        return couponRepository.findById(id)
                .map(existingCoupon -> {
                    existingCoupon.setCode(couponDetails.getCode());
                    existingCoupon.setStartDate(couponDetails.getStartDate());
                    existingCoupon.setEndDate(couponDetails.getEndDate());
                    existingCoupon.setMinOrderValue(couponDetails.getMinOrderValue());
                    existingCoupon.setDiscountPercentage(couponDetails.getDiscountPercentage());
                    existingCoupon.setIsActive(couponDetails.getIsActive());
                    existingCoupon.setDescription(couponDetails.getDescription());
                    return couponRepository.save(existingCoupon);
                });
    }
    
    @Transactional
    public Optional<Coupon> updateCouponStatus(Long id, Boolean isActive) {
        return couponRepository.findById(id)
                .map(existingCoupon -> {
                    existingCoupon.setIsActive(isActive);
                    return couponRepository.save(existingCoupon);
                });
    }
    
    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }
    
    public Double calculateDiscount(String couponCode, Double orderTotal) {
        return getCouponByCode(couponCode)
                .map(coupon -> coupon.calculateDiscount(orderTotal))
                .orElse(0.0);
    }
    
    public boolean isCouponApplicable(String couponCode, Double orderTotal) {
        return getCouponByCode(couponCode)
                .map(coupon -> coupon.isApplicable(orderTotal))
                .orElse(false);
    }
} 