package org.fxtravel.fxspringboot.controller.common;

import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentRequest;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/complete")
    public ResponseEntity<Boolean> completePayment(@RequestBody PaymentRequest request) {
        boolean result = paymentService.completePayment(request.getOrderNumber(), request.getData());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/fail")
    public ResponseEntity<Boolean> cancelPayment(@RequestBody PaymentRequest request) {
        boolean result = paymentService.failPayment(request.getOrderNumber(), request.getData());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/finish")
    public ResponseEntity<Boolean> finishPayment(@RequestBody PaymentRequest request) {
        boolean result = paymentService.finishPayment(request.getOrderNumber(), request.getData());
        return ResponseEntity.ok(result);
    }
}