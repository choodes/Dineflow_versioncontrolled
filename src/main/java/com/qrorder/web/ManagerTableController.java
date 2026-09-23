package com.qrorder.web;

import com.qrorder.model.RestaurantTable;
import com.qrorder.repo.TableRepository;
import com.qrorder.service.QrCodeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/tables")
public class ManagerTableController {

    private final TableRepository tableRepository;
    private final QrCodeService qrCodeService;

    public ManagerTableController(TableRepository tableRepository, QrCodeService qrCodeService) {
        this.tableRepository = tableRepository;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping
    public List<RestaurantTable> list() {
        return tableRepository.findAll();
    }

    @PostMapping
    public RestaurantTable create(@RequestBody Map<String, String> body) {
        String number = body.get("tableNumber");
        String token = "table-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        return tableRepository.save(new RestaurantTable(number, token));
    }

    /**
     * Streams a PNG QR code that encodes this table's live ordering URL. The
     * host/port are read from the incoming request itself (not hardcoded),
     * so this works whether you open it via localhost or your machine's LAN
     * IP - which matters if you want to actually scan it with a phone on
     * the same WiFi network.
     */
    @GetMapping(value = "/{id}/qrcode.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrCode(@PathVariable Long id, HttpServletRequest request) throws Exception {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String orderingUrl = baseUrl + "/customer.html?table=" + table.getQrToken();

        byte[] png = qrCodeService.generatePng(orderingUrl, 320);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
