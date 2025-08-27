package controller.hotel;

import jakarta.servlet.http.HttpSession;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.common.Role;
import org.fxtravel.fxspringboot.controller.hotel.RoomOrderController;
import org.fxtravel.fxspringboot.mapper.hotel.HotelMapper;
import org.fxtravel.fxspringboot.mapper.hotel.RoomMapper;
import org.fxtravel.fxspringboot.pojo.dto.hotel.BookHotelRequest;
import org.fxtravel.fxspringboot.pojo.dto.hotel.RoomOrderResponse;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentRequest;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentResultDTO;
import org.fxtravel.fxspringboot.pojo.entities.RoomOrder;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.hotel.RoomOrderService;
import org.fxtravel.fxspringboot.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class RoomOrderControllerTest {

    private RoomOrderService roomOrderService;
    private PaymentService paymentService;
    private HotelMapper hotelMapper;
    private RoomMapper roomMapper;
    private RoomOrderController controller;
    private HttpSession session;
    private BindingResult bindingResult;

    @BeforeEach
    public void setUp() {
        roomOrderService = Mockito.mock(RoomOrderService.class);
        paymentService = Mockito.mock(PaymentService.class);
        hotelMapper = Mockito.mock(HotelMapper.class);
        roomMapper = Mockito.mock(RoomMapper.class);
        controller = new RoomOrderController();
        session = Mockito.mock(HttpSession.class);
        bindingResult = Mockito.mock(BindingResult.class);

        // 反射注入依赖
        try {
            var f1 = RoomOrderController.class.getDeclaredField("roomOrderService");
            f1.setAccessible(true);
            f1.set(controller, roomOrderService);
            var f2 = RoomOrderController.class.getDeclaredField("paymentService");
            f2.setAccessible(true);
            f2.set(controller, paymentService);
            var f3 = RoomOrderController.class.getDeclaredField("hotelMapper");
            f3.setAccessible(true);
            f3.set(controller, hotelMapper);
            var f4 = RoomOrderController.class.getDeclaredField("roomMapper");
            f4.setAccessible(true);
            f4.set(controller, roomMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // getRoom 正向
    @Test
    public void testGetRoom_success() {
        User user = new User();
        user.setId(1);
        user.setRole(Role.REGULAR);
        when(session.getAttribute("user")).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        BookHotelRequest req = new BookHotelRequest();
        req.setUserId(1);

        RoomOrder order = new RoomOrder();
        order.setId(10);
        order.setOrderNumber("A123");
        order.setRoomId(5);
        order.setTotalAmount(1000.0);
        when(roomOrderService.createOrder(any())).thenReturn(order);

        // mock AuthUtil.check 返回 null
        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.getRoom(req, bindingResult, session);
            assertEquals(200, resp.getStatusCodeValue());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("预订酒店成功", body.get("message"));
            assertEquals(order.getId(), body.get("id"));
        }
    }

    // getRoom 反向：参数校验失败
    @Test
    public void testGetRoom_paramError() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(true);

        BookHotelRequest req = new BookHotelRequest();

        Map<String, Object> errorBody = Map.of("error", "参数错误");
        ResponseEntity<Map<String, Object>> errorResp = ResponseEntity.status(400).body(errorBody);
        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(errorResp);

            ResponseEntity<?> resp = controller.getRoom(req, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
        }
    }

    // getRoom 反向：无权限
    @Test
    public void testGetRoom_noPermission() {
        User user = new User();
        user.setId(2);
        user.setRole(Role.REGULAR);
        when(session.getAttribute("user")).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        BookHotelRequest req = new BookHotelRequest();
        req.setUserId(1);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.getRoom(req, bindingResult, session);
            assertEquals(403, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
        }
    }

    // getRoom 反向：服务异常
    @Test
    public void testGetRoom_serviceException() {
        User user = new User();
        user.setId(1);
        user.setRole(Role.REGULAR);
        when(session.getAttribute("user")).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        BookHotelRequest req = new BookHotelRequest();
        req.setUserId(1);

        when(roomOrderService.createOrder(any())).thenThrow(new RuntimeException("数据库异常"));

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.getRoom(req, bindingResult, session);
            assertEquals(500, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).get("error").toString().contains("预订酒店失败"));
        }
    }

    // getOrderPaymentStatus 正向
    @Test
    public void testGetOrderPaymentStatus_success() {
        RoomOrder order = new RoomOrder();
        order.setRelatedPaymentId(123);
        when(roomOrderService.getOrderById(1)).thenReturn(order);

        PaymentResultDTO result = new PaymentResultDTO();
        when(paymentService.checkPaymentStatus(123)).thenReturn(result);

        ResponseEntity<?> resp = controller.getOrderPaymentStatus(1);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(result, resp.getBody());
    }

    // getOrderPaymentStatus 反向：订单不存在
    @Test
    public void testGetOrderPaymentStatus_notFound() {
        when(roomOrderService.getOrderById(2)).thenReturn(null);

        ResponseEntity<?> resp = controller.getOrderPaymentStatus(2);
        assertEquals(404, resp.getStatusCodeValue());
    }

    // getOrderByUserId 正向
    @Test
    public void testGetOrderByUserId_success() {
        RoomOrder order = new RoomOrder();
        order.setId(1);
        order.setOrderNumber("A1");
        order.setUserId(1);
        order.setHotelId(2);
        order.setRoomId(3);
        order.setCheckInDate(LocalDate.now());
        order.setCheckOutDate(LocalDate.now().plusDays(1));
        order.setStatus(E_PaymentStatus.PENDING);
        order.setTotalAmount(500.0);
        order.setCreateTime(LocalDateTime.now());

        when(roomOrderService.getOrdersByUserId(1)).thenReturn(List.of(order));
        var hotel = Mockito.mock(org.fxtravel.fxspringboot.pojo.entities.Hotel.class);
        when(hotel.getName()).thenReturn("豪华酒店");
        when(hotelMapper.selectById(2)).thenReturn(hotel);
        var room = Mockito.mock(org.fxtravel.fxspringboot.pojo.entities.Room.class);
        when(room.getName()).thenReturn("大床房");
        when(roomMapper.selectById(3)).thenReturn(room);

        ResponseEntity<List<RoomOrderResponse>> resp = controller.getOrderByUserId(1);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().size());
        assertEquals("豪华酒店", resp.getBody().get(0).getHotelName());
    }

    // getOrderByUserId 反向：无订单
    @Test
    public void testGetOrderByUserId_empty() {
        when(roomOrderService.getOrdersByUserId(2)).thenReturn(Collections.emptyList());

        ResponseEntity<List<RoomOrderResponse>> resp = controller.getOrderByUserId(2);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isEmpty());
    }

    // refund 正向
    @Test
    public void testRefund_success() {
        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        PaymentRequest req = new PaymentRequest();
        req.setOrderNumber("A1");
        req.setData(Map.of());

        when(paymentService.refundPayment(anyString(), anyMap())).thenReturn(true);

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
        when(session.getAttribute("user")).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(true);

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