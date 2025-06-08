package org.fxtravel.fxspringboot.pojo.dto.trainmeal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainMealOrderResponse {
    private int id;
    private String OrderNumber;
    private int quantity;
    private double totalAmount;
    private String trainMealName;
    private String reservationSeatOrderNumber;
    private E_PaymentStatus status;
    private LocalDateTime createTime;
}
