package com.test.security.orderItem;

import com.test.security.order.Order;
import com.test.security.order.OrderRepository;
import com.test.security.product.Product;
import com.test.security.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderItemDTO createOrderItem(OrderItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .subtotal(product.getPrice() * request.getQuantity())
                .status(OrderItemStatus.PENDING)
                .build();

        return new OrderItemDTO(orderItemRepository.save(orderItem));
    }

    public List<OrderItemDTO> getOrderItemsByOrder(Order order) {
        return orderItemRepository.findByOrder(order)
                .stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
    }

    public List<OrderItemDTO> getOrderItemsBySellerId(int sellerId) {
        return orderItemRepository.findBySellerId(sellerId)
                .stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderItemDTO updateOrderItemStatus(Long orderItemId, OrderItemStatus status) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found"));
        
        orderItem.setStatus(status);
        return new OrderItemDTO(orderItemRepository.save(orderItem));
    }

    public void deleteOrderItem(Long orderItemId) {
        orderItemRepository.deleteById(orderItemId);
    }

    public OrderItemDTO getOrderItemById(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("Order item not found"));
        return new OrderItemDTO(orderItem);
    }

    public SellerOrderStatsDTO getSellerOrderStats(Long sellerId) {
        return orderItemRepository.getSellerOrderStats(sellerId)
                .orElse(new SellerOrderStatsDTO(0L, 0L, 0.0, sellerId, null));
    }
} 