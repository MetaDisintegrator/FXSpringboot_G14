package service.impl.common;

import org.fxtravel.fxspringboot.common.Gender;
import org.fxtravel.fxspringboot.mapper.UserMapper;
import org.fxtravel.fxspringboot.pojo.dto.user.RegisterRequest;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.service.impl.common.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;

    @Test
    public void testRegister_EmailAlreadyExists_ShouldReturnNull() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");

        when(userMapper.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        User result = userService.register(request);

        // Assert
        assertNull(result);
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    public void testRegister_EmailNotExists_ShouldReturnUserAndInsert() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setUsername("John");
        request.setPassword("password123");
        request.setGender(Gender.MALE);

        when(userMapper.existsByEmail("new@example.com")).thenReturn(false);

        // Act
        User result = userService.register(request);

        // Assert
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("John", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertEquals(Gender.MALE, result.getGender());
        assertTrue(result.isVerified());

        // 验证 userMapper.insert 是否被调用，并捕获参数
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).insert(userCaptor.capture());
        User insertedUser = userCaptor.getValue();
        assertEquals("new@example.com", insertedUser.getEmail());
    }

    @Test
    public void testLogin_UserNotFound_ShouldReturnNull() {
        when(userMapper.getUserByEmail("notfound@example.com")).thenReturn(null);
        User result = userService.login("notfound@example.com", "password");
        assertNull(result);
    }

    @Test
    public void testLogin_PasswordIncorrect_ShouldReturnNull() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("correctPassword");
        user.setVerified(true);
        when(userMapper.getUserByEmail("user@example.com")).thenReturn(user);
        User result = userService.login("user@example.com", "wrongPassword");
        assertNull(result);
    }

    @Test
    public void testLogin_Success_ShouldReturnUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setVerified(true);
        when(userMapper.getUserByEmail("user@example.com")).thenReturn(user);
        User result = userService.login("user@example.com", "password");
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    public void testLogin_UserNotVerified_ShouldReturnNull() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setVerified(false);
        when(userMapper.getUserByEmail("user@example.com")).thenReturn(user);
        User result = userService.login("user@example.com", "password");
        assertNull(result);
    }
}
