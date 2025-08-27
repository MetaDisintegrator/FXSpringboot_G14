package service.impl.trainmeal;

import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.event.data.PaymentInfo;
import org.fxtravel.fxspringboot.mapper.trainmeal.TrainMealMapper;
import org.fxtravel.fxspringboot.pojo.entities.trainmeal.TrainMeal;
import org.fxtravel.fxspringboot.service.impl.trainmeal.TrainMealServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainMealServiceImplTest {

    @InjectMocks
    private TrainMealServiceImpl service;
    @Mock
    private TrainMealMapper trainMealMapper;
    @Mock
    private EventCenter eventCenter;

    // getMealsByTrain4User 正向
    @Test
    void getMealsByTrain4User_shouldReturnMeals() {
        TrainMeal meal = new TrainMeal();
        when(trainMealMapper.selectByTrain(1)).thenReturn(Arrays.asList(meal));
        List<TrainMeal> result = service.getMealsByTrain4User(1);
        assertEquals(1, result.size());
    }

    // getMealsByTrain4User 反向
    @Test
    void getMealsByTrain4User_shouldReturnEmptyList() {
        when(trainMealMapper.selectByTrain(2)).thenReturn(Collections.emptyList());
        List<TrainMeal> result = service.getMealsByTrain4User(2);
        assertTrue(result.isEmpty());
    }

    // getMealById 正向
    @Test
    void getMealById_shouldReturnMeal() {
        TrainMeal meal = new TrainMeal();
        when(trainMealMapper.selectById(1)).thenReturn(meal);
        TrainMeal result = service.getMealById(1);
        assertNotNull(result);
    }

    // getMealById 反向
    @Test
    void getMealById_shouldReturnNull() {
        when(trainMealMapper.selectById(2)).thenReturn(null);
        TrainMeal result = service.getMealById(2);
        assertNull(result);
    }

    // checkAndGet 正向
    @Test
    void checkAndGet_shouldDeductStockAndReturnTrue() {
        TrainMeal meal = new TrainMeal();
        meal.setRemain(10);
        when(trainMealMapper.selectByIdForUpdate(1)).thenReturn(meal);
        when(trainMealMapper.deduct(1, 5)).thenReturn(1);
        boolean result = service.checkAndGet(1, 5, null);
        assertTrue(result);
    }

    // checkAndGet 反向（库存不足）
    @Test
    void checkAndGet_shouldReturnFalseWhenStockNotEnough() {
        TrainMeal meal = new TrainMeal();
        meal.setRemain(2);
        when(trainMealMapper.selectByIdForUpdate(1)).thenReturn(meal);
        boolean result = service.checkAndGet(1, 5, null);
        assertFalse(result);
    }

    // checkAndGet 反向（无此商品）
    @Test
    void checkAndGet_shouldReturnFalseWhenMealNotFound() {
        when(trainMealMapper.selectByIdForUpdate(99)).thenReturn(null);
        boolean result = service.checkAndGet(99, 1, null);
        assertFalse(result);
    }

    // putBack 正向
    @Test
    void putBack_shouldAddStock() {
        service.putBack(1, 5, null);
        verify(trainMealMapper, times(1)).add(1, 5);
    }

    // putBack 反向（数量为0）
    @Test
    void putBack_shouldNotAddWhenCountIsZero() {
        service.putBack(1, 0, null);
        verify(trainMealMapper, times(1)).add(1, 0);
    }

}