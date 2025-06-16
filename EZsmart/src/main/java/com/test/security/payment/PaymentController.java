package com.test.security.payment;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.test.security.order.Order;
import com.test.security.order.OrderRepository;
import com.test.security.order.OrderStatus;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.exception.StripeException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentController(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    // Create Stripe Checkout Session
    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody PaymentRequest request) {
        try {
            // Validate input
            if (request == null || request.getOrderId() == null || request.getAmount() == null || request.getCurrency() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid payment request",
                                "details", "Required fields: orderId, amount, currency"));
            }

            // Validate amount
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid amount",
                                "details", "Amount must be greater than zero"));
            }

            Optional<Order> optionalOrder = orderRepository.findById(request.getOrderId());
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found",
                                "orderId", request.getOrderId()));
            }

            Order order = optionalOrder.get();

            // Validate order status
            if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Order is already paid or canceled",
                                "currentStatus", order.getStatus().name()));
            }

            // Validate ownership
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            if (!order.getUser().getEmail().equals(currentUserEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You do not own this order",
                                "orderOwner", order.getUser().getEmail(),
                                "currentUser", currentUserEmail));
            }

            // Create Stripe session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("https://your-frontend.com/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("https://your-frontend.com/cancel")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.getCurrency())
                                                    .setUnitAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + order.getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("order_id", order.getId().toString())
                    .setCustomerEmail(order.getUser().getEmail())
                    .build();

            Session session = Session.create(params);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("sessionId", session.getId());
            response.put("url", session.getUrl()); // Include the checkout URL
            response.put("orderId", order.getId());
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            // Handle Stripe-specific errors
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("error", "Stripe API error");
            errorDetails.put("type", e.getStripeError() != null ? e.getStripeError().getType() : "unknown");
            errorDetails.put("code", e.getStripeError() != null ? e.getStripeError().getCode() : "unknown");
            errorDetails.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorDetails);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid numeric value",
                            "details", e.getMessage()));

        } catch (Exception e) {
            // Handle all other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create checkout session",
                            "details", e.getMessage(),
                            "exceptionType", e.getClass().getSimpleName()));
        }
    }

    // Stripe Webhook
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        // Log raw payload for debugging
        System.out.println("Received webhook payload: " + payload);

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            // Process the event
            switch (event.getType()) {
                case "checkout.session.completed":
                    Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                    if (session != null) {
                        handleCheckoutSessionCompleted(session);
                    }
                    break;
                // ... other event types
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    private void handleCheckoutSessionCompleted(Session session) {
        String orderIdStr = session.getMetadata().get("order_id");

        if (orderIdStr == null) return;

        Long orderId = Long.parseLong(orderIdStr);
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isEmpty()) return;

        Order order = optionalOrder.get();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentAmount(BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100)));
        payment.setPaymentMethod(PaymentMethod.STRIPE);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());

        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

}