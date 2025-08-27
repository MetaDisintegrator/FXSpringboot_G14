package controller.hotel;

import jakarta.servlet.http.HttpSession;
import org.fxtravel.fxspringboot.controller.hotel.HotelController;
import org.fxtravel.fxspringboot.pojo.dto.hotel.HotelSearchResult;
import org.fxtravel.fxspringboot.pojo.dto.hotel.SearchHotelRequest;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.service.inter.hotel.HotelService;
import org.fxtravel.fxspringboot.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class HotelControllerTest {

    private HotelController hotelController;
    private HotelService hotelService;
    private HttpSession session;
    private BindingResult bindingResult;

    @BeforeEach
    public void setUp() {
        hotelService = Mockito.mock(HotelService.class);
        hotelController = new HotelController(hotelService);
        session = Mockito.mock(HttpSession.class);
        bindingResult = Mockito.mock(BindingResult.class);
    }

    // 正向：参数正确且已登录
    @Test
    public void testSearchHotel_success() {
        SearchHotelRequest req = new SearchHotelRequest();
        req.setDestination("北京");
        req.setNamePattern("豪华");

        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        List<HotelSearchResult> results = Collections.singletonList(new HotelSearchResult());
        when(hotelService.searchHotels(anyString(), anyString())).thenReturn(results);

        ResponseEntity<?> response = hotelController.searchHotel(req, bindingResult, session);
        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("查询成功", body.get("message"));
        assertEquals(results, body.get("data"));
    }

    // 反向：参数校验失败
    @Test
    public void testSearchHotel_paramError() {
        SearchHotelRequest req = new SearchHotelRequest();
        when(bindingResult.hasErrors()).thenReturn(true);

        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);

        Map<String, Object> errorBody = Map.of("error", "参数错误");
        ResponseEntity<Map<String, Object>> errorResponse = ResponseEntity.status(400).body(errorBody);
        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(errorResponse);

            ResponseEntity<?> response = hotelController.searchHotel(req, bindingResult, session);
            assertEquals(400, response.getStatusCodeValue());
            assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
        }
    }

    // 反向：未登录
    @Test
    public void testSearchHotel_notLoggedIn() {
        SearchHotelRequest req = new SearchHotelRequest();
        when(bindingResult.hasErrors()).thenReturn(false);
        when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = hotelController.searchHotel(req, bindingResult, session);
        assertEquals(401, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    // 反向：服务层抛异常
    @Test
    public void testSearchHotel_serviceException() {
        SearchHotelRequest req = new SearchHotelRequest();
        req.setDestination("上海");
        req.setNamePattern("经济");

        User user = new User();
        when(session.getAttribute("user")).thenReturn(user);
        when(bindingResult.hasErrors()).thenReturn(false);

        when(hotelService.searchHotels(anyString(), anyString()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        ResponseEntity<?> response = hotelController.searchHotel(req, bindingResult, session);
        assertEquals(500, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).get("error").toString().contains("查询失败"));
    }
}