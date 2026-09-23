package com.qrorder.web;

import com.qrorder.model.Order;
import com.qrorder.model.OrderItem;
import com.qrorder.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    private final OrderService orderService;

    public KitchenController(OrderService orderService) {
        this.orderService = orderService;
    }

    public record KitchenOrderView(Long id, Long sessionId, String status, List<OrderItem> items) {}

    /** "Order received" queue: PLACED (awaiting accept/reject) and ACCEPTED (being cooked). */
    @GetMapping("/queue")
    public List<KitchenOrderView> queue() {
        return orderService.kitchenQueue().stream()
                .map(o -> new KitchenOrderView(o.getId(), o.getSessionId(), o.getStatus().name(),
                        orderService.itemsFor(o.getId())))
                .toList();
    }

    /** "Accept order?" = YES */
    @PostMapping("/orders/{id}/accept")
    public Order accept(@PathVariable Long id) {
        return orderService.accept(id);
    }

    /** "Accept order?" = NO -> "Reject order" -> notify customer */
    @PostMapping("/orders/{id}/reject")
    public Order reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderService.reject(id, body.getOrDefault("reason", "Unavailable"));
    }

    /** Food cooked, ready for the waiter to deliver. */
    @PostMapping("/orders/{id}/ready")
    public Order markReady(@PathVariable Long id) {
        return orderService.markReady(id);
    }
}
