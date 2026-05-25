package com.management.requirement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.requirement.dto.CreateFeatureRequest;
import com.management.requirement.dto.UpdateFeatureRequest;
import com.management.requirement.entity.Feature;
import com.management.requirement.mapper.FeatureMapper;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureService {
    private final FeatureMapper featureMapper;
    private final UserMapper userMapper;

    public List<Feature> listByRequirement(Long requirementId) {
        List<Feature> list = featureMapper.selectList(
                new LambdaQueryWrapper<Feature>()
                        .eq(Feature::getRequirementId, requirementId)
                        .orderByAsc(Feature::getCreatedAt));
        for (Feature f : list) fillAssociations(f);
        return list;
    }

    public Feature create(Long requirementId, CreateFeatureRequest req) {
        Feature f = new Feature();
        f.setRequirementId(requirementId);
        f.setTitle(req.getTitle());
        f.setDescription(req.getDescription());
        f.setStatus("planned");
        f.setDeveloperId(req.getDeveloperId());
        f.setTesterId(req.getTesterId());
        featureMapper.insert(f);
        fillAssociations(f);
        log.info("Feature created: id={}, title={}", f.getId(), f.getTitle());
        return f;
    }

    public Feature update(Long id, UpdateFeatureRequest req) {
        Feature f = featureMapper.selectById(id);
        if (f == null) throw new BusinessException(404, "功能点不存在");
        if (req.getTitle() != null) f.setTitle(req.getTitle());
        if (req.getDescription() != null) f.setDescription(req.getDescription());
        if (req.getDeveloperId() != null) f.setDeveloperId(req.getDeveloperId());
        if (req.getTesterId() != null) f.setTesterId(req.getTesterId());
        featureMapper.updateById(f);
        fillAssociations(f);
        return f;
    }

    public void changeStatus(Long id, String newStatus) {
        Feature f = featureMapper.selectById(id);
        if (f == null) throw new BusinessException(404, "功能点不存在");
        f.setStatus(newStatus);
        featureMapper.updateById(f);
    }

    public void delete(Long id) {
        featureMapper.deleteById(id);
    }

    private void fillAssociations(Feature f) {
        if (f.getDeveloperId() != null) f.setDeveloper(userMapper.selectById(f.getDeveloperId()));
        if (f.getTesterId() != null) f.setTester(userMapper.selectById(f.getTesterId()));
    }
}
