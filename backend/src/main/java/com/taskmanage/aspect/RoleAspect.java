package com.taskmanage.aspect;

import com.taskmanage.annotation.RequireRole;
import com.taskmanage.common.ErrorCode;
import com.taskmanage.entity.User;
import com.taskmanage.exception.BusinessException;
import com.taskmanage.util.JwtUtil;
import com.taskmanage.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Around("@annotation(requireRole)")
    public Object doCheck(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String token = extractTokenFromCookie(request);
        if (token == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token无效或已过期");
        }

        Long userId = Long.valueOf(claims.getSubject());
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
        }

        String userRole = user.getRole();
        request.setAttribute("userId", user.getId());
        request.setAttribute("username", user.getUsername());
        request.setAttribute("role", userRole);
        request.setAttribute("nickname", user.getNickname());

        String[] requiredRoles = requireRole.value();
        if (requiredRoles.length == 0) {
            return joinPoint.proceed();
        }

        Set<String> roleSet = Arrays.stream(requiredRoles).collect(Collectors.toSet());
        if (!roleSet.contains(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "角色权限不足，需要：" + String.join(" / ", requiredRoles));
        }

        return joinPoint.proceed();
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
