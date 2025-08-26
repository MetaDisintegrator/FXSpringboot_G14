package service.impl.trainseat;


import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.mapper.PaymentMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatOrderMapper;
import org.fxtravel.fxspringboot.pojo.dto.train.GetTicketRequest;
import org.fxtravel.fxspringboot.pojo.entities.TrainSeat;
import org.fxtravel.fxspringboot.pojo.entities.TrainSeatOrder;
import org.fxtravel.fxspringboot.pojo.entities.payment;
import org.fxtravel.fxspringboot.service.impl.trainseat.TrainSeatOrderServiceImpl;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.trainseat.TrainSeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainSeatOrderServiceImplTest {

    private TrainSeatMapper trainSeatMapper;
    private TrainSeatOrderMapper trainSeatOrderMapper;
    private PaymentMapper paymentMapper;
    private PaymentService paymentService;
    private TrainSeatService trainSeatService;
    private EventCenter eventCenter;
    private TrainSeatOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        trainSeatMapper = mock(TrainSeatMapper.class);
        trainSeatOrderMapper = mock(TrainSeatOrderMapper.class);
        paymentMapper = mock(PaymentMapper.class);
        paymentService = mock(PaymentService.class);
        trainSeatService = mock(TrainSeatService.class);
        eventCenter = mock(EventCenter.class);

        service = new TrainSeatOrderServiceImpl();
        injectField(service, "trainSeatMapper", trainSeatMapper);
        injectField(service, "trainSeatOrderMapper", trainSeatOrderMapper);
        injectField(service, "paymentMapper", paymentMapper);
        injectField(service, "paymentService", paymentService);
        injectField(service, "trainSeatService", trainSeatService);
        injectField(service, "eventCenter", eventCenter);
    }

    private void injectField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void existsByTrainAndUser_shouldReturnTrue() {
        when(trainSeatOrderMapper.existsByTrainAndUser(1, 2)).thenReturn(true);
        assertTrue(service.existsByTrainAndUser(1, 2));
    }

    @Test
    void existsByTrainAndUser_shouldReturnFalse() {
        when(trainSeatOrderMapper.existsByTrainAndUser(1, 2)).thenReturn(false);
        assertFalse(service.existsByTrainAndUser(1, 2));
    }

    @Test
    void getOrdersByUserId_shouldReturnList() {
        TrainSeatOrder order = new TrainSeatOrder();
        when(trainSeatOrderMapper.findByUserID(2)).thenReturn(List.of(order));
        List<TrainSeatOrder> result = service.getOrdersByUserId(2);
        assertEquals(1, result.size());
    }

    @Test
    void getOrdersByUserId_shouldReturnEmptyList() {
        when(trainSeatOrderMapper.findByUserID(2)).thenReturn(Collections.emptyList());
        List<TrainSeatOrder> result = service.getOrdersByUserId(2);
        assertTrue(result.isEmpty());
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        TrainSeatOrder order = new TrainSeatOrder();
        when(trainSeatOrderMapper.selectById(1)).thenReturn(order);
        assertNotNull(service.getOrderById(1));
    }

    @Test
    void getOrderById_shouldReturnNull() {
        when(trainSeatOrderMapper.selectById(1)).thenReturn(null);
        assertNull(service.getOrderById(1));
    }

    @Test
    void getOrderByNumber_shouldReturnOrder() {
        TrainSeatOrder order = new TrainSeatOrder();
        when(trainSeatOrderMapper.findByOrderNumber("NO123")).thenReturn(order);
        assertNotNull(service.getOrderByNumber("NO123"));
    }

    @Test
    void getOrderByNumber_shouldReturnNull() {
        when(trainSeatOrderMapper.findByOrderNumber("NO123")).thenReturn(null);
        assertNull(service.getOrderByNumber("NO123"));
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() {
        GetTicketRequest request = mock(GetTicketRequest.class);
        when(request.getSeatId()).thenReturn(1);
        when(request.getUserId()).thenReturn(2);

        TrainSeat seat = new TrainSeat();
        seat.setId(1);
        seat.setPrice(100.0);
        seat.setTrainId(10);
        when(trainSeatMapper.selectById(1)).thenReturn(seat);

        doAnswer(invocation -> {
            TrainSeatOrder order = invocation.getArgument(0);
            order.setId(5); // 模拟数据库生成ID
            return null;
        }).when(trainSeatOrderMapper).insert(any(TrainSeatOrder.class));

        payment payment = new payment();
        payment.setId(10); // 必须设置 ID
        payment.setOrderNumber("ORDER123"); // 必须设置订单号
        payment.setUserId(2);

        when(paymentService.createPayment(anyInt(), any(), anyDouble(), anyInt(), anyInt(), anyInt())).thenReturn(payment);

        when(trainSeatService.checkAndGet(anyInt(), anyInt(), any())).thenReturn(true);

        TrainSeatOrder result = service.createOrder(request);

        assertNotNull(result);
        assertEquals(2, result.getUserId());
        assertEquals(1, result.getTrainSeatId());
        assertEquals("ORDER123", result.getOrderNumber());
    }


    @Test
    void createOrder_shouldThrowExceptionWhenSeatNotFound() {
        GetTicketRequest req = mock(GetTicketRequest.class);
        when(req.getSeatId()).thenReturn(1);
        when(trainSeatMapper.selectById(1)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(req));
    }

}