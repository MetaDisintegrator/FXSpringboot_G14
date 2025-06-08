// TrainSeatOrderMapper.java
package org.fxtravel.fxspringboot.mapper.trainseat;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.pojo.entities.TrainSeatOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TrainSeatOrderMapper extends BaseMapper<TrainSeatOrder> {
    @Select("SELECT EXISTS (SELECT 1 FROM train_seat_order WHERE " +
            "train_seat_id = #{trainSeatID} AND user_id = #{userID} AND status = 'COMPLETED')")
    boolean existsBySeatAndUser(@Param("trainSeatID") Integer trainSeatID, @Param("userID") Integer userID);

    @Select("SELECT * FROM train_seat_order WHERE user_id = #{userID}")
    List<TrainSeatOrder> findByUserID(Integer userID);

    @Select("SELECT * FROM train_seat_order WHERE order_number = #{orderNumber} LIMIT 1")
    TrainSeatOrder findByOrderNumber(String orderNumber);

    // 更新订单状态
    @Update("UPDATE train_seat_order SET status = #{status} WHERE id = #{id}")
    int updateStatus(Integer id, E_PaymentStatus status);
}