package controller.trainmeal;

import jakarta.servlet.http.HttpSession;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.controller.trainmeal.TrainMealOrderController;
import org.fxtravel.fxspringboot.mapper.trainmeal.TrainMealMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatOrderMapper;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentRequest;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentResultDTO;
import org.fxtravel.fxspringboot.pojo.dto.trainmeal.TrainMealOrderDTO;
import org.fxtravel.fxspringboot.pojo.dto.trainmeal.TrainMealOrderResponse;
import org.fxtravel.fxspringboot.pojo.entities.TrainSeatOrder;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMeal;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMealOrder;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.trainmeal.TrainMealOrderService;
import org.fxtravel.fxspringboot.service.inter.trainmeal.TrainMealService;
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

public class TrainMealOrderControllerTest {

    private TrainMealOrderController controller;
    private TrainMealService trainMealService;
    private TrainMealOrderService trainMealOrderService;
    private TrainSeatOrderMapper trainSeatOrderMapper;
    private PaymentService paymentService;
    private TrainMealMapper trainMealMapper;
    private HttpSession session;
    private BindingResult bindingResult;

    @BeforeEach
    public void setUp() throws Exception {
        controller = new TrainMealOrderController();
        trainMealService = Mockito.mock(TrainMealService.class);
        trainMealOrderService = Mockito.mock(TrainMealOrderService.class);
        trainSeatOrderMapper = Mockito.mock(TrainSeatOrderMapper.class);
        paymentService = Mockito.mock(PaymentService.class);
        trainMealMapper = Mockito.mock(TrainMealMapper.class);
        session = Mockito.mock(HttpSession.class);
        bindingResult = Mockito.mock(BindingResult.class);

        var f1 = TrainMealOrderController.class.getDeclaredField("trainMealService");
        f1.setAccessible(true); f1.set(controller, trainMealService);
        var f2 = TrainMealOrderController.class.getDeclaredField("trainMealOrderService");
        f2.setAccessible(true); f2.set(controller, trainMealOrderService);
        var f3 = TrainMealOrderController.class.getDeclaredField("trainSeatOrderMapper");
        f3.setAccessible(true); f3.set(controller, trainSeatOrderMapper);
        var f4 = TrainMealOrderController.class.getDeclaredField("paymentService");
        f4.setAccessible(true); f4.set(controller, paymentService);
        var f5 = TrainMealOrderController.class.getDeclaredField("trainMealMapper");
        f5.setAccessible(true); f5.set(controller, trainMealMapper);
    }

    // getOrdersByUser 正向
    @Test
    public void testGetOrdersByUser_success() {
        User user = new User(); user.setId(1);
        Mockito.when(session.getAttribute("user")).thenReturn(user);

        TrainMealOrder order = new TrainMealOrder();
        order.setId(10); order.setOrderNumber("M1"); order.setQuantity(2);
        order.setTotalAmount(100.0);
        order.setTrainMealId(5); order.setSeatOrderId(7);
        order.setStatus(E_PaymentStatus.PENDING); order.setCreateTime(LocalDateTime.now());

        Mockito.when(trainMealOrderService.getOrdersByUser(1)).thenReturn(List.of(order));
        var meal = Mockito.mock(TrainMeal.class);
        Mockito.when(meal.getName()).thenReturn("套餐A");
        Mockito.when(trainMealMapper.selectById(5)).thenReturn(meal);
        var seatOrder = Mockito.mock(TrainSeatOrder.class);
        Mockito.when(seatOrder.getOrderNumber()).thenReturn("T123");
        Mockito.when(trainSeatOrderMapper.selectById(7)).thenReturn(seatOrder);

        ResponseEntity<?> resp = controller.getOrdersByUser(1, session);
        assertEquals(200, resp.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertEquals("查询成功", body.get("message"));
        List<?> data = (List<?>) body.get("data");
        assertEquals(1, data.size());
    }

    // getOrdersByUser 反向：未登录或ID不符
    @Test
    public void testGetOrdersByUser_notLoggedIn() {
        Mockito.when(session.getAttribute("user")).thenReturn(null);
        ResponseEntity<?> resp = controller.getOrdersByUser(1, session);
        assertEquals(401, resp.getStatusCodeValue());
    }

    @Test
    public void testGetOrdersByUser_idNotMatch() {
        User user = new User(); user.setId(2);
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        ResponseEntity<?> resp = controller.getOrdersByUser(1, session);
        assertEquals(401, resp.getStatusCodeValue());
    }

    // getOrdersBySeatOrder 正向
    @Test
    public void testGetOrdersBySeatOrder_success() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);

        TrainMealOrder order = new TrainMealOrder();
        Mockito.when(trainMealOrderService.getOrdersBySeatOrder(7)).thenReturn(List.of(order));

        ResponseEntity<?> resp = controller.getOrdersBySeatOrder(7, session);
        assertEquals(200, resp.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertEquals("查询成功", body.get("message"));
        assertEquals(1, ((List<?>) body.get("data")).size());
    }

    // getOrdersBySeatOrder 反向：未登录
    @Test
    public void testGetOrdersBySeatOrder_notLoggedIn() {
        Mockito.when(session.getAttribute("user")).thenReturn(null);
        ResponseEntity<?> resp = controller.getOrdersBySeatOrder(7, session);
        assertEquals(401, resp.getStatusCodeValue());
        assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
    }

    // createOrder 正向
    @Test
    public void testCreateOrder_success() {
        User user = new User(); user.setId(1);
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        TrainMealOrderDTO dto = new TrainMealOrderDTO();
        dto.setTicketReservationId(8); dto.setTrainMealId(5);

        TrainSeatOrder seat = new TrainSeatOrder();
        seat.setStatus(E_PaymentStatus.COMPLETED);
        Mockito.when(trainSeatOrderMapper.selectById(8)).thenReturn(seat);

        TrainMeal meal = new TrainMeal();
        meal.setTrainId(99);
        Mockito.when(trainMealService.getMealById(5)).thenReturn(meal);
        Mockito.when(trainSeatOrderMapper.existsByTrainAndUser(99, 1)).thenReturn(true);

        TrainMealOrder order = new TrainMealOrder();
        order.setId(10); order.setOrderNumber("M1");
        order.setTrainMealId(5); order.setSeatOrderId(8);
        Mockito.when(trainMealOrderService.createOrder(any())).thenReturn(order);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.createOrder(dto, bindingResult, session);
            assertEquals(200, resp.getStatusCodeValue());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("列车餐预订成功", body.get("message"));
            assertEquals(10, body.get("id"));
        }
    }

    // createOrder 反向：参数校验失败
    @Test
    public void testCreateOrder_paramError() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(true);

        TrainMealOrderDTO dto = new TrainMealOrderDTO();

        Map<String, Object> errorBody = Map.of("error", "参数错误");
        ResponseEntity<Map<String, Object>> errorResp = ResponseEntity.status(400).body(errorBody);
        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(errorResp);

            ResponseEntity<?> resp = controller.createOrder(dto, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
        }
    }

    // createOrder 反向：未购票
    @Test
    public void testCreateOrder_noTicket() {
        User user = new User(); user.setId(1);
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        TrainMealOrderDTO dto = new TrainMealOrderDTO();
        dto.setTicketReservationId(8); dto.setTrainMealId(5);

        TrainSeatOrder seat = new TrainSeatOrder();
        seat.setStatus(E_PaymentStatus.PENDING);
        Mockito.when(trainSeatOrderMapper.selectById(8)).thenReturn(seat);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.createOrder(dto, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
        }
    }

    // createOrder 反向：未在对应列车购票
    @Test
    public void testCreateOrder_noTrainTicket() {
        User user = new User(); user.setId(1);
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        TrainMealOrderDTO dto = new TrainMealOrderDTO();
        dto.setTicketReservationId(8); dto.setTrainMealId(5);

        TrainSeatOrder seat = new TrainSeatOrder();
        seat.setStatus(E_PaymentStatus.COMPLETED);
        Mockito.when(trainSeatOrderMapper.selectById(8)).thenReturn(seat);

        TrainMeal meal = new TrainMeal();
        meal.setTrainId(99);
        Mockito.when(trainMealService.getMealById(5)).thenReturn(meal);
        Mockito.when(trainSeatOrderMapper.existsByTrainAndUser(99, 1)).thenReturn(false);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.createOrder(dto, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
        }
    }

    // getOrderPaymentStatus 正向
    @Test
    public void testGetOrderPaymentStatus_success() {
        TrainMealOrder order = new TrainMealOrder();
        order.setRelatedPaymentId(123);
        Mockito.when(trainMealOrderService.getOrderById(1)).thenReturn(order);

        PaymentResultDTO result = new PaymentResultDTO();
        Mockito.when(paymentService.checkPaymentStatus(123)).thenReturn(result);

        ResponseEntity<?> resp = controller.getOrderPaymentStatus(1);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(result, resp.getBody());
    }

    // getOrderPaymentStatus 反向：订单不存在
    @Test
    public void testGetOrderPaymentStatus_notFound() {
        Mockito.when(trainMealOrderService.getOrderById(2)).thenReturn(null);
        ResponseEntity<?> resp = controller.getOrderPaymentStatus(2);
        assertEquals(404, resp.getStatusCodeValue());
    }

    // refund 正向
    @Test
    public void testRefund_success() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        PaymentRequest req = new PaymentRequest();
        req.setOrderNumber("M1");
        req.setData(Map.of());

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
}