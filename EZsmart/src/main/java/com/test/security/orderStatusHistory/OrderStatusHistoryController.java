package com.test.security.orderStatusHistory;

import com.test.security.order.Order;
import com.test.security.order.OrderRepository;
import com.test.security.user.Role;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.test.security.user.User;

import java.util.List;

@RestController
@RequestMapping("/api/order-status-history")
public class OrderStatusHistoryController {

    private final OrderStatusHistoryService orderStatusHistoryService;
    private final OrderRepository orderRepository;

    public OrderStatusHistoryController(OrderStatusHistoryService orderStatusHistoryService , OrderRepository orderRepository) {
        this.orderStatusHistoryService = orderStatusHistoryService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public void saveOrderStatusHistory(@RequestBody OrderStatusHistory orderStatusHistory) {
        orderStatusHistoryService.saveOrderStatusHistory(orderStatusHistory);
    }

    //get order status history by order id
    @GetMapping("/{orderId}")
    public List<OrderStatusHistory> getOrderStatusHistoryByOrderId(@PathVariable Long orderId , @AuthenticationPrincipal User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if(!user.getRole().equals(Role.ROLE_ADMIN) && user.getId() != order.getUser().getId()) {
            throw new RuntimeException("You are not authorized to access this resource");
        }
        return orderStatusHistoryService.getOrderStatusHistoryByOrder(order);
    }
}
