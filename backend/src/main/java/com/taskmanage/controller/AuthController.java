package com.taskmanage.controller;

import com.taskmanage.annotation.RequireRole;
import com.taskmanage.common.Result;
import com.taskmanage.dto.LoginRequest;
import com.taskmanage.dto.LoginResponse;
import com.taskmanage.dto.RegisterRequest;
import com.taskmanage.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户认证", description = "登录注册接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = userService.login(request);
        Cookie cookie = new Cookie("token", loginResponse.getToken());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
        return Result.success(loginResponse);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户信息")
    @RequireRole
    @GetMapping("/me")
    public Result<LoginResponse.UserInfo> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String nickname = (String) request.getAttribute("nickname");
        String role = (String) request.getAttribute("role");
        return Result.success(new LoginResponse.UserInfo(userId, username, nickname, role));
    }
}
