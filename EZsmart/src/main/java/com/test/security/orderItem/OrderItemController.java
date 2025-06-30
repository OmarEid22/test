package com.test.security.orderItem;

import com.test.security.user.Role;
import com.test.security.user.User;
import com.test.security.seller.Seller;
import com.test.security.product.Product;
import com.test.security.products.ProductService;
import com.test.security.order.Order;
import com.test.security.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final UserRepository userRepository;


    @PostMapping
    public ResponseEntity<OrderItemDTO> createOrderItem(@RequestBody OrderItemRequest orderItemRequest) {
        return ResponseEntity.ok(orderItemService.createOrderItem(orderItemRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemDTO> getOrderItem(@PathVariable Long id) {
        return ResponseEntity.ok(orderItemService.getOrderItemById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<OrderItemDTO> updateOrderItemStatus(
            @PathVariable Long id,
            @RequestBody OrderItemStatusRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        OrderItemDTO orderItem = orderItemService.getOrderItemById(id);
        Product product = productService.getProductById(orderItem.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        
        Seller seller = product.getSeller();
        if(!seller.getId().equals(authenticatedUser.getSeller().getId())) {
            throw new AccessDeniedException("You are not authorized to access this resource");
        }
        return ResponseEntity.ok(orderItemService.updateOrderItemStatus(id, request.getStatus()));
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<List<OrderItemDTO>> getOrderItemsBySellerId(
            @AuthenticationPrincipal User authenticatedUser) {
        System.out.println(authenticatedUser.getRole());
        System.out.println(authenticatedUser.getSeller());
        System.out.println(authenticatedUser.getEmail());
        if(!authenticatedUser.getRole().equals(Role.ROLE_SELLER)) {
            throw new RuntimeException("You are not authorized to access this resource");
        }
        Seller seller = authenticatedUser.getSeller();
        return ResponseEntity.ok(orderItemService.getOrderItemsBySellerId(seller.getId()));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seller/stats")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<SellerOrderStatsDTO> getSellerOrderStats(@AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Seller seller = user.getSeller();
        if (seller == null) {
            throw new RuntimeException("No seller associated with this user");
        }
        return ResponseEntity.ok(orderItemService.getSellerOrderStats(seller.getId().longValue()));
    }

    //monthly revenue of seller
    @GetMapping("/seller/monthly-revenue")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Map<String, Double>> getMonthlyRevenueBySellerId(@AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Seller seller = user.getSeller();
        if (seller == null) {
            throw new RuntimeException("No seller associated with this user");
        }
        return ResponseEntity.ok(orderItemService.getMonthlySalesBySellerId(seller.getId()));
    }

    //get total revenue of seller
    @GetMapping("/seller/total-revenue")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Double> getTotalRevenueBySellerId(@AuthenticationPrincipal User authenticatedUser) {
        User user = userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Seller seller = user.getSeller();
        if (seller == null) {
            throw new RuntimeException("No seller associated with this user");
        }
        return ResponseEntity.ok(orderItemService.getMonthlySalesBySellerId(seller.getId()).values().stream().mapToDouble(Double::doubleValue).sum());
    }
} 