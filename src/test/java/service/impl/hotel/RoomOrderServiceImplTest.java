package service.impl.hotel;

import org.fxtravel.fxspringboot.common.E_PaymentType;
import org.fxtravel.fxspringboot.mapper.hotel.RoomOrderMapper;
import org.fxtravel.fxspringboot.pojo.dto.hotel.BookHotelRequest;
import org.fxtravel.fxspringboot.pojo.entities.Hotel;
import org.fxtravel.fxspringboot.pojo.entities.Room;
import org.fxtravel.fxspringboot.pojo.entities.RoomOrder;
import org.fxtravel.fxspringboot.pojo.entities.payment;
import org.fxtravel.fxspringboot.service.impl.hotel.HotelServiceImpl;
import org.fxtravel.fxspringboot.service.impl.hotel.RoomOrderServiceImpl;
import org.fxtravel.fxspringboot.service.inter.common.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoomOrderServiceImplTest {
    @InjectMocks
    private RoomOrderServiceImpl roomOrderService;
    @Mock
    private RoomOrderMapper roomOrderMapper;
    @Mock
    private HotelServiceImpl hotelService;
    @Mock
    private PaymentService paymentService;

    @Test
    void testCreateOrder_success() {
        BookHotelRequest request = new BookHotelRequest();
        request.setUserId(1);
        request.setHotelId(100);
        request.setRoomId(200);
        request.setCheckInDate(LocalDate.of(2024, 7, 1));
        request.setCheckOutDate(LocalDate.of(2024, 7, 3));

        Hotel hotel = new Hotel();
        hotel.setId(100);
        hotel.setName("Test Hotel");

        Room room = new Room();
        room.setId(200);
        room.setHotelId(100);
        room.setPricePerNight(150.0);

        when(hotelService.getHotelById(100)).thenReturn(hotel);
        when(hotelService.getRoomById(200)).thenReturn(room);
        when(roomOrderMapper.insert(any(RoomOrder.class))).thenAnswer(invocation -> {
            RoomOrder order = invocation.getArgument(0);
            order.setId(1); // Simulate DB generated ID
            return null;
        });
        when(paymentService.createPayment(eq(1), eq(E_PaymentType.HOTEL), eq(300.0), eq(1), anyInt(), anyInt()))
                .thenReturn(mock(payment.class));

        RoomOrder order = roomOrderService.createOrder(request);

        assertNotNull(order);
        assertEquals(1, order.getId());
        assertEquals(1, order.getUserId());
        assertEquals(100, order.getHotelId());
        assertEquals(200, order.getRoomId());
        assertEquals(LocalDate.of(2024, 7, 1), order.getCheckInDate());
        assertEquals(LocalDate.of(2024, 7, 3), order.getCheckOutDate());
    }

    @Test
    void testCreateOrder_hotelOrRoomNotExist_shouldThrowException() {
        BookHotelRequest request = new BookHotelRequest();
        request.setUserId(1);
        request.setHotelId(100);
        request.setRoomId(200);
        request.setCheckInDate(LocalDate.of(2024, 7, 1));
        request.setCheckOutDate(LocalDate.of(2024, 7, 3));

        when(hotelService.getHotelById(100)).thenReturn(null);
        when(hotelService.getRoomById(200)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            roomOrderService.createOrder(request);
        });
    }

}
