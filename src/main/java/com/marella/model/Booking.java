package com.marella.model;--

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Booking {

    // ✅ Validate dates before save/update
	@PrePersist
	@PreUpdate
	public void beforeSave() {

	    // ✅ Validate dates
	    if (checkIn != null && checkOut != null && checkOut.isBefore(checkIn)) {
	        throw new IllegalArgumentException("Check-out date must be after check-in date");
	    }

	    // ✅ Generate booking reference (only once)
	    if (this.bookingReference == null) {
	        this.bookingReference = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	    }

	    // ✅ Default booking status
	    if (this.status == null) {
	        this.status = "CHECKED_IN";
	    }

	    // ✅ Default payment status
	    if (this.paymentStatus == null) {
	        this.paymentStatus = "PENDING";
	    }
	}
	
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Guest Details
    @NotBlank(message = "Guest name is required")
    @Column(nullable = false)
    private String guestName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false)
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    // ✅ Room Info
    @NotBlank(message = "Room name is required")
    @Column(nullable = false)
    private String roomName;

    @Min(value = 1, message = "Guests must be at least 1")
    private int guests;

    // ✅ Dates
    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    // ✅ Pricing
    @Min(value = 0, message = "Price cannot be negative")
    private double totalPrice;

    private String paymentMethod;

    // ✅ Razorpay Payment Details (NEW)
    private String paymentId;
    private String orderId;
    private String paymentStatus; // SUCCESS / FAILED

    // ✅ Unique Booking Reference
    @Column(unique = true, nullable = false)
    private String bookingReference;

    // ✅ Booking Status
    @Column(nullable = false)
    private String status; // CHECKED_IN, CHECKED_OUT
    
    private int roomsCount = 1;


    // ------------------ GETTERS & SETTERS ------------------

    public Long getId() {
        return id;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // ✅ Payment getters/setters

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    // ✅ Booking reference

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    // ✅ Status

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getRoomsCount() {
        return roomsCount;
    }
    
    public void setRoomsCount(int roomsCount) {
        this.roomsCount = roomsCount;
    }
    
    
}