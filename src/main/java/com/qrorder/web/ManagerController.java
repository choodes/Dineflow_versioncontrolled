package com.qrorder.web;

import com.qrorder.model.*;
import com.qrorder.service.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final MenuService menuService;
    private final SessionService sessionService;
    private final BillingService billingService;
    private final DisputeService disputeService;

    public ManagerController(MenuService menuService, SessionService sessionService,
                              BillingService billingService, DisputeService disputeService) {
        this.menuService = menuService;
        this.sessionService = sessionService;
        this.billingService = billingService;
        this.disputeService = disputeService;
    }

    // ---- "Edit menu items" ----

    @GetMapping("/menu")
    public List<MenuItem> menu() {
        return menuService.getAllForManager();
    }

    @PostMapping("/menu")
    public MenuItem createMenuItem(@RequestBody MenuItem item) {
        return menuService.create(item);
    }

    @PutMapping("/menu/{id}")
    public MenuItem updateMenuItem(@PathVariable Long id, @RequestBody MenuItem item) {
        return menuService.update(id, item);
    }

    @DeleteMapping("/menu/{id}")
    public void deleteMenuItem(@PathVariable Long id) {
        menuService.delete(id);
    }

    // ---- "View sessions/bills" ----

    public record SessionView(Long id, Long tableId, String status, String paymentChoice,
                               String paymentMethod, String paymentStatus, String customerPhone, BigDecimal total) {}

    @GetMapping("/sessions")
    public List<SessionView> allOpenSessions() {
        return sessionService.findAllOpenSessions().stream()
                .map(s -> new SessionView(
                        s.getId(), s.getTableId(), s.getStatus().name(),
                        s.getPaymentChoice().name(),
                        s.getPaymentMethod() == null ? null : s.getPaymentMethod().name(),
                        s.getPaymentStatus().name(),
                        s.getCustomerPhone(),
                        billingService.calculate(s.getId()).grandTotal()
                )).toList();
    }

    @GetMapping("/sessions/{id}/bill")
    public BillingService.Bill bill(@PathVariable Long id) {
        return billingService.calculate(id);
    }

    /** "Override/close sessions" */
    @PostMapping("/sessions/{id}/override-close")
    public Session overrideClose(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return sessionService.managerOverrideClose(id, body.getOrDefault("reason", "Manager override"));
    }

    // ---- "Handle disputes" / "Review orders/calls/bills" ----

    @GetMapping("/disputes")
    public List<Dispute> disputes(@RequestParam(required = false) Boolean openOnly) {
        return (openOnly != null && openOnly) ? disputeService.listOpen() : disputeService.listAll();
    }

    @PostMapping("/disputes")
    public Dispute raiseDispute(@RequestBody Map<String, String> body) {
        return disputeService.raise(Long.valueOf(body.get("sessionId")), body.get("note"),
                body.getOrDefault("raisedBy", "employee"));
    }

    @PostMapping("/disputes/{id}/resolve")
    public Dispute resolveDispute(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return disputeService.resolve(id, body.getOrDefault("resolutionNote", ""));
    }
}
