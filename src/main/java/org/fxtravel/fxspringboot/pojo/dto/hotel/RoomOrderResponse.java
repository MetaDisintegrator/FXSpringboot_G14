package org.fxtravel.fxspringboot.pojo.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomOrderResponse {
    private Integer id;// 订单唯⼀标识
    private String orderNumber;
    private int userId;// 下单⽤户 ID
    private String hotelName;
    private String roomName;
    private LocalDate checkInDate;// ⼊住⽇期（YYYY-MM-DD）
    private LocalDate checkOutDate;// 离店⽇期（YYYY-MM-DD）
    private E_PaymentStatus status;// 订单状态
    private Double totalAmount;// 订单总⾦额（元）
    private LocalDateTime createTime; // 创建时间
}
