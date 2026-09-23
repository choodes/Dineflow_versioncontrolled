package com.qrorder.web;

import com.qrorder.model.*;
import com.qrorder.service.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/waiter")
public class WaiterController {

    private final OrderService orderService;
    private final WaiterCallService waiterCallService;
    private final SessionService sessionService;
    private final BillingService billingService;

    public WaiterController(OrderService orderService, WaiterCallService waiterCallService,
                             SessionService sessionService, BillingService billingService) {
        this.orderService = orderService;
        this.waiterCallService = waiterCallService;
        this.sessionService = sessionService;
        this.billingService = billingService;
    }

    // ---- Deliveries: orders the chef has marked READY ----

    public record DeliveryView(Long orderId, Long sessionId, List<OrderItem> items) {}

    @GetMapping("/deliveries/ready")
    public List<DeliveryView> deliveries() {
        return orderService.readyOrders().stream()
                .map(o -> new DeliveryView(o.getId(), o.getSessionId(), orderService.itemsFor(o.getId())))
                .toList();
    }

    /** "Waiter deliver" -> "Mark served" */
    @PostMapping("/orders/{id}/served")
    public Order markServed(@PathVariable Long id) {
        return orderService.markServed(id);
    }

    // ---- Waiter calls: customer calls, payment-collect handoffs, regular checks ----

    @GetMapping("/calls")
    public List<WaiterCall> openCalls() {
        return waiterCallService.listOpen();
    }

    /** "Attend table" */
    @PostMapping("/calls/{id}/attend")
    public WaiterCall attend(@PathVariable Long id) {
        return waiterCallService.attend(id);
    }

    /** Resolve a non-payment call (customer call / regular check) without closing any session. */
    @PostMapping("/calls/{id}/resolve")
    public WaiterCall resolveCall(@PathVariable Long id) {
        return waiterCallService.resolve(id);
    }

    /** "Collect payment" (cash/card) -> "Confirm payment" -> finalized -> Leave */
    @PostMapping("/calls/{id}/mark-paid")
    public Session markPaidAndClose(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        WaiterCall call = waiterCallService.get(id);
        Session session = sessionService.getById(call.getSessionId());
        session.setPaymentStatus(PaymentStatus.PAID);
        sessionService.save(session);
        waiterCallService.resolve(id);
        return sessionService.closeSession(session.getId());
    }

    // ---- Quick lookups for the waiter dashboard ----

    @GetMapping("/sessions/{sessionId}/bill")
    public BillingService.Bill bill(@PathVariable Long sessionId) {
        return billingService.calculate(sessionId);
    }

    // ---- Tables overview with red-flag for idle unpaid sessions ----

    public record SessionOverview(Long id, Long tableId, String status, String paymentChoice,
                                   String customerPhone, Integer partySize, long minutesSinceLastOrder,
                                   boolean hasOrders, boolean needsAttention, BigDecimal total) {}

    /**
     * "Session open 25+ minutes without a new order, and unpaid" - flagged
     * red for the waiter to check on. Uses the same rule and threshold as
     * {@link com.qrorder.service.RegularCheckScheduler}, which is what
     * actually raises the notification in the Waiter Calls list; this
     * overview just surfaces the same condition as a persistent row you can
     * see at a glance without needing to open the calls list.
     */
    @GetMapping("/sessions")
    public List<SessionOverview> sessionsOverview() {
        return sessionService.findAllOpenSessions().stream().map(this::toOverview).toList();
    }

    /** Counter staff pulling up a bill using the phone number the customer gave at the table. */
    @GetMapping("/sessions/search")
    public List<SessionOverview> searchByPhone(@RequestParam String phone) {
        return sessionService.findOpenSessionsByPhone(phone).stream().map(this::toOverview).toList();
    }

    private SessionOverview toOverview(Session s) {
        boolean hasOrders = !orderService.ordersForSession(s.getId()).isEmpty();
        long minutesSinceLastOrder = Duration.between(s.getLastActivityAt(), Instant.now()).toMinutes();
        boolean needsAttention = s.getPaymentStatus() != PaymentStatus.PAID
                && minutesSinceLastOrder >= RegularCheckScheduler.IDLE_THRESHOLD_MINUTES;
        BigDecimal total = billingService.calculate(s.getId()).grandTotal();
        return new SessionOverview(s.getId(), s.getTableId(), s.getStatus().name(),
                s.getPaymentChoice().name(), s.getCustomerPhone(), s.getPartySize(),
                minutesSinceLastOrder, hasOrders, needsAttention, total);
    }
}
