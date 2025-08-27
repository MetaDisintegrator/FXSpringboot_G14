package service.impl.trainseat;

import org.fxtravel.fxspringboot.common.E_PaymentStatus;
import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.event.data.PaymentInfo;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainMapper;
import org.fxtravel.fxspringboot.mapper.trainseat.TrainSeatMapper;
import org.fxtravel.fxspringboot.pojo.dto.train.TrainSearchResult;
import org.fxtravel.fxspringboot.pojo.entities.Train;
import org.fxtravel.fxspringboot.pojo.entities.TrainSeat;
import org.fxtravel.fxspringboot.service.impl.trainseat.TrainSeatServiceImpl;
import org.fxtravel.fxspringboot.utils.SeatUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainSeatServiceImplTest {

    private TrainMapper trainMapper;
    private TrainSeatMapper trainSeatMapper;
    private EventCenter eventCenter;
    private TrainSeatServiceImpl service;

    @BeforeEach
    void setUp() {
        trainMapper = mock(TrainMapper.class);
        trainSeatMapper = mock(TrainSeatMapper.class);
        eventCenter = mock(EventCenter.class);

        service = new TrainSeatServiceImpl();
        injectField(service, "trainMapper", trainMapper);
        injectField(service, "trainSeatMapper", trainSeatMapper);
        injectField(service, "eventCenter", eventCenter);
    }

    // 工具方法：反射注入
    private void injectField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // getTrainById 正向
    @Test
    void getTrainById_shouldReturnTrain() {
        Train train = new Train();
        train.setId(1);
        when(trainMapper.selectById(1)).thenReturn(train);

        Train result = service.getTrainById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    // getTrainById 反向
    @Test
    void getTrainById_shouldReturnNull() {
        when(trainMapper.selectById(999)).thenReturn(null);

        Train result = service.getTrainById(999);

        assertNull(result);
    }

    // findByRouteAndTimeOrderByTime 正向
    @Test
    void findByRouteAndTimeOrderByTime_shouldReturnSearchResults() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        Train train = new Train();
        train.setId(1);
        List<Train> trains = Arrays.asList(train);

        TrainSeat seat = new TrainSeat();
        seat.setId(1);
        seat.setTrainId(1);
        List<TrainSeat> seats = Arrays.asList(seat);

        when(trainMapper.findByRouteAndTimeOrderByTime("北京", "上海", startOfDay, endOfDay))
                .thenReturn(trains);
        when(trainSeatMapper.findByTrain(1)).thenReturn(seats);

        List<TrainSearchResult> results = service.findByRouteAndTimeOrderByTime("北京", "上海", date);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(train, results.get(0).getTrain());
        assertEquals(seats, results.get(0).getTrainseats());
    }

    // findByRouteAndTimeOrderByTime 反向（无列车）
    @Test
    void findByRouteAndTimeOrderByTime_shouldReturnNullWhenNoTrains() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        when(trainMapper.findByRouteAndTimeOrderByTime("北京", "上海", startOfDay, endOfDay))
                .thenReturn(Collections.emptyList());

        List<TrainSearchResult> results = service.findByRouteAndTimeOrderByTime("北京", "上海", date);

        assertNull(results);
    }

    // findByRouteAndTimeOrderByTime 反向（列车无座位）
    @Test
    void findByRouteAndTimeOrderByTime_shouldFilterOutTrainsWithoutSeats() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        Train train = new Train();
        train.setId(1);
        List<Train> trains = Arrays.asList(train);

        when(trainMapper.findByRouteAndTimeOrderByTime("北京", "上海", startOfDay, endOfDay))
                .thenReturn(trains);
        when(trainSeatMapper.findByTrain(1)).thenReturn(Collections.emptyList());

        List<TrainSearchResult> results = service.findByRouteAndTimeOrderByTime("北京", "上海", date);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // findByRouteAndTimeOrderByDuration 正向
    @Test
    void findByRouteAndTimeOrderByDuration_shouldReturnSearchResults() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        Train train = new Train();
        train.setId(1);
        List<Train> trains = Arrays.asList(train);

        TrainSeat seat = new TrainSeat();
        seat.setId(1);
        seat.setTrainId(1);
        List<TrainSeat> seats = Arrays.asList(seat);

        when(trainMapper.findByRouteAndTimeOrderByDuration("北京", "上海", startOfDay, endOfDay))
                .thenReturn(trains);
        when(trainSeatMapper.findByTrain(1)).thenReturn(seats);

        List<TrainSearchResult> results = service.findByRouteAndTimeOrderByDuration("北京", "上海", date);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(train, results.get(0).getTrain());
        assertEquals(seats, results.get(0).getTrainseats());
    }

    // findByRouteAndTimeOrderByDuration 反向
    @Test
    void findByRouteAndTimeOrderByDuration_shouldReturnNullWhenNoTrains() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        when(trainMapper.findByRouteAndTimeOrderByDuration("北京", "上海", startOfDay, endOfDay))
                .thenReturn(Collections.emptyList());

        List<TrainSearchResult> results = service.findByRouteAndTimeOrderByDuration("北京", "上海", date);

        assertNull(results);
    }

    // checkAndGet 正向
    @Test
    void checkAndGet_shouldReturnTrueAndSetSeatNumber() {
        try (MockedStatic<SeatUtil> mockedSeatUtil = mockStatic(SeatUtil.class)) {
            String[] seatNumber = new String[1];
            TrainSeat seat = new TrainSeat();
            seat.setId(1);
            seat.setRemain(5);
            seat.setSeatAllocation(new byte[]{0, 0, 0, 0});

            when(trainSeatMapper.selectById(1)).thenReturn(seat);
            when(trainSeatMapper.deduct(eq(1), any(byte[].class))).thenReturn(1);

            mockedSeatUtil.when(() -> SeatUtil.nextAvailable(any())).thenReturn(0);
            mockedSeatUtil.when(() -> SeatUtil.idx2number(0)).thenReturn("1A");
            mockedSeatUtil.when(() -> SeatUtil.setBit(any(), eq(0))).thenReturn(new byte[]{1, 0, 0, 0});

            boolean result = service.checkAndGet(1, 1, seatNumber);

            assertTrue(result);
            assertEquals("1A", seatNumber[0]);
            verify(trainSeatMapper, times(1)).deduct(eq(1), any(byte[].class));
        }
    }

    // checkAndGet 反向（座位不存在）
    @Test
    void checkAndGet_shouldReturnFalseWhenSeatNotFound() {
        String[] seatNumber = new String[1];
        when(trainSeatMapper.selectById(999)).thenReturn(null);

        boolean result = service.checkAndGet(999, 1, seatNumber);

        assertFalse(result);
        assertNull(seatNumber[0]);
    }

    // checkAndGet 反向（座位余量不足）
    @Test
    void checkAndGet_shouldReturnFalseWhenNoRemainingSeat() {
        String[] seatNumber = new String[1];
        TrainSeat seat = new TrainSeat();
        seat.setId(1);
        seat.setRemain(0);

        when(trainSeatMapper.selectById(1)).thenReturn(seat);

        boolean result = service.checkAndGet(1, 1, seatNumber);

        assertFalse(result);
        assertNull(seatNumber[0]);
    }

    // checkAndGet 反向（参数类型错误）
    @Test
    void checkAndGet_shouldReturnFalseWhenInvalidParameterType() {
        Integer[] invalidParam = new Integer[1];

        boolean result = service.checkAndGet(1, 1, invalidParam);

        assertFalse(result);
    }

    // checkAndGet 反向（无可用座位）
    @Test
    void checkAndGet_shouldReturnFalseWhenNoAvailableSeat() {
        try (MockedStatic<SeatUtil> mockedSeatUtil = mockStatic(SeatUtil.class)) {
            String[] seatNumber = new String[1];
            TrainSeat seat = new TrainSeat();
            seat.setId(1);
            seat.setRemain(5);
            seat.setSeatAllocation(new byte[]{-1, -1, -1, -1});

            when(trainSeatMapper.selectById(1)).thenReturn(seat);
            mockedSeatUtil.when(() -> SeatUtil.nextAvailable(any())).thenReturn(null);

            boolean result = service.checkAndGet(1, 1, seatNumber);

            assertFalse(result);
            assertNull(seatNumber[0]);
        }
    }

    // putBack 正向
    @Test
    void putBack_shouldUpdateSeatAllocation() {
        try (MockedStatic<SeatUtil> mockedSeatUtil = mockStatic(SeatUtil.class)) {
            TrainSeat seat = new TrainSeat();
            seat.setId(1);
            seat.setSeatAllocation(new byte[]{-1, -1, -1, -1});

            when(trainSeatMapper.selectById(1)).thenReturn(seat);
            mockedSeatUtil.when(() -> SeatUtil.number2idx("1A")).thenReturn(0);
            mockedSeatUtil.when(() -> SeatUtil.clearBit(any(), eq(0))).thenReturn(new byte[]{0, -1, -1, -1});

            service.putBack(1, 1, "1A");

            verify(trainSeatMapper, times(1)).add(eq(1), any(byte[].class));
        }
    }

    // putBack 反向（座位不存在）
    @Test
    void putBack_shouldDoNothingWhenSeatNotFound() {
        when(trainSeatMapper.selectById(999)).thenReturn(null);

        service.putBack(999, 1, "1A");

        verify(trainSeatMapper, never()).add(anyInt(), any(byte[].class));
    }

    // putBack 反向（参数类型错误）
    @Test
    void putBack_shouldDoNothingWhenInvalidParameterType() {
        Integer invalidParam = 123;

        service.putBack(1, 1, invalidParam);

        verify(trainSeatMapper, never()).add(anyInt(), any(byte[].class));
    }

    // putBack 反向（无效座位号）
    @Test
    void putBack_shouldDoNothingWhenInvalidSeatNumber() {
        try (MockedStatic<SeatUtil> mockedSeatUtil = mockStatic(SeatUtil.class)) {
            TrainSeat seat = new TrainSeat();
            seat.setId(1);
            seat.setSeatAllocation(new byte[]{-1, -1, -1, -1});

            when(trainSeatMapper.selectById(1)).thenReturn(seat);
            mockedSeatUtil.when(() -> SeatUtil.number2idx("INVALID")).thenReturn(-1);

            service.putBack(1, 1, "INVALID");

            verify(trainSeatMapper, never()).add(anyInt(), any(byte[].class));
        }
    }

    // handlePaymentStatusChange 正向（FAILED状态）
    @Test
    void handlePaymentStatusChange_shouldPutBackOnFailedStatus() throws Exception {
        try (MockedStatic<SeatUtil> mockedSeatUtil = mockStatic(SeatUtil.class)) {
            PaymentInfo info = mock(PaymentInfo.class);
            when(info.getNewStatus()).thenReturn(E_PaymentStatus.FAILED);
            when(info.getGoodId()).thenReturn(1);
            when(info.getQuantity()).thenReturn(2);
            when(info.getData()).thenReturn("1A");

            TrainSeat seat = new TrainSeat();
            seat.setId(1);
            seat.setSeatAllocation(new byte[]{-1, -1, -1, -1});
            when(trainSeatMapper.selectById(1)).thenReturn(seat);

            mockedSeatUtil.when(() -> SeatUtil.number2idx("1A")).thenReturn(0);
            mockedSeatUtil.when(() -> SeatUtil.clearBit(any(), eq(0))).thenReturn(new byte[]{0, -1, -1, -1});

            var method = service.getClass().getDeclaredMethod("handlePaymentStatusChange", PaymentInfo.class);
            method.setAccessible(true);
            method.invoke(service, info);

            verify(trainSeatMapper, times(1)).add(eq(1), any(byte[].class));
        }
    }

    // handlePaymentStatusChange 正向（REFUNDED状态）
    @Test
    void handlePaymentStatusChange_shouldPutBackOnRefundedStatus() throws Exception {
        try (MockedStatic<SeatUtil> mockedSeatUtil = mockStatic(SeatUtil.class)) {
            PaymentInfo info = mock(PaymentInfo.class);
            when(info.getNewStatus()).thenReturn(E_PaymentStatus.REFUNDED);
            when(info.getGoodId()).thenReturn(1);
            when(info.getQuantity()).thenReturn(2);
            when(info.getData()).thenReturn("1A");

            TrainSeat seat = new TrainSeat();
            seat.setId(1);
            seat.setSeatAllocation(new byte[]{-1, -1, -1, -1});
            when(trainSeatMapper.selectById(1)).thenReturn(seat);

            mockedSeatUtil.when(() -> SeatUtil.number2idx("1A")).thenReturn(0);
            mockedSeatUtil.when(() -> SeatUtil.clearBit(any(), eq(0))).thenReturn(new byte[]{0, -1, -1, -1});

            var method = service.getClass().getDeclaredMethod("handlePaymentStatusChange", PaymentInfo.class);
            method.setAccessible(true);
            method.invoke(service, info);

            verify(trainSeatMapper, times(1)).add(eq(1), any(byte[].class));
        }
    }

    // handlePaymentStatusChange 反向（其他状态）
    @Test
    void handlePaymentStatusChange_shouldNotPutBackOnOtherStatus() throws Exception {
        PaymentInfo info = mock(PaymentInfo.class);
        when(info.getNewStatus()).thenReturn(E_PaymentStatus.COMPLETED);

        var method = service.getClass().getDeclaredMethod("handlePaymentStatusChange", PaymentInfo.class);
        method.setAccessible(true);
        method.invoke(service, info);

        verify(trainSeatMapper, never()).add(anyInt(), any(byte[].class));
    }
}