package com.taskmanage.service;

import com.taskmanage.dto.LoginRequest;
import com.taskmanage.dto.LoginResponse;
import com.taskmanage.dto.RegisterRequest;
import com.taskmanage.entity.User;

public interface UserService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    User getById(Long id);
}
