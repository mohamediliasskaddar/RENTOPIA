package com.rentaldapp.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", url = "${services.payment.url:http://localhost:8084}")
public interface PaymentServiceClient {

    @PostMapping("/api/payments/refund")
    void initiateRefund(@RequestParam("reservationId") Integer reservationId,
                        @RequestParam("reason") String reason);

    @PostMapping("/api/payments/escrow/release")
    void releaseEscrow(@RequestParam("reservationId") Integer reservationId,
                       @RequestParam("hostWallet") String hostWallet);
}