package com.test.security.order;

import com.test.security.orderItem.OrderItem;
import com.test.security.orderItem.OrderItemDTO;
import com.test.security.orderItem.OrderItemRequest;
import com.test.security.coupon.CouponService;
import com.test.security.orderItem.OrderItemStatus;
import com.test.security.orderItem.OrderItemRepository;
import com.test.security.orderStatusHistory.OrderStatusHistory;
import com.test.security.orderStatusHistory.OrderStatusHistoryService;
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
    private final CouponService couponService;
    private final OrderItemRepository orderItemsRepository;
    private final OrderStatusHistoryService orderStatusHistoryService;

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
                .status(OrderStatus.PLACED)
                .shippingAddress(orderRequest.getShippingAddress())
                .paymentMethod(orderRequest.getPaymentMethod())
                .couponCode(orderRequest.getCouponCode())
                .originalAmount(orderRequest.getOriginalAmount())
                .discountAmount(orderRequest.getDiscountAmount())
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

//        // Apply coupon if provided
//        if (orderRequest.getCouponCode() != null) {
//            double discountAmount = couponService.calculateDiscount(orderRequest.getCouponCode(), totalAmount);
//            order.setTotalAmount(totalAmount - discountAmount);
//        }

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
            OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
            orderStatusHistory.setOrder(updatedOrder);
            orderStatusHistory.setStatus(status);
            orderStatusHistoryService.saveOrderStatusHistory(orderStatusHistory);
            return Optional.of(new OrderDTO(updatedOrder));
        }
        return Optional.empty();
    }

    public void updateOrderStatusIfAllItemsMatch(Long orderId, OrderItemStatus targetStatus) {
        List<OrderItem> items = orderItemsRepository.findByOrder(orderRepository.findById(orderId).get());
        if (items.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        boolean allMatch = items.stream()
                .allMatch(item -> item.getStatus() == targetStatus);

        if (allMatch) {
            OrderStatus status = mapOrderItemStatusToOrderStatus(targetStatus);
            updateOrderStatus(orderId, status);
        }
    }

   // Map orderItemStatus to OrderStatus
    private OrderStatus mapOrderItemStatusToOrderStatus(OrderItemStatus status) {
        switch (status) {
            case PACKED:
                return OrderStatus.PACKED;
            case SHIPPED:
                return OrderStatus.SHIPPED;
            case DELIVERED:
                return OrderStatus.DELIVERED;
            case CANCELLED:
                return OrderStatus.CANCELLED;
            default:
                return OrderStatus.PLACED;
        }
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
} 