package com.test.security.order;

import com.test.security.orderItem.OrderItem;
import com.test.security.orderItem.OrderItemDTO;
import com.test.security.orderItem.OrderItemRequest;
import com.test.security.product.Product;
import com.test.security.product.ProductRepository;

import com.test.security.user.User;
import com.test.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByUserId(Integer userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderDTO::new)
                .collect(Collectors.toList());
    }

    public Optional<OrderDTO> getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(OrderDTO::new);
    }

    @Transactional
    public OrderDTO createOrder(OrderRequest orderRequest, User user) {
        // Validate user
        User orderUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create order
        Order order = Order.builder()
                .user(orderUser)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .shippingAddress(orderRequest.getShippingAddress())
                .paymentMethod(orderRequest.getPaymentMethod())
                .build();
        
        // Calculate total amount and add order items
        double totalAmount = 0.0;
        List<OrderItem> orderItems = orderRequest.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
                    
                    double unitPrice = product.getPrice();
                    double subtotal = unitPrice * item.getQuantity();
                    
                    OrderItem orderItem = OrderItem.builder()
                            .order(order)
                            .product(product)
                            .quantity(item.getQuantity())
                            .unitPrice(unitPrice)
                            .subtotal(subtotal)
                            .build();
                    
                    return orderItem;
                })
                .collect(Collectors.toList());
        
        totalAmount = orderItems.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
        
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);
        return new OrderDTO(savedOrder);
    }

    @Transactional
    public Optional<OrderDTO> updateOrderStatus(Long id, OrderStatus status) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);
            return Optional.of(new OrderDTO(updatedOrder));
        }
        return Optional.empty();
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

} 