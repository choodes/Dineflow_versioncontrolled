package com.qrorder.repo;

import com.qrorder.model.Order;
import com.qrorder.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findBySessionId(Long sessionId);
    List<Order> findBySessionIdAndStatusNot(Long sessionId, OrderStatus status);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStatusIn(List<OrderStatus> statuses);
}
