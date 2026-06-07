package com.management.iteration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.iteration.entity.Iteration;
import com.management.iteration.mapper.IterationMapper;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IterationService {
    private final IterationMapper iterationMapper;
    private final UserMapper userMapper;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public List<Iteration> list() {
        List<Iteration> list = iterationMapper.selectList(
                new LambdaQueryWrapper<Iteration>().orderByDesc(Iteration::getCreatedAt));
        for (Iteration it : list) {
            if (it.getCreatedBy() != null) it.setCreator(userMapper.selectById(it.getCreatedBy()));
        }
        return list;
    }

    public Iteration create(Iteration iteration) {
        Long userId = currentUser().getUserId();
        iteration.setCreatedBy(userId);
        iterationMapper.insert(iteration);
        if (iteration.getCreatedBy() != null) iteration.setCreator(userMapper.selectById(iteration.getCreatedBy()));
        return iteration;
    }

    public Iteration update(Long id, Iteration req) {
        Iteration it = iterationMapper.selectById(id);
        if (it == null) throw new BusinessException(404, "迭代不存在");
        if (req.getName() != null) it.setName(req.getName());
        if (req.getReleaseTime() != null) it.setReleaseTime(req.getReleaseTime());
        if (req.getNotes() != null) it.setNotes(req.getNotes());
        if (req.getReleaseNotes() != null) it.setReleaseNotes(req.getReleaseNotes());
        iterationMapper.updateById(it);
        if (it.getCreatedBy() != null) it.setCreator(userMapper.selectById(it.getCreatedBy()));
        return it;
    }

    public void delete(Long id) {
        iterationMapper.deleteById(id);
    }
}
