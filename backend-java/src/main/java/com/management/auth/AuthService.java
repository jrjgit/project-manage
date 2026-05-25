package com.management.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUtils;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public Long register(RegisterRequest req) {
        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getName, req.getName()));
        if (existing != null) {
            throw new BusinessException(409, "username already exists");
        }
        User user = new User();
        user.setName(req.getName());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setGroupId(req.getGroupId());
        user.setWechatId(req.getWechatId());
        user.setEmail(req.getEmail());
        userMapper.insert(user);
        log.info("User registered: name={}, role={}, id={}", req.getName(), req.getRole(), user.getId());
        return user.getId();
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getName, req.getName()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "invalid username or password");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getName(),
                user.getRole(), user.getGroupId());
        log.info("User logged in: name={}, id={}", user.getName(), user.getId());
        return new LoginResponse(token, user);
    }
}
