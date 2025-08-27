package controller.common;

import jakarta.servlet.http.HttpSession;
import org.fxtravel.fxspringboot.common.Gender;
import org.fxtravel.fxspringboot.common.Role;
import org.fxtravel.fxspringboot.controller.common.UserController;
import org.fxtravel.fxspringboot.pojo.dto.user.GrantAdminRequest;
import org.fxtravel.fxspringboot.pojo.dto.user.UpdateUserDataRequest;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.service.inter.common.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @InjectMocks
    private UserController userController;
    @Mock
    private UserService userService;
    @Mock
    private HttpSession session;

    // getUserInfo
    @Test
    public void testGetUserInfo_success() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);

        ResponseEntity<?> response = userController.getUserInfo(session);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    public void testGetUserInfo_fail() {
        Mockito.when(session.getAttribute("user")).thenReturn(null);

        ResponseEntity<?> response = userController.getUserInfo(session);
        assertEquals(401, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    // updateUserData
    @Test
    public void testUpdateUserData_success() {
        User user = new User();
        Mockito.when(session.getAttribute("user")).thenReturn(user);

        UpdateUserDataRequest req = new UpdateUserDataRequest();
        req.setUsername("newname");
        req.setGender(Gender.MALE);

        ResponseEntity<?> response = userController.updateUserData(session, req);
        assertEquals(200, response.getStatusCodeValue());
        Mockito.verify(userService).updateUserData(user, "newname", Gender.MALE);
    }

    @Test
    public void testUpdateUserData_fail() {
        Mockito.when(session.getAttribute("user")).thenReturn(null);

        UpdateUserDataRequest req = new UpdateUserDataRequest();
        ResponseEntity<?> response = userController.updateUserData(session, req);
        assertEquals(401, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    // grantAdminRole
    @Test
    public void testGrantAdminRole_success() {
        User admin = new User();
        admin.setRole(Role.ADMIN);
        Mockito.when(session.getAttribute("user")).thenReturn(admin);

        GrantAdminRequest req = new GrantAdminRequest();
        req.setUserId(123);

        Mockito.when(userService.grantAdminRole(123)).thenReturn(true);

        ResponseEntity<?> response = userController.grantAdminRole(session, req);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("已成功授予管理员权限", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    public void testGrantAdminRole_fail_not_logged_in() {
        Mockito.when(session.getAttribute("user")).thenReturn(null);

        GrantAdminRequest req = new GrantAdminRequest();
        ResponseEntity<?> response = userController.grantAdminRole(session, req);
        assertEquals(401, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    public void testGrantAdminRole_fail_no_permission() {
        User regular = new User();
        regular.setRole(Role.REGULAR);
        Mockito.when(session.getAttribute("user")).thenReturn(regular);

        GrantAdminRequest req = new GrantAdminRequest();
        ResponseEntity<?> response = userController.grantAdminRole(session, req);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }

    @Test
    public void testGrantAdminRole_fail_user_not_found() {
        User admin = new User();
        admin.setRole(Role.ADMIN);
        Mockito.when(session.getAttribute("user")).thenReturn(admin);

        GrantAdminRequest req = new GrantAdminRequest();
        req.setUserId(999);

        Mockito.when(userService.grantAdminRole(999)).thenReturn(false);

        ResponseEntity<?> response = userController.grantAdminRole(session, req);
        assertEquals(404, response.getStatusCodeValue());
        assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
    }
}