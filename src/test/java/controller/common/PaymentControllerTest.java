package controller.common;

import org.fxtravel.fxspringboot.controller.common.PaymentController;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentRequest;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;
    @Mock
    private PaymentService paymentService;

    @Test
    public void testCompletePayment_success() {
        Mockito.when(paymentService.completePayment(anyString(), anyMap())).thenReturn(true);
        PaymentRequest request = new PaymentRequest();
        request.setOrderNumber("123");
        request.setData(java.util.Collections.emptyMap());

        ResponseEntity<Boolean> response = paymentController.completePayment(request);
        assertTrue(response.getBody());
    }

    @Test
    public void testCompletePayment_fail() {
        Mockito.when(paymentService.completePayment(anyString(), anyMap())).thenReturn(false);
        PaymentRequest request = new PaymentRequest();
        request.setOrderNumber("123");
        request.setData(java.util.Collections.emptyMap());

        ResponseEntity<Boolean> response = paymentController.completePayment(request);
        assertFalse(response.getBody());
    }

    @Test
    public void testCancelPayment_success() {
        Mockito.when(paymentService.failPayment(anyString(), anyMap())).thenReturn(true);
        PaymentRequest request = new PaymentRequest();
        request.setOrderNumber("456");
        request.setData(java.util.Collections.emptyMap());

        ResponseEntity<Boolean> response = paymentController.cancelPayment(request);
        assertTrue(response.getBody());
    }

    @Test
    public void testCancelPayment_fail() {
        Mockito.when(paymentService.failPayment(anyString(), anyMap())).thenReturn(false);
        PaymentRequest request = new PaymentRequest();
        request.setOrderNumber("456");
        request.setData(java.util.Collections.emptyMap());

        ResponseEntity<Boolean> response = paymentController.cancelPayment(request);
        assertFalse(response.getBody());
    }

    @Test
    public void testFinishPayment_success() {
        Mockito.when(paymentService.finishPayment(anyString(), anyMap())).thenReturn(true);
        PaymentRequest request = new PaymentRequest();
        request.setOrderNumber("789");
        request.setData(java.util.Collections.emptyMap());

        ResponseEntity<Boolean> response = paymentController.finishPayment(request);
        assertTrue(response.getBody());
    }

    @Test
    public void testFinishPayment_fail() {
        Mockito.when(paymentService.finishPayment(anyString(), anyMap())).thenReturn(false);
        PaymentRequest request = new PaymentRequest();
        request.setOrderNumber("789");
        request.setData(java.util.Collections.emptyMap());

        ResponseEntity<Boolean> response = paymentController.finishPayment(request);
        assertFalse(response.getBody());
    }
}