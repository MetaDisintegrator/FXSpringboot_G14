package org.fxtravel.fxspringboot.service.impl.common;

import jakarta.transaction.Transactional;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.common.E_PaymentType;
import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.event.EventType;
import org.fxtravel.fxspringboot.event.data.PaymentInfo;
import org.fxtravel.fxspringboot.mapper.PaymentMapper;
import org.fxtravel.fxspringboot.pojo.dto.payment.PaymentResultDTO;
import org.fxtravel.fxspringboot.pojo.entities.payment;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {


    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private EventCenter eventCenter;

    private final Map<String, CompletableFuture<Boolean>> pendingPayments = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final DateTimeFormatter ORDER_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // -------------------- 基础支付操作实现 --------------------
    @Override
    @Transactional
    public payment createPayment(Integer userId, E_PaymentType type, Double amount,
                                 Integer relatedId, Integer quantity, Integer goodId) {
        if (userId == null || type == null || amount == null || relatedId == null ||
                quantity == null || goodId == null || amount < 0 || quantity <= 0) {
            throw new IllegalArgumentException("Invalid payment parameters");
        }
        payment payment = new payment();
        payment.setUserId(userId);
        payment.setType(type);
        payment.setAmount(amount);
        payment.setRelatedId(relatedId);
        payment.setStatus(E_PaymentStatus.IDLE);
        payment.setOrderNumber(generateOrderNumber(type));
        payment.setTimeoutSeconds(0L);
        payment.setQuantity(quantity);
        payment.setGoodId(goodId);

        paymentMapper.insert(payment);
        notifyPaymentCreated(type, relatedId, quantity, goodId, userId);

        return payment;
    }

    // 状态变更通知
    private void notifyPaymentStatusChanged(E_PaymentType type, Integer relatedId,
                                            E_PaymentStatus newStatus, Integer quantity, Integer goodId,
                                            Integer userId, Object data) {
        PaymentInfo info = new PaymentInfo(relatedId, goodId, quantity, newStatus, userId, data);

        switch (type) {
            case TRAIN_TICKET:
                eventCenter.publish(EventType.TT_STATUS_CHANGED, info);
            case TRAIN_MEAL:
                eventCenter.publish(EventType.TM_STATUS_CHANGED, info);
            case HOTEL:
                eventCenter.publish(EventType.HT_STATUS_CHANGED, info);
        }
    }

    // 创建通知
    public void notifyPaymentCreated(E_PaymentType type, Integer relatedId,
                                     Integer quantity, Integer goodId, Integer userId) {
        PaymentInfo info = new PaymentInfo(relatedId, goodId, quantity, E_PaymentStatus.IDLE, userId, null);

        switch (type) {
            case TRAIN_TICKET:
                eventCenter.publish(EventType.TT_CREATED, info);
                break;
            case TRAIN_MEAL:
                eventCenter.publish(EventType.TM_CREATED, info);
                break;
            case HOTEL:
                eventCenter.publish(EventType.HT_CREATED, info);
                break;
        }
    }

    @Override
    public boolean completePayment(String orderNumber, Object data) {
        // 检查当前状态是否为PENDING
        payment payment = paymentMapper.selectByOrderNumber(orderNumber);
        if (payment == null || payment.getStatus() != E_PaymentStatus.PENDING) {
            return false;
        }

        // 更新支付状态
        int result = paymentMapper.updateStatus(orderNumber, E_PaymentStatus.COMPLETED, LocalDateTime.now());
        if (result > 0) {
            payment.setStatus(E_PaymentStatus.COMPLETED);
            notifyPaymentStatusChanged(payment.getType(), payment.getRelatedId(),
                    E_PaymentStatus.COMPLETED, payment.getQuantity(),
                    payment.getGoodId(), payment.getUserId(), data);
        }

        // 通知等待中的支付流程
        CompletableFuture<Boolean> future = pendingPayments.remove(orderNumber);
        if (future != null) {
            future.complete(true);
        }

        return result > 0;
    }

    @Override
    public boolean failPayment(String orderNumber, Object data) {
        // 检查当前状态是否为PENDING
        payment payment = paymentMapper.selectByOrderNumber(orderNumber);
        if (payment == null ||
                payment.getStatus() != E_PaymentStatus.PENDING &&
                payment.getStatus() != E_PaymentStatus.IDLE) {
            return false;
        }

        // 更新支付状态
        int result = paymentMapper.updateStatus(orderNumber, E_PaymentStatus.FAILED, LocalDateTime.now());
        if (result > 0) {
            payment.setStatus(E_PaymentStatus.FAILED);
            notifyPaymentStatusChanged(payment.getType(), payment.getRelatedId(),
                    E_PaymentStatus.FAILED, payment.getQuantity(),
                    payment.getGoodId(), payment.getUserId(), data);
        }

        // 通知等待中的支付流程
        CompletableFuture<Boolean> future = pendingPayments.remove(orderNumber);
        if (future != null) {
            future.complete(false);
        }

        return result > 0;
    }

    @Override
    public boolean refundPayment(String orderNumber, Object data) {
        // 检查当前状态是否为COMPLETED(且不是FINISHED)
        payment payment = paymentMapper.selectByOrderNumber(orderNumber);

        System.out.println(payment);
        System.out.println(orderNumber);

        if (payment == null ||
                payment.getStatus() != E_PaymentStatus.COMPLETED) {
            return false;
        }

        int result = paymentMapper.updateStatus(orderNumber, E_PaymentStatus.REFUNDED, LocalDateTime.now());
        if (result > 0) {
            payment.setStatus(E_PaymentStatus.REFUNDED);
            notifyPaymentStatusChanged(payment.getType(), payment.getRelatedId(),
                    E_PaymentStatus.REFUNDED, payment.getQuantity(),
                    payment.getGoodId(), payment.getUserId(), data);
        }

        return result > 0;
    }

    @Override
    public boolean finishPayment(String orderNumber, Object data) {
        // 检查当前状态是否为COMPLETED
        payment payment = paymentMapper.selectByOrderNumber(orderNumber);
        if (payment == null || payment.getStatus() != E_PaymentStatus.COMPLETED) {
            return false;
        }

        int result = paymentMapper.updateStatus(orderNumber, E_PaymentStatus.FINISHED, LocalDateTime.now());
        if (result > 0) {
            payment.setStatus(E_PaymentStatus.FINISHED);
            notifyPaymentStatusChanged(payment.getType(), payment.getRelatedId(),
                    E_PaymentStatus.FINISHED, payment.getQuantity(),
                    payment.getGoodId(), payment.getUserId(),data);
        }

        return result > 0;
    }

    // -------------------- 支付模拟接口实现 --------------------
    @Override
    public PaymentResultDTO simulatePaymentProcess(String orderNumber, long timeout
            , Supplier<Boolean> inventoryDeduction, Supplier<Object> data) {
        PaymentResultDTO result = new PaymentResultDTO();
        result.setOrderNumber(orderNumber);

        // 检查订单是否存在
        payment payment = paymentMapper.selectByOrderNumber(orderNumber);
        if (payment == null) {
            result.setCurrentStatus(null);
            result.setMessage("Order not found");
            result.setRemainingTimeSeconds(0L);
            return result;
        }

        // 获取当前状态
        E_PaymentStatus currentStatus = payment.getStatus();
        result.setCurrentStatus(currentStatus);

        // 如果状态不是IDLE(未开启)，直接返回当前状态
        if (currentStatus != E_PaymentStatus.IDLE) {
            result.setMessage(currentStatus == E_PaymentStatus.PENDING ? "Payment already in progress"
                    : "Payment already processed");
            // 计算剩余时间
            if (currentStatus == E_PaymentStatus.PENDING) {
                result.setRemainingTimeSeconds(calculateRemainingTime(payment));
            } else {
                result.setRemainingTimeSeconds(0L);
            }

            return result;
        }

        // 库存检查（原子性）
        if (!inventoryDeduction.get()) {
            failPayment(orderNumber, data.get());
            result.setCurrentStatus(E_PaymentStatus.FAILED);
            result.setRemainingTimeSeconds(0L);
            result.setMessage("库存不足，订单创建失败");
            return result;
        }

        // 更新支付记录，设置状态和超时时间
        payment.setStatus(E_PaymentStatus.PENDING);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setTimeoutSeconds(timeout);
        paymentMapper.updateById(payment);

        result.setCurrentStatus(E_PaymentStatus.PENDING);
        result.setRemainingTimeSeconds(timeout);
        result.setMessage("Payment processing started");

        // 模拟支付处理
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        pendingPayments.put(orderNumber, future);

        // 设置超时处理
        scheduler.schedule(() -> {
            if (!future.isDone()) {
                // 检查状态是否仍然是PENDING（可能已经被其他操作改变）
                payment currentPayment = paymentMapper.selectByOrderNumber(orderNumber);
                if (currentPayment != null && currentPayment.getStatus() == E_PaymentStatus.PENDING) {
                    failPayment(orderNumber, data.get());

                    future.complete(false);
                    pendingPayments.remove(orderNumber);
                }
            }
        }, timeout, TimeUnit.SECONDS);

        return result;
    }

    @Override
    public PaymentResultDTO checkPaymentStatus(String orderNumber) {
        payment payment = paymentMapper.selectByOrderNumber(orderNumber);
        return getPaymentResultDTO(payment);
    }

    @Override
    public PaymentResultDTO checkPaymentStatus(Integer paymentId) {
        payment payment = paymentMapper.selectById(paymentId);
        return getPaymentResultDTO(payment);
    }

    // -------------------- 私有方法 --------------------
    private String generateOrderNumber(E_PaymentType type) {
        String prefix = "";
        switch (type) {
            case TRAIN_TICKET:
                prefix = "TT";
                break;
            case TRAIN_MEAL:
                prefix = "TM";
                break;
            case HOTEL:
                prefix = "HT";
                break;
        }
        String timestamp = LocalDateTime.now().format(ORDER_NUMBER_DATE_FORMAT);
        String random = String.format("%04d", new Random().nextInt(10000));
        return prefix + timestamp + random;
    }

    // 计算剩余时间
    private long calculateRemainingTime(payment payment) {
        if (payment.getStatus() != E_PaymentStatus.PENDING ||
                payment.getPaymentTime() == null ||
                payment.getTimeoutSeconds() == null) {
            return 0L;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutTime = payment.getPaymentTime().plusSeconds(payment.getTimeoutSeconds());
        long remaining = Duration.between(now, timeoutTime).getSeconds();
        return Math.max(0, remaining);
    }

    private PaymentResultDTO getPaymentResultDTO(payment payment) {
        PaymentResultDTO result = new PaymentResultDTO();
        if (payment == null) {
            result.setCurrentStatus(null);
            result.setMessage("Order not found");
            result.setRemainingTimeSeconds(0L);
            return result;
        }

        result.setCurrentStatus(payment.getStatus());
        result.setOrderNumber(payment.getOrderNumber());

        if (payment.getStatus() == E_PaymentStatus.PENDING) {
            result.setRemainingTimeSeconds(calculateRemainingTime(payment));
            result.setMessage("Payment in progress");
        } else {
            result.setRemainingTimeSeconds(0L);
            result.setMessage("Payment not in progress");
        }
        return result;
    }
}
