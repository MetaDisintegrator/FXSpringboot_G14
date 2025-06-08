package org.fxtravel.fxspringboot.controller.trainseat;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/train")
public class TrainSeatOrderController {
    @Autowired
    private TrainSeatOrderService trainSeatOrderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private TrainSeatMapper trainSeatMapper;
    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private TrainMealOrderMapper trainMealOrderMapper;

    // 根据座次生成车票接口
    @PostMapping("/ticket/get")
    public ResponseEntity<?> getTicket(@Valid @RequestBody GetTicketRequest request,
                                       BindingResult bindingResult,
                                       HttpSession session) {
//        if (bindingResult.hasErrors()) {
//            List<String> errors = bindingResult.getFieldErrors()
//                    .stream()
//                    .map(FieldError::getDefaultMessage)
//                    .toList();
//            return ResponseEntity.badRequest().body(Map.of("errors", errors));
//        }
//
//        User user = (User) session.getAttribute("user");
//        if (user == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "未登录"));
//        }
//
//        // 验证用户只能为自己生成车票（除非是管理员）
//        if (!user.getRole().equals(Role.ADMIN) && user.getId() != (request.getUserId())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限为其他用户生成车票"));
//        }

        try {
            TrainSeatOrder order = trainSeatOrderService.createOrder(request);

            return ResponseEntity.ok(Map.of(
                    "message", "车票生成成功",
                    "id", order.getId(),
                    "number", order.getOrderNumber(),
                    "seat", order.getSeatNumber()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "生成车票失败: " + e.getMessage()));
        }
    }

    @GetMapping("/ticket/{orderId}")
    public ResponseEntity<PaymentResultDTO> getOrderPaymentStatus(@PathVariable Integer orderId) {
        TrainSeatOrder order = trainSeatOrderService.getOrderById(orderId);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id not found");
        }

        PaymentResultDTO result = order.getRelatedPaymentId() != null ?
                paymentService.checkPaymentStatus(order.getRelatedPaymentId()) : null;

        return ResponseEntity.ok(result);
    }

    @GetMapping("/order/get/{userId}")
    public ResponseEntity<List<TrainSeatOrderDTO>> getTicket(@PathVariable Integer userId) {
        // 1. 获取用户订单
        List<TrainSeatOrder> orders = trainSeatOrderService.getOrdersByUserId(userId);
        if (orders.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }


        // 3. 使用Stream转换DTO
        List<TrainSeatOrderDTO> dtos = orders.stream()
                .map(order -> {
                    Train train = trainMapper.selectById(order.getTrainSeatId());
                    TrainSeat seat = trainSeatMapper.selectById(order.getTrainSeatId());

                    return new TrainSeatOrderDTO(
                            order.getId(),
                            order.getUserId(),
                            train,
                            seat,  // 使用预加载的座位数据
                            order.getSeatNumber(),
                            order.getRelatedPaymentId(),
                            order.getTotalAmount(),
                            order.getStatus(),
                            order.getCreateTime()
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refund(@Valid @RequestBody PaymentRequest request,
                                    BindingResult bindingResult, HttpSession session) {
        User user = (User) session.getAttribute("user");

        ResponseEntity<? extends Map<String, ?>> errors = AuthUtil.check(bindingResult, user);
        if (errors != null) return errors;

        TrainSeatOrder order = trainSeatOrderService.getOrderByNumber(request.getOrderNumber());
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        if (trainMealOrderMapper.existsBySeatOrderId(order.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("有未取消的餐品");
        }

        return ResponseEntity.ok(paymentService.refundPayment(request.getOrderNumber(), request.getData()));
    }
}
