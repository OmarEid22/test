package com.test.security.orderItem;

import com.test.security.order.Order;
import com.test.security.product.Product;
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
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private Integer quantity;

    private Double unitPrice;

    private Double subtotal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    //order_item_status
    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    @PrePersist
    protected void onCreate() {
        this.status = OrderItemStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.unitPrice = this.product.getPrice();
        this.subtotal = this.unitPrice * this.quantity;
    }
} 