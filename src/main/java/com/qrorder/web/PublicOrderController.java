package com.qrorder.web;

import com.qrorder.model.Order;
import com.qrorder.model.OrderItem;
import com.qrorder.model.Session;
import com.qrorder.service.OrderService;
import com.qrorder.service.SessionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/orders")
public class PublicOrderController {

    private final OrderService orderService;
    private final SessionService sessionService;

    public PublicOrderController(OrderService orderService, SessionService sessionService) {
        this.orderService = orderService;
        this.sessionService = sessionService;
    }

    public record CartLineDto(Long menuItemId, int quantity) {}
    public record PlaceOrderRequest(String sessionToken, List<CartLineDto> items) {}

    public record OrderView(Long id, String status, List<OrderItem> items, String rejectionReason) {}

    @PostMapping
    public Order place(@RequestBody PlaceOrderRequest req) {
        List<OrderService.CartLine> lines = req.items().stream()
                .map(i -> new OrderService.CartLine(i.menuItemId(), i.quantity()))
                .toList();
        return orderService.placeOrder(req.sessionToken(), lines);
    }

    @GetMapping
    public List<OrderView> forSession(@RequestParam String sessionToken) {
        Session session = sessionService.getOpenSessionByToken(sessionToken);
        List<Order> orders = orderService.ordersForSession(session.getId());
        return orders.stream().map(o -> new OrderView(
                o.getId(), o.getStatus().name(), orderService.itemsFor(o.getId()), o.getRejectionReason()
        )).toList();
    }

    /** Customer taps "Reorder" after a chef rejection. */
    @PostMapping("/{orderId}/reorder")
    public Order reorder(@PathVariable Long orderId) {
        return orderService.reorder(orderId);
    }
}
