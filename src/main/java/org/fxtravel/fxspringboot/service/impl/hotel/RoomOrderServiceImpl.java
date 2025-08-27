package org.fxtravel.fxspringboot.service.impl.hotel;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.common.E_PaymentType;
import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.event.EventType;
import org.fxtravel.fxspringboot.event.data.PaymentInfo;
import org.fxtravel.fxspringboot.mapper.hotel.RoomOrderMapper;
import org.fxtravel.fxspringboot.pojo.dto.hotel.BookHotelRequest;
import org.fxtravel.fxspringboot.pojo.entities.Hotel;
import org.fxtravel.fxspringboot.pojo.entities.Room;
import org.fxtravel.fxspringboot.pojo.entities.RoomOrder;
import org.fxtravel.fxspringboot.pojo.entities.payment;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.fxtravel.fxspringboot.service.inter.hotel.HotelService;
import org.fxtravel.fxspringboot.service.inter.hotel.RoomOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RoomOrderServiceImpl implements RoomOrderService {
    @Autowired
    RoomOrderMapper roomOrderMapper;

    @Autowired
    HotelService hotelService;
    @Autowired
    PaymentService paymentService;

    @Autowired
    private EventCenter eventCenter;

    @PostConstruct
    public void init() {
        // 注册回调，确保在服务启动时就注册
        eventCenter.subscribe(EventType.HT_STATUS_CHANGED, this::handlePaymentStatusChange);
    }

    @PreDestroy
    public void destroy() {
        // 服务关闭时注销回调
        eventCenter.unsubscribe(EventType.HT_STATUS_CHANGED, this::handlePaymentStatusChange);
    }

    // 处理支付状态变更的回调方法
    private void handlePaymentStatusChange(PaymentInfo info) {
        roomOrderMapper.updateStatus(info.getOrderId(), info.getNewStatus());
    }

    @Override
    public List<RoomOrder> getOrdersByUserId(Integer userId) {
        return roomOrderMapper.findByUserID(userId);
    }

    @Override
    public RoomOrder getOrderById(Integer orderId) {
        return roomOrderMapper.selectById(orderId);
    }

    @Override
    public RoomOrder createOrder(BookHotelRequest request) {
        // 1. 基础验证
        Hotel hotel = hotelService.getHotelById(request.getHotelId());
        Room room = hotelService.getRoomById(request.getRoomId());
        if (hotel == null || room == null) {
            throw new IllegalArgumentException("酒店或房型不存在");
        }

        // 2. 计算入住天数和总价
        long nights = ChronoUnit.DAYS.between(
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        Double totalAmount = room.getPricePerNight() * nights;

        // 3. 创建订单
        RoomOrder order = new RoomOrder();
        order.setUserId(request.getUserId());
        order.setHotelId(request.getHotelId());
        order.setRoomId(request.getRoomId());
        order.setCheckInDate(request.getCheckInDate());
        order.setCheckOutDate(request.getCheckOutDate());
        order.setTotalAmount(totalAmount);  // 使用计算后的总价
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(E_PaymentStatus.IDLE);
        roomOrderMapper.insert(order);

        // 4. 创建支付记录
        payment payment = paymentService.createPayment(
                order.getUserId(),
                E_PaymentType.HOTEL,
                order.getTotalAmount(),  // 传递计算后的总价
                order.getId(),
                1,
                room.getId()
        );

        // 5. 更新订单支付信息
        order.setRelatedPaymentId(payment.getId());
        order.setOrderNumber(payment.getOrderNumber());
        roomOrderMapper.updateById(order);

        // 6. 模拟支付流程（保持原有逻辑）
        paymentService.simulatePaymentProcess(
                payment.getOrderNumber(),
                30,
                () -> hotelService.checkAndGet(room.getId(), 1, null),
                () -> null
        );

        return order;
    }
}
