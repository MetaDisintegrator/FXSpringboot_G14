package service.impl.common;

import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.common.E_PaymentType;
import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.mapper.PaymentMapper;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentResultDTO;
import org.fxtravel.fxspringboot.pojo.entities.payment;
import org.fxtravel.fxspringboot.service.impl.common.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private EventCenter eventCenter;

    @Test
    void testCreatePayment_success() {
        Integer userId = 1;
        E_PaymentType type = E_PaymentType.TRAIN_TICKET;
        Double amount = 100.0;
        Integer relatedId = 10;
        Integer quantity = 2;
        Integer goodId = 5;

        payment payment = new payment();
        payment.setUserId(userId);
        payment.setType(type);
        payment.setAmount(amount);
        payment.setRelatedId(relatedId);
        payment.setStatus(E_PaymentStatus.IDLE);
        payment.setOrderNumber("TT202401010000000001");
        payment.setTimeoutSeconds(0L);
        payment.setQuantity(quantity);
        payment.setGoodId(goodId);

        when(paymentMapper.insert(any(payment.class))).thenReturn(1);

        payment result = paymentService.createPayment(userId, type, amount, relatedId, quantity, goodId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(type, result.getType());
        assertEquals(amount, result.getAmount());
        assertEquals(relatedId, result.getRelatedId());
        assertEquals(E_PaymentStatus.IDLE, result.getStatus());
        assertEquals(quantity, result.getQuantity());
        assertEquals(goodId, result.getGoodId());
        assertNotNull(result.getOrderNumber());
    }

    @Test
    void testCreatePayment_failWithNullUserId() {
        E_PaymentType type = E_PaymentType.TRAIN_TICKET;
        Double amount = 100.0;
        Integer relatedId = 10;
        Integer quantity = 2;
        Integer goodId = 5;

        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(null, type, amount, relatedId, quantity, goodId);
        });
    }

    @Test
    void testCompletePayment_success() {
        String orderNumber = "TT202401010000000001";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.PENDING);
        payment.setOrderNumber(orderNumber);
        payment.setType(E_PaymentType.TRAIN_TICKET);
        payment.setRelatedId(10);
        payment.setQuantity(2);
        payment.setGoodId(5);
        payment.setUserId(1);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);
        when(paymentMapper.updateStatus(eq(orderNumber), eq(E_PaymentStatus.COMPLETED), any())).thenReturn(1);

        boolean result = paymentService.completePayment(orderNumber, null);

        assertTrue(result);
        assertEquals(E_PaymentStatus.COMPLETED, payment.getStatus());
        verify(paymentMapper).updateStatus(eq(orderNumber), eq(E_PaymentStatus.COMPLETED), any());
    }

    @Test
    void testCompletePayment_failWhenNotPending() {
        String orderNumber = "TT202401010000000002";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.IDLE);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);

        boolean result = paymentService.completePayment(orderNumber, null);

        assertFalse(result);
        verify(paymentMapper, never()).updateStatus(anyString(), any(), any());
    }

    @Test
    void testFailPayment_successWhenPending() {
        String orderNumber = "TT202401010000000003";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.PENDING);
        payment.setOrderNumber(orderNumber);
        payment.setType(E_PaymentType.TRAIN_TICKET);
        payment.setRelatedId(10);
        payment.setQuantity(2);
        payment.setGoodId(5);
        payment.setUserId(1);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);
        when(paymentMapper.updateStatus(eq(orderNumber), eq(E_PaymentStatus.FAILED), any())).thenReturn(1);

        boolean result = paymentService.failPayment(orderNumber, null);

        assertTrue(result);
        assertEquals(E_PaymentStatus.FAILED, payment.getStatus());
        verify(paymentMapper).updateStatus(eq(orderNumber), eq(E_PaymentStatus.FAILED), any());
    }

    @Test
    void testFailPayment_failWhenNotPendingOrIdle() {
        String orderNumber = "TT202401010000000004";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.COMPLETED);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);

        boolean result = paymentService.failPayment(orderNumber, null);

        assertFalse(result);
        verify(paymentMapper, never()).updateStatus(anyString(), any(), any());
    }

    @Test
    void testRefundPayment_success() {
        String orderNumber = "TT202401010000000005";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.COMPLETED);
        payment.setOrderNumber(orderNumber);
        payment.setType(E_PaymentType.TRAIN_TICKET);
        payment.setRelatedId(10);
        payment.setQuantity(2);
        payment.setGoodId(5);
        payment.setUserId(1);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);
        when(paymentMapper.updateStatus(eq(orderNumber), eq(E_PaymentStatus.REFUNDED), any())).thenReturn(1);

        boolean result = paymentService.refundPayment(orderNumber, null);

        assertTrue(result);
        assertEquals(E_PaymentStatus.REFUNDED, payment.getStatus());
        verify(paymentMapper).updateStatus(eq(orderNumber), eq(E_PaymentStatus.REFUNDED), any());
    }

    @Test
    void testRefundPayment_failWhenNotCompleted() {
        String orderNumber = "TT202401010000000006";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.PENDING);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);

        boolean result = paymentService.refundPayment(orderNumber, null);

        assertFalse(result);
        verify(paymentMapper, never()).updateStatus(anyString(), any(), any());
    }

    @Test
    void testFinishPayment_success() {
        String orderNumber = "TT202401010000000007";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.COMPLETED);
        payment.setOrderNumber(orderNumber);
        payment.setType(E_PaymentType.TRAIN_TICKET);
        payment.setRelatedId(10);
        payment.setQuantity(2);
        payment.setGoodId(5);
        payment.setUserId(1);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);
        when(paymentMapper.updateStatus(eq(orderNumber), eq(E_PaymentStatus.FINISHED), any())).thenReturn(1);

        boolean result = paymentService.finishPayment(orderNumber, null);

        assertTrue(result);
        assertEquals(E_PaymentStatus.FINISHED, payment.getStatus());
        verify(paymentMapper).updateStatus(eq(orderNumber), eq(E_PaymentStatus.FINISHED), any());
    }

    @Test
    void testFinishPayment_failWhenNotCompleted() {
        String orderNumber = "TT202401010000000008";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.PENDING);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);

        boolean result = paymentService.finishPayment(orderNumber, null);

        assertFalse(result);
        verify(paymentMapper, never()).updateStatus(anyString(), any(), any());
    }

    @Test
    void testSimulatePaymentProcess_success() {
        String orderNumber = "TT202401010000000009";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.IDLE);
        payment.setOrderNumber(orderNumber);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);

        Supplier<Boolean> inventoryDeduction = () -> true;
        Supplier<Object> data = () -> null;

        PaymentResultDTO result = paymentService.simulatePaymentProcess(orderNumber, 60L, inventoryDeduction, data);

        assertEquals(E_PaymentStatus.PENDING, result.getCurrentStatus());
        assertEquals(orderNumber, result.getOrderNumber());
        assertEquals(60L, result.getRemainingTimeSeconds());
        assertEquals("Payment processing started", result.getMessage());
    }

    @Test
    void testSimulatePaymentProcess_inventoryFail() {
        String orderNumber = "TT202401010000000010";
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.IDLE);
        payment.setOrderNumber(orderNumber);

        when(paymentMapper.selectByOrderNumber(orderNumber)).thenReturn(payment);

        Supplier<Boolean> inventoryDeduction = () -> false;
        Supplier<Object> data = () -> null;

        PaymentResultDTO result = paymentService.simulatePaymentProcess(orderNumber, 60L, inventoryDeduction, data);

        assertEquals(E_PaymentStatus.FAILED, result.getCurrentStatus());
        assertEquals(orderNumber, result.getOrderNumber());
        assertEquals(0L, result.getRemainingTimeSeconds());
        assertEquals("库存不足，订单创建失败", result.getMessage());
    }

    @Test
    void testCheckPaymentStatus_success() {
        Integer paymentId = 123;
        payment payment = new payment();
        payment.setStatus(E_PaymentStatus.PENDING);
        payment.setOrderNumber("TT202401010000000011");
        payment.setPaymentTime(LocalDateTime.now());
        payment.setTimeoutSeconds(60L);

        when(paymentMapper.selectById(paymentId)).thenReturn(payment);

        PaymentResultDTO result = paymentService.checkPaymentStatus(paymentId);

        assertNotNull(result);
        assertEquals(E_PaymentStatus.PENDING, result.getCurrentStatus());
        assertEquals("TT202401010000000011", result.getOrderNumber());
        assertEquals("Payment in progress", result.getMessage());
    }

    @Test
    void testCheckPaymentStatus_failWhenNotFound() {
        Integer paymentId = 999;
        when(paymentMapper.selectById(paymentId)).thenReturn(null);

        PaymentResultDTO result = paymentService.checkPaymentStatus(paymentId);

        assertNull(result.getCurrentStatus());
        assertEquals("Order not found", result.getMessage());
        assertEquals(0L, result.getRemainingTimeSeconds());
    }
}