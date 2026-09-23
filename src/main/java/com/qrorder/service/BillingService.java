package com.qrorder.service;

import com.qrorder.model.Order;
import com.qrorder.model.OrderItem;
import com.qrorder.model.OrderStatus;
import com.qrorder.repo.OrderItemRepository;
import com.qrorder.repo.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public BillingService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public record BillLine(Long orderId, String status, BigDecimal orderTotal, List<OrderItem> items) {}
    public record Bill(Long sessionId, List<BillLine> lines, BigDecimal grandTotal) {}

    /** Rejected orders never counted; everything else (placed onward) does. */
    public Bill calculate(Long sessionId) {
        List<Order> billable = orderRepository.findBySessionId(sessionId).stream()
                .filter(o -> o.getStatus() != OrderStatus.REJECTED)
                .toList();

        if (billable.isEmpty()) {
            return new Bill(sessionId, List.of(), BigDecimal.ZERO);
        }

        List<Long> orderIds = billable.stream().map(Order::getId).toList();
        Map<Long, List<OrderItem>> itemsByOrder = orderItemRepository.findByOrderIdIn(orderIds)
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<BillLine> lines = billable.stream().map(o -> {
            List<OrderItem> items = itemsByOrder.getOrDefault(o.getId(), List.of());
            BigDecimal total = items.stream()
                    .map(i -> i.getPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new BillLine(o.getId(), o.getStatus().name(), total, items);
        }).toList();

        BigDecimal grandTotal = lines.stream().map(BillLine::orderTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Bill(sessionId, lines, grandTotal);
    }
}
