package com.qrorder.web;

import com.qrorder.model.PaymentMethod;
import com.qrorder.model.Session;
import com.qrorder.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/payments")
public class PublicPaymentController {

    private final PaymentService paymentService;

    public PublicPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/upi/initiate")
    public PaymentService.UpiPaymentIntent initiateUpi(@RequestBody Map<String, String> body) {
        return paymentService.initiateUpiPayment(body.get("sessionToken"));
    }

    /** In production this is a Razorpay webhook, not a client call - simulated here for the demo. */
    @PostMapping("/upi/confirm")
    public Session confirmUpi(@RequestBody Map<String, String> body) {
        return paymentService.confirmUpiPayment(body.get("sessionToken"));
    }

    @PostMapping("/cash-or-card")
    public Session chooseCashOrCard(@RequestBody Map<String, String> body) {
        PaymentMethod method = PaymentMethod.valueOf(body.get("method"));
        return paymentService.chooseCashOrCard(body.get("sessionToken"), method);
    }
}
