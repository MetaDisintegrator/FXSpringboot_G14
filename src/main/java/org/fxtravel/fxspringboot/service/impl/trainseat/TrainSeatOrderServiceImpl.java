package org.fxtravel.fxspringboot.service.impl.trainseat;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.common.E_PaymentType;
import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.event.EventType;
import org.fxtravel.fxspringboot.event.data.PaymentInfo;
import org.fxtravel.fxspringboot.mapper.PaymentMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatOrderMapper;
import org.fxtravel.fxspringboot.pojo.dto.train.GetTicketRequest;
import org.fxtravel.fxspringboot.pojo.entities.TrainSeat;
import org.fxtravel.fxspringboot.pojo.entities.TrainSeatOrder;
import org.fxtravel.fxspringboot.pojo.entities.payment;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.trainseat.TrainSeatOrderService;
import org.fxtravel.fxspringboot.service.inter.trainseat.TrainSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainSeatOrderServiceImpl implements TrainSeatOrderService {
    @Autowired
    TrainSeatMapper trainSeatMapper;
    @Autowired
    TrainSeatOrderMapper trainSeatOrderMapper;
    @Autowired
    PaymentMapper paymentMapper;
    @Autowired
    PaymentService paymentService;
    @Autowired
    TrainSeatService trainSeatService;

    @Autowired
    private EventCenter eventCenter;

    @PostConstruct
    public void init() {
        // 注册回调，确保在服务启动时就注册
        eventCenter.subscribe(EventType.TM_STATUS_CHANGED, this::handlePaymentStatusChange);
    }

    @PreDestroy
    public void destroy() {
        // 服务关闭时注销回调
        eventCenter.unsubscribe(EventType.TM_STATUS_CHANGED, this::handlePaymentStatusChange);
    }

    // 处理支付状态变更的回调方法
    private void handlePaymentStatusChange(PaymentInfo info) {
        trainSeatOrderMapper.updateStatus(info.getOrderId(), info.getNewStatus());
    }

    @Override
    public boolean existsByTrainAndUser(Integer trainId, Integer userId) {
        return trainSeatOrderMapper.existsByTrainAndUser(trainId, userId);
    }

    @Override
    public List<TrainSeatOrder> getOrdersByUserId(Integer userId) {
        return trainSeatOrderMapper.findByUserID(userId);
    }

    @Override
    public TrainSeatOrder getOrderById(Integer orderId) {
        return trainSeatOrderMapper.selectById(orderId);
    }

    @Override
    public TrainSeatOrder getOrderByNumber(String orderNumber) {
        return trainSeatOrderMapper.findByOrderNumber(orderNumber);
    }

    public TrainSeatOrder createOrder(GetTicketRequest request) {
        TrainSeat seat = trainSeatMapper.selectById(request.getSeatId());
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found");
        }
        TrainSeatOrder order = new TrainSeatOrder();

        order.setUserId(request.getUserId());
        order.setTrainSeatId(request.getSeatId());
        order.setTotalAmount(seat.getPrice());
        order.setTrainId(seat.getTrainId());
        order.setStatus(E_PaymentStatus.IDLE);
        order.setCreateTime(LocalDateTime.now());
        trainSeatOrderMapper.insert(order);

        payment payment = paymentService.createPayment(
                order.getUserId(),
                E_PaymentType.TRAIN_TICKET,
                order.getTotalAmount(),
                order.getId(),
                1,
                order.getTrainSeatId()
        );

        order.setRelatedPaymentId(payment.getId());
        order.setOrderNumber(payment.getOrderNumber());
        trainSeatOrderMapper.updateById(order);


        paymentService.simulatePaymentProcess(payment.getOrderNumber(), 30,
                () -> {
            String[] buf = new String[1];
            boolean res = trainSeatService.checkAndGet(seat.getId(), 1, buf);
            if (res) {
                order.setSeatNumber(buf[0]);
                trainSeatOrderMapper.updateById(order);
            }
            return res;
        }, order::getSeatNumber);
        return order;
    }
}
