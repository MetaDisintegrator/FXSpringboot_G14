package service.impl.hotel;

import org.fxtravel.fxspringboot.common.E_NotificationEventType;
import org.fxtravel.fxspringboot.event.EventCenter;
import org.fxtravel.fxspringboot.event.EventType;
import org.fxtravel.fxspringboot.event.data.PaymentInfo;
import org.fxtravel.fxspringboot.mapper.hotel.HotelMapper;
import org.fxtravel.fxspringboot.mapper.hotel.RoomMapper;
import org.fxtravel.fxspringboot.pojo.dto.hotel.HotelSearchResult;
import org.fxtravel.fxspringboot.pojo.entities.Hotel;
import org.fxtravel.fxspringboot.pojo.entities.Room;
import org.fxtravel.fxspringboot.service.impl.hotel.HotelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @InjectMocks
    private HotelServiceImpl hotelService;
    @Mock
    private HotelMapper hotelMapper;
    @Mock
    private RoomMapper roomMapper;
    @Mock
    private EventCenter eventCenter;

    @Test
    void testGetHotelById_Found() {
        Hotel hotel = new Hotel();
        when(hotelMapper.selectById(1)).thenReturn(hotel);
        assertEquals(hotel, hotelService.getHotelById(1));
    }

    @Test
    void testGetHotelById_NotFound() {
        when(hotelMapper.selectById(2)).thenReturn(null);
        assertNull(hotelService.getHotelById(2));
    }

    @Test
    void testGetRoomById_Found() {
        Room room = new Room();
        when(roomMapper.selectById(1)).thenReturn(room);
        assertEquals(room, hotelService.getRoomById(1));
    }

    @Test
    void testGetRoomById_NotFound() {
        when(roomMapper.selectById(2)).thenReturn(null);
        assertNull(hotelService.getRoomById(2));
    }

    @Test
    void testSearchHotels_WithPattern_Found() {
        Hotel hotel = new Hotel();
        when(hotelMapper.findByDestAndName("Beijing", "Luxury")).thenReturn(Arrays.asList(hotel));
        Room room = new Room();
        when(roomMapper.findByHotel(hotel.getId())).thenReturn(Arrays.asList(room));
        List<HotelSearchResult> results = hotelService.searchHotels("Beijing", "Luxury");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testSearchHotels_WithPattern_NotFound() {
        when(hotelMapper.findByDestAndName("Beijing", "Luxury")).thenReturn(Collections.emptyList());
        List<HotelSearchResult> results = hotelService.searchHotels("Beijing", "Luxury");
        assertNull(results);
    }

    @Test
    void testSearchHotels_WithoutPattern_Found() {
        Hotel hotel = new Hotel();
        when(hotelMapper.findByDest("Shanghai")).thenReturn(Arrays.asList(hotel));
        Room room = new Room();
        when(roomMapper.findByHotel(hotel.getId())).thenReturn(Arrays.asList(room));
        List<HotelSearchResult> results = hotelService.searchHotels("Shanghai", "");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testSearchHotels_WithoutPattern_NotFound() {
        when(hotelMapper.findByDest("Shanghai")).thenReturn(Collections.emptyList());
        List<HotelSearchResult> results = hotelService.searchHotels("Shanghai", "");
        assertNull(results);
    }

    @Test
    void testCheckAndGet_Success() {
        Room room = new Room();
        room.setRemain(5);
        when(roomMapper.selectById(1)).thenReturn(room);
        when(roomMapper.deduct(1, 2)).thenReturn(1);
        assertTrue(hotelService.checkAndGet(1, 2, null));
    }

    @Test
    void testCheckAndGet_Fail_NotEnoughStock() {
        Room room = new Room();
        room.setRemain(1);
        when(roomMapper.selectById(1)).thenReturn(room);
        assertFalse(hotelService.checkAndGet(1, 2, null));
    }

    @Test
    void testCheckAndGet_Fail_RoomNotFound() {
        when(roomMapper.selectById(2)).thenReturn(null);
        assertFalse(hotelService.checkAndGet(2, 1, null));
    }

    @Test
    void testCheckAndGet_Fail_DeductFailed() {
        Room room = new Room();
        room.setRemain(5);
        when(roomMapper.selectById(1)).thenReturn(room);
        when(roomMapper.deduct(1, 2)).thenReturn(0);
        assertFalse(hotelService.checkAndGet(1, 2, null));
    }

    @Test
    void testPutBack_Normal() {
        hotelService.putBack(1, 2, null);
        verify(roomMapper, times(1)).add(1, 2);
    }

    @Test
    void testHandlePaymentStatusChange_FailedOrRefunded() {
        PaymentInfo info = mock(PaymentInfo.class);
        when(info.getNewStatus()).thenReturn(org.fxtravel.fxspringboot.common.E_PaymentStatus.FAILED);
        when(info.getGoodId()).thenReturn(1);
        when(info.getQuantity()).thenReturn(2);
        hotelService.handlePaymentStatusChange(info);
        verify(roomMapper, times(1)).add(1, 2);
    }

    @Test
    void testHandlePaymentStatusChange_OtherStatus() {
        PaymentInfo info = mock(PaymentInfo.class);
        when(info.getNewStatus()).thenReturn(org.fxtravel.fxspringboot.common.E_PaymentStatus.COMPLETED);
        hotelService.handlePaymentStatusChange(info);
        verify(roomMapper, never()).add(anyInt(), anyInt());
    }
}