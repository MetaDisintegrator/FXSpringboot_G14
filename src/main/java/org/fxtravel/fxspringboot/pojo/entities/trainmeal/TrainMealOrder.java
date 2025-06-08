package org.fxtravel.fxspringboot.pojo.entities.trainmeal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;

import java.time.LocalDateTime;

// 列车餐订单实体
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "train_meal_order")
public class TrainMealOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String orderNumber;
    @Column(nullable = false)
    private Integer userId; // 用户ID
    @Column(nullable = false)
    private Integer seatOrderNumber; // 车票预购表ID
    @Column(nullable = false)
    private Integer trainMealId; // 列车餐食ID
    private Integer relatedPaymentId; // 支付模拟系统相关
    @Column(nullable = false)
    private Integer quantity; // 购买数量
    @Column(nullable = false)
    private Double totalAmount; // 总金额
    @Enumerated(EnumType.STRING)
    private E_PaymentStatus status; // 与关联的payment状态同步，只是便于查询使用
    private LocalDateTime createTime; // 创建时间
}
