package org.fxtravel.fxspringboot.controller.trainmeal;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatOrderMapper;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentRequest;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentResultDTO;
import org.fxtravel.fxspringboot.pojo.dto.trainmeal.TrainMealOrderDTO;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMeal;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMealOrder;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.trainmeal.TrainMealOrderService;
import org.fxtravel.fxspringboot.service.inter.trainmeal.TrainMealService;
import org.fxtravel.fxspringboot.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/train/meal")
public class TrainMealOrderController {

    @Autowired
    private TrainMealService trainMealService;

    @Autowired
    private TrainMealOrderService trainMealOrderService;

    @Autowired
    private TrainSeatOrderMapper trainSeatOrderMapper;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/orders/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Integer userId,
                                                                HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getId() != userId) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<TrainMealOrder> orders = trainMealOrderService.getOrdersByUser(userId);
        return ResponseEntity.ok(Map.of(
                "message", "查询成功",
                "data", orders
        ));
    }

    @GetMapping("/orders/by-ticket/{seatOrderId}")
    public ResponseEntity<?> getOrdersBySeatOrder(
            @PathVariable Integer seatOrderId, BindingResult bindingResult,
            HttpSession session) {
        User user = (User) session.getAttribute("user");

        ResponseEntity<? extends Map<String, ?>> errors = AuthUtil.check(bindingResult, user);
        if (errors != null) return errors;

        List<TrainMealOrder> orders = trainMealOrderService.getOrdersBySeatOrder(seatOrderId);
        return ResponseEntity.ok(Map.of(
                "message", "查询成功",
                "data", orders
        ));
    }

    @PostMapping("/get")
    public ResponseEntity<?> createOrder(@Valid @RequestBody TrainMealOrderDTO orderDTO
            , BindingResult bindingResult, HttpSession session) {
        User user = (User) session.getAttribute("user");

        ResponseEntity<? extends Map<String, ?>> errors = AuthUtil.check(bindingResult, user);
        if (errors != null) return errors;

        // 必须有购票
        TrainMeal meal = trainMealService.getMealById(orderDTO.getTrainMealId());
        if (!trainSeatOrderMapper.existsBySeatAndUser(meal.getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "未在对应列车上购票"));
        }

        TrainMealOrder order = trainMealOrderService.createOrder(orderDTO);
        return ResponseEntity.ok(Map.of(
                "message", "列车餐预订成功",
                "id", order.getId(),
                "number", order.getOrderNumber(),
                "meal", order.getTrainMealId(),
                "seatOrder", order.getSeatOrderNumber()
        ));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderPaymentStatus(@PathVariable Integer orderId) {
        TrainMealOrder order = trainMealOrderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        PaymentResultDTO result = order.getRelatedPaymentId() != null ?
                paymentService.checkPaymentStatus(order.getRelatedPaymentId()) : null;
        return ResponseEntity.ok(result);
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refund(@Valid @RequestBody PaymentRequest request,
                                    BindingResult bindingResult, HttpSession session) {
        User user = (User) session.getAttribute("user");

        ResponseEntity<? extends Map<String, ?>> errors = AuthUtil.check(bindingResult, user);
        if (errors != null) return errors;

        return ResponseEntity.ok(paymentService.refundPayment(request.getOrderNumber(), request.getData()));
    }
}
