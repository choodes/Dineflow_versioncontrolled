package com.qrorder.service;

import com.qrorder.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mock payment gateway integration. This demo cannot reach the real Razorpay
 * API (no network/API keys in this environment), so UPI "payment" is
 * simulated as instantly successful. To go live: replace
 * {@link #initiateUpiPayment} with a real Razorpay Orders API call, and
 * {@link #confirmUpiPayment} with verifying the payment webhook/signature
 * Razorpay sends back, instead of always returning true.
 */
@Service
public class PaymentService {

    private final SessionService sessionService;
    private final BillingService billingService;
    private final WaiterCallService waiterCallService;

    public PaymentService(SessionService sessionService, BillingService billingService,
                           WaiterCallService waiterCallService) {
        this.sessionService = sessionService;
        this.billingService = billingService;
        this.waiterCallService = waiterCallService;
    }

    public record UpiPaymentIntent(String sessionToken, String mockGatewayOrderId, BigDecimal amount) {}

    /** "Choose method" -> UPI -> "razor pay" */
    public UpiPaymentIntent initiateUpiPayment(String sessionToken) {
        Session session = sessionService.getOpenSessionByToken(sessionToken);
        session.setPaymentMethod(PaymentMethod.UPI);
        session.setPaymentStatus(PaymentStatus.PROCESSING);
        sessionService.save(session);

        BigDecimal amount = billingService.calculate(session.getId()).grandTotal();
        String mockOrderId = "mock_rzp_" + UUID.randomUUID().toString().substring(0, 12);
        return new UpiPaymentIntent(sessionToken, mockOrderId, amount);
    }

    /**
     * Called after the (mock) gateway confirms payment. In production this
     * would be a webhook Razorpay calls, verified via signature - not a
     * client-triggered call.
     */
    public Session confirmUpiPayment(String sessionToken) {
        Session session = sessionService.getOpenSessionByToken(sessionToken);
        session.setPaymentStatus(PaymentStatus.PAID);
        sessionService.save(session);

        // Even after UPI payment, per the workflow, the waiter still comes to
        // confirm and clear the table before the session formally closes.
        waiterCallService.raise(session.getTableId(), session.getId(), WaiterCallType.PAYMENT_COLLECT);
        return session;
    }

    /** "Choose method" -> Cash/Card -> "Process" -> notify waiter */
    public Session chooseCashOrCard(String sessionToken, PaymentMethod method) {
        if (method != PaymentMethod.CASH && method != PaymentMethod.CARD) {
            throw new IllegalArgumentException("Use initiateUpiPayment for UPI");
        }
        Session session = sessionService.getOpenSessionByToken(sessionToken);
        session.setPaymentMethod(method);
        sessionService.save(session);

        waiterCallService.raise(session.getTableId(), session.getId(), WaiterCallType.PAYMENT_COLLECT);
        return session;
    }
}
