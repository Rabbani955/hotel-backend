package com.marella.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Room Name
    @NotBlank(message = "Room name is required")
    @Column(nullable = false)
    private String name;

    // ✅ Pricing
    @Min(value = 0, message = "Base price cannot be negative")
    @Column(name = "base_price", nullable = false)
    private int basePrice;

    @Min(value = 0, message = "Extra guest price cannot be negative")
    @Column(name = "extra_guest_price")
    private int extraGuestPrice;

    // ✅ Rating (0 to 5)
    @DecimalMin(value = "0.0", message = "Rating must be >= 0")
    @DecimalMax(value = "5.0", message = "Rating must be <= 5")
    private double rating;

    // ✅ Availability
    @Column(name = "sold_out")
    private boolean soldOut;

    // ✅ Description
    @Size(max = 1000, message = "Description too long")
    @Column(length = 1000)
    private String description;

    // ---------------- GETTERS & SETTERS ----------------

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(int basePrice) {
        this.basePrice = basePrice;
    }

    public int getExtraGuestPrice() {
        return extraGuestPrice;
    }

    public void setExtraGuestPrice(int extraGuestPrice) {
        this.extraGuestPrice = extraGuestPrice;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isSoldOut() {
        return soldOut;
    }

    public void setSoldOut(boolean soldOut) {
        this.soldOut = soldOut;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}