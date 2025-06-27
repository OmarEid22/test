package com.test.security.orderStatusHistory;

import com.test.security.order.Order;
import com.test.security.order.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_status_history")
public class OrderStatusHistory {
    //id , order_id , status , timestamp

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    private OrderStatus status;
    private LocalDateTime statusDate;

    @PrePersist
    protected void onCreate() {
        this.statusDate = LocalDateTime.now();
    }
}
