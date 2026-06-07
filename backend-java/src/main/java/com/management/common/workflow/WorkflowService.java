package com.management.common.workflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final WorkflowRuleMapper ruleMapper;

    /** 缓存: ruleType -> role -> fromStatus -> Set<toStatus> */
    private volatile Map<String, Map<String, Map<String, Set<String>>>> cache = new HashMap<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * 获取允许的状态流转，判断指定角色的状态变更是否合法
     */
    public boolean isAllowed(String role, String from, String to, String type) {
        Map<String, Map<String, Set<String>>> typeRules = cache.get(type);
        if (typeRules == null) return false;
        Map<String, Set<String>> roleRules = typeRules.get(role);
        if (roleRules == null) return false;
        Set<String> allowed = roleRules.get(from);
        return allowed != null && allowed.contains(to);
    }

    /** 数据库变更后刷新缓存 */
    public synchronized void refreshCache() {
        List<WorkflowRule> rules = ruleMapper.selectList(null);
        Map<String, Map<String, Map<String, Set<String>>>> newCache = new HashMap<>();
        for (WorkflowRule r : rules) {
            newCache
                .computeIfAbsent(r.getRuleType(), k -> new HashMap<>())
                .computeIfAbsent(r.getRole(), k -> new HashMap<>())
                .computeIfAbsent(r.getFromStatus(), k -> new HashSet<>())
                .add(r.getToStatus());
        }
        this.cache = newCache;
        log.info("Workflow cache refreshed, total rules: {}", rules.size());
    }

    /** 获取所有规则 */
    public List<WorkflowRule> listAll() {
        return ruleMapper.selectList(null);
    }

    /** 批量替换规则 */
    @Transactional
    public void replaceAll(String ruleType, List<WorkflowRule> newRules) {
        ruleMapper.delete(new LambdaQueryWrapper<WorkflowRule>()
                .eq(WorkflowRule::getRuleType, ruleType));
        for (WorkflowRule rule : newRules) {
            rule.setRuleType(ruleType);
            rule.setId(null);
            ruleMapper.insert(rule);
        }
        refreshCache();
        log.warn("Workflow rules for {} replaced, new count: {}", ruleType, newRules.size());
    }
}
