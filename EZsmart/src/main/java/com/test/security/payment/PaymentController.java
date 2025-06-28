package com.test.security.payment;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.test.security.order.Order;
import com.test.security.order.OrderRepository;
import com.test.security.order.OrderStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    /**
     * Create Stripe Checkout Session with enhanced error handling and validation
     */
    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody PaymentRequest request) {
        try {
            log.info("Creating checkout session for order: {}", request.getOrderId());

            // Enhanced input validation
            if (request == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Request body is required");
            }

            if (request.getOrderId() == null) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Order ID is required");
            }

            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Valid amount greater than zero is required");
            }

            if (request.getCurrency() == null || request.getCurrency().trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Currency is required");
            }

            // Validate currency format
            String currency = request.getCurrency().toLowerCase().trim();
            if (currency.length() != 3) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Currency must be a valid 3-letter ISO code");
            }

            // Fetch and validate order
            Optional<Order> optionalOrder = orderRepository.findById(request.getOrderId());
            if (optionalOrder.isEmpty()) {
                return createErrorResponse(HttpStatus.NOT_FOUND, "Order not found",
                    Map.of("orderId", request.getOrderId()));
            }

            Order order = optionalOrder.get();

            // Validate order status
            if (order.getStatus() == OrderStatus.PAID) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Order is already paid",
                    Map.of("currentStatus", order.getStatus().name(), "orderId", order.getId()));
            }

            if (order.getStatus() == OrderStatus.CANCELLED) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Order is cancelled",
                    Map.of("currentStatus", order.getStatus().name(), "orderId", order.getId()));
            }

            // Validate ownership
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            if (!order.getUser().getEmail().equals(currentUserEmail)) {
                log.warn("User {} attempted to pay for order {} owned by {}",
                    currentUserEmail, order.getId(), order.getUser().getEmail());
                return createErrorResponse(HttpStatus.FORBIDDEN, "You do not have permission to pay for this order");
            }

            // Check if payment already exists
            if (paymentService.paymentExistsForOrder(order.getId())) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Payment already exists for this order");
            }

            // Validate amount matches order total (if you track order total)
            // This depends on your Order entity structure
            // if (order.getTotalAmount() != null && !order.getTotalAmount().equals(request.getAmount())) {
            //     return createErrorResponse(HttpStatus.BAD_REQUEST, "Amount does not match order total");
            // }

            // Create Stripe session with enhanced configuration
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("https://your-frontend.com/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl("https://your-frontend.com/cancel?order_id=" + order.getId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency)
                                                    .setUnitAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Order #" + order.getId())
                                                                    .setDescription("EZsmart Order Payment")
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("user_email", order.getUser().getEmail())
                    .putMetadata("app_name", "EZsmart")
                    .setCustomerEmail(order.getUser().getEmail())
                    .setAllowPromotionCodes(true) // Allow discount codes
                    .build();

            Session session = Session.create(params);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("sessionId", session.getId());
            response.put("url", session.getUrl());
            response.put("orderId", order.getId());
            response.put("amount", request.getAmount());
            response.put("currency", currency.toUpperCase());

            log.info("Successfully created checkout session {} for order {}", session.getId(), order.getId());
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            log.error("Stripe API error while creating checkout session for order {}: {}",
                request != null ? request.getOrderId() : "unknown", e.getMessage());

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("error", "Payment service error");
            errorDetails.put("type", e.getStripeError() != null ? e.getStripeError().getType() : "unknown");
            errorDetails.put("code", e.getStripeError() != null ? e.getStripeError().getCode() : "unknown");
            errorDetails.put("message", "Unable to create payment session. Please try again.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);

        } catch (NumberFormatException e) {
            log.error("Invalid numeric value in payment request: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid numeric value",
                Map.of("details", e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error creating checkout session: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Enhanced Stripe Webhook Handler with proper security and error handling
     */
    @PostMapping(value = "/webhook", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        final long startTime = System.currentTimeMillis();

        try {
            log.info("Received webhook payload of {} bytes", payload != null ? payload.length() : 0);
            log.debug("Webhook secret configured: {}", webhookSecret != null ? "Yes (length: " + webhookSecret.length() + ")" : "No");
            log.debug("Stripe-Signature header present: {}", sigHeader != null ? "Yes" : "No");

            // Validate required headers
            if (sigHeader == null || sigHeader.trim().isEmpty()) {
                log.warn("Webhook request missing Stripe-Signature header");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing Stripe-Signature header");
            }

            if (payload == null || payload.trim().isEmpty()) {
                log.warn("Webhook request has empty payload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Empty payload");
            }

            // Check if webhook secret is configured
            if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
                log.error("Webhook secret is not configured in application properties");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook secret not configured");
            }

            // Verify webhook signature - CRITICAL for security
            Event event;
            try {
                log.debug("Attempting signature verification with secret starting with: {}",
                    webhookSecret.substring(0, Math.min(10, webhookSecret.length())) + "...");
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
                log.info("Successfully verified webhook signature for event: {} ({})",
                    event.getId(), event.getType());
            } catch (SignatureVerificationException e) {
                log.error("Webhook signature verification failed: {}", e.getMessage());
                log.error("Configured webhook secret starts with: {}",
                    webhookSecret.substring(0, Math.min(10, webhookSecret.length())) + "...");
                log.error("Received signature header: {}", sigHeader);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
            }

            // Process the event asynchronously - Return 200 immediately
            try {
                paymentService.processWebhookEventAsync(event);

                long processingTime = System.currentTimeMillis() - startTime;
                log.info("Webhook {} processed in {}ms", event.getId(), processingTime);

                return ResponseEntity.ok("Webhook received and queued for processing");

            } catch (Exception e) {
                log.error("Error queuing webhook event {} for processing: {}", event.getId(), e.getMessage());
                // Still return 200 to prevent Stripe retries for application errors
                return ResponseEntity.ok("Webhook received but processing failed");
            }

        } catch (Exception e) {
            log.error("Critical error in webhook handler: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }
    }

    /**
     * Utility method to create consistent error responses
     */
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message) {
        return createErrorResponse(status, message, new HashMap<>());
    }

    /**
     * Utility method to create consistent error responses with additional details
     */
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Map<String, Object> additionalDetails) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", java.time.Instant.now().toString());

        if (additionalDetails != null && !additionalDetails.isEmpty()) {
            errorResponse.putAll(additionalDetails);
        }

        return ResponseEntity.status(status).body(errorResponse);
    }
}
