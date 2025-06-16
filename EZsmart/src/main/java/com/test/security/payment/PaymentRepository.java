package com.test.security.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.test.security.order.Order;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrder(Order order);
}

