package org.fxtravel.fxspringboot.pojo.dto.trainmeal;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;

import java.time.LocalDateTime;

@Data
public class TrainMealOrderQueryResult {
    private Integer id;
    private String orderNumber;
    private Integer userId; // 用户ID
    private Integer seatOrderId; // 车票预购表ID
    private String seatOrderNumber;
    private Integer trainMealId; // 列车餐食ID
    private Integer relatedPaymentId; // 支付模拟系统相关
    private Integer quantity; // 购买数量
    private Double totalAmount; // 总金额
    @Enumerated(EnumType.STRING)
    private E_PaymentStatus status; // 与关联的payment状态同步，只是便于查询使用
    private LocalDateTime createTime; // 创建时间
}
