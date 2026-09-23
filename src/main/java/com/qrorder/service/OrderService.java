package com.qrorder.service;

import com.qrorder.model.*;
import com.qrorder.repo.MenuItemRepository;
import com.qrorder.repo.OrderItemRepository;
import com.qrorder.repo.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final SessionService sessionService;

    public OrderService(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         MenuItemRepository menuItemRepository,
                         SessionService sessionService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.sessionService = sessionService;
    }

    public record CartLine(Long menuItemId, int quantity) {}

    /** "Add to cart" -> "Place order" -> notifies kitchen ("Order received"). */
    @Transactional
    public Order placeOrder(String sessionToken, List<CartLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        Session session = sessionService.getOpenSessionByToken(sessionToken);

        Order order = new Order();
        order.setSessionId(session.getId());
        order.setStatus(OrderStatus.PLACED);
        Order saved = orderRepository.save(order);

        for (CartLine line : lines) {
            if (line.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            MenuItem item = menuItemRepository.findById(line.menuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
            if (!item.isAvailable()) {
                throw new IllegalStateException(item.getName() + " is currently unavailable");
            }
            OrderItem oi = new OrderItem();
            oi.setOrderId(saved.getId());
            oi.setMenuItemId(item.getId());
            oi.setNameSnapshot(item.getName());
            oi.setPriceSnapshot(item.getPrice());
            oi.setQuantity(line.quantity());
            orderItemRepository.save(oi);
        }

        sessionService.touchActivity(session); // resets the 20-min idle clock
        return saved;
    }

    public List<Order> ordersForSession(Long sessionId) {
        return orderRepository.findBySessionId(sessionId);
    }

    public List<OrderItem> itemsFor(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Map<Long, List<OrderItem>> itemsForOrders(List<Long> orderIds) {
        return orderItemRepository.findByOrderIdIn(orderIds)
                .stream().collect(java.util.stream.Collectors.groupingBy(OrderItem::getOrderId));
    }

    // ---- Kitchen actions ----

    public List<Order> kitchenQueue() {
        return orderRepository.findByStatusIn(List.of(OrderStatus.PLACED, OrderStatus.ACCEPTED));
    }

    /** Orders the chef has cooked and marked READY - waiter's delivery queue. */
    public List<Order> readyOrders() {
        return orderRepository.findByStatus(OrderStatus.READY);
    }

    /** "Accept order?" = YES -> Prepare food */
    public Order accept(Long orderId) {
        Order o = get(orderId);
        o.setStatus(OrderStatus.ACCEPTED);
        return orderRepository.save(o);
    }

    /** "Accept order?" = NO -> Reject order -> notify customer */
    public Order reject(Long orderId, String reason) {
        Order o = get(orderId);
        o.setStatus(OrderStatus.REJECTED);
        o.setRejectionReason(reason);
        return orderRepository.save(o);
    }

    /** food cooked, ready for the waiter to deliver */
    public Order markReady(Long orderId) {
        Order o = get(orderId);
        o.setStatus(OrderStatus.READY);
        return orderRepository.save(o);
    }

    /** "Waiter deliver" -> "Mark served" */
    public Order markServed(Long orderId) {
        Order o = get(orderId);
        o.setStatus(OrderStatus.SERVED);
        return orderRepository.save(o);
    }

    /** Customer taps "Reorder" after a rejection - resubmits the same items as a new order. */
    @Transactional
    public Order reorder(Long rejectedOrderId) {
        Order rejected = get(rejectedOrderId);
        if (rejected.getStatus() != OrderStatus.REJECTED) {
            throw new IllegalStateException("Only rejected orders can be reordered");
        }
        List<OrderItem> oldItems = orderItemRepository.findByOrderId(rejectedOrderId);

        Order newOrder = new Order();
        newOrder.setSessionId(rejected.getSessionId());
        newOrder.setStatus(OrderStatus.PLACED);
        newOrder.setReorderOfOrderId(rejectedOrderId);
        Order saved = orderRepository.save(newOrder);

        for (OrderItem old : oldItems) {
            OrderItem oi = new OrderItem();
            oi.setOrderId(saved.getId());
            oi.setMenuItemId(old.getMenuItemId());
            oi.setNameSnapshot(old.getNameSnapshot());
            oi.setPriceSnapshot(old.getPriceSnapshot());
            oi.setQuantity(old.getQuantity());
            orderItemRepository.save(oi);
        }
        return saved;
    }

    public Order get(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }
}
