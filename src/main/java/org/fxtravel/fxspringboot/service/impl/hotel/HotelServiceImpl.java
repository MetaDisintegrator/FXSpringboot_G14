package org.fxtravel.fxspringboot.service.impl.hotel;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.fxtravel.fxspringboot.common.E_NotificationEventType;
import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.event.EventType;
import org.fxtravel.fxspringboot.event.data.PaymentInfo;
import org.fxtravel.fxspringboot.mapper.hotel.HotelMapper;
import org.fxtravel.fxspringboot.mapper.hotel.RoomMapper;
import org.fxtravel.fxspringboot.pojo.dto.hotel.HotelSearchResult;
import org.fxtravel.fxspringboot.pojo.dto.notification.NotificationRequestDTO;
import org.fxtravel.fxspringboot.pojo.dto.train.TrainSearchResult;
import org.fxtravel.fxspringboot.pojo.entities.*;
import org.fxtravel.fxspringboot.service.inter.hotel.HotelService;
import org.fxtravel.fxspringboot.service.inter.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HotelServiceImpl implements HotelService {
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private RoomMapper roomMapper;

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
    public void handlePaymentStatusChange(PaymentInfo info) {
        switch (info.getNewStatus()){
            case FAILED:
            case REFUNDED:
                putBack(info.getGoodId(), info.getQuantity(), null);
                break;
        }
    }

    @Override
    public Hotel getHotelById(Integer id) {
        return hotelMapper.selectById(id);
    }

    @Override
    public Room getRoomById(Integer id) {
        return roomMapper.selectById(id);
    }

    @Override
    public List<HotelSearchResult> searchHotels(String destination, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return getResults(hotelMapper.findByDest(destination));
        }

        return getResults(hotelMapper.findByDestAndName(destination, pattern));
    }

    private List<HotelSearchResult> getResults(List<Hotel> hotels) {
        if (hotels.isEmpty()) {
            return null;
        }

        // 转换为搜索结果
        List<HotelSearchResult> results = new ArrayList<>();

        for (Hotel hotel : hotels) {
            List<Room> roomList = roomMapper.findByHotel(hotel.getId());
            if (roomList.isEmpty()) {
                continue;
            }
            // 创建搜索结果对象
            HotelSearchResult result = new HotelSearchResult();
            result.setHotel(hotel);
            result.setRooms(roomList);
            results.add(result);
        }

        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndGet(int id, int count, Object data) {
        // 1. 检查库存是否充足
        Room obj = roomMapper.selectById(id);
        if (obj == null || obj.getRemain() < count) {
            return false;
        }

        // 2. 扣减库存
        int updated = roomMapper.deduct(id, count);

        return updated > 0;
    }

    @Override
    public void putBack(int id, int count, Object data) {
        roomMapper.add(id, count);
    }
}