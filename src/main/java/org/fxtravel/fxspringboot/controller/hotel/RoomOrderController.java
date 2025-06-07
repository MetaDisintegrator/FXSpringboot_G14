package org.fxtravel.fxspringboot.controller.hotel;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.fxtravel.fxspringboot.common.Role;
import org.fxtravel.fxspringboot.pojo.dto.hotel.BookHotelRequest;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentResultDTO;
import org.fxtravel.fxspringboot.pojo.entities.RoomOrder;
import org.fxtravel.fxspringboot.pojo.entities.TrainSeatOrder;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.hotel.RoomOrderService;
import org.fxtravel.fxspringboot.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel")
public class RoomOrderController {
    @Autowired
    private RoomOrderService roomOrderService;
    @Autowired
    private PaymentService paymentService;

    // 根据座次生成车票接口
    @PostMapping("/room/get")
    public ResponseEntity<?> getRoom(@Valid @RequestBody BookHotelRequest request,
                                       BindingResult bindingResult,
                                       HttpSession session) {
        User user = (User) session.getAttribute("user");

        ResponseEntity<? extends Map<String, ?>> errors = AuthUtil.check(bindingResult, user);
        if (errors != null) return errors;

        // 验证用户只能为自己操作
        if (!user.getRole().equals(Role.ADMIN) && user.getId() != (request.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限为其他用户操作"));
        }

        try {
            RoomOrder order = roomOrderService.createOrder(request);

            return ResponseEntity.ok(Map.of(
                    "message", "车票生成成功",
                    "id", order.getId(),
                    "number", order.getOrderNumber(),
                    "roomId", order.getRoomId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "生成车票失败: " + e.getMessage()));
        }
    }

    @GetMapping("/hotel/{orderId}")
    public ResponseEntity<PaymentResultDTO> getOrderPaymentStatus(@PathVariable Integer orderId) {
        RoomOrder order = roomOrderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        PaymentResultDTO result = order.getRelatedPaymentId() != null ?
                paymentService.checkPaymentStatus(order.getRelatedPaymentId()) : null;

        return ResponseEntity.ok(result);
    }

    @GetMapping("/hotel/orders/{userId}")
    public ResponseEntity<List<RoomOrder>> getOrderByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(roomOrderService.getOrdersByUserId(userId));
    }
}
