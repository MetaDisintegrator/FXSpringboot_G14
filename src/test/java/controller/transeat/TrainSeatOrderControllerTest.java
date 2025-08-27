package controller.transeat;

import jakarta.servlet.http.HttpSession;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.common.Role;
import org.fxtravel.fxspringboot.controller.trainseat.TrainSeatOrderController;
import org.fxtravel.fxspringboot.mapper.trainmeal.TrainMealOrderMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatOrderMapper;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentRequest;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentResultDTO;
import org.fxtravel.fxspringboot.pojo.dto.train.GetTicketRequest;
import org.fxtravel.fxspringboot.pojo.dto.train.TrainSeatOrderDTO;
import org.fxtravel.fxspringboot.pojo.entities.*;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.trainseat.TrainSeatOrderService;
import org.fxtravel.fxspringboot.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class TrainSeatOrderControllerTest {

    private TrainSeatOrderController controller;
    private TrainSeatOrderService trainSeatOrderService;
    private PaymentService paymentService;
    private TrainSeatMapper trainSeatMapper;
    private TrainMapper trainMapper;
    private TrainMealOrderMapper trainMealOrderMapper;
    private HttpSession session;
    private BindingResult bindingResult;

    @BeforeEach
    public void setUp() throws Exception {
        controller = new TrainSeatOrderController();
        trainSeatOrderService = Mockito.mock(TrainSeatOrderService.class);
        paymentService = Mockito.mock(PaymentService.class);
        trainSeatMapper = Mockito.mock(TrainSeatMapper.class);
        trainMapper = Mockito.mock(TrainMapper.class);
        trainMealOrderMapper = Mockito.mock(TrainMealOrderMapper.class);
        session = Mockito.mock(HttpSession.class);
        bindingResult = Mockito.mock(BindingResult.class);

        var f1 = TrainSeatOrderController.class.getDeclaredField("trainSeatOrderService");
        f1.setAccessible(true); f1.set(controller, trainSeatOrderService);
        var f2 = TrainSeatOrderController.class.getDeclaredField("paymentService");
        f2.setAccessible(true); f2.set(controller, paymentService);
        var f3 = TrainSeatOrderController.class.getDeclaredField("trainSeatMapper");
        f3.setAccessible(true); f3.set(controller, trainSeatMapper);
        var f4 = TrainSeatOrderController.class.getDeclaredField("trainMapper");
        f4.setAccessible(true); f4.set(controller, trainMapper);
        var f5 = TrainSeatOrderController.class.getDeclaredField("trainMealOrderMapper");
        f5.setAccessible(true); f5.set(controller, trainMealOrderMapper);
    }

    // getTicket (生成车票) 正向
    @Test
    public void testGetTicket_success() {
        GetTicketRequest req = new GetTicketRequest();
        req.setUserId(1);

        TrainSeatOrder order = new TrainSeatOrder();
        order.setId(10);
        order.setOrderNumber("T123");
        order.setSeatNumber("A01");
        Mockito.when(trainSeatOrderService.createOrder(any())).thenReturn(order);

        ResponseEntity<?> resp = controller.getTicket(req, bindingResult, session);
        assertEquals(200, resp.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertEquals("车票生成成功", body.get("message"));
        assertEquals(order.getId(), body.get("id"));
        assertEquals(order.getOrderNumber(), body.get("number"));
        assertEquals(order.getSeatNumber(), body.get("seat"));
    }

    // getTicket (生成车票) 反向：服务异常
    @Test
    public void testGetTicket_serviceException() {
        GetTicketRequest req = new GetTicketRequest();
        req.setUserId(1);

        Mockito.when(trainSeatOrderService.createOrder(any()))
                .thenThrow(new RuntimeException("数据库异常"));

        ResponseEntity<?> resp = controller.getTicket(req, bindingResult, session);
        assertEquals(500, resp.getStatusCodeValue());
        assertTrue(((Map<?, ?>) resp.getBody()).get("error").toString().contains("生成车票失败"));
    }

    // getOrderPaymentStatus 正向
    @Test
    public void testGetOrderPaymentStatus_success() {
        TrainSeatOrder order = new TrainSeatOrder();
        order.setRelatedPaymentId(123);
        Mockito.when(trainSeatOrderService.getOrderById(1)).thenReturn(order);

        PaymentResultDTO result = new PaymentResultDTO();
        Mockito.when(paymentService.checkPaymentStatus(123)).thenReturn(result);

        ResponseEntity<PaymentResultDTO> resp = controller.getOrderPaymentStatus(1);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(result, resp.getBody());
    }

    // getOrderPaymentStatus 反向：订单不存在
    @Test
    public void testGetOrderPaymentStatus_notFound() {
        Mockito.when(trainSeatOrderService.getOrderById(2)).thenReturn(null);

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> controller.getOrderPaymentStatus(2));
    }

    // getTicket (查询用户订单) 正向
    @Test
    public void testGetTicketByUser_success() {
        TrainSeatOrder order = new TrainSeatOrder();
        order.setId(1);
        order.setOrderNumber("T1");
        order.setUserId(1);
        order.setTrainId(2);
        order.setTrainSeatId(3);
        order.setSeatNumber("A01");
        order.setRelatedPaymentId(123);
        order.setTotalAmount(100.0);
        order.setStatus(E_PaymentStatus.PENDING);
        order.setCreateTime(LocalDateTime.now());

        Mockito.when(trainSeatOrderService.getOrdersByUserId(1)).thenReturn(List.of(order));
        var train = Mockito.mock(Train.class);
        Mockito.when(trainMapper.selectById(2)).thenReturn(train);
        var seat = Mockito.mock(TrainSeat.class);
        Mockito.when(trainSeatMapper.selectById(3)).thenReturn(seat);

        ResponseEntity<List<TrainSeatOrderDTO>> resp = controller.getTicket(1);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().size());
    }

    // getTicket (查询用户订单) 反向：无订单
    @Test
    public void testGetTicketByUser_empty() {
        Mockito.when(trainSeatOrderService.getOrdersByUserId(2)).thenReturn(Collections.emptyList());

        ResponseEntity<List<TrainSeatOrderDTO>> resp = controller.getTicket(2);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // refund 正向
    @Test
    public void testRefund_success() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        PaymentRequest req = new PaymentRequest();
        req.setOrderNumber("T1");
        req.setData(Map.of());

        TrainSeatOrder order = new TrainSeatOrder();
        order.setId(1);
        Mockito.when(trainSeatOrderService.getOrderByNumber(anyString())).thenReturn(order);
        Mockito.when(trainMealOrderMapper.existsBySeatOrderId(1)).thenReturn(false);
        Mockito.when(paymentService.refundPayment(anyString(), anyMap())).thenReturn(true);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.refund(req, bindingResult, session);
            assertEquals(200, resp.getStatusCodeValue());
            assertEquals(true, resp.getBody());
        }
    }

    // refund 反向：参数校验失败
    @Test
    public void testRefund_paramError() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(true);

        PaymentRequest req = new PaymentRequest();

        Map<String, Object> errorBody = Map.of("error", "参数错误");
        ResponseEntity<Map<String, Object>> errorResp = ResponseEntity.status(400).body(errorBody);
        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(errorResp);

            ResponseEntity<?> resp = controller.refund(req, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
        }
    }

    // refund 反向：订单无效
    @Test
    public void testRefund_invalidOrder() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        PaymentRequest req = new PaymentRequest();
        req.setOrderNumber("T1");

        Mockito.when(trainSeatOrderService.getOrderByNumber(anyString())).thenReturn(null);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.refund(req, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).get("error").toString().contains("订单无效"));
        }
    }

    // refund 反向：有未取消餐品
    @Test
    public void testRefund_mealNotCancelled() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        PaymentRequest req = new PaymentRequest();
        req.setOrderNumber("T1");

        TrainSeatOrder order = new TrainSeatOrder();
        order.setId(1);
        Mockito.when(trainSeatOrderService.getOrderByNumber(anyString())).thenReturn(order);
        Mockito.when(trainMealOrderMapper.existsBySeatOrderId(1)).thenReturn(true);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.refund(req, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).get("error").toString().contains("有未取消的餐品"));
        }
    }
}