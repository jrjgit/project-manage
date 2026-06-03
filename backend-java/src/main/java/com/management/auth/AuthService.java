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
                .eq(User::getAccount, req.getAccount()));
        if (existing != null) {
            throw new BusinessException(409, "账号已存在");
        }
        User user = new User();
        user.setName(req.getName());
        user.setAccount(req.getAccount());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setWechatId(req.getWechatId());
        user.setEmail(req.getEmail());
        userMapper.insert(user);
        log.info("User registered: name={}, account={}, role={}, id={}", req.getName(), req.getAccount(), req.getRole(), user.getId());
        return user.getId();
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, req.getAccount()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "账号或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getName(),
                user.getRole());
        log.info("User logged in: name={}, account={}, id={}", user.getName(), user.getAccount(), user.getId());
        return new LoginResponse(token, user);
    }
}
