package com.test.security.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.test.security.order.Order;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by Stripe session ID (for idempotency)
    Optional<Payment> findByStripeSessionId(String stripeSessionId);

    // Find payment by Stripe event ID (for duplicate event prevention)
    Optional<Payment> findByStripeEventId(String stripeEventId);

    // Find payment by order ID
    Optional<Payment> findByOrderId(Long orderId);

    // Check if payment exists for a specific order and status
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.paymentStatus = :status")
    Optional<Payment> findByOrderIdAndPaymentStatus(@Param("orderId") Long orderId, @Param("status") PaymentStatus status);

    // Find all payments for a specific order
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.createdAt DESC")
    java.util.List<Payment> findAllByOrderId(@Param("orderId") Long orderId);

    boolean existsByOrder(Order order);
}
