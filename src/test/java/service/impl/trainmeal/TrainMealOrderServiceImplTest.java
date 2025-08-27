package service.impl.trainmeal;

import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.common.E_PaymentType;
import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.event.data.PaymentInfo;
import org.fxtravel.fxspringboot.mapper.trainmeal.TrainMealOrderMapper;
import org.fxtravel.fxspringboot.pojo.dto.trainmeal.TrainMealOrderDTO;
import org.fxtravel.fxspringboot.pojo.entities.payment;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMeal;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMealOrder;
import org.fxtravel.fxspringboot.service.impl.trainmeal.TrainMealOrderServiceImpl;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.trainmeal.TrainMealService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainMealOrderServiceImplTest {

    private TrainMealOrderMapper trainMealOrderMapper;
    private TrainMealService trainMealService;
    private PaymentService paymentService;
    private EventCenter eventCenter;
    private TrainMealOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        trainMealOrderMapper = mock(TrainMealOrderMapper.class);
        trainMealService = mock(TrainMealService.class);
        paymentService = mock(PaymentService.class);
        eventCenter = mock(EventCenter.class);

        service = new TrainMealOrderServiceImpl();
        injectField(service, "trainMealOrderMapper", trainMealOrderMapper);
        injectField(service, "trainMealService", trainMealService);
        injectField(service, "paymentService", paymentService);
        injectField(service, "eventCenter", eventCenter);
    }

    // 工具方法：反射注入
    private void injectField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // getOrderById 正向
    @Test
    void getOrderById_shouldReturnOrder() {
        TrainMealOrder order = new TrainMealOrder();
        order.setId(1);
        when(trainMealOrderMapper.selectById(1)).thenReturn(order);

        TrainMealOrder result = service.getOrderById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    // getOrderById 反向
    @Test
    void getOrderById_shouldReturnNull() {
        when(trainMealOrderMapper.selectById(999)).thenReturn(null);

        TrainMealOrder result = service.getOrderById(999);

        assertNull(result);
    }

    // getOrdersByUser 正向
    @Test
    void getOrdersByUser_shouldReturnOrderList() {
        TrainMealOrder order1 = new TrainMealOrder();
        TrainMealOrder order2 = new TrainMealOrder();
        when(trainMealOrderMapper.selectByUser(1)).thenReturn(Arrays.asList(order1, order2));

        List<TrainMealOrder> result = service.getOrdersByUser(1);

        assertEquals(2, result.size());
    }

    // getOrdersByUser 反向
    @Test
    void getOrdersByUser_shouldReturnEmptyList() {
        when(trainMealOrderMapper.selectByUser(1)).thenReturn(Collections.emptyList());

        List<TrainMealOrder> result = service.getOrdersByUser(1);

        assertTrue(result.isEmpty());
    }

    // getOrdersBySeatOrder 正向
    @Test
    void getOrdersBySeatOrder_shouldReturnOrderList() {
        TrainMealOrder order = new TrainMealOrder();
        when(trainMealOrderMapper.selectBySeatOrder(1)).thenReturn(Arrays.asList(order));

        List<TrainMealOrder> result = service.getOrdersBySeatOrder(1);

        assertEquals(1, result.size());
    }

    // getOrdersBySeatOrder 反向
    @Test
    void getOrdersBySeatOrder_shouldReturnEmptyList() {
        when(trainMealOrderMapper.selectBySeatOrder(1)).thenReturn(Collections.emptyList());

        List<TrainMealOrder> result = service.getOrdersBySeatOrder(1);

        assertTrue(result.isEmpty());
    }

    // createOrder 正向
    @Test
    void createOrder_shouldCreateOrderSuccessfully() {
        // 准备测试数据
        TrainMealOrderDTO orderDTO = new TrainMealOrderDTO();
        orderDTO.setUserId(1);
        orderDTO.setTicketReservationId(10);
        orderDTO.setTrainMealId(5);
        orderDTO.setQuantity(2);

        TrainMeal meal = new TrainMeal();
        meal.setId(5);
        meal.setPrice(50.0);
        meal.setEnabled(true);

        payment mockPayment = mock(payment.class);
        when(mockPayment.getId()).thenReturn(100);
        when(mockPayment.getOrderNumber()).thenReturn("ORDER123");

        // Mock 依赖
        when(trainMealService.getMealById(5)).thenReturn(meal);
        when(paymentService.createPayment(
                eq(1), eq(E_PaymentType.TRAIN_MEAL), eq(100.0),
                any(), eq(2), eq(5))).thenReturn(mockPayment);
        when(trainMealService.checkAndGet(eq(5), eq(2), any())).thenReturn(true);

        // 执行测试
        TrainMealOrder result = service.createOrder(orderDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(10, result.getSeatOrderId());
        assertEquals(5, result.getTrainMealId());
        assertEquals(2, result.getQuantity());
        assertEquals(100.0, result.getTotalAmount());
        assertEquals(E_PaymentStatus.IDLE, result.getStatus());
        assertEquals(100, result.getRelatedPaymentId());
        assertEquals("ORDER123", result.getOrderNumber());

        verify(trainMealOrderMapper, times(1)).insert(any(TrainMealOrder.class));
        verify(trainMealOrderMapper, times(1)).updateById(any(TrainMealOrder.class));
        verify(paymentService, times(1)).simulatePaymentProcess(
                eq("ORDER123"), eq(30L), any(), any());
    }

    // createOrder 反向（餐食不存在）
    @Test
    void createOrder_shouldThrowExceptionWhenMealNotFound() {
        TrainMealOrderDTO orderDTO = new TrainMealOrderDTO();
        orderDTO.setTrainMealId(999);

        when(trainMealService.getMealById(999)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.createOrder(orderDTO));
        assertEquals("餐食不存在或已下架", exception.getMessage());
    }

    // createOrder 反向（餐食已下架）
    @Test
    void createOrder_shouldThrowExceptionWhenMealDisabled() {
        TrainMealOrderDTO orderDTO = new TrainMealOrderDTO();
        orderDTO.setTrainMealId(5);

        TrainMeal meal = new TrainMeal();
        meal.setId(5);
        meal.setEnabled(false);

        when(trainMealService.getMealById(5)).thenReturn(meal);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.createOrder(orderDTO));
        assertEquals("餐食不存在或已下架", exception.getMessage());
    }

    // existsBySeatOrder 正向
    @Test
    void existsBySeatOrder_shouldReturnTrue() {
        when(trainMealOrderMapper.existsBySeatOrderId(1)).thenReturn(true);

        boolean result = service.existsBySeatOrder(1);

        assertTrue(result);
    }

    // existsBySeatOrder 反向
    @Test
    void existsBySeatOrder_shouldReturnFalse() {
        when(trainMealOrderMapper.existsBySeatOrderId(1)).thenReturn(false);

        boolean result = service.existsBySeatOrder(1);

        assertFalse(result);
    }

    // handlePaymentStatusChange 正向
    @Test
    void handlePaymentStatusChange_shouldUpdateStatus() throws Exception {
        PaymentInfo info = mock(PaymentInfo.class);
        when(info.getOrderId()).thenReturn(1);
        when(info.getNewStatus()).thenReturn(E_PaymentStatus.COMPLETED);

        var method = service.getClass().getDeclaredMethod("handlePaymentStatusChange", PaymentInfo.class);
        method.setAccessible(true);
        method.invoke(service, info);

        verify(trainMealOrderMapper, times(1)).updateStatus(1, E_PaymentStatus.COMPLETED);
    }

    // handlePaymentStatusChange 反向（orderId为null）
    @Test
    void handlePaymentStatusChange_shouldCallUpdateStatusEvenWhenOrderIdNull() throws Exception {
        PaymentInfo info = mock(PaymentInfo.class);
        when(info.getOrderId()).thenReturn(null);
        when(info.getNewStatus()).thenReturn(E_PaymentStatus.FAILED);

        var method = service.getClass().getDeclaredMethod("handlePaymentStatusChange", PaymentInfo.class);
        method.setAccessible(true);
        method.invoke(service, info);

        // 根据实际实现，会调用 updateStatus(null, status)
        verify(trainMealOrderMapper, times(1)).updateStatus(null, E_PaymentStatus.FAILED);
    }
}