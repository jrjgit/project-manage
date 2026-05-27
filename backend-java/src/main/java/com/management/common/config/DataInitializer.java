package com.management.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import com.management.common.workflow.WorkflowRule;
import com.management.common.workflow.WorkflowRuleMapper;
import com.management.common.workflow.WorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.management.dictionary.entity.Dictionary;
import com.management.dictionary.mapper.DictionaryMapper;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final WorkflowRuleMapper workflowRuleMapper;
    private final WorkflowService workflowService;
    private final PasswordEncoder passwordEncoder;
    private final DictionaryMapper dictionaryMapper;

    @Override
    public void run(ApplicationArguments args) {
        initDefaultAdmin();
        initDefaultWorkflowRules();
        initDefaultDictionaries();
    }

    private void initDefaultAdmin() {
        long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "pm"));
        if (count == 0) {
            log.warn("No PM user found, creating default admin (admin/admin123)");
            User admin = new User();
            admin.setName("admin");
            admin.setAccount("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("pm");
            userMapper.insert(admin);
            log.info("Default admin created, id={}", admin.getId());
        }
    }

    private void initDefaultWorkflowRules() {
        if (workflowRuleMapper.selectCount(null) > 0) return;

        log.info("Initializing default workflow rules");
        List<WorkflowRule> rules = List.of(
            rule("task", "pm",          "pending",     "developing"),
            rule("task", "pm",          "pending",     "testing"),
            rule("task", "pm",          "pending",     "closed"),
            rule("task", "pm",          "developing",  "testing"),
            rule("task", "pm",          "developing",  "closed"),
            rule("task", "pm",          "testing",     "closed"),
            rule("task", "pm",          "testing",     "developing"),
            rule("task", "dev_lead",    "pending",     "developing"),
            rule("task", "dev",         "developing",  "testing"),
            rule("task", "tester",      "testing",     "closed"),
            rule("task", "tester",      "testing",     "developing"),
            rule("bug", "tester",      "new",           "assigned"),
            rule("bug", "tester",      "pending_verify","closed"),
            rule("bug", "tester",      "pending_verify","reopened"),
            rule("bug", "tester_lead", "new",           "assigned"),
            rule("bug", "tester_lead", "pending_verify","closed"),
            rule("bug", "tester_lead", "pending_verify","reopened"),
            rule("bug", "dev_lead",    "assigned",      "fixing"),
            rule("bug", "dev_lead",    "assigned",      "closed"),
            rule("bug", "dev_lead",    "reopened",      "fixing"),
            rule("bug", "dev_lead",    "reopened",      "closed"),
            rule("bug", "dev",         "fixing",        "fixed"),
            // cross-role bug rules
            rule("bug", "tester",      "reopened",      "assigned"),
            rule("bug", "tester_lead", "reopened",      "assigned"),
            rule("bug", "pm",          "reopened",      "assigned"),
            rule("bug", "pm",          "new",           "assigned"),
            // === requirement status rules (PM only) ===
            rule("requirement", "pm", "planned",          "in_progress"),
            rule("requirement", "pm", "planned",          "closed"),
            rule("requirement", "pm", "in_progress",      "integration_test"),
            rule("requirement", "pm", "in_progress",      "closed"),
            rule("requirement", "pm", "integration_test", "business_test"),
            rule("requirement", "pm", "integration_test", "closed"),
            rule("requirement", "pm", "business_test",    "pending_release"),
            rule("requirement", "pm", "business_test",    "closed"),
            rule("requirement", "pm", "pending_release",  "released"),
            rule("requirement", "pm", "pending_release",  "business_test")
        );
        for (WorkflowRule rule : rules) {
            workflowRuleMapper.insert(rule);
        }
        workflowService.refreshCache();
        log.info("Default workflow rules initialized, count={}", rules.size());
    }

    private void initDefaultDictionaries() {
        if (dictionaryMapper.selectCount(null) > 0) return;

        log.info("Initializing default dictionaries");
        String[][] dicts = {
            {"system", "backend",  "后台",   "1"},
            {"system", "ios",      "iOS",    "2"},
            {"system", "android",  "安卓",   "3"},
            {"system", "harmony",  "鸿蒙",   "4"},
            {"system", "miniapp",  "小程序", "5"},
            {"system", "h5",       "H5",     "6"},
            {"source", "internal", "内部需求", "1"},
            {"source", "external", "客户需求", "2"},
            {"project_type", "ops",     "运维需求", "1"},
            {"project_type", "project", "项目需求", "2"},
            {"skill", "backend",  "后台开发", "1"},
            {"skill", "ios",      "iOS开发",  "2"},
            {"skill", "android",  "安卓开发", "3"},
            {"skill", "harmony",  "鸿蒙开发", "4"},
            {"skill", "miniapp",  "小程序开发", "5"},
            {"skill", "h5",       "H5开发",   "6"},
            {"skill", "frontend", "前端开发", "7"},
            {"skill", "other",    "其他",     "8"},
        };
        for (String[] d : dicts) {
            Dictionary dict = new Dictionary();
            dict.setDictType(d[0]);
            dict.setDictKey(d[1]);
            dict.setDictValue(d[2]);
            dict.setSortOrder(Integer.parseInt(d[3]));
            dictionaryMapper.insert(dict);
        }
        log.info("Default dictionaries initialized, count={}", dicts.length);
    }

    private static WorkflowRule rule(String type, String role, String from, String to) {
        WorkflowRule r = new WorkflowRule();
        r.setRuleType(type);
        r.setRole(role);
        r.setFromStatus(from);
        r.setToStatus(to);
        return r;
    }
}
