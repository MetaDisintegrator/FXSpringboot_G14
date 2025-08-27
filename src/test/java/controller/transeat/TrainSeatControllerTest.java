package controller.transeat;

import jakarta.servlet.http.HttpSession;
import org.fxtravel.fxspringboot.controller.trainseat.TrainSeatController;
import org.fxtravel.fxspringboot.pojo.dto.train.SearchTrainRequest;
import org.fxtravel.fxspringboot.pojo.dto.train.TrainSearchResult;
import org.fxtravel.fxspringboot.pojo.entities.Train;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.service.inter.trainseat.TrainSeatService;
import org.fxtravel.fxspringboot.utils.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

public class TrainSeatControllerTest {

    private TrainSeatController controller;
    private TrainSeatService trainSeatService;
    private HttpSession session;
    private BindingResult bindingResult;

    @BeforeEach
    public void setUp() throws Exception {
        controller = new TrainSeatController();
        trainSeatService = Mockito.mock(TrainSeatService.class);
        session = Mockito.mock(HttpSession.class);
        bindingResult = Mockito.mock(BindingResult.class);

        var field = TrainSeatController.class.getDeclaredField("trainSeatService");
        field.setAccessible(true);
        field.set(controller, trainSeatService);
    }

    // getTrain 正向
    @Test
    public void testGetTrain_success() {
        Train train = new Train();
        Mockito.when(trainSeatService.getTrainById(1)).thenReturn(train);

        Train result = controller.getTrain(1);
        assertEquals(train, result);
    }

    // getTrain 反向：无此车次
    @Test
    public void testGetTrain_notFound() {
        Mockito.when(trainSeatService.getTrainById(2)).thenReturn(null);

        Train result = controller.getTrain(2);
        assertNull(result);
    }

    // searchTrainByDepartureTime 正向
    @Test
    public void testSearchTrainByDepartureTime_success() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        SearchTrainRequest req = new SearchTrainRequest();
        req.setDepartureStation("A");
        req.setArrivalStation("B");
        req.setDepartureDate(LocalDate.now());

        List<TrainSearchResult> results = Collections.singletonList(new TrainSearchResult());
        Mockito.when(trainSeatService.findByRouteAndTimeOrderByTime(anyString(), anyString(), any()))
                .thenReturn(results);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.searchTrainByDepartureTime(req, bindingResult, session);
            assertEquals(200, resp.getStatusCodeValue());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("查询成功", body.get("message"));
            assertEquals(results, body.get("data"));
        }
    }

    // searchTrainByDepartureTime 反向：参数校验失败
    @Test
    public void testSearchTrainByDepartureTime_paramError() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(true);

        SearchTrainRequest req = new SearchTrainRequest();

        Map<String, Object> errorBody = Map.of("error", "参数错误");
        ResponseEntity<Map<String, Object>> errorResp = ResponseEntity.status(400).body(errorBody);
        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(errorResp);

            ResponseEntity<?> resp = controller.searchTrainByDepartureTime(req, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
        }
    }

    // searchTrainByDepartureTime 反向：服务异常
    @Test
    public void testSearchTrainByDepartureTime_serviceException() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        SearchTrainRequest req = new SearchTrainRequest();
        req.setDepartureStation("A");
        req.setArrivalStation("B");
        req.setDepartureDate(LocalDate.now());

        Mockito.when(trainSeatService.findByRouteAndTimeOrderByTime(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("数据库异常"));

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.searchTrainByDepartureTime(req, bindingResult, session);
            assertEquals(500, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).get("error").toString().contains("查询失败"));
        }
    }

    // searchTrainByDuration 正向
    @Test
    public void testSearchTrainByDuration_success() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        SearchTrainRequest req = new SearchTrainRequest();
        req.setDepartureStation("A");
        req.setArrivalStation("B");
        req.setDepartureDate(LocalDate.now());

        List<TrainSearchResult> results = Collections.singletonList(new TrainSearchResult());
        Mockito.when(trainSeatService.findByRouteAndTimeOrderByDuration(anyString(), anyString(), any()))
                .thenReturn(results);

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.searchTrainByDuration(req, bindingResult, session);
            assertEquals(200, resp.getStatusCodeValue());
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertEquals("查询成功", body.get("message"));
            assertEquals(results, body.get("data"));
        }
    }

    // searchTrainByDuration 反向：参数校验失败
    @Test
    public void testSearchTrainByDuration_paramError() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(true);

        SearchTrainRequest req = new SearchTrainRequest();

        Map<String, Object> errorBody = Map.of("error", "参数错误");
        ResponseEntity<Map<String, Object>> errorResp = ResponseEntity.status(400).body(errorBody);
        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(errorResp);

            ResponseEntity<?> resp = controller.searchTrainByDuration(req, bindingResult, session);
            assertEquals(400, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).containsKey("error"));
        }
    }

    // searchTrainByDuration 反向：服务异常
    @Test
    public void testSearchTrainByDuration_serviceException() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);
        Mockito.when(bindingResult.hasErrors()).thenReturn(false);

        SearchTrainRequest req = new SearchTrainRequest();
        req.setDepartureStation("A");
        req.setArrivalStation("B");
        req.setDepartureDate(LocalDate.now());

        Mockito.when(trainSeatService.findByRouteAndTimeOrderByDuration(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("数据库异常"));

        try (var mocked = Mockito.mockStatic(AuthUtil.class)) {
            mocked.when(() -> AuthUtil.check(bindingResult, user)).thenReturn(null);

            ResponseEntity<?> resp = controller.searchTrainByDuration(req, bindingResult, session);
            assertEquals(500, resp.getStatusCodeValue());
            assertTrue(((Map<?, ?>) resp.getBody()).get("error").toString().contains("查询失败"));
        }
    }
}