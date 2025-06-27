package com.test.security.orderStatusHistory;

import com.test.security.order.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderStatusHistoryService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;

    public OrderStatusHistoryService(OrderStatusHistoryRepository orderStatusHistoryRepository) {
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
    }

    public void saveOrderStatusHistory(OrderStatusHistory orderStatusHistory) {
        orderStatusHistoryRepository.save(orderStatusHistory);
    }

    public List<OrderStatusHistory> getOrderStatusHistoryByOrder(Order order) {
        return orderStatusHistoryRepository.findByOrder(order);
    }

}
