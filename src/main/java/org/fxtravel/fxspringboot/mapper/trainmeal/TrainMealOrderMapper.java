package org.fxtravel.fxspringboot.mapper.trainmeal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.pojo.dto.trainmeal.TrainMealOrderQueryDTO;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMealOrder;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainMealOrderMapper extends BaseMapper<TrainMealOrder> {
    @Select("SELECT * FROM train_meal_order WHERE user_id = #{userId}")
    List<TrainMealOrder> selectByUser(Integer userId);

    @Select("SELECT * FROM train_meal_order WHERE seat_order_Id = #{seatOrderId}")
    List<TrainMealOrder> selectBySeatOrder(Integer seatOrderId);

    @Select("SELECT EXISTS(SELECT 1 FROM train_meal_order WHERE seat_order_Id = #{seatOrderId})")
    boolean existsBySeatOrderId(Integer seatOrderId);

    // 更新订单状态
    @Update("UPDATE train_meal_order SET status = #{status} WHERE id = #{id}")
    int updateStatus(Integer id, E_PaymentStatus status);
}