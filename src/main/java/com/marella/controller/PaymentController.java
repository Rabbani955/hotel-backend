package com.marella.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import java.time.LocalDate;

import com.marella.model.Booking;
import com.marella.repository.BookingRepository;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin
public class PaymentController {

    private final BookingRepository bookingRepository;

    public PaymentController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Value("${razorpay.key}")
    private String KEY;

    @Value("${razorpay.secret}")
    private String SECRET;

    // ✅ CREATE RAZORPAY ORDER
    @PostMapping("/create-order")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> data) {
        try {

            // ✅ Safe amount parsing
            int amount = (int) Math.round(
                    Double.parseDouble(data.get("amount").toString())
            );

            RazorpayClient razorpay = new RazorpayClient(KEY, SECRET);

            JSONObject options = new JSONObject();
            options.put("amount", amount * 100); // paisa
            options.put("currency", "INR");
            options.put("receipt", "order_" + System.currentTimeMillis());

            Order order = razorpay.orders.create(options);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", KEY);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order: " + e.getMessage());
        }
    }

    // ✅ VERIFY PAYMENT + SAVE BOOKING
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, Object> data) {

        // ✅ Validate required fields
        if (data.get("razorpay_payment_id") == null ||
            data.get("razorpay_order_id") == null ||
            data.get("razorpay_signature") == null) {

            return ResponseEntity.badRequest().body("Invalid payment response");
        }

        String paymentId = (String) data.get("razorpay_payment_id");
        String orderId = (String) data.get("razorpay_order_id");
        String signature = (String) data.get("razorpay_signature");

        Map<String, Object> bookingData = (Map<String, Object>) data.get("bookingData");

        if (bookingData == null) {
            return ResponseEntity.badRequest().body("Missing booking data");
        }

        // ✅ VERIFY SIGNATURE
        boolean isValid = verifySignature(orderId, paymentId, signature);

        if (!isValid) {
            return ResponseEntity.badRequest().body("Payment verification failed");
        }

        // ✅ CREATE BOOKING ONLY AFTER SUCCESS
        Booking booking = new Booking();

        booking.setGuestName((String) bookingData.get("guestName"));
        booking.setEmail((String) bookingData.get("email"));
        booking.setPhone((String) bookingData.get("phone"));
        booking.setRoomName((String) bookingData.get("roomName"));

        booking.setGuests(
                Integer.parseInt(bookingData.get("guests").toString())
        );

        booking.setCheckIn(
                LocalDate.parse((String) bookingData.get("checkIn"))
        );

        booking.setCheckOut(
                LocalDate.parse((String) bookingData.get("checkOut"))
        );

        booking.setTotalPrice(
                Double.parseDouble(bookingData.get("totalPrice").toString())
        );

        booking.setPaymentId(paymentId);
        booking.setOrderId(orderId);
        booking.setPaymentStatus("SUCCESS");

        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // ✅ SIGNATURE VERIFICATION METHOD
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            return Utils.verifySignature(payload, signature, SECRET);
        } catch (Exception e) {
            return false;
        }
    }
}