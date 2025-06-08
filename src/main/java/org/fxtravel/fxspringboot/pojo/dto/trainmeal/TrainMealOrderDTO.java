package org.fxtravel.fxspringboot.pojo.dto.trainmeal;

import jakarta.annotation.Nullable;
import lombok.Data;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;

import java.time.LocalDateTime;

// 列车餐订单DTO
@Data
public class TrainMealOrderDTO {
    @Nullable
    private Integer id;
    private Integer userId;
    private Integer ticketReservationId;
    private Integer trainMealId;
    private Integer quantity;
}
