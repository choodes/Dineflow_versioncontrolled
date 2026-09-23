package com.qrorder.web;

import com.qrorder.model.PaymentChoice;
import com.qrorder.model.Session;
import com.qrorder.model.WaiterCallType;
import com.qrorder.service.BillingService;
import com.qrorder.service.SessionService;
import com.qrorder.service.WaiterCallService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/sessions")
public class PublicSessionController {

    private final SessionService sessionService;
    private final BillingService billingService;
    private final WaiterCallService waiterCallService;

    public PublicSessionController(SessionService sessionService, BillingService billingService,
                                    WaiterCallService waiterCallService) {
        this.sessionService = sessionService;
        this.billingService = billingService;
        this.waiterCallService = waiterCallService;
    }

    public record CreateSessionRequest(Long tableId, String existingToken) {}

    /** Fired the moment a QR is scanned. */
    @PostMapping
    public Session createOrResume(@RequestBody CreateSessionRequest req) {
        return sessionService.getOrCreateSession(req.tableId(), req.existingToken());
    }

    @GetMapping("/{token}")
    public Session get(@PathVariable String token) {
        return sessionService.getOpenSessionByToken(token);
    }

    /** Captured right after scan, before the menu unlocks: phone + party size. */
    @PostMapping("/{token}/details")
    public Session setDetails(@PathVariable String token, @RequestBody Map<String, Object> body) {
        String phone = String.valueOf(body.get("phone"));
        Integer partySize = body.get("partySize") == null ? null : Integer.valueOf(String.valueOf(body.get("partySize")));
        return sessionService.setCustomerDetails(token, phone, partySize);
    }

    /** "Pay now?" decision point. */
    @PostMapping("/{token}/payment-choice")
    public Session choosePaymentTiming(@PathVariable String token, @RequestBody Map<String, String> body) {
        PaymentChoice choice = PaymentChoice.valueOf(body.get("choice"));
        return sessionService.choosePaymentTiming(token, choice);
    }

    /** "Close order?" = YES. */
    @PostMapping("/{token}/request-bill")
    public Session requestBill(@PathVariable String token) {
        return sessionService.requestBill(token);
    }

    @GetMapping("/{token}/bill")
    public BillingService.Bill bill(@PathVariable String token) {
        Session s = sessionService.getOpenSessionByToken(token);
        return billingService.calculate(s.getId());
    }

    /** Customer taps "Call Waiter" (used for cash/card payment requests, or general help). */
    @PostMapping("/{token}/call-waiter")
    public Object callWaiter(@PathVariable String token) {
        Session s = sessionService.getOpenSessionByToken(token);
        return waiterCallService.raise(s.getTableId(), s.getId(), WaiterCallType.CUSTOMER_CALL);
    }
}
