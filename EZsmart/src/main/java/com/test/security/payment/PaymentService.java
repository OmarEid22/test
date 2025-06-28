package com.test.security.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.test.security.order.Order;
import com.test.security.order.OrderRepository;
import com.test.security.order.OrderStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * Processes Stripe webhook events asynchronously
     */
    @Async
    public void processWebhookEventAsync(Event event) {
        try {
            log.info("Processing Stripe webhook event: {} with ID: {}", event.getType(), event.getId());

            switch (event.getType()) {
                case "checkout.session.completed":
                case "checkout.session.async_payment_succeeded":
                    try {
                        // Use the proper Stripe approach for deserializing webhook data
                        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);

                        if (stripeObject instanceof Session) {
                            Session session = (Session) stripeObject;
                            log.info("Successfully deserialized session: {}", session.getId());
                            handleCheckoutSessionCompleted(session, event);
                        } else {
                            log.warn("Event data object is not a Session for event: {}, object type: {}",
                                event.getId(), stripeObject != null ? stripeObject.getClass().getSimpleName() : "null");

                            // Fallback: try to retrieve session directly from Stripe API
                            // Extract session ID from the raw event data
                            String sessionId = extractSessionIdFromRawEventData(event);
                            if (sessionId != null) {
                                log.info("Attempting to retrieve session {} directly from Stripe API", sessionId);
                                Session session = Session.retrieve(sessionId);
                                handleCheckoutSessionCompleted(session, event);
                            } else {
                                log.error("Could not extract session ID from event: {}", event.getId());
                                log.debug("Raw event data: {}", event.getData() != null ? event.getData().toJson() : "null");
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error processing checkout session completed event {}: {}", event.getId(), e.getMessage(), e);
                    }
                    break;
                case "checkout.session.async_payment_failed":
                    try {
                        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);

                        if (stripeObject instanceof Session) {
                            Session failedSession = (Session) stripeObject;
                            handleCheckoutSessionFailed(failedSession, event);
                        } else {
                            log.warn("Failed session event data object is not a Session for event: {}", event.getId());

                            String sessionId = extractSessionIdFromRawEventData(event);
                            if (sessionId != null) {
                                Session failedSession = Session.retrieve(sessionId);
                                handleCheckoutSessionFailed(failedSession, event);
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error processing failed session event {}: {}", event.getId(), e.getMessage(), e);
                    }
                    break;
                default:
                    log.info("Unhandled webhook event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("Error processing webhook event {}: {}", event.getId(), e.getMessage(), e);
            // In production, you might want to send this to a dead letter queue for retry
        }
    }

    /**
     * Helper method to extract session ID from raw event data using JSON parsing
     */
    private String extractSessionIdFromRawEventData(Event event) {
        try {
            if (event.getData() != null && event.getData().getObject() != null) {
                String rawJson = event.getData().getObject().toJson();
                log.debug("Attempting to extract session ID from raw JSON: {}", rawJson);

                if (rawJson != null && rawJson.contains("\"id\":")) {
                    // Use regex to extract the id field value
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
                    java.util.regex.Matcher matcher = pattern.matcher(rawJson);
                    if (matcher.find()) {
                        String sessionId = matcher.group(1);
                        log.debug("Extracted session ID: {}", sessionId);
                        return sessionId;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract session ID from raw event data: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Handles successful checkout session completion
     */
    @Transactional
    public void handleCheckoutSessionCompleted(Session session, Event event) {
        try {
            log.info("Processing completed checkout session: {}", session.getId());

            // Check for duplicate processing using event ID
            if (paymentRepository.findByStripeEventId(event.getId()).isPresent()) {
                log.info("Event {} already processed, skipping", event.getId());
                return;
            }

            // Check if payment already exists for this session
            Optional<Payment> existingPayment = paymentRepository.findByStripeSessionId(session.getId());
            if (existingPayment.isPresent()) {
                log.info("Payment already exists for session {}, updating status if needed", session.getId());
                updateExistingPayment(existingPayment.get(), session, event);
                return;
            }

            // Extract order ID from metadata
            String orderIdStr = session.getMetadata().get("order_id");
            if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
                log.error("No order_id found in session metadata for session: {}", session.getId());
                return;
            }

            Long orderId;
            try {
                orderId = Long.parseLong(orderIdStr);
            } catch (NumberFormatException e) {
                log.error("Invalid order_id format in session metadata: {}", orderIdStr);
                return;
            }

            // Fetch the order
            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isEmpty()) {
                log.error("Order not found with ID: {} for session: {}", orderId, session.getId());
                return;
            }

            Order order = optionalOrder.get();

            // Validate order state
            if (order.getStatus() == OrderStatus.PAID) {
                log.info("Order {} is already marked as paid, skipping payment creation", orderId);
                return;
            }

            // Create new payment record
            Payment payment = createPaymentFromSession(order, session, event);
            paymentRepository.save(payment);

            // Update order status
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            log.info("Successfully processed payment for order: {} with session: {}", orderId, session.getId());

        } catch (Exception e) {
            log.error("Error handling checkout session completed for session {}: {}", session.getId(), e.getMessage(), e);
            throw e; // Re-throw to ensure transaction rollback
        }
    }

    /**
     * Handles failed checkout session
     */
    @Transactional
    public void handleCheckoutSessionFailed(Session session, Event event) {
        try {
            log.info("Processing failed checkout session: {}", session.getId());

            // Check if we already processed this event
            if (paymentRepository.findByStripeEventId(event.getId()).isPresent()) {
                log.info("Failed event {} already processed, skipping", event.getId());
                return;
            }

            String orderIdStr = session.getMetadata().get("order_id");
            if (orderIdStr == null) {
                log.error("No order_id found in failed session metadata: {}", session.getId());
                return;
            }

            Long orderId = Long.parseLong(orderIdStr);
            Optional<Order> optionalOrder = orderRepository.findById(orderId);

            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();

                // Create failed payment record
                Payment failedPayment = createFailedPaymentFromSession(order, session, event);
                paymentRepository.save(failedPayment);

                // Update order status to failed if it's still pending
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                }

                log.info("Recorded failed payment for order: {} with session: {}", orderId, session.getId());
            }

        } catch (Exception e) {
            log.error("Error handling failed checkout session for session {}: {}", session.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates a Payment entity from a successful Stripe session
     */
    private Payment createPaymentFromSession(Order order, Session session, Event event) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStripeSessionId(session.getId());
        payment.setStripePaymentIntentId(session.getPaymentIntent());
        payment.setStripeEventId(event.getId());
        payment.setPaymentMethod(PaymentMethod.STRIPE);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setCurrency(session.getCurrency() != null ? session.getCurrency().toUpperCase() : "USD");

        // Convert from cents to currency units
        BigDecimal amount = BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100));
        payment.setPaymentAmount(amount);

        payment.setPaymentDate(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        return payment;
    }

    /**
     * Creates a Payment entity from a failed Stripe session
     */
    private Payment createFailedPaymentFromSession(Order order, Session session, Event event) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStripeSessionId(session.getId());
        payment.setStripeEventId(event.getId());
        payment.setPaymentMethod(PaymentMethod.STRIPE);
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setCurrency(session.getCurrency() != null ? session.getCurrency().toUpperCase() : "USD");

        // Convert from cents to currency units
        BigDecimal amount = BigDecimal.valueOf(session.getAmountTotal()).divide(BigDecimal.valueOf(100));
        payment.setPaymentAmount(amount);

        payment.setFailureReason("Payment failed during checkout session");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        return payment;
    }

    /**
     * Updates existing payment if needed
     */
    private void updateExistingPayment(Payment existingPayment, Session session, Event event) {
        boolean updated = false;

        if (existingPayment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            existingPayment.setPaymentStatus(PaymentStatus.COMPLETED);
            existingPayment.setStripeEventId(event.getId());
            existingPayment.setUpdatedAt(LocalDateTime.now());
            updated = true;
        }

        if (updated) {
            paymentRepository.save(existingPayment);
            log.info("Updated existing payment for session: {}", session.getId());
        }
    }

    /**
     * Checks if a payment exists for a given order
     */
    @Transactional(readOnly = true)
    public boolean paymentExistsForOrder(Long orderId) {
        return paymentRepository.findByOrderIdAndPaymentStatus(orderId, PaymentStatus.COMPLETED).isPresent();
    }

    /**
     * Gets payment by order ID
     */
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
}
