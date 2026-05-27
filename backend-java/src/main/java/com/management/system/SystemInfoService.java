package com.management.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.system.entity.SystemInfo;
import com.management.system.mapper.SystemInfoMapper;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemInfoService {
    private final SystemInfoMapper systemInfoMapper;
    private final UserMapper userMapper;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public List<SystemInfo> list() {
        List<SystemInfo> list = systemInfoMapper.selectList(
                new LambdaQueryWrapper<SystemInfo>().orderByDesc(SystemInfo::getCreatedAt));
        for (SystemInfo s : list) {
            if (s.getCreatedBy() != null) s.setCreator(userMapper.selectById(s.getCreatedBy()));
        }
        return list;
    }

    public SystemInfo create(SystemInfo req) {
        Long userId = currentUser().getUserId();
        req.setCreatedBy(userId);
        systemInfoMapper.insert(req);
        fillCreator(req);
        return req;
    }

    public SystemInfo get(Long id) {
        SystemInfo s = systemInfoMapper.selectById(id);
        if (s == null) throw new BusinessException(404, "系统不存在");
        fillCreator(s);
        return s;
    }

    public SystemInfo update(Long id, SystemInfo req) {
        SystemInfo s = systemInfoMapper.selectById(id);
        if (s == null) throw new BusinessException(404, "系统不存在");
        if (req.getName() != null) s.setName(req.getName());
        if (req.getItContact() != null) s.setItContact(req.getItContact());
        if (req.getBizContact() != null) s.setBizContact(req.getBizContact());
        if (req.getTechContact() != null) s.setTechContact(req.getTechContact());
        if (req.getAddress() != null) s.setAddress(req.getAddress());
        systemInfoMapper.updateById(s);
        fillCreator(s);
        return s;
    }

    public void delete(Long id) {
        systemInfoMapper.deleteById(id);
    }

    private void fillCreator(SystemInfo s) {
        if (s.getCreatedBy() != null) s.setCreator(userMapper.selectById(s.getCreatedBy()));
    }
}
