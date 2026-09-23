package com.qrorder.web;

import com.qrorder.model.RestaurantTable;
import com.qrorder.service.SessionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/tables")
public class PublicTableController {

    private final SessionService sessionService;

    public PublicTableController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/resolve")
    public Map<String, Object> resolve(@RequestParam String token) {
        RestaurantTable table = sessionService.resolveTable(token);
        return Map.of("tableId", table.getId(), "tableNumber", table.getTableNumber());
    }
}
