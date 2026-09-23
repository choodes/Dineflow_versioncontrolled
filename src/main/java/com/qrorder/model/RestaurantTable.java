package com.qrorder.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tableNumber;

    // The token encoded in this table's QR code. In production with the e-ink
    // display this value is rotated periodically; here it is static per table
    // for simplicity of the demo.
    @Column(nullable = false, unique = true)
    private String qrToken;

    public RestaurantTable() {}

    public RestaurantTable(String tableNumber, String qrToken) {
        this.tableNumber = tableNumber;
        this.qrToken = qrToken;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }
}
