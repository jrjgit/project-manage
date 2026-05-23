# 需求管理系统 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 基于现有 backend-java 和 frontend，新增需求管理（Requirement）、功能点（Feature）、发布迭代（Iteration）、统计看板四大模块。

**Architecture:** Requirement → Feature → Task 三层数据模型；改造现有 tasks/bugs 表加 requirement_id；新增 iterations/dictionaries 表；前端 6 个新页面。

**Tech Stack:** Java 17, Spring Boot 3.2.7, MyBatis-Plus 3.5.7, Vue 3, Naive UI 2.41, Vite 6

---

## 文件结构

### 新建后端文件
```
backend-java/src/main/java/com/management/requirement/
    entity/Requirement.java, Feature.java
    mapper/RequirementMapper.java, FeatureMapper.java
    dto/CreateRequirementRequest.java, UpdateRequirementRequest.java,
        CreateFeatureRequest.java, UpdateFeatureRequest.java
    RequirementService.java, FeatureService.java
    RequirementController.java, FeatureController.java

backend-java/src/main/java/com/management/iteration/
    entity/Iteration.java
    mapper/IterationMapper.java
    IterationService.java, IterationController.java

backend-java/src/main/java/com/management/dictionary/
    entity/Dictionary.java
    mapper/DictionaryMapper.java
    DictionaryService.java, DictionaryController.java

backend-java/src/main/java/com/management/statistics/
    StatisticsService.java, StatisticsController.java
```

### 改造后端文件
```
backend-java/src/main/java/com/management/task/entity/Task.java        — 加 requirement_id/feature_id/terminal/progress
backend-java/src/main/java/com/management/bug/entity/Bug.java          — 加 requirement_id/developer_id
backend-java/src/main/java/com/management/common/config/DataInitializer.java — 追加规则/字典
```

### 新建前端文件
```
frontend/src/api/requirements.js, iterations.js, statistics.js, dictionaries.js
frontend/src/views/Requirements.vue, RequirementDetail.vue, Iterations.vue,
                  RevenueDashboard.vue, PerformanceStats.vue, Dictionary.vue
frontend/src/constants/requirementMeta.js
```

### 改造前端文件
```
frontend/src/router/index.js  — 注册 6 条新路由
```

---

## Phase S1: 数据层

### Task 1: Requirement Entity + Mapper

**Files:**
- Create: `backend-java/src/main/java/com/management/requirement/entity/Requirement.java`
- Create: `backend-java/src/main/java/com/management/requirement/mapper/RequirementMapper.java`
- Create: `backend-java/src/main/java/com/management/requirement/entity/Feature.java`
- Create: `backend-java/src/main/java/com/management/requirement/mapper/FeatureMapper.java`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p backend-java/src/main/java/com/management/requirement/entity
mkdir -p backend-java/src/main/java/com/management/requirement/mapper
mkdir -p backend-java/src/main/java/com/management/requirement/dto
```

- [ ] **Step 2: 编写 Requirement.java**

```java
package com.management.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.project.entity.Project;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("requirements")
public class Requirement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requirementId;
    private String number;
    private String title;
    private String description;
    private String source;
    private String status;
    private String priority;
    private String system;
    private Long projectId;
    @TableField(exist = false)
    private Project project;
    private String projectType;
    private Long personId;
    @TableField(exist = false)
    private User person;
    private String relevant;
    private String totalAmount;
    private String devTotal;
    private String devPrice;
    private String testTotal;
    private String testPrice;
    private String bizTestTotal;
    private String bizTestPrice;
    private LocalDateTime releaseTime;
    private String iterationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 编写 RequirementMapper.java**

```java
package com.management.requirement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.requirement.entity.Requirement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RequirementMapper extends BaseMapper<Requirement> {
}
```

- [ ] **Step 4: 编写 Feature.java**

```java
package com.management.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("features")
public class Feature {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long requirementId;
    private String title;
    private String description;
    private String status;
    private Long developerId;
    @TableField(exist = false)
    private User developer;
    private Long testerId;
    @TableField(exist = false)
    private User tester;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 5: 编写 FeatureMapper.java**

```java
package com.management.requirement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.requirement.entity.Feature;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeatureMapper extends BaseMapper<Feature> {
}
```

- [ ] **Step 6: mvn compile + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && git add -A && git commit -m "feat(s1): add Requirement and Feature entities and mappers"
```

---

### Task 2: Iteration + Dictionary Entity + Mapper

**Files:**
- Create: `backend-java/src/main/java/com/management/iteration/entity/Iteration.java`
- Create: `backend-java/src/main/java/com/management/iteration/mapper/IterationMapper.java`
- Create: `backend-java/src/main/java/com/management/dictionary/entity/Dictionary.java`
- Create: `backend-java/src/main/java/com/management/dictionary/mapper/DictionaryMapper.java`

- [ ] **Step 1: 创建目录**

```bash
mkdir -p backend-java/src/main/java/com/management/iteration/entity
mkdir -p backend-java/src/main/java/com/management/iteration/mapper
mkdir -p backend-java/src/main/java/com/management/dictionary/entity
mkdir -p backend-java/src/main/java/com/management/dictionary/mapper
```

- [ ] **Step 2: 编写 Iteration.java**

```java
package com.management.iteration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("iterations")
public class Iteration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String iterationId;
    private String name;
    private LocalDateTime releaseTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 编写 IterationMapper.java**

```java
package com.management.iteration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.iteration.entity.Iteration;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IterationMapper extends BaseMapper<Iteration> {
}
```

- [ ] **Step 4: 编写 Dictionary.java**

```java
package com.management.dictionary.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dictionaries")
public class Dictionary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictType;
    private String dictKey;
    private String dictValue;
    private Integer sortOrder;
}
```

- [ ] **Step 5: 编写 DictionaryMapper.java**

```java
package com.management.dictionary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.dictionary.entity.Dictionary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DictionaryMapper extends BaseMapper<Dictionary> {
}
```

- [ ] **Step 6: mvn compile + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && git add -A && git commit -m "feat(s1): add Iteration and Dictionary entities and mappers"
```

---

### Task 3: 改造 Task + Bug Entity

**Files:**
- Modify: `backend-java/src/main/java/com/management/task/entity/Task.java` — 加 5 个字段
- Modify: `backend-java/src/main/java/com/management/bug/entity/Bug.java` — 加 2 个字段

- [ ] **Step 1: 在 Task.java 的 updatedAt 字段前追加新字段**

读取 Task.java，在 `private LocalDateTime updatedAt;` 之前插入：

```java
    private Long requirementId;
    private Long featureId;
    private String terminal;
    private Integer progress;
    private String performance;
```

- [ ] **Step 2: 在 Bug.java 的 updatedAt 字段前追加新字段**

读取 Bug.java，在 `private LocalDateTime updatedAt;` 之前插入：

```java
    private Long requirementId;
    private Long developerId;
```

- [ ] **Step 3: mvn compile + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && git add -A && git commit -m "feat(s1): extend Task and Bug entities with requirement fields"
```

---

### Task 4: DataInitializer 追加规则 + 默认字典

**Files:**
- Modify: `backend-java/src/main/java/com/management/common/config/DataInitializer.java`
- Create: `backend-java/src/main/resources/db/migration/V2__add_tables.sql`

- [ ] **Step 1: 编写数据库迁移 SQL**

创建 `backend-java/src/main/resources/db/migration/V2__add_tables.sql`：

```sql
CREATE TABLE IF NOT EXISTS requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id VARCHAR(128) NOT NULL,
    number VARCHAR(128) NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    source VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'planned',
    priority VARCHAR(20) DEFAULT 'medium',
    system VARCHAR(50),
    project_id BIGINT,
    project_type VARCHAR(50),
    person_id BIGINT,
    relevant TEXT,
    total_amount VARCHAR(128),
    dev_total VARCHAR(128),
    dev_price VARCHAR(128),
    test_total VARCHAR(128),
    test_price VARCHAR(128),
    biz_test_total VARCHAR(128),
    biz_test_price VARCHAR(128),
    release_time DATETIME,
    iteration_id VARCHAR(128),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'planned',
    developer_id BIGINT,
    tester_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS iterations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    iteration_id VARCHAR(128) NOT NULL,
    name VARCHAR(255) NOT NULL,
    release_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dictionaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL,
    dict_key VARCHAR(100) NOT NULL,
    dict_value VARCHAR(255) NOT NULL,
    sort_order INT DEFAULT 0
);

-- tasks 表新增字段
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS requirement_id BIGINT;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS feature_id BIGINT;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS terminal VARCHAR(50);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS progress INT DEFAULT 0;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS performance VARCHAR(128);

-- bugs 表新增字段
ALTER TABLE bugs ADD COLUMN IF NOT EXISTS requirement_id BIGINT;
ALTER TABLE bugs ADD COLUMN IF NOT EXISTS developer_id BIGINT;
```

- [ ] **Step 2: 在 DataInitializer 中追加需求工作流规则和默认字典**

读取现有 DataInitializer.java 的 `initDefaultWorkflowRules()` 方法，在其 for 循环前追加 requirement 和 feature 规则；新增 `initDefaultDictionaries()` 方法并在 `run()` 中调用。

追加的 requirement 规则（PM 手动修改状态，无自动流转）：
```java
    private void initDefaultWorkflowRules() {
        // ... 现有 task/bug 规则保持不变 ...
        // 追加 requirement 状态变更规则 (仅 PM 可操作)
        String[][] reqRules = {
            {"requirement", "pm", "planned",          "in_progress"},
            {"requirement", "pm", "planned",          "closed"},
            {"requirement", "pm", "in_progress",      "integration_test"},
            {"requirement", "pm", "in_progress",      "closed"},
            {"requirement", "pm", "integration_test", "business_test"},
            {"requirement", "pm", "integration_test", "closed"},
            {"requirement", "pm", "business_test",    "pending_release"},
            {"requirement", "pm", "business_test",    "closed"},
            {"requirement", "pm", "pending_release",  "released"},
            {"requirement", "pm", "pending_release",  "business_test"},
        };
        for (String[] r : reqRules) {
            WorkflowRule rule = new WorkflowRule();
            rule.setRuleType(r[0]); rule.setRole(r[1]);
            rule.setFromStatus(r[2]); rule.setToStatus(r[3]);
            rules.add(rule);
        }
        // 追加 feature 状态变更规则
        String[][] featRules = {
            {"feature", "pm",     "planned",      "developing"},
            {"feature", "dev",    "developing",    "pending_test"},
            {"feature", "tester", "pending_test", "closed"},
            {"feature", "pm",     "pending_test", "closed"},
        };
        for (String[] r : featRules) {
            WorkflowRule rule = new WorkflowRule();
            rule.setRuleType(r[0]); rule.setRole(r[1]);
            rule.setFromStatus(r[2]); rule.setToStatus(r[3]);
            rules.add(rule);
        }
        // ... 后续 insert 逻辑不变 ...
```

新增默认字典初始化方法：
```java
    private void initDefaultDictionaries() {
        if (dictionaryMapper.selectCount(null) > 0) return;
        String[][] dicts = {
            {"system", "backend",  "后台",     "1"},
            {"system", "ios",      "iOS",      "2"},
            {"system", "android",  "安卓",     "3"},
            {"system", "harmony",  "鸿蒙",     "4"},
            {"system", "miniapp",  "小程序",   "5"},
            {"system", "h5",       "H5",       "6"},
            {"source", "internal", "内部需求", "1"},
            {"source", "external", "客户需求", "2"},
            {"project_type", "ops",     "运维需求", "1"},
            {"project_type", "project", "项目需求", "2"},
        };
        for (String[] d : dicts) {
            Dictionary dict = new Dictionary();
            dict.setDictType(d[0]); dict.setDictKey(d[1]);
            dict.setDictValue(d[2]); dict.setSortOrder(Integer.parseInt(d[3]));
            dictionaryMapper.insert(dict);
        }
    }
```

- [ ] **Step 3: 同步更新 test/resources/schema.sql**

在 `backend-java/src/test/resources/schema.sql` 中追加上述 4 张新表的 DDL。

- [ ] **Step 4: mvn compile + mvn test + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && mvn test && git add -A && git commit -m "feat(s1): add DB migration, workflow rules, default dictionaries"
```

---

## Phase S2: 需求核心

### Task 5: Requirement DTOs

**Files:**
- Create: `backend-java/src/main/java/com/management/requirement/dto/CreateRequirementRequest.java`
- Create: `backend-java/src/main/java/com/management/requirement/dto/UpdateRequirementRequest.java`

- [ ] **Step 1: 编写 CreateRequirementRequest.java**

```java
package com.management.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRequirementRequest {
    @NotBlank private String title;
    private String description;
    private String source;
    @NotBlank private String system;
    @NotBlank private Long projectId;
    @NotBlank private String projectType;
    private Long personId;
    private String relevant;
    private String priority;
    private String totalAmount;
    private String devTotal;
    private String devPrice;
    private String testTotal;
    private String testPrice;
    private String bizTestTotal;
    private String bizTestPrice;
    private String iterationId;
}
```

- [ ] **Step 2: 编写 UpdateRequirementRequest.java**

```java
package com.management.requirement.dto;

import lombok.Data;

@Data
public class UpdateRequirementRequest {
    private String title;
    private String description;
    private String source;
    private String system;
    private Long projectId;
    private Long personId;
    private String relevant;
    private String priority;
    private String totalAmount;
    private String devTotal;
    private String devPrice;
    private String testTotal;
    private String testPrice;
    private String bizTestTotal;
    private String bizTestPrice;
    private String requirementId;
    private String iterationId;
}
```

- [ ] **Step 3: mvn compile + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && git add -A && git commit -m "feat(s2): add Requirement DTOs"
```

---

### Task 6: RequirementService + RequirementController

**Files:**
- Create: `backend-java/src/main/java/com/management/requirement/RequirementService.java`
- Create: `backend-java/src/main/java/com/management/requirement/RequirementController.java`

- [ ] **Step 1: 编写 RequirementService.java**

```java
package com.management.requirement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.project.mapper.ProjectMapper;
import com.management.requirement.dto.CreateRequirementRequest;
import com.management.requirement.dto.UpdateRequirementRequest;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequirementService {
    private final RequirementMapper requirementMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 创建需求，自动生成编号 */
    @Transactional
    public Requirement create(CreateRequirementRequest req) {
        Requirement r = new Requirement();
        r.setTitle(req.getTitle());
        r.setDescription(req.getDescription());
        r.setSource(req.getSource());
        r.setSystem(req.getSystem());
        r.setProjectId(req.getProjectId());
        r.setProjectType(req.getProjectType());
        r.setPersonId(req.getPersonId());
        r.setRelevant(req.getRelevant());
        r.setPriority(req.getPriority() != null ? req.getPriority() : "medium");
        r.setStatus("planned");
        r.setTotalAmount(req.getTotalAmount());
        r.setDevTotal(req.getDevTotal());
        r.setDevPrice(req.getDevPrice());
        r.setTestTotal(req.getTestTotal());
        r.setTestPrice(req.getTestPrice());
        r.setBizTestTotal(req.getBizTestTotal());
        r.setBizTestPrice(req.getBizTestPrice());
        r.setIterationId(req.getIterationId());
        requirementMapper.insert(r);
        // 生成唯一编号
        r.setRequirementId("REQ-" + java.time.Year.now().getValue() + "-" + String.format("%03d", r.getId()));
        r.setNumber(r.getRequirementId());
        requirementMapper.updateById(r);
        fillAssociations(r);
        log.info("Requirement created: id={}, title={}", r.getRequirementId(), r.getTitle());
        return r;
    }

    /** 按条件查询列表 */
    public List<Requirement> list(String status, String system, String projectType, String source) {
        LambdaQueryWrapper<Requirement> q = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) q.eq(Requirement::getStatus, status);
        if (system != null && !system.isBlank()) q.eq(Requirement::getSystem, system);
        if (projectType != null && !projectType.isBlank()) q.eq(Requirement::getProjectType, projectType);
        if (source != null && !source.isBlank()) q.eq(Requirement::getSource, source);
        q.orderByDesc(Requirement::getUpdatedAt);
        List<Requirement> list = requirementMapper.selectList(q);
        for (Requirement r : list) fillAssociations(r);
        return list;
    }

    /** 运维需求清单 */
    public List<Requirement> opsList() {
        List<Requirement> list = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>()
                        .eq(Requirement::getProjectType, "ops")
                        .ne(Requirement::getStatus, "released")
                        .orderByDesc(Requirement::getUpdatedAt));
        for (Requirement r : list) fillAssociations(r);
        return list;
    }

    /** 项目需求清单 */
    public List<Requirement> projectList() {
        List<Requirement> list = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>()
                        .eq(Requirement::getProjectType, "project")
                        .ne(Requirement::getStatus, "released")
                        .orderByDesc(Requirement::getUpdatedAt));
        for (Requirement r : list) fillAssociations(r);
        return list;
    }

    /** 需求详情 */
    public Requirement getById(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        fillAssociations(r);
        return r;
    }

    /** 更新需求 */
    public Requirement update(Long id, UpdateRequirementRequest req) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        if (req.getTitle() != null) r.setTitle(req.getTitle());
        if (req.getDescription() != null) r.setDescription(req.getDescription());
        if (req.getSource() != null) r.setSource(req.getSource());
        if (req.getSystem() != null) r.setSystem(req.getSystem());
        if (req.getProjectId() != null) r.setProjectId(req.getProjectId());
        if (req.getPersonId() != null) r.setPersonId(req.getPersonId());
        if (req.getRelevant() != null) r.setRelevant(req.getRelevant());
        if (req.getPriority() != null) r.setPriority(req.getPriority());
        if (req.getTotalAmount() != null) r.setTotalAmount(req.getTotalAmount());
        if (req.getDevTotal() != null) r.setDevTotal(req.getDevTotal());
        if (req.getDevPrice() != null) r.setDevPrice(req.getDevPrice());
        if (req.getTestTotal() != null) r.setTestTotal(req.getTestTotal());
        if (req.getTestPrice() != null) r.setTestPrice(req.getTestPrice());
        if (req.getBizTestTotal() != null) r.setBizTestTotal(req.getBizTestTotal());
        if (req.getBizTestPrice() != null) r.setBizTestPrice(req.getBizTestPrice());
        if (req.getIterationId() != null) r.setIterationId(req.getIterationId());
        requirementMapper.updateById(r);
        fillAssociations(r);
        return r;
    }

    /** 手动修改状态 */
    public void changeStatus(Long id, String newStatus) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        r.setStatus(newStatus);
        requirementMapper.updateById(r);
    }

    /** 删除需求 */
    public void delete(Long id) {
        requirementMapper.deleteById(id);
    }

    private void fillAssociations(Requirement r) {
        if (r.getProjectId() != null) {
            var p = projectMapper.selectById(r.getProjectId());
            if (p != null) { p.setPm(userMapper.selectById(p.getPmId())); r.setProject(p); }
        }
        if (r.getPersonId() != null) r.setPerson(userMapper.selectById(r.getPersonId()));
    }
}
```

- [ ] **Step 2: 编写 RequirementController.java**

```java
package com.management.requirement;

import com.management.common.result.Result;
import com.management.requirement.dto.CreateRequirementRequest;
import com.management.requirement.dto.UpdateRequirementRequest;
import com.management.requirement.entity.Requirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {
    private final RequirementService requirementService;

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Requirement> create(@Valid @RequestBody CreateRequirementRequest req) {
        return Result.ok(requirementService.create(req));
    }

    @GetMapping
    public Result<List<Requirement>> list(@RequestParam(required = false) String status,
                                           @RequestParam(required = false) String system,
                                           @RequestParam(required = false) String projectType,
                                           @RequestParam(required = false) String source) {
        return Result.ok(requirementService.list(status, system, projectType, source));
    }

    @GetMapping("/ops")
    public Result<List<Requirement>> opsList() {
        return Result.ok(requirementService.opsList());
    }

    @GetMapping("/project")
    public Result<List<Requirement>> projectList() {
        return Result.ok(requirementService.projectList());
    }

    @GetMapping("/{id}")
    public Result<Requirement> get(@PathVariable Long id) {
        return Result.ok(requirementService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Requirement> update(@PathVariable Long id,
                                       @RequestBody UpdateRequirementRequest req) {
        return Result.ok(requirementService.update(id, req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        requirementService.changeStatus(id, body.get("status"));
        return Result.ok(Map.of("message", "status changed"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        requirementService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
```

- [ ] **Step 3: mvn compile + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && git add -A && git commit -m "feat(s2): add RequirementService and RequirementController"
```

---

### Task 7: 需求管理前端（API + 路由 + 列表页 + 详情页）

**Files:**
- Create: `frontend/src/api/requirements.js`
- Modify: `frontend/src/router/index.js`
- Create: `frontend/src/constants/requirementMeta.js`
- Create: `frontend/src/views/Requirements.vue`
- Create: `frontend/src/views/RequirementDetail.vue`

- [ ] **Step 1: 编写 requirements.js API 模块**

```javascript
import request from './request'

export const getRequirements = (params) => request.get('/requirements', { params })
export const getRequirement = (id) => request.get(`/requirements/${id}`)
export const createRequirement = (data) => request.post('/requirements', data)
export const updateRequirement = (id, data) => request.put(`/requirements/${id}`, data)
export const changeRequirementStatus = (id, status) => request.patch(`/requirements/${id}/status`, { status })
export const deleteRequirement = (id) => request.delete(`/requirements/${id}`)
export const getOpsRequirements = () => request.get('/requirements/ops')
export const getProjectRequirements = () => request.get('/requirements/project')
```

- [ ] **Step 2: 在 router/index.js 中注册路由**

在现有路由数组中追加：

```javascript
  { path: '/requirements', name: 'Requirements', component: () => import('@/views/Requirements.vue'), meta: { requiresAuth: true } },
  { path: '/requirements/:id', name: 'RequirementDetail', component: () => import('@/views/RequirementDetail.vue'), meta: { requiresAuth: true } },
```

- [ ] **Step 3: 编写 requirementMeta.js 常量**

```javascript
export const requirementStatusMeta = {
  planned:          { label: '待规划',       tone: 'default', color: '#909399' },
  in_progress:      { label: '进行中',       tone: 'info',    color: '#2080f0' },
  integration_test: { label: '综合测试',     tone: 'warning', color: '#f59e0b' },
  business_test:    { label: '业务测试',     tone: 'warning', color: '#f0a020' },
  pending_release:  { label: '待发布',       tone: 'error',   color: '#d03050' },
  released:         { label: '已发布',       tone: 'success', color: '#18a058' },
  closed:           { label: '已关闭',       tone: 'default', color: '#909399' },
}

export const requirementSourceMeta = {
  internal: { label: '内部需求' },
  external: { label: '客户需求' },
}
```

- [ ] **Step 4: 编写 Requirements.vue（需求列表页）**

完整代码（约 300 行），核心结构：
- `<AppLayout>` 包裹，Hero 区域显示统计卡片（运维/项目需求数、各状态计数）
- 两个 NTabs：运维需求清单 / 项目需求清单，切换时分别调 `getOpsRequirements()` 和 `getProjectRequirements()`
- 筛选：系统(NSelect 单选)、状态(NSelect 单选)
- NDataTable 列：编号(点击跳详情)、标题、业务负责人、状态(NTag)、优先级(NTag 点击编辑)、开发进度(进度条)、综合测试进度(进度条)、业务测试进度(进度条)、发布迭代、所属项目、操作(纳入发布清单)
- 优先级/备注点击可直接编辑（NInput + 回车保存）

- [ ] **Step 5: 编写 RequirementDetail.vue（需求详情页）**

完整代码（约 350 行），核心结构：
- `<AppLayout>` 包裹，右上角状态 NTabs 切换
- 顶部信息卡片（渐变 Hero）：编号、业务负责人(NSelect 可编辑)、状态(NTag)、优先级(NSelect 可编辑)、发布迭代(NSelect 可编辑)、所属项目
- 描述/备注区域（NInput type=textarea，失焦自动保存）
- 开发进度卡片：按终端分组（NSelect 选择终端），进度条(NProgress) + 延期天数(NTag)
- 综合测试/业务测试进度卡片：Bug 统计网格（累计/已关闭/修复中/待复测）
- 底部操作按钮：纳入发布清单 / 从发布库移除

- [ ] **Step 6: commit**

```bash
cd frontend && git add -A && git commit -m "feat(s2): add requirement list and detail pages"
```

---

## Phase S3: 迭代 + 功能点

### Task 8: Feature DTOs + FeatureService + FeatureController

**Files:**
- Create: `backend-java/src/main/java/com/management/requirement/dto/CreateFeatureRequest.java`
- Create: `backend-java/src/main/java/com/management/requirement/dto/UpdateFeatureRequest.java`
- Create: `backend-java/src/main/java/com/management/requirement/FeatureService.java`
- Create: `backend-java/src/main/java/com/management/requirement/FeatureController.java`

- [ ] **Step 1: 编写 CreateFeatureRequest.java**

```java
package com.management.requirement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateFeatureRequest {
    @NotBlank private String title;
    private String description;
    @NotNull private Long developerId;
    private Long testerId;
}
```

- [ ] **Step 2: 编写 UpdateFeatureRequest.java**

```java
package com.management.requirement.dto;

import lombok.Data;

@Data
public class UpdateFeatureRequest {
    private String title;
    private String description;
    private Long developerId;
    private Long testerId;
}
```

- [ ] **Step 3: 编写 FeatureService.java**

```java
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

    /** 需求下功能点列表 */
    public List<Feature> listByRequirement(Long requirementId) {
        List<Feature> list = featureMapper.selectList(
                new LambdaQueryWrapper<Feature>()
                        .eq(Feature::getRequirementId, requirementId)
                        .orderByAsc(Feature::getCreatedAt));
        for (Feature f : list) fillAssociations(f);
        return list;
    }

    /** 创建功能点 */
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

    /** 更新功能点 */
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

    /** 功能点状态变更 */
    public void changeStatus(Long id, String newStatus) {
        Feature f = featureMapper.selectById(id);
        if (f == null) throw new BusinessException(404, "功能点不存在");
        f.setStatus(newStatus);
        featureMapper.updateById(f);
    }

    /** 删除功能点 */
    public void delete(Long id) {
        featureMapper.deleteById(id);
    }

    private void fillAssociations(Feature f) {
        if (f.getDeveloperId() != null) f.setDeveloper(userMapper.selectById(f.getDeveloperId()));
        if (f.getTesterId() != null) f.setTester(userMapper.selectById(f.getTesterId()));
    }
}
```

- [ ] **Step 4: 编写 FeatureController.java**

```java
package com.management.requirement;

import com.management.common.result.Result;
import com.management.requirement.dto.CreateFeatureRequest;
import com.management.requirement.dto.UpdateFeatureRequest;
import com.management.requirement.entity.Feature;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FeatureController {
    private final FeatureService featureService;

    @GetMapping("/api/requirements/{requirementId}/features")
    public Result<List<Feature>> listByRequirement(@PathVariable Long requirementId) {
        return Result.ok(featureService.listByRequirement(requirementId));
    }

    @PostMapping("/api/requirements/{requirementId}/features")
    @PreAuthorize("hasAnyRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Feature> create(@PathVariable Long requirementId,
                                   @Valid @RequestBody CreateFeatureRequest req) {
        return Result.ok(featureService.create(requirementId, req));
    }

    @PutMapping("/api/features/{id}")
    @PreAuthorize("hasAnyRole('PM')")
    public Result<Feature> update(@PathVariable Long id,
                                   @RequestBody UpdateFeatureRequest req) {
        return Result.ok(featureService.update(id, req));
    }

    @PatchMapping("/api/features/{id}/status")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        featureService.changeStatus(id, body.get("status"));
        return Result.ok(Map.of("message", "status changed"));
    }

    @DeleteMapping("/api/features/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        featureService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
```

- [ ] **Step 5: mvn compile + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && git add -A && git commit -m "feat(s3): add FeatureService and FeatureController"
```

---

### Task 9: IterationService + IterationController + 发布清单逻辑

**Files:**
- Create: `backend-java/src/main/java/com/management/iteration/IterationService.java`
- Create: `backend-java/src/main/java/com/management/iteration/IterationController.java`
- Modify: `backend-java/src/main/java/com/management/requirement/RequirementService.java` — 追加 release/unrelease 方法

- [ ] **Step 1: 编写 IterationService.java**

```java
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
```

- [ ] **Step 2: 编写 IterationController.java**

```java
package com.management.iteration;

import com.management.common.result.Result;
import com.management.iteration.entity.Iteration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iterations")
@RequiredArgsConstructor
public class IterationController {
    private final IterationService iterationService;

    @GetMapping
    public Result<List<Iteration>> list() {
        return Result.ok(iterationService.list());
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Iteration> create(@RequestBody Iteration req) {
        return Result.ok(iterationService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Iteration> update(@PathVariable Long id, @RequestBody Iteration req) {
        return Result.ok(iterationService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        iterationService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
```

- [ ] **Step 3: 在 RequirementService 中追加发布清单方法**

```java
    /** 纳入发布清单 */
    public void addToRelease(Long id, String iterationId) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        r.setIterationId(iterationId);
        r.setStatus("pending_release");
        requirementMapper.updateById(r);
    }

    /** 从发布库移除 */
    public void removeFromRelease(Long id) {
        Requirement r = requirementMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "需求不存在");
        r.setIterationId(null);
        // 回退到业务测试状态
        if ("pending_release".equals(r.getStatus()) || "released".equals(r.getStatus())) {
            r.setStatus("business_test");
        }
        requirementMapper.updateById(r);
    }
```

在 RequirementController 中追加：

```java
    @PutMapping("/{id}/release")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> addToRelease(@PathVariable Long id,
                                                     @RequestBody Map<String, String> body) {
        requirementService.addToRelease(id, body.get("iteration_id"));
        return Result.ok(Map.of("message", "added to release"));
    }

    @DeleteMapping("/{id}/release")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> removeFromRelease(@PathVariable Long id) {
        requirementService.removeFromRelease(id);
        return Result.ok(Map.of("message", "removed from release"));
    }
```

- [ ] **Step 4: 编写前端 iterations API + Iterations.vue**

`frontend/src/api/iterations.js`：
```javascript
import request from './request'
export const getIterations = () => request.get('/iterations')
export const createIteration = (data) => request.post('/iterations', data)
export const updateIteration = (id, data) => request.put(`/iterations/${id}`, data)
export const deleteIteration = (id) => request.delete(`/iterations/${id}`)
```

在 router/index.js 追加：
```javascript
  { path: '/iterations', name: 'Iterations', component: () => import('@/views/Iterations.vue'), meta: { requiresAuth: true } },
```

`Iterations.vue`：NDataTable + CRUD 模态框（PM 可见创建/编辑/删除按钮），展开行显示关联需求列表。约 150 行。

- [ ] **Step 5: mvn compile + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && git add -A && git commit -m "feat(s3): add IterationService, release checklist, Iterations page"
```

---

## Phase S4: 统计看板

### Task 10: StatisticsService + DictionaryService + 前端统计/字典页

**Files:**
- Create: `backend-java/src/main/java/com/management/statistics/StatisticsService.java`
- Create: `backend-java/src/main/java/com/management/statistics/StatisticsController.java`
- Create: `backend-java/src/main/java/com/management/dictionary/DictionaryService.java`
- Create: `backend-java/src/main/java/com/management/dictionary/DictionaryController.java`
- Create: `frontend/src/api/statistics.js`, `frontend/src/api/dictionaries.js`
- Create: `frontend/src/views/RevenueDashboard.vue`, `frontend/src/views/PerformanceStats.vue`, `frontend/src/views/Dictionary.vue`

- [ ] **Step 1: 编写 StatisticsService.java**

```java
package com.management.statistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.requirement.entity.Requirement;
import com.management.requirement.mapper.RequirementMapper;
import com.management.task.entity.Task;
import com.management.task.mapper.TaskMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final RequirementMapper requirementMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    /** 年度营收统计 */
    public Map<String, Object> revenueStats(int year) {
        List<Requirement> all = requirementMapper.selectList(
                new LambdaQueryWrapper<Requirement>()
                        .ge(Requirement::getCreatedAt, year + "-01-01")
                        .lt(Requirement::getCreatedAt, (year + 1) + "-01-01"));

        long totalCount = all.size();
        long plannedCount = all.stream().filter(r -> "planned".equals(r.getStatus())).count();
        long doneCount = all.stream().filter(r -> "released".equals(r.getStatus())).count();
        long inProgressCount = all.stream().filter(r -> "in_progress".equals(r.getStatus())).count();
        long testingCount = all.stream().filter(r -> "integration_test".equals(r.getStatus())
                || "business_test".equals(r.getStatus())).count();

        // 月度创建/完成统计
        int[] monthlyCreated = new int[12];
        int[] monthlyDone = new int[12];
        for (Requirement r : all) {
            int m = r.getCreatedAt().getMonthValue() - 1;
            monthlyCreated[m]++;
            if ("released".equals(r.getStatus()) && r.getReleaseTime() != null) {
                int rm = r.getReleaseTime().getMonthValue() - 1;
                monthlyDone[rm]++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_count", totalCount);
        result.put("planned_count", plannedCount);
        result.put("done_count", doneCount);
        result.put("in_progress_count", inProgressCount);
        result.put("testing_count", testingCount);
        result.put("monthly_created", monthlyCreated);
        result.put("monthly_done", monthlyDone);
        return result;
    }

    /** 人员绩效统计 */
    public Map<String, Object> performanceStats(int year, int month) {
        String start = String.format("%d-%02d-01", year, month);
        String end = month < 12
                ? String.format("%d-%02d-01", year, month + 1)
                : String.format("%d-01-01", year + 1);

        List<User> users = userMapper.selectList(null);
        List<Map<String, Object>> stats = new ArrayList<>();

        for (User u : users) {
            List<Task> tasks = taskMapper.selectList(
                    new LambdaQueryWrapper<Task>().eq(Task::getAssigneeId, u.getId()));
            long inProgress = tasks.stream()
                    .filter(t -> "developing".equals(t.getStatus())).count();
            long overdue = tasks.stream()
                    .filter(t -> "developing".equals(t.getStatus())
                            && t.getDeadline() != null
                            && t.getDeadline().isBefore(java.time.LocalDateTime.now()))
                    .count();
            long done = tasks.stream()
                    .filter(t -> "developed".equals(t.getStatus())
                            || "passed".equals(t.getStatus())
                            || "closed".equals(t.getStatus()))
                    .count();

            Map<String, Object> userStat = new LinkedHashMap<>();
            userStat.put("user_id", u.getId());
            userStat.put("user_name", u.getName());
            userStat.put("in_progress", inProgress);
            userStat.put("overdue", overdue);
            userStat.put("done", done);
            stats.add(userStat);
        }
        return Map.of("users", stats);
    }
}
```

- [ ] **Step 2: 编写 DictionaryService.java**

```java
package com.management.dictionary;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.dictionary.entity.Dictionary;
import com.management.dictionary.mapper.DictionaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryService {
    private final DictionaryMapper dictionaryMapper;

    public List<Dictionary> listByType(String dictType) {
        return dictionaryMapper.selectList(
                new LambdaQueryWrapper<Dictionary>()
                        .eq(Dictionary::getDictType, dictType)
                        .orderByAsc(Dictionary::getSortOrder));
    }

    public List<Dictionary> listAll() {
        return dictionaryMapper.selectList(
                new LambdaQueryWrapper<Dictionary>().orderByAsc(Dictionary::getSortOrder));
    }

    public Dictionary create(Dictionary dict) {
        dictionaryMapper.insert(dict);
        return dict;
    }

    public Dictionary update(Long id, Dictionary dict) {
        dict.setId(id);
        dictionaryMapper.updateById(dict);
        return dict;
    }

    public void delete(Long id) {
        dictionaryMapper.deleteById(id);
    }
}
```

- [ ] **Step 3: 编写 StatisticsController.java**

```java
package com.management.statistics;

import com.management.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/revenue")
    public Result<Map<String, Object>> revenue(@RequestParam(defaultValue = "2026") int year) {
        return Result.ok(statisticsService.revenueStats(year));
    }

    @GetMapping("/performance")
    public Result<Map<String, Object>> performance(@RequestParam int year, @RequestParam int month) {
        return Result.ok(statisticsService.performanceStats(year, month));
    }
}
```

编写 DictionaryController.java：

```java
package com.management.dictionary;

import com.management.common.result.Result;
import com.management.dictionary.entity.Dictionary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryService dictionaryService;

    @GetMapping
    public Result<List<Dictionary>> list(@RequestParam(required = false) String type) {
        if (type != null) return Result.ok(dictionaryService.listByType(type));
        return Result.ok(dictionaryService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Dictionary> create(@RequestBody Dictionary dict) {
        return Result.ok(dictionaryService.create(dict));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Dictionary> update(@PathVariable Long id, @RequestBody Dictionary dict) {
        return Result.ok(dictionaryService.update(id, dict));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        dictionaryService.delete(id);
        return Result.ok(Map.of("message", "deleted"));
    }
}
```

- [ ] **Step 4: 编写前端统计和字典页面**

`frontend/src/api/statistics.js`：
```javascript
import request from './request'
export const getRevenueStats = (year) => request.get('/statistics/revenue', { params: { year } })
export const getPerformanceStats = (year, month) => request.get('/statistics/performance', { params: { year, month } })
```

`frontend/src/api/dictionaries.js`：
```javascript
import request from './request'
export const getDictionaries = (type) => request.get('/dictionaries', { params: { type } })
export const createDictionary = (data) => request.post('/dictionaries', data)
export const updateDictionary = (id, data) => request.put(`/dictionaries/${id}`, data)
export const deleteDictionary = (id) => request.delete(`/dictionaries/${id}`)
```

router/index.js 追加 3 条路由：
```javascript
  { path: '/revenue', name: 'RevenueDashboard', component: () => import('@/views/RevenueDashboard.vue'), meta: { requiresAuth: true } },
  { path: '/performance', name: 'PerformanceStats', component: () => import('@/views/PerformanceStats.vue'), meta: { requiresAuth: true } },
  { path: '/dictionary', name: 'Dictionary', component: () => import('@/views/Dictionary.vue'), meta: { requiresAuth: true, requiresPM: true } },
```

三个页面核心结构：
- `RevenueDashboard.vue`：年度 NSelect 筛选→ Hero 卡片（NStatistic 累计接收/未开始/已完成/开发中/测试中）→ 月度柱状图可通过 ECharts 或原生 canvas 实现，也可先用 NDataTable 展示月度数据作为 MVP
- `PerformanceStats.vue`：年度+月度 NSelect 筛选→ 人员绩效列表（NDataTable，列：姓名/进行中任务/已延期/已完成）→ 柱状图（ECharts bar chart）
- `Dictionary.vue`：NDataTable（列：类型/键/值/排序） + 新建/编辑 NModal 表单，类型下拉选 dictType

- [ ] **Step 5: mvn compile + 前端构建验证 + commit**

```bash
cd backend-java && export JAVA_HOME="D:/repository/jdk17" && export PATH="$JAVA_HOME/bin:$PATH" && mvn compile && git add -A && git commit -m "feat(s4): add statistics and dictionary modules"
```

---

## 实施顺序总结

| 序号 | Task | Phase | 依赖 |
|------|------|-------|------|
| 1 | Requirement + Feature Entity/Mapper | S1 | — |
| 2 | Iteration + Dictionary Entity/Mapper | S1 | — |
| 3 | 改造 Task + Bug Entity | S1 | — |
| 4 | DataInitializer + 迁移 SQL | S1 | 1,2,3 |
| 5 | Requirement DTOs | S2 | 1 |
| 6 | RequirementService + Controller | S2 | 5 |
| 7 | 需求管理前端 | S2 | 6 |
| 8 | FeatureService + Controller | S3 | 1 |
| 9 | Iteration + 发布清单 + 前端 | S3 | 1,6 |
| 10 | Statistics + Dictionary 全栈 | S4 | 1,6 |

每个 Task 都以 `mvn compile` + `git commit` 结束，S1/S2/S3/S4 各有独立验证点。
