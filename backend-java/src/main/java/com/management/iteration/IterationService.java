package com.management.iteration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.iteration.entity.Iteration;
import com.management.iteration.mapper.IterationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IterationService {
    private final IterationMapper iterationMapper;

    public List<Iteration> list() {
        return iterationMapper.selectList(
                new LambdaQueryWrapper<Iteration>().orderByDesc(Iteration::getCreatedAt));
    }

    public Iteration create(Iteration iteration) {
        iterationMapper.insert(iteration);
        iteration.setIterationId("ITER-" + java.time.Year.now().getValue() + "-" + String.format("%03d", iteration.getId()));
        iterationMapper.updateById(iteration);
        return iteration;
    }

    public Iteration update(Long id, Iteration req) {
        Iteration it = iterationMapper.selectById(id);
        if (it == null) throw new BusinessException(404, "迭代不存在");
        if (req.getName() != null) it.setName(req.getName());
        if (req.getReleaseTime() != null) it.setReleaseTime(req.getReleaseTime());
        iterationMapper.updateById(it);
        return it;
    }

    public void delete(Long id) {
        iterationMapper.deleteById(id);
    }
}
