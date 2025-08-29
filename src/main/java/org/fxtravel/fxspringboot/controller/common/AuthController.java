package org.fxtravel.fxspringboot.controller.common;

import jakarta.servlet.http.HttpSession;
import org.fxtravel.fxspringboot.pojo.dto.user.*;
import org.fxtravel.fxspringboot.pojo.entities.User;
import org.fxtravel.fxspringboot.service.inter.common.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody RegisterRequest request, HttpSession session) {
        System.out.println(request);
        User user = userService.register(request);

        session.setAttribute("user", user);

        if (user != null) {
            return ResponseEntity.ok(Map.of("message", "注册成功"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "邮箱已注册"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody VerifyRequest request) {
        boolean success = userService.verifyCode(request.getCode());
        if (success) {
            return ResponseEntity.ok().body(Map.of("message","验证成功"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "验证失败"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        User user = userService.login(request.getEmail(), request.getPassword());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "邮箱或密码错误"));
        }

        session.setAttribute("user", user);

        return ResponseEntity.ok(Map.of("message", "登录成功"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate(); // 销毁 Session
        return ResponseEntity.ok(Map.of("message", "已登出"));
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgetPassword(@RequestBody ForgotPasswordRequest request) {
        boolean success = userService.forgotPassword(request.getEmail());
        if (success) {
            return ResponseEntity.ok(Map.of("message","重置密码验证邮件以发送，注意查收"));
        } else {
            return ResponseEntity.ok(Map.of("error","账号不存在或未验证"));
        }
    }

    @PostMapping("/resetPassword/ByVerificationCode")
    public ResponseEntity<?> resetPasswordByVerificationCode(@RequestBody ResetPasswordByVerificationCodeRequest request) {
        boolean success = userService.resetPasswordByVerificationCode(request.getEmail(),request.getCode(),request.getPassword());
        if (success) {
            return ResponseEntity.ok(Map.of("message","重置密码成功"));
        } else {
            return ResponseEntity.ok(Map.of("error","验证码错误或账号未验证"));
        }
    }

    @PostMapping("/resetPassword/ByOldPassword")
    public ResponseEntity<?> resetPasswordByOldPassword(@RequestBody ResetPasswordByOldPasswordRequest request) {
        boolean success = userService.resetPasswordByOldPassword(request.getEmail(),request.getOldPassword(),request.getNewPassword());
        if (success) {
            return ResponseEntity.ok(Map.of("message","重置密码成功"));
        } else {
            return ResponseEntity.ok(Map.of("error","旧密码错误或账号未验证"));
        }
    }
}
