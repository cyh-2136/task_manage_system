package com.taskmanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.taskmanage.common.Result;
import com.taskmanage.dto.LoginRequest;
import com.taskmanage.dto.LoginResponse;
import com.taskmanage.dto.RegisterRequest;
import com.taskmanage.entity.User;
import com.taskmanage.mapper.UserMapper;
import com.taskmanage.service.UserService;
import com.taskmanage.util.JwtUtil;
import com.taskmanage.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, new LoginResponse.UserInfo(
                user.getId(), user.getUsername(), user.getNickname(), user.getRole()
        ));
    }

    @Override
    public void register(RegisterRequest request) {
        long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encrypt(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(request.getRole());
        userMapper.insert(user);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
