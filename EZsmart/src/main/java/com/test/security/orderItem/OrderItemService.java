package com.test.security.orderItem;

import com.test.security.order.Order;
import com.test.security.order.OrderRepository;
import com.test.security.order.OrderService;
import com.test.security.order.OrderStatus;
import com.test.security.user.User;
import com.test.security.user.UserService;
import com.test.security.payment.Payment;
import com.test.security.payment.PaymentService;
import com.test.security.product.Product;
import com.test.security.product.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userservice;

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
                .status(OrderItemStatus.PROCESSING)
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
        orderService.updateOrderStatusIfAllItemsMatch(orderItem.getOrder().getId(), status);
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

    public List<OrderItemDTO> getPaidOrderItemsBySellerId(int sellerId) {
        List<OrderItemDTO> orderItems = getOrderItemsBySellerId(sellerId);
        orderItems = orderItems.stream()
                .filter(item -> orderService.getOrderById(item.getOrderId()).get().getStatus() == OrderStatus.PAID)
                .collect(Collectors.toList());
        return orderItems;
    }

    public Map<String, Double> getMonthlySalesBySellerId(int sellerId) {
        List<OrderItemDTO> orderItems = getPaidOrderItemsBySellerId(sellerId);
        return orderItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getCreatedAt().getMonth().name(),
                        Collectors.summingDouble(OrderItemDTO::getSubtotal)
                ));
    }

    public List<SellerPaymentsDTO> getLastPaymentsForSeller(int sellerId) {
        List<OrderItemDTO> orderItems = getPaidOrderItemsBySellerId(sellerId);
        return orderItems.stream()
                .map(item -> {
                    Payment payment = paymentService.getPaymentByOrderId(item.getOrderId()).get();
                    return new SellerPaymentsDTO(item.getSubtotal() , payment.getPaymentDate(), userservice.getUserById(item.getUserId()).get());
                })
                .collect(Collectors.toList());
    }


} 