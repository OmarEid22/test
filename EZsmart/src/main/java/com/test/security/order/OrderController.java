package com.test.security.order;

import com.test.security.orderItem.OrderItemDTO;
import com.test.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.test.security.order.OrderRequest;
import com.test.security.order.OrderService;
import com.test.security.order.OrderRepository;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/user")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return orderService.getOrderById(id)
                .map(orderDTO -> {
                    // Allow access if admin or if the order belongs to the authenticated user
                    if (user.getRole().name().equals("ROLE_ADMIN") || orderDTO.getUserId().equals(user.getId())) {
                        return ResponseEntity.ok(orderDTO);
                    } else {
                        return ResponseEntity.status(403).build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderRequest orderRequest, @AuthenticationPrincipal User user) {
        OrderDTO createdOrder = orderService.createOrder(orderRequest, user);
        return ResponseEntity.ok(createdOrder);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusRequest statusRequest) {
        return orderService.updateOrderStatus(id, statusRequest.getStatus())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }

    //cancel order
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@AuthenticationPrincipal User user ,@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if(!order.getUser().equals(user)) {
            throw new RuntimeException("You are not authorized to cancel this order");
        }
        return orderService.updateOrderStatus(id, OrderStatus.CANCELLED)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 