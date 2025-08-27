package org.fxtravel.fxspringboot.pojo.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "room_order")
public class RoomOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;// 订单唯⼀标识

    private String orderNumber;
    @Column(name = "user_id", nullable = false)
    private int userId;// 下单⽤户 ID
    @Column(name = "hotel_id", nullable = false)
    private Integer hotelId;// 关联酒店 ID
    @Column(name = "room_id", nullable = false)
    private Integer roomId;// 关联房型 ID
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;// ⼊住⽇期（YYYY-MM-DD）
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;// 离店⽇期（YYYY-MM-DD）
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private E_PaymentStatus status;// 订单状态
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;// 订单总⾦额（元）
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime; // 创建时间
    @Column(name = "related_payment_id")
    private Integer relatedPaymentId;// 关联支付系统
}
