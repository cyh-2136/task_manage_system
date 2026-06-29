package com.taskmanage.service;

import com.taskmanage.model.auth.dto.LoginRequest;
import com.taskmanage.model.auth.dto.LoginResponse;
import com.taskmanage.model.auth.dto.RegisterRequest;
import com.taskmanage.model.common.entity.User;

public interface UserService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    User getById(Long id);
}
