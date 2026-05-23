# Go → Java 后端重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 Go/Gin 后端完全重构为 Java Spring Boot 应用，API 接口与前端完全兼容。

**Architecture:** 按业务领域分包（auth/task/bug/project/user/group/common），标准三层架构（Controller → Service → Mapper），Spring Security 无状态 JWT 认证，MyBatis-Plus 持久层，MySQL 数据库。

**Tech Stack:** Java 17, Spring Boot 3.2.7, MyBatis-Plus 3.5.7, MySQL 8.x, JJWT 0.12.6, Lombok, JUnit 5

---

### Task 1: 项目骨架

**Files:**
- Create: `backend-java/pom.xml`
- Create: `backend-java/src/main/resources/application.yml`
- Create: `backend-java/src/main/resources/application-dev.yml`
- Create: `backend-java/src/main/java/com/management/ManagementApplication.java`

- [ ] **Step 1: 创建目录结构**

```bash
mkdir -p backend-java/src/main/java/com/management
mkdir -p backend-java/src/main/resources
mkdir -p backend-java/src/test/java/com/management
```

- [ ] **Step 2: 编写 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.7</version>
        <relativePath/>
    </parent>

    <groupId>com.management</groupId>
    <artifactId>management</artifactId>
    <version>1.0.0</version>
    <name>management</name>
    <description>Workflow Management System Backend</description>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 编写 application.yml**

```yaml
server:
  port: ${PORT:8080}

spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DB:management}?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver
  mail:
    host: ${SMTP_HOST:}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USER:}
    password: ${SMTP_PASSWORD:}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

mybatis-plus:
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

app:
  jwt-secret: ${JWT_SECRET:your-secret-key-change-in-production}
  nanobot-path: ${NANOBOT_PATH:nanobot}
  notification:
    email-enable: ${NOTIFY_EMAIL_ENABLE:false}
    nanobot-enable: ${NOTIFY_NANOBOT_ENABLE:true}
    async: ${NOTIFY_ASYNC:true}
```

- [ ] **Step 4: 编写 application-dev.yml**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/management?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

- [ ] **Step 5: 编写 ManagementApplication.java**

```java
package com.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagementApplication.class, args);
    }
}
```

- [ ] **Step 6: 执行 mvn compile 验证项目可编译**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 7: Commit**

```bash
cd backend-java && git init && git add -A && git commit -m "chore: init Spring Boot project skeleton"
```

---

### Task 2: 通用基础组件

**Files:**
- Create: `backend-java/src/main/java/com/management/common/result/Result.java`
- Create: `backend-java/src/main/java/com/management/common/exception/BusinessException.java`
- Create: `backend-java/src/main/java/com/management/common/exception/GlobalExceptionHandler.java`
- Create: `backend-java/src/main/java/com/management/common/config/PasswordEncoderConfig.java`

- [ ] **Step 1: 编写 Result.java**

```java
package com.management.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private T data;
    private String error;

    private Result() {}

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.data = data;
        return r;
    }

    public static Result<Void> ok() {
        return new Result<>();
    }

    public static Result<Void> fail(String error) {
        Result<Void> r = new Result<>();
        r.error = error;
        return r;
    }
}
```

- [ ] **Step 2: 编写 BusinessException.java**

```java
package com.management.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int status;

    public BusinessException(String message) {
        super(message);
        this.status = 400;
    }

    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }
}
```

- [ ] **Step 3: 编写 GlobalExceptionHandler.java**

```java
package com.management.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusiness(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", msg);
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied() {
        return ResponseEntity.status(403).body(Map.of("error", "无权限执行此操作"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnknown(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(500).body(Map.of("error", "服务器内部错误"));
    }
}
```

- [ ] **Step 4: 编写 PasswordEncoderConfig.java**

```java
package com.management.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 5: 执行 mvn compile 验证编译**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 6: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add Result, BusinessException, GlobalExceptionHandler"
```

---

### Task 3: JWT 安全链路

**Files:**
- Create: `backend-java/src/main/java/com/management/common/jwt/JwtUtils.java`
- Create: `backend-java/src/main/java/com/management/common/jwt/JwtUserDetails.java`
- Create: `backend-java/src/main/java/com/management/common/jwt/JwtAuthFilter.java`
- Create: `backend-java/src/main/java/com/management/common/config/SecurityConfig.java`

- [ ] **Step 1: 编写 JwtUserDetails.java**

```java
package com.management.common.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class JwtUserDetails implements UserDetails {
    private Long userId;
    private String name;
    private String role;
    private Long groupId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return name; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
```

- [ ] **Step 2: 编写 JwtUtils.java**

```java
package com.management.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey key;

    public JwtUtils(@Value("${app.jwt-secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())));
    }

    /** 生成 JWT，有效期 24 小时 */
    public String generateToken(Long userId, String name, String role, Long groupId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .claim("user_id", userId)
                .claim("name", name)
                .claim("role", role)
                .claim("group_id", groupId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 从 token 解析 Claims */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **Step 3: 编写 JwtAuthFilter.java**

```java
package com.management.common.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtUtils.parseToken(token);

            JwtUserDetails userDetails = new JwtUserDetails(
                    ((Number) claims.get("user_id")).longValue(),
                    (String) claims.get("name"),
                    (String) claims.get("role"),
                    claims.get("group_id") != null
                            ? ((Number) claims.get("group_id")).longValue() : null
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.debug("JWT parse failed: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
```

- [ ] **Step 4: 编写 SecurityConfig.java**

```java
package com.management.common.config;

import com.management.common.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    PrintWriter w = res.getWriter();
                    w.write("{\"error\":\"未登录或令牌已过期\"}");
                    w.flush();
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType("application/json;charset=UTF-8");
                    PrintWriter w = res.getWriter();
                    w.write("{\"error\":\"无权限执行此操作\"}");
                    w.flush();
                }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

- [ ] **Step 5: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 6: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add JWT authentication chain with Spring Security"
```

---

### Task 4: 配置类

**Files:**
- Create: `backend-java/src/main/java/com/management/common/config/MyBatisPlusConfig.java`
- Create: `backend-java/src/main/java/com/management/common/config/WebMvcConfig.java`
- Create: `backend-java/src/main/java/com/management/common/config/AsyncConfig.java`

- [ ] **Step 1: 编写 MyBatisPlusConfig.java**

```java
package com.management.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

- [ ] **Step 2: 编写 WebMvcConfig.java**

```java
package com.management.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(
                        "file:../frontend/dist/assets/",
                        "file:frontend/dist/assets/",
                        "classpath:/static/assets/");
    }
}
```

- [ ] **Step 3: 编写 AsyncConfig.java**

```java
package com.management.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("notifyExecutor")
    public Executor notifyExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notify-");
        executor.initialize();
        return executor;
    }
}
```

- [ ] **Step 4: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 5: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add MyBatisPlus, CORS, async configs"
```

---

### Task 4b: 启动初始化器

> **依赖**: Task 5 (UserMapper), Task 7 (WorkflowRuleMapper, WorkflowService) 完成后才可编译通过。

**Files:**
- Create: `backend-java/src/main/java/com/management/common/config/DataInitializer.java`

- [ ] **Step 1: 编写 DataInitializer.java**

```java
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

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserMapper userMapper;
    private final WorkflowRuleMapper workflowRuleMapper;
    private final WorkflowService workflowService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        initDefaultAdmin();
        initDefaultWorkflowRules();
    }

    /** 无 PM 用户时创建默认管理员 admin/admin123 */
    private void initDefaultAdmin() {
        long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "pm"));
        if (count == 0) {
            log.warn("No PM user found, creating default admin (admin/admin123)");
            User admin = new User();
            admin.setName("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("pm");
            userMapper.insert(admin);
            log.info("Default admin created, id={}", admin.getId());
        }
    }

    /** workflow_rules 为空时插入默认流转规则 */
    private void initDefaultWorkflowRules() {
        if (workflowRuleMapper.selectCount(null) > 0) return;

        log.info("Initializing default workflow rules");
        List<WorkflowRule> rules = List.of(
            // === task rules ===
            rule("task", "pm",          "pending",       "assigned_lead"),
            rule("task", "pm",          "pending",       "closed"),
            rule("task", "pm",          "pending_test",  "testing"),
            rule("task", "pm",          "pending_test",  "closed"),
            rule("task", "pm",          "rejected",      "assigned_lead"),
            rule("task", "pm",          "rejected",      "closed"),
            rule("task", "pm",          "passed",        "closed"),
            rule("task", "dev_lead",    "assigned_lead", "developing"),
            rule("task", "dev_lead",    "assigned_lead", "rejected"),
            rule("task", "dev_lead",    "pending_test",  "testing"),
            rule("task", "dev_lead",    "rejected",      "developing"),
            rule("task", "dev",         "developing",    "developed"),
            rule("task", "tester_lead", "pending_test",  "testing"),
            rule("task", "tester_lead", "testing",       "passed"),
            rule("task", "tester_lead", "testing",       "rejected"),
            rule("task", "tester",      "testing",       "passed"),
            rule("task", "tester",      "testing",       "rejected"),
            // === bug rules ===
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
            // cross-role rules
            rule("bug", "tester",      "new",           "assigned"),
            rule("bug", "tester_lead", "new",           "assigned"),
            rule("bug", "tester",      "reopened",      "assigned"),
            rule("bug", "tester_lead", "reopened",      "assigned"),
            rule("bug", "pm",          "reopened",      "assigned"),
            rule("bug", "pm",          "new",           "assigned")
        );
        for (WorkflowRule rule : rules) {
            workflowRuleMapper.insert(rule);
        }
        workflowService.refreshCache();
        log.info("Default workflow rules initialized, count={}", rules.size());
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
```

- [ ] **Step 2: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 3: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add DataInitializer (default admin + workflow rules)"
```

---

### Task 5: Entity 层 — User + Project + Group

**Files:**
- Create: `backend-java/src/main/java/com/management/user/entity/User.java`
- Create: `backend-java/src/main/java/com/management/user/mapper/UserMapper.java`
- Create: `backend-java/src/main/java/com/management/project/entity/Project.java`
- Create: `backend-java/src/main/java/com/management/project/mapper/ProjectMapper.java`
- Create: `backend-java/src/main/java/com/management/group/entity/Group.java`
- Create: `backend-java/src/main/java/com/management/group/mapper/GroupMapper.java`

- [ ] **Step 1: 编写 User.java**

```java
package com.management.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    @JsonIgnore
    private String password;
    private String role;
    private Long groupId;
    private String wechatId;
    private String dingtalkId;
    private String feishuId;
    private String email;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: 编写 UserMapper.java**

```java
package com.management.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

- [ ] **Step 3: 编写 Project.java**

```java
package com.management.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("projects")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long pmId;
    @TableField(exist = false)
    private User pm;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: 编写 ProjectMapper.java**

```java
package com.management.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.project.entity.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
```

- [ ] **Step 5: 编写 Group.java**

```java
package com.management.group.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("groups")
public class Group {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long pmId;
    @TableField(exist = false)
    private User pm;
    private Long devLeadId;
    @TableField(exist = false)
    private User devLead;
    private String leadRole;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 6: 编写 GroupMapper.java**

```java
package com.management.group.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.group.entity.Group;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {
}
```

- [ ] **Step 7: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 8: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add User, Project, Group entities and mappers"
```

---

### Task 6: Entity 层 — Task + Bug 及相关表

**Files:**
- Create: `backend-java/src/main/java/com/management/task/entity/Task.java`
- Create: `backend-java/src/main/java/com/management/task/entity/TaskAssignee.java`
- Create: `backend-java/src/main/java/com/management/task/entity/TaskStatusHistory.java`
- Create: `backend-java/src/main/java/com/management/task/mapper/TaskMapper.java`
- Create: `backend-java/src/main/java/com/management/task/mapper/TaskAssigneeMapper.java`
- Create: `backend-java/src/main/java/com/management/task/mapper/TaskStatusHistoryMapper.java`
- Create: `backend-java/src/main/java/com/management/bug/entity/Bug.java`
- Create: `backend-java/src/main/java/com/management/bug/entity/BugStatusHistory.java`
- Create: `backend-java/src/main/java/com/management/bug/mapper/BugMapper.java`
- Create: `backend-java/src/main/java/com/management/bug/mapper/BugStatusHistoryMapper.java`

- [ ] **Step 1: 编写 Task.java**

```java
package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.project.entity.Project;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("tasks")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long projectId;
    @TableField(exist = false)
    private Project project;
    private Long creatorId;
    @TableField(exist = false)
    private User creator;
    private Long assigneeId;
    @TableField(exist = false)
    private User assignee;
    private Long devLeadId;
    @TableField(exist = false)
    private User devLead;
    private Long testerLeadId;
    @TableField(exist = false)
    private User testerLead;
    private Long testerId;
    @TableField(exist = false)
    private User tester;
    @TableField(exist = false)
    private List<TaskAssignee> assignees;
    private String rejectReason;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: 编写 TaskAssignee.java**

```java
package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_assignees")
public class TaskAssignee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long userId;
    @TableField(exist = false)
    private User user;
    private String platform;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 3: 编写 TaskStatusHistory.java**

```java
package com.management.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_status_histories")
public class TaskStatusHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String fromStatus;
    private String toStatus;
    private Long changedBy;
    @TableField(exist = false)
    private User user;
    private LocalDateTime changedAt;
    private String comment;
}
```

- [ ] **Step 4: 编写三个 Task Mapper**

```java
// TaskMapper.java
package com.management.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.task.entity.Task;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}

// TaskAssigneeMapper.java
package com.management.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.task.entity.TaskAssignee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskAssigneeMapper extends BaseMapper<TaskAssignee> {
}

// TaskStatusHistoryMapper.java
package com.management.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.task.entity.TaskStatusHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskStatusHistoryMapper extends BaseMapper<TaskStatusHistory> {
}
```

- [ ] **Step 5: 编写 Bug.java**

```java
package com.management.bug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.task.entity.Task;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bugs")
public class Bug {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String severity;
    private String status;
    private Long taskId;
    @TableField(exist = false)
    private Task task;
    private Long creatorId;
    @TableField(exist = false)
    private User creator;
    private Long assigneeId;
    @TableField(exist = false)
    private User assignee;
    private String fixComment;
    private String reopenReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 6: 编写 BugStatusHistory.java**

```java
package com.management.bug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.management.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bug_status_histories")
public class BugStatusHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bugId;
    private String fromStatus;
    private String toStatus;
    private Long changedBy;
    @TableField(exist = false)
    private User user;
    private LocalDateTime changedAt;
    private String comment;
}
```

- [ ] **Step 7: 编写 Bug Mapper**

```java
// BugMapper.java
package com.management.bug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.bug.entity.Bug;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BugMapper extends BaseMapper<Bug> {
}

// BugStatusHistoryMapper.java
package com.management.bug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.management.bug.entity.BugStatusHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BugStatusHistoryMapper extends BaseMapper<BugStatusHistory> {
}
```

- [ ] **Step 8: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 9: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add Task, Bug entities and mappers"
```

---

### Task 7: Workflow 规则实体与 Service

**Files:**
- Create: `backend-java/src/main/java/com/management/common/workflow/WorkflowRule.java`
- Create: `backend-java/src/main/java/com/management/common/workflow/WorkflowRuleMapper.java`
- Create: `backend-java/src/main/java/com/management/common/workflow/WorkflowService.java`

- [ ] **Step 1: 编写 WorkflowRule.java**

```java
package com.management.common.workflow;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("workflow_rules")
public class WorkflowRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** task | bug */
    private String ruleType;
    /** pm | dev_lead | dev | tester_lead | tester */
    private String role;
    /** 当前状态 */
    private String fromStatus;
    /** 目标状态 */
    private String toStatus;
}
```

- [ ] **Step 2: 编写 WorkflowRuleMapper.java**

```java
package com.management.common.workflow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowRuleMapper extends BaseMapper<WorkflowRule> {
}
```

- [ ] **Step 3: 编写 WorkflowService.java**

```java
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

    /** 判断流转是否合法 */
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
```

- [ ] **Step 4: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 5: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add WorkflowRule entity, mapper, and WorkflowService with cache"
```

---

### Task 8: Auth 领域

**Files:**
- Create: `backend-java/src/main/java/com/management/auth/LoginRequest.java`
- Create: `backend-java/src/main/java/com/management/auth/RegisterRequest.java`
- Create: `backend-java/src/main/java/com/management/auth/LoginResponse.java`
- Create: `backend-java/src/main/java/com/management/auth/AuthService.java`
- Create: `backend-java/src/main/java/com/management/auth/AuthController.java`

- [ ] **Step 1: 编写 DTOs**

```java
// LoginRequest.java
package com.management.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String password;
}

// RegisterRequest.java
package com.management.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;
    @NotBlank @Size(min = 6)
    private String password;
    @NotBlank
    private String role;
    private Long groupId;
    private String wechatId;
}

// LoginResponse.java
package com.management.auth;

import com.management.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private User user;
}
```

- [ ] **Step 2: 编写 AuthService.java**

```java
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

    /** 注册，返回新用户 ID */
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
        userMapper.insert(user);
        log.info("User registered: name={}, role={}, id={}", req.getName(), req.getRole(), user.getId());
        return user.getId();
    }

    /** 登录，返回 JWT + user */
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
```

- [ ] **Step 3: 编写 AuthController.java**

```java
package com.management.auth;

import com.management.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        Long userId = authService.register(req);
        return Result.ok(Map.of("message", "user created successfully", "user_id", userId));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }
}
```

- [ ] **Step 4: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 5: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add auth domain (register/login)"
```

---

### Task 9: User 领域

**Files:**
- Create: `backend-java/src/main/java/com/management/user/UserService.java`
- Create: `backend-java/src/main/java/com/management/user/UserController.java`

- [ ] **Step 1: 编写 UserService.java**

```java
package com.management.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    /** 获取当前登录用户 */
    public JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 列出用户：tester/tester_lead 只能看 dev */
    public List<User> listUsers() {
        JwtUserDetails user = currentUser();
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        if ("tester".equals(user.getRole()) || "tester_lead".equals(user.getRole())) {
            q.eq(User::getRole, "dev");
        }
        return userMapper.selectList(q);
    }

    /** 获取单个用户 */
    public User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        return user;
    }

    /** 更新用户角色 */
    public void updateRole(Long id, String role, Long groupId) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        user.setRole(role);
        if (groupId != null) {
            user.setGroupId(groupId);
        }
        userMapper.updateById(user);
    }
}
```

- [ ] **Step 2: 编写 UserController.java**

```java
package com.management.user;

import com.management.common.result.Result;
import com.management.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PM','DEV_LEAD','DEV','TESTER','TESTER_LEAD')")
    public Result<List<User>> listUsers() {
        return Result.ok(userService.listUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.ok(userService.getUser(id));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> updateRole(@PathVariable Long id,
                                                   @RequestBody UpdateRoleRequest req) {
        userService.updateRole(id, req.getRole(), req.getGroupId());
        return Result.ok(Map.of("message", "user updated"));
    }
}
```

- [ ] **Step 3: 编写 UpdateRoleRequest（内联 DTO）**

```java
package com.management.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
class UpdateRoleRequest {
    @NotBlank
    private String role;
    private Long groupId;
}
```

- [ ] **Step 4: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 5: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add user domain (list/get/updateRole)"
```

---

### Task 10: Project 领域

**Files:**
- Create: `backend-java/src/main/java/com/management/project/dto/CreateProjectRequest.java`
- Create: `backend-java/src/main/java/com/management/project/dto/UpdateProjectRequest.java`
- Create: `backend-java/src/main/java/com/management/project/ProjectService.java`
- Create: `backend-java/src/main/java/com/management/project/ProjectController.java`

- [ ] **Step 1: 编写 DTOs**

```java
// dto/CreateProjectRequest.java
package com.management.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotBlank
    private String name;
}

// dto/UpdateProjectRequest.java
package com.management.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProjectRequest {
    @NotBlank
    private String name;
}
```

- [ ] **Step 2: 编写 ProjectService.java**

```java
package com.management.project;

import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.project.dto.CreateProjectRequest;
import com.management.project.dto.UpdateProjectRequest;
import com.management.project.entity.Project;
import com.management.project.mapper.ProjectMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    public List<Project> listProjects() {
        List<Project> projects = projectMapper.selectList(null);
        for (Project p : projects) {
            p.setPm(userMapper.selectById(p.getPmId()));
        }
        return projects;
    }

    public Project createProject(CreateProjectRequest req) {
        Long userId = currentUser().getUserId();
        Project project = new Project();
        project.setName(req.getName());
        project.setPmId(userId);
        projectMapper.insert(project);
        return project;
    }

    public Project getProject(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(404, "project not found");
        }
        project.setPm(userMapper.selectById(project.getPmId()));
        return project;
    }

    public void updateProject(Long id, UpdateProjectRequest req) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(404, "project not found");
        }
        project.setName(req.getName());
        projectMapper.updateById(project);
    }

    public void deleteProject(Long id) {
        projectMapper.deleteById(id);
    }
}
```

- [ ] **Step 3: 编写 ProjectController.java**

```java
package com.management.project;

import com.management.common.result.Result;
import com.management.project.dto.CreateProjectRequest;
import com.management.project.dto.UpdateProjectRequest;
import com.management.project.entity.Project;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public Result<List<Project>> list() {
        return Result.ok(projectService.listProjects());
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Project> create(@Valid @RequestBody CreateProjectRequest req) {
        return Result.ok(projectService.createProject(req));
    }

    @GetMapping("/{id}")
    public Result<Project> get(@PathVariable Long id) {
        return Result.ok(projectService.getProject(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> update(@PathVariable Long id,
                                              @Valid @RequestBody UpdateProjectRequest req) {
        projectService.updateProject(id, req);
        return Result.ok(Map.of("message", "project updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.ok(Map.of("message", "project deleted"));
    }
}
```

- [ ] **Step 4: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 5: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add project domain (CRUD)"
```

---

### Task 11: Group 领域

**Files:**
- Create: `backend-java/src/main/java/com/management/group/dto/CreateGroupRequest.java`
- Create: `backend-java/src/main/java/com/management/group/dto/UpdateGroupRequest.java`
- Create: `backend-java/src/main/java/com/management/group/dto/AddMemberRequest.java`
- Create: `backend-java/src/main/java/com/management/group/GroupService.java`
- Create: `backend-java/src/main/java/com/management/group/GroupController.java`

- [ ] **Step 1: 编写 DTOs**

```java
// dto/CreateGroupRequest.java
package com.management.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateGroupRequest {
    @NotBlank
    private String name;
    @NotNull
    private Long devLeadId;
    private String leadRole;
}

// dto/UpdateGroupRequest.java
package com.management.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateGroupRequest {
    @NotBlank
    private String name;
    private Long devLeadId;
    private String leadRole;
}

// dto/AddMemberRequest.java
package com.management.group.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberRequest {
    @NotNull
    private Long userId;
}
```

- [ ] **Step 2: 编写 GroupService.java**

```java
package com.management.group;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.group.dto.CreateGroupRequest;
import com.management.group.dto.UpdateGroupRequest;
import com.management.group.entity.Group;
import com.management.group.mapper.GroupMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 列出所有组，含组长和成员计数 */
    public List<Map<String, Object>> listGroups() {
        List<Group> groups = groupMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Group g : groups) {
            g.setDevLead(userMapper.selectById(g.getDevLeadId()));
            String expectedRole = "test".equals(g.getLeadRole()) ? "tester" : "dev";
            long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getGroupId, g.getId())
                    .eq(User::getRole, expectedRole));
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("name", g.getName());
            item.put("pm_id", g.getPmId());
            item.put("dev_lead_id", g.getDevLeadId());
            item.put("lead_role", g.getLeadRole());
            item.put("created_at", g.getCreatedAt());
            item.put("dev_lead", g.getDevLead());
            item.put("member_count", count);
            result.add(item);
        }
        return result;
    }

    /** 创建组 */
    public Group createGroup(CreateGroupRequest req) {
        Long userId = currentUser().getUserId();
        String leadRole = req.getLeadRole() != null ? req.getLeadRole() : "dev";
        Group group = new Group();
        group.setName(req.getName());
        group.setPmId(userId);
        group.setDevLeadId(req.getDevLeadId());
        group.setLeadRole(leadRole);
        groupMapper.insert(group);

        // 将组长的 group_id 设为此组
        User devLead = userMapper.selectById(req.getDevLeadId());
        if (devLead != null) {
            devLead.setGroupId(group.getId());
            userMapper.updateById(devLead);
        }
        log.info("Group created: name={}, id={}, leadRole={}", group.getName(), group.getId(), leadRole);
        return group;
    }

    /** 获取组详情 + 成员列表 */
    public Map<String, Object> getGroup(Long id) {
        Group group = groupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException(404, "group not found");
        }
        group.setDevLead(userMapper.selectById(group.getDevLeadId()));
        List<User> members = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getGroupId, id));
        return Map.of("group", group, "members", members);
    }

    /** 更新组 */
    public void updateGroup(Long id, UpdateGroupRequest req) {
        Group oldGroup = groupMapper.selectById(id);
        if (oldGroup == null) {
            throw new BusinessException(404, "group not found");
        }
        // 更换组长时，更新新旧组长的 group_id
        if (req.getDevLeadId() != null && !req.getDevLeadId().equals(oldGroup.getDevLeadId())) {
            User oldLead = new User();
            oldLead.setId(oldGroup.getDevLeadId());
            oldLead.setGroupId(null);
            userMapper.updateById(oldLead);

            User newLead = new User();
            newLead.setId(req.getDevLeadId());
            newLead.setGroupId(id);
            userMapper.updateById(newLead);
        }
        oldGroup.setName(req.getName());
        if (req.getDevLeadId() != null) oldGroup.setDevLeadId(req.getDevLeadId());
        if (req.getLeadRole() != null) oldGroup.setLeadRole(req.getLeadRole());
        groupMapper.updateById(oldGroup);
    }

    /** 删除组，清空成员 group_id */
    public void deleteGroup(Long id) {
        User update = new User();
        update.setGroupId(null);
        userMapper.update(update, new LambdaQueryWrapper<User>()
                .eq(User::getGroupId, id));
        groupMapper.deleteById(id);
    }

    /** 添加成员 */
    public void addMember(Long groupId, Long userId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) throw new BusinessException(404, "group not found");

        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "user not found");

        String expectedRole = "test".equals(group.getLeadRole()) ? "tester" : "dev";
        if (!expectedRole.equals(user.getRole())) {
            throw new BusinessException("member role does not match group type");
        }
        user.setGroupId(groupId);
        userMapper.updateById(user);
    }

    /** 移除成员 */
    public void removeMember(Long groupId, Long userId) {
        User user = new User();
        user.setId(userId);
        user.setGroupId(null);
        userMapper.update(user, new LambdaQueryWrapper<User>()
                .eq(User::getId, userId).eq(User::getGroupId, groupId));
    }

    /** 开发组长查看自己的组 */
    public Map<String, Object> getMyTeam() {
        JwtUserDetails u = currentUser();
        Group group = groupMapper.selectOne(new LambdaQueryWrapper<Group>()
                .eq(Group::getDevLeadId, u.getUserId()));
        if (group == null) {
            return Map.of("group", null, "members", List.of());
        }
        List<User> members = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getGroupId, group.getId()));
        return Map.of("group", group, "members", members);
    }
}
```

- [ ] **Step 3: 编写 GroupController.java**

```java
package com.management.group;

import com.management.common.result.Result;
import com.management.group.dto.AddMemberRequest;
import com.management.group.dto.CreateGroupRequest;
import com.management.group.dto.UpdateGroupRequest;
import com.management.group.entity.Group;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(groupService.listGroups());
    }

    @PostMapping
    @PreAuthorize("hasRole('PM')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Group> create(@Valid @RequestBody CreateGroupRequest req) {
        return Result.ok(groupService.createGroup(req));
    }

    @GetMapping("/my-team")
    @PreAuthorize("hasRole('DEV_LEAD')")
    public Result<Map<String, Object>> myTeam() {
        return Result.ok(groupService.getMyTeam());
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        return Result.ok(groupService.getGroup(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateGroupRequest req) {
        groupService.updateGroup(id, req);
        return Result.ok(Map.of("message", "group updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> delete(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return Result.ok(Map.of("message", "group deleted"));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> addMember(@PathVariable Long id,
                                                  @Valid @RequestBody AddMemberRequest req) {
        groupService.addMember(id, req.getUserId());
        return Result.ok(Map.of("message", "member added"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('PM')")
    public Result<Map<String, String>> removeMember(@PathVariable Long id,
                                                     @PathVariable Long userId) {
        groupService.removeMember(id, userId);
        return Result.ok(Map.of("message", "member removed"));
    }
}
```

- [ ] **Step 4: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 5: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add group domain (CRUD + members + my-team)"
```

---

### Task 12: 通知系统

**Files:**
- Create: `backend-java/src/main/java/com/management/common/notification/Notifier.java`
- Create: `backend-java/src/main/java/com/management/common/notification/LogNotifier.java`
- Create: `backend-java/src/main/java/com/management/common/notification/EmailNotifier.java`
- Create: `backend-java/src/main/java/com/management/common/notification/NanobotNotifier.java`
- Create: `backend-java/src/main/java/com/management/common/notification/MultiNotifier.java`
- Create: `backend-java/src/main/java/com/management/common/notification/NotificationService.java`

- [ ] **Step 1: 编写 Notifier 接口**

```java
package com.management.common.notification;

public interface Notifier {
    void notify(String recipient, String message);
}
```

- [ ] **Step 2: 编写 LogNotifier.java**

```java
package com.management.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogNotifier implements Notifier {
    @Override
    public void notify(String recipient, String message) {
        log.info("[Notify] to={}, msg={}", recipient, message);
    }
}
```

- [ ] **Step 3: 编写 EmailNotifier.java**

```java
package com.management.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.notification.email-enable", havingValue = "true")
@RequiredArgsConstructor
public class EmailNotifier implements Notifier {
    private final JavaMailSender mailSender;

    @Override
    public void notify(String email, String message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("Management System Notification");
            mail.setText(message);
            mailSender.send(mail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
        }
    }
}
```

- [ ] **Step 4: 编写 NanobotNotifier.java**

```java
package com.management.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.notification.nanobot-enable", havingValue = "true", matchIfMissing = true)
public class NanobotNotifier implements Notifier {

    private final String nanobotPath;

    public NanobotNotifier(@Value("${app.nanobot-path:nanobot}") String nanobotPath) {
        this.nanobotPath = nanobotPath;
    }

    @Override
    public void notify(String userName, String message) {
        try {
            String instruction = "通知用户 " + userName + "：" + message;
            ProcessBuilder pb = new ProcessBuilder(nanobotPath, "agent", "-m", instruction);
            Process p = pb.start();
            boolean finished = p.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                log.warn("Nanobot timeout for user {}", userName);
                return;
            }
            if (p.exitValue() != 0) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    log.warn("Nanobot error: {}", br.readLine());
                }
            }
        } catch (Exception e) {
            log.error("Nanobot notify failed: {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 5: 编写 MultiNotifier.java**

```java
package com.management.common.notification;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MultiNotifier implements Notifier {
    private final List<Notifier> delegates = new ArrayList<>();

    public void addDelegate(Notifier notifier) {
        this.delegates.add(notifier);
    }

    @Override
    public void notify(String recipient, String message) {
        for (Notifier n : delegates) {
            n.notify(recipient, message);
        }
    }
}
```

- [ ] **Step 6: 编写 NotificationService.java**

```java
package com.management.common.notification;

import com.management.bug.entity.Bug;
import com.management.task.entity.Task;
import com.management.user.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MultiNotifier multiNotifier;
    private final LogNotifier logNotifier;
    @SuppressWarnings("all")
    private final EmailNotifier emailNotifier;
    @SuppressWarnings("all")
    private final NanobotNotifier nanobotNotifier;

    @Value("${app.notification.email-enable:false}")
    private boolean emailEnable;
    @Value("${app.notification.nanobot-enable:true}")
    private boolean nanobotEnable;
    @Value("${app.notification.async:true}")
    private boolean asyncEnabled;

    private static final Map<String, String> TASK_STATUS_LABELS = Map.of(
            "pending", "待分配",
            "assigned_lead", "已分配",
            "developing", "开发中",
            "developed", "开发完成",
            "pending_test", "待测试",
            "testing", "测试中",
            "passed", "测试通过",
            "rejected", "打回修改",
            "closed", "已关闭"
    );

    private static final Map<String, String> BUG_STATUS_LABELS = Map.of(
            "new", "新建",
            "assigned", "已分配",
            "fixing", "修复中",
            "fixed", "已修复",
            "pending_verify", "待验证",
            "closed", "已关闭",
            "reopened", "重新打开"
    );

    @PostConstruct
    void init() {
        multiNotifier.addDelegate(logNotifier);
        try {
            if (emailEnable && emailNotifier != null) multiNotifier.addDelegate(emailNotifier);
            if (nanobotEnable && nanobotNotifier != null) multiNotifier.addDelegate(nanobotNotifier);
        } catch (Exception ignored) {
            // EmailNotifier/NanobotNotifier may not be in context
        }
    }

    /** 发送任务事件通知 */
    public void emitTaskEvent(Task task, String oldStatus, String newStatus,
                               String operatorName, List<User> targets, String comment) {
        String msg = buildTaskMessage(task.getTitle(), oldStatus, newStatus, operatorName, comment);
        if (asyncEnabled) {
            sendAsync(targets, operatorName, msg);
        } else {
            sendToTargets(targets, operatorName, msg);
        }
    }

    /** 发送 Bug 事件通知 */
    public void emitBugEvent(Bug bug, String oldStatus, String newStatus,
                              String operatorName, List<User> targets, String comment) {
        String msg = buildBugMessage(bug.getTitle(), oldStatus, newStatus, operatorName, comment);
        if (asyncEnabled) {
            sendAsync(targets, operatorName, msg);
        } else {
            sendToTargets(targets, operatorName, msg);
        }
    }

    @Async("notifyExecutor")
    public void sendAsync(List<User> targets, String operatorName, String message) {
        sendToTargets(targets, operatorName, message);
    }

    private void sendToTargets(List<User> targets, String operatorName, String message) {
        if (targets == null) return;
        for (User target : targets) {
            if (operatorName != null && operatorName.equals(target.getName())) continue;
            if (target.getEmail() != null && !target.getEmail().isBlank()) {
                multiNotifier.notify(target.getEmail(), message);
            }
            multiNotifier.notify(target.getName(), message);
        }
    }

    private String buildTaskMessage(String title, String oldStatus, String newStatus,
                                     String operator, String comment) {
        String oldLabel = TASK_STATUS_LABELS.getOrDefault(oldStatus, oldStatus);
        String newLabel = TASK_STATUS_LABELS.getOrDefault(newStatus, newStatus);
        StringBuilder sb = new StringBuilder();
        sb.append("任务【").append(title).append("】状态已从【")
                .append(oldLabel).append("】变更为【").append(newLabel)
                .append("】，操作人：").append(operator);
        if (comment != null && !comment.isBlank()) {
            sb.append("，备注：").append(comment);
        }
        sb.append("。请及时处理。");
        return sb.toString();
    }

    private String buildBugMessage(String title, String oldStatus, String newStatus,
                                    String operator, String comment) {
        String oldLabel = BUG_STATUS_LABELS.getOrDefault(oldStatus, oldStatus);
        String newLabel = BUG_STATUS_LABELS.getOrDefault(newStatus, newStatus);
        StringBuilder sb = new StringBuilder();
        sb.append("Bug【").append(title).append("】状态已从【")
                .append(oldLabel).append("】变更为【").append(newLabel)
                .append("】，操作人：").append(operator);
        if (comment != null && !comment.isBlank()) {
            sb.append("，备注：").append(comment);
        }
        sb.append("。请及时处理。");
        return sb.toString();
    }
}
```

- [ ] **Step 7: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 8: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add notification system (email + nanobot + async)"
```

---

### Task 13: Task 领域 — DTOs + 查询操作

**Files:**
- Create: `backend-java/src/main/java/com/management/task/dto/CreateTaskRequest.java`
- Create: `backend-java/src/main/java/com/management/task/dto/UpdateTaskRequest.java`
- Create: `backend-java/src/main/java/com/management/task/dto/ChangeTaskStatusRequest.java`
- Create: `backend-java/src/main/java/com/management/task/dto/AddTaskAssigneeRequest.java`
- Create: `backend-java/src/main/java/com/management/task/TaskService.java` (query methods)
- Create: `backend-java/src/main/java/com/management/task/TaskController.java` (query endpoints)

- [ ] **Step 1: 编写 DTOs**

```java
// dto/CreateTaskRequest.java
package com.management.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateTaskRequest {
    @NotBlank
    private String title;
    private String description;
    private String priority;
    @NotNull
    private Long projectId;
    @NotNull
    private Long devLeadId;
    private Long assigneeId;
    private List<Long> assigneeIds;
    private List<TaskAssigneeItem> assignees;
    private Long testerId;
    private String deadline;

    @Data
    public static class TaskAssigneeItem {
        @NotNull
        private Long userId;
        private String platform;
    }
}

// dto/UpdateTaskRequest.java
package com.management.task.dto;

import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private String priority;
    private Long projectId;
    private Long devLeadId;
    private Long assigneeId;
    private Long testerLeadId;
    private Long testerId;
    private String deadline;
}

// dto/ChangeTaskStatusRequest.java
package com.management.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeTaskStatusRequest {
    @NotBlank
    private String newStatus;
    private String comment;
}

// dto/AddTaskAssigneeRequest.java
package com.management.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTaskAssigneeRequest {
    @NotNull
    private Long userId;
    private String platform;
}
```

- [ ] **Step 2: 编写 TaskService.java（查询 + 辅助方法）**

```java
package com.management.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.common.notification.NotificationService;
import com.management.common.workflow.WorkflowService;
import com.management.project.entity.Project;
import com.management.project.mapper.ProjectMapper;
import com.management.task.dto.*;
import com.management.task.entity.*;
import com.management.task.mapper.*;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final TaskAssigneeMapper taskAssigneeMapper;
    private final TaskStatusHistoryMapper historyMapper;
    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;

    /** 获取当前用户 */
    public JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 按角色过滤查询任务列表 */
    public List<Task> listTasks(String projectId, String status, String priority) {
        JwtUserDetails u = currentUser();
        LambdaQueryWrapper<Task> q = new LambdaQueryWrapper<>();

        switch (u.getRole()) {
            case "pm":
                // PM 查看所有
                break;
            case "dev_lead":
                // 本组任务
                if (u.getGroupId() != null) {
                    q.and(w -> w.eq(Task::getDevLeadId, u.getUserId())
                            .or().inSql(Task::getAssigneeId,
                                    "SELECT id FROM users WHERE group_id = " + u.getGroupId()));
                } else {
                    q.eq(Task::getDevLeadId, u.getUserId());
                }
                break;
            case "dev":
                // 看自己被分配到的任务
                q.inSql(Task::getId,
                        "SELECT task_id FROM task_assignees WHERE user_id = " + u.getUserId());
                break;
            case "tester_lead":
                // 测试组长
                q.and(w -> w.eq(Task::getTesterLeadId, u.getUserId())
                        .or().in(Task::getStatus, List.of("pending_test", "testing", "passed", "rejected")));
                break;
            case "tester":
                // tester_id=自己 或 creator_id=自己
                q.and(w -> w.eq(Task::getTesterId, u.getUserId())
                        .or().eq(Task::getCreatorId, u.getUserId()));
                break;
            default:
                q.apply("1=0");
        }

        if (projectId != null && !projectId.isBlank()) q.eq(Task::getProjectId, projectId);
        if (status != null && !status.isBlank()) q.eq(Task::getStatus, status);
        if (priority != null && !priority.isBlank()) q.eq(Task::getPriority, priority);

        q.orderByDesc(Task::getUpdatedAt);
        List<Task> tasks = taskMapper.selectList(q);
        // 填充关联对象
        for (Task t : tasks) {
            fillAssociations(t);
        }
        return tasks;
    }

    /** 获取任务详情（含 assignees） */
    public TaskDetail getTask(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) throw new BusinessException(404, "task not found");
        fillAssociations(task);

        List<TaskAssignee> assignees = taskAssigneeMapper.selectList(
                new LambdaQueryWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, id));
        for (TaskAssignee ta : assignees) {
            ta.setUser(userMapper.selectById(ta.getUserId()));
        }

        TaskDetail detail = new TaskDetail();
        detail.setTask(task);
        detail.setAssignees(assignees);
        return detail;
    }

    /** 获取状态历史 */
    public List<TaskStatusHistory> getTaskHistory(Long id) {
        List<TaskStatusHistory> list = historyMapper.selectList(
                new LambdaQueryWrapper<TaskStatusHistory>()
                        .eq(TaskStatusHistory::getTaskId, id)
                        .orderByDesc(TaskStatusHistory::getChangedAt));
        for (TaskStatusHistory h : list) {
            h.setUser(userMapper.selectById(h.getChangedBy()));
        }
        return list;
    }

    /** 填充关联对象 */
    private void fillAssociations(Task t) {
        if (t.getProjectId() != null) {
            Project p = projectMapper.selectById(t.getProjectId());
            if (p != null) { p.setPm(userMapper.selectById(p.getPmId())); t.setProject(p); }
        }
        if (t.getCreatorId() != null) t.setCreator(userMapper.selectById(t.getCreatorId()));
        if (t.getAssigneeId() != null) t.setAssignee(userMapper.selectById(t.getAssigneeId()));
        if (t.getDevLeadId() != null) t.setDevLead(userMapper.selectById(t.getDevLeadId()));
        if (t.getTesterLeadId() != null) t.setTesterLead(userMapper.selectById(t.getTesterLeadId()));
        if (t.getTesterId() != null) t.setTester(userMapper.selectById(t.getTesterId()));
    }

    /** Task 详情 DTO */
    @lombok.Data
    public static class TaskDetail {
        private Task task;
        private List<TaskAssignee> assignees;
    }
}
```

- [ ] **Step 3: 编写 TaskController.java（查询部分）**

```java
package com.management.task;

import com.management.common.result.Result;
import com.management.task.entity.Task;
import com.management.task.entity.TaskStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public Result<List<Task>> list(@RequestParam(required = false) String projectId,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String priority) {
        return Result.ok(taskService.listTasks(projectId, status, priority));
    }

    @GetMapping("/{id}")
    public Result<TaskService.TaskDetail> get(@PathVariable Long id) {
        return Result.ok(taskService.getTask(id));
    }

    @GetMapping("/{id}/history")
    public Result<List<TaskStatusHistory>> history(@PathVariable Long id) {
        return Result.ok(taskService.getTaskHistory(id));
    }
}
```

- [ ] **Step 4: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 5: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add task DTOs, list/get/history queries"
```

---

### Task 14: Task 领域 — 写操作与状态机

**Files:**
- Modify: `backend-java/src/main/java/com/management/task/TaskService.java` (追加写操作)
- Modify: `backend-java/src/main/java/com/management/task/TaskController.java` (追加写端点)

- [ ] **Step 1: 在 TaskService.java 中追加写操作方法**

```java
/** 创建任务（仅 PM） */
@Transactional
public Task createTask(CreateTaskRequest req) {
    Long userId = currentUser().getUserId();

    LocalDateTime deadline = null;
    if (req.getDeadline() != null && !req.getDeadline().isBlank()) {
        deadline = LocalDate.parse(req.getDeadline(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
    }

    Task task = new Task();
    task.setTitle(req.getTitle());
    task.setDescription(req.getDescription());
    task.setStatus("pending");
    task.setPriority(req.getPriority() != null ? req.getPriority() : "medium");
    task.setProjectId(req.getProjectId());
    task.setCreatorId(userId);
    task.setDevLeadId(req.getDevLeadId());
    task.setDeadline(deadline);
    if (req.getAssigneeId() != null) task.setAssigneeId(req.getAssigneeId());
    if (req.getTesterId() != null) task.setTesterId(req.getTesterId());
    taskMapper.insert(task);

    // 处理多指派人
    if (req.getAssignees() != null && !req.getAssignees().isEmpty()) {
        for (var item : req.getAssignees()) {
            TaskAssignee ta = new TaskAssignee();
            ta.setTaskId(task.getId());
            ta.setUserId(item.getUserId());
            ta.setPlatform(item.getPlatform());
            ta.setStatus("pending");
            taskAssigneeMapper.insert(ta);
        }
        if (task.getAssigneeId() == null) {
            task.setAssigneeId(req.getAssignees().get(0).getUserId());
            taskMapper.updateById(task);
        }
    } else if (req.getAssigneeIds() != null && !req.getAssigneeIds().isEmpty()) {
        for (Long uid : req.getAssigneeIds()) {
            TaskAssignee ta = new TaskAssignee();
            ta.setTaskId(task.getId());
            ta.setUserId(uid);
            ta.setStatus("pending");
            taskAssigneeMapper.insert(ta);
        }
        if (task.getAssigneeId() == null) {
            task.setAssigneeId(req.getAssigneeIds().get(0));
            taskMapper.updateById(task);
        }
    } else if (req.getAssigneeId() != null) {
        TaskAssignee ta = new TaskAssignee();
        ta.setTaskId(task.getId());
        ta.setUserId(req.getAssigneeId());
        ta.setStatus("pending");
        taskAssigneeMapper.insert(ta);
    }

    fillAssociations(task);
    log.info("Task created: id={}, title={}", task.getId(), task.getTitle());
    return task;
}

/** 更新任务基本信息 */
public Task updateTask(Long id, UpdateTaskRequest req) {
    Task task = taskMapper.selectById(id);
    if (task == null) throw new BusinessException(404, "task not found");

    Long oldTesterId = task.getTesterId();

    if (req.getTitle() != null && !req.getTitle().isBlank()) task.setTitle(req.getTitle());
    if (req.getDescription() != null) task.setDescription(req.getDescription());
    if (req.getPriority() != null && !req.getPriority().isBlank()) task.setPriority(req.getPriority());
    if (req.getProjectId() != null) task.setProjectId(req.getProjectId());
    if (req.getDevLeadId() != null) task.setDevLeadId(req.getDevLeadId());
    if (req.getAssigneeId() != null) task.setAssigneeId(req.getAssigneeId());
    if (req.getTesterLeadId() != null) task.setTesterLeadId(req.getTesterLeadId());
    if (req.getTesterId() != null) task.setTesterId(req.getTesterId());
    if (req.getDeadline() != null && !req.getDeadline().isBlank()) {
        task.setDeadline(LocalDate.parse(req.getDeadline(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay());
    }
    taskMapper.updateById(task);

    // tester_id 变化时通知新 tester
    if (req.getTesterId() != null && oldTesterId != null && !req.getTesterId().equals(oldTesterId)) {
        User newTester = userMapper.selectById(req.getTesterId());
        if (newTester != null) {
            JwtUserDetails op = currentUser();
            notificationService.emitTaskEvent(task, task.getStatus(), task.getStatus(),
                    op.getName(), List.of(newTester), "您被分配为测试人员");
        }
    }

    fillAssociations(task);
    return task;
}

/** 变更任务状态（核心状态机逻辑） */
@Transactional
public void changeStatus(Long taskId, ChangeTaskStatusRequest req) {
    Long operatorId = currentUser().getUserId();
    String role = currentUser().getRole();
    String newStatus = req.getNewStatus();

    Task task = taskMapper.selectById(taskId);
    if (task == null) throw new BusinessException("任务不存在");

    String oldStatus = task.getStatus();

    // rejected 必须有原因
    if ("rejected".equals(newStatus) && (req.getComment() == null || req.getComment().isBlank())) {
        throw new BusinessException("reject reason is required");
    }

    // 多开发场景处理
    if ("developing".equals(newStatus) && "dev".equals(role) && "developing".equals(task.getStatus())) {
        taskAssigneeMapper.update(null, new LambdaQueryWrapper<TaskAssignee>()
                .eq(TaskAssignee::getTaskId, taskId)
                .eq(TaskAssignee::getUserId, operatorId)
                .set(TaskAssignee::getStatus, "developing"));
        return;
    }
    if ("developed".equals(newStatus) && "dev".equals(role)) {
        List<TaskAssignee> assignees = taskAssigneeMapper.selectList(
                new LambdaQueryWrapper<TaskAssignee>().eq(TaskAssignee::getTaskId, taskId));
        if (!assignees.isEmpty()) {
            taskAssigneeMapper.update(null, new LambdaQueryWrapper<TaskAssignee>()
                    .eq(TaskAssignee::getTaskId, taskId)
                    .eq(TaskAssignee::getUserId, operatorId)
                    .set(TaskAssignee::getStatus, "developed"));
            long pending = taskAssigneeMapper.selectCount(new LambdaQueryWrapper<TaskAssignee>()
                    .eq(TaskAssignee::getTaskId, taskId).ne(TaskAssignee::getStatus, "developed"));
            if (pending > 0) return; // 还有人没完成
        }
    }

    // 校验流转合法性
    if (!workflowService.isAllowed(role, oldStatus, newStatus, "task")) {
        throw new BusinessException(
                String.format("角色 %s 不能将任务从 %s 变更为 %s", role, oldStatus, newStatus));
    }

    // 更新状态
    task.setStatus(newStatus);
    taskMapper.updateById(task);

    // 记录历史
    TaskStatusHistory history = new TaskStatusHistory();
    history.setTaskId(taskId);
    history.setFromStatus(oldStatus);
    history.setToStatus(newStatus);
    history.setChangedBy(operatorId);
    history.setComment(req.getComment());
    historyMapper.insert(history);

    log.info("Task {} status changed: {} -> {} by user {} ({})",
            taskId, oldStatus, newStatus, operatorId, role);

    // 通知目标
    String operatorName = currentUser().getName();
    List<User> targets = resolveTaskNotifyTargets(task, oldStatus, newStatus);
    notificationService.emitTaskEvent(task, oldStatus, newStatus, operatorName, targets, req.getComment());

    // 关键：developed -> pending_test 自动跳转
    if ("developed".equals(newStatus)) {
        task.setStatus("pending_test");
        taskMapper.updateById(task);

        TaskStatusHistory autoHistory = new TaskStatusHistory();
        autoHistory.setTaskId(taskId);
        autoHistory.setFromStatus("developed");
        autoHistory.setToStatus("pending_test");
        autoHistory.setChangedBy(0L);
        autoHistory.setComment("系统自动将任务加入测试池");
        historyMapper.insert(autoHistory);

        if (task.getTesterLeadId() != null) {
            User testerLead = userMapper.selectById(task.getTesterLeadId());
            if (testerLead != null) {
                notificationService.emitTaskEvent(task, "developed", "pending_test",
                        null, List.of(testerLead), "任务已加入测试池");
            }
        }
    }

    // rejected 时保存原因到任务
    if ("rejected".equals(newStatus)) {
        taskMapper.update(null, new LambdaQueryWrapper<Task>()
                .eq(Task::getId, taskId).set(Task::getRejectReason, req.getComment()));
    }

    // dev 开始开发时更新 TaskAssignee
    if ("developing".equals(newStatus) && "dev".equals(role)) {
        taskAssigneeMapper.update(null, new LambdaQueryWrapper<TaskAssignee>()
                .eq(TaskAssignee::getTaskId, taskId)
                .eq(TaskAssignee::getUserId, operatorId)
                .set(TaskAssignee::getStatus, "developing"));
    }
}

/** 根据状态流转确定通知目标（对等 Go resolveTaskNotifyTargets） */
private List<User> resolveTaskNotifyTargets(Task task, String oldStatus, String newStatus) {
    List<User> targets = new ArrayList<>();
    String key = oldStatus + "->" + newStatus;

    switch (key) {
        case "pending->assigned_lead":
            if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
            break;
        case "assigned_lead->developing":
            if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
            break;
        case "developing->developed":
            if (task.getTesterLeadId() != null) addUser(targets, task.getTesterLeadId());
            break;
        case "pending_test->testing":
            if (task.getTesterLeadId() != null) addUser(targets, task.getTesterLeadId());
            if (task.getTesterId() != null) addUser(targets, task.getTesterId());
            break;
        case "testing->passed":
            Project p = projectMapper.selectById(task.getProjectId());
            if (p != null) addUser(targets, p.getPmId());
            if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
            if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
            break;
        case "testing->rejected":
            if (task.getAssigneeId() != null) addUser(targets, task.getAssigneeId());
            if (task.getDevLeadId() != null) addUser(targets, task.getDevLeadId());
            break;
        case "rejected->developing":
            if (task.getTesterId() != null) addUser(targets, task.getTesterId());
            if (task.getTesterLeadId() != null) addUser(targets, task.getTesterLeadId());
            break;
    }
    return targets;
}

private void addUser(List<User> targets, Long userId) {
    User u = userMapper.selectById(userId);
    if (u != null) targets.add(u);
}

/** 添加指派人 */
public TaskAssignee addAssignee(Long taskId, AddTaskAssigneeRequest req) {
    TaskAssignee existing = taskAssigneeMapper.selectOne(new LambdaQueryWrapper<TaskAssignee>()
            .eq(TaskAssignee::getTaskId, taskId).eq(TaskAssignee::getUserId, req.getUserId()));
    if (existing != null) throw new BusinessException("user already assigned to this task");

    TaskAssignee ta = new TaskAssignee();
    ta.setTaskId(taskId);
    ta.setUserId(req.getUserId());
    ta.setPlatform(req.getPlatform());
    ta.setStatus("pending");
    taskAssigneeMapper.insert(ta);

    Task task = taskMapper.selectById(taskId);
    if (task != null && task.getAssigneeId() == null) {
        task.setAssigneeId(req.getUserId());
        taskMapper.updateById(task);
    }
    return ta;
}

/** 移除指派人 */
public void removeAssignee(Long taskId, Long userId) {
    taskAssigneeMapper.delete(new LambdaQueryWrapper<TaskAssignee>()
            .eq(TaskAssignee::getTaskId, taskId).eq(TaskAssignee::getUserId, userId));
}
```

- [ ] **Step 2: 在 TaskController.java 中追加写端点**

```java
// 追加到 TaskController.java：

@PostMapping
@PreAuthorize("hasRole('PM')")
@org.springframework.http.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
public Result<Task> create(@Valid @RequestBody CreateTaskRequest req) {
    return Result.ok(taskService.createTask(req));
}

@PutMapping("/{id}")
public Result<Task> update(@PathVariable Long id, @RequestBody UpdateTaskRequest req) {
    return Result.ok(taskService.updateTask(id, req));
}

@PatchMapping("/{id}/status")
public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                 @Valid @RequestBody ChangeTaskStatusRequest req) {
    taskService.changeStatus(id, req);
    return Result.ok(Map.of("message", "status changed successfully"));
}

@PostMapping("/{id}/assignees")
@PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
public Result<TaskAssignee> addAssignee(@PathVariable Long id,
                                         @Valid @RequestBody AddTaskAssigneeRequest req) {
    return Result.ok(taskService.addAssignee(id, req));
}

@DeleteMapping("/{id}/assignees/{userId}")
@PreAuthorize("hasAnyRole('PM','DEV_LEAD')")
public Result<Map<String, String>> removeAssignee(@PathVariable Long id,
                                                   @PathVariable Long userId) {
    taskService.removeAssignee(id, userId);
    return Result.ok(Map.of("message", "assignee removed"));
}
```

- [ ] **Step 3: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 4: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add task write operations (create/update/changeStatus/assignees)"
```

---

### Task 15: Bug 领域

**Files:**
- Create: `backend-java/src/main/java/com/management/bug/dto/CreateBugRequest.java`
- Create: `backend-java/src/main/java/com/management/bug/dto/UpdateBugRequest.java`
- Create: `backend-java/src/main/java/com/management/bug/dto/ChangeBugStatusRequest.java`
- Create: `backend-java/src/main/java/com/management/bug/BugService.java`
- Create: `backend-java/src/main/java/com/management/bug/BugController.java`

- [ ] **Step 1: 编写 Bug DTOs**

```java
// dto/CreateBugRequest.java
package com.management.bug.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBugRequest {
    @NotBlank private String title;
    private String description;
    private String severity;
    @NotNull private Long taskId;
    @NotNull private Long assigneeId;
}

// dto/UpdateBugRequest.java
package com.management.bug.dto;

import lombok.Data;

@Data
public class UpdateBugRequest {
    private String title;
    private String description;
    private String severity;
    private Long taskId;
    private Long assigneeId;
    private String fixComment;
    private String reopenReason;
}

// dto/ChangeBugStatusRequest.java
package com.management.bug.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeBugStatusRequest {
    @NotBlank private String newStatus;
    private String comment;
}
```

- [ ] **Step 2: 编写 BugService.java**

```java
package com.management.bug;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.management.bug.dto.*;
import com.management.bug.entity.*;
import com.management.bug.mapper.*;
import com.management.common.exception.BusinessException;
import com.management.common.jwt.JwtUserDetails;
import com.management.common.notification.NotificationService;
import com.management.common.workflow.WorkflowService;
import com.management.task.entity.Task;
import com.management.task.mapper.TaskMapper;
import com.management.user.entity.User;
import com.management.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BugService {
    private final BugMapper bugMapper;
    private final BugStatusHistoryMapper historyMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final WorkflowService workflowService;
    private final NotificationService notificationService;

    private JwtUserDetails currentUser() {
        return (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 按角色过滤 Bug 列表 */
    public List<Bug> listBugs(String taskId, String status, String severity) {
        JwtUserDetails u = currentUser();
        LambdaQueryWrapper<Bug> q = new LambdaQueryWrapper<>();

        switch (u.getRole()) {
            case "pm":
            case "tester_lead":
                break;
            case "dev_lead":
                if (u.getGroupId() != null) {
                    q.inSql(Bug::getAssigneeId,
                            "SELECT id FROM users WHERE group_id = " + u.getGroupId());
                } else {
                    q.eq(Bug::getAssigneeId, u.getUserId());
                }
                break;
            case "dev":
                q.eq(Bug::getAssigneeId, u.getUserId());
                break;
            case "tester":
                q.eq(Bug::getCreatorId, u.getUserId());
                break;
            default:
                q.apply("1=0");
        }

        if (taskId != null && !taskId.isBlank()) q.eq(Bug::getTaskId, taskId);
        if (status != null && !status.isBlank()) q.eq(Bug::getStatus, status);
        if (severity != null && !severity.isBlank()) q.eq(Bug::getSeverity, severity);
        q.orderByDesc(Bug::getUpdatedAt);

        List<Bug> bugs = bugMapper.selectList(q);
        for (Bug b : bugs) fillAssociations(b);
        return bugs;
    }

    /** 创建 Bug */
    @Transactional
    public Bug createBug(CreateBugRequest req) {
        Long userId = currentUser().getUserId();
        Bug bug = new Bug();
        bug.setTitle(req.getTitle());
        bug.setDescription(req.getDescription());
        bug.setSeverity(req.getSeverity() != null ? req.getSeverity() : "medium");
        bug.setStatus("assigned"); // 创建即指派
        bug.setTaskId(req.getTaskId());
        bug.setCreatorId(userId);
        bug.setAssigneeId(req.getAssigneeId());
        bugMapper.insert(bug);

        fillAssociations(bug);

        // 通知被指派人
        if (bug.getAssigneeId() != null) {
            User target = userMapper.selectById(bug.getAssigneeId());
            if (target != null) {
                notificationService.emitBugEvent(bug, "new", "assigned",
                        currentUser().getName(), List.of(target), "");
            }
        }

        log.info("Bug created: id={}, title={}", bug.getId(), bug.getTitle());
        return bug;
    }

    /** 获取 Bug 详情 */
    public Bug getBug(Long id) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw new BusinessException(404, "bug not found");
        fillAssociations(bug);
        return bug;
    }

    /** 更新 Bug */
    public Bug updateBug(Long id, UpdateBugRequest req) {
        Bug bug = bugMapper.selectById(id);
        if (bug == null) throw new BusinessException(404, "bug not found");
        if (req.getTitle() != null && !req.getTitle().isBlank()) bug.setTitle(req.getTitle());
        if (req.getDescription() != null) bug.setDescription(req.getDescription());
        if (req.getSeverity() != null && !req.getSeverity().isBlank()) bug.setSeverity(req.getSeverity());
        if (req.getTaskId() != null) bug.setTaskId(req.getTaskId());
        if (req.getAssigneeId() != null) bug.setAssigneeId(req.getAssigneeId());
        if (req.getFixComment() != null) bug.setFixComment(req.getFixComment());
        if (req.getReopenReason() != null) bug.setReopenReason(req.getReopenReason());
        bugMapper.updateById(bug);
        fillAssociations(bug);
        return bug;
    }

    /** 变更 Bug 状态 */
    @Transactional
    public void changeStatus(Long bugId, ChangeBugStatusRequest req) {
        Long operatorId = currentUser().getUserId();
        String role = currentUser().getRole();
        Bug bug = bugMapper.selectById(bugId);
        if (bug == null) throw new BusinessException("Bug 不存在");

        if ("reopened".equals(req.getNewStatus())
                && (req.getComment() == null || req.getComment().isBlank())) {
            throw new BusinessException("reopen reason is required");
        }

        String oldStatus = bug.getStatus();

        if (!workflowService.isAllowed(role, oldStatus, req.getNewStatus(), "bug")) {
            throw new BusinessException(
                    String.format("角色 %s 不能将 Bug 从 %s 变更为 %s", role, oldStatus, req.getNewStatus()));
        }

        bug.setStatus(req.getNewStatus());
        bugMapper.updateById(bug);

        BugStatusHistory history = new BugStatusHistory();
        history.setBugId(bugId);
        history.setFromStatus(oldStatus);
        history.setToStatus(req.getNewStatus());
        history.setChangedBy(operatorId);
        history.setComment(req.getComment());
        historyMapper.insert(history);

        log.info("Bug {} status changed: {} -> {} by user {}",
                bugId, oldStatus, req.getNewStatus(), operatorId);

        String operatorName = currentUser().getName();
        List<User> targets = resolveBugNotifyTargets(bug, oldStatus, req.getNewStatus());
        notificationService.emitBugEvent(bug, oldStatus, req.getNewStatus(), operatorName, targets, req.getComment());

        // 关键：fixed -> pending_verify 自动跳转
        if ("fixed".equals(req.getNewStatus())) {
            bug.setStatus("pending_verify");
            bugMapper.updateById(bug);

            BugStatusHistory autoHistory = new BugStatusHistory();
            autoHistory.setBugId(bugId);
            autoHistory.setFromStatus("fixed");
            autoHistory.setToStatus("pending_verify");
            autoHistory.setChangedBy(0L);
            autoHistory.setComment("系统自动将 Bug 置为待验证");
            historyMapper.insert(autoHistory);

            List<User> verifyTargets = resolveBugNotifyTargets(bug, "fixed", "pending_verify");
            notificationService.emitBugEvent(bug, "fixed", "pending_verify",
                    operatorName, verifyTargets, "Bug 已进入待验证");
        }

        // reopened 时保存原因
        if ("reopened".equals(req.getNewStatus())) {
            bugMapper.update(null, new LambdaQueryWrapper<Bug>()
                    .eq(Bug::getId, bugId).set(Bug::getReopenReason, req.getComment()));
        }
    }

    /** 获取 Bug 状态历史 */
    public List<BugStatusHistory> getBugHistory(Long id) {
        List<BugStatusHistory> list = historyMapper.selectList(
                new LambdaQueryWrapper<BugStatusHistory>()
                        .eq(BugStatusHistory::getBugId, id)
                        .orderByDesc(BugStatusHistory::getChangedAt));
        for (BugStatusHistory h : list) {
            h.setUser(userMapper.selectById(h.getChangedBy()));
        }
        return list;
    }

    private List<User> resolveBugNotifyTargets(Bug bug, String oldStatus, String newStatus) {
        List<User> targets = new ArrayList<>();
        String key = oldStatus + "->" + newStatus;
        switch (key) {
            case "new->assigned":
            case "reopened->assigned":
                if (bug.getAssigneeId() != null) addUser(targets, bug.getAssigneeId());
                break;
            case "assigned->fixing":
                addUser(targets, bug.getCreatorId());
                break;
            case "fixing->fixed":
            case "fixed->pending_verify":
                addUser(targets, bug.getCreatorId());
                break;
            case "pending_verify->closed":
            case "pending_verify->reopened":
                if (bug.getAssigneeId() != null) addUser(targets, bug.getAssigneeId());
                break;
        }
        return targets;
    }

    private void addUser(List<User> targets, Long userId) {
        User u = userMapper.selectById(userId);
        if (u != null) targets.add(u);
    }

    private void fillAssociations(Bug b) {
        if (b.getTaskId() != null) b.setTask(taskMapper.selectById(b.getTaskId()));
        if (b.getCreatorId() != null) b.setCreator(userMapper.selectById(b.getCreatorId()));
        if (b.getAssigneeId() != null) b.setAssignee(userMapper.selectById(b.getAssigneeId()));
    }
}
```

- [ ] **Step 3: 编写 BugController.java**

```java
package com.management.bug;

import com.management.bug.dto.*;
import com.management.bug.entity.*;
import com.management.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bugs")
@RequiredArgsConstructor
public class BugController {
    private final BugService bugService;

    @GetMapping
    public Result<List<Bug>> list(@RequestParam(required = false) String taskId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String severity) {
        return Result.ok(bugService.listBugs(taskId, status, severity));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TESTER','TESTER_LEAD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Bug> create(@Valid @RequestBody CreateBugRequest req) {
        return Result.ok(bugService.createBug(req));
    }

    @GetMapping("/{id}")
    public Result<Bug> get(@PathVariable Long id) {
        return Result.ok(bugService.getBug(id));
    }

    @PutMapping("/{id}")
    public Result<Bug> update(@PathVariable Long id, @RequestBody UpdateBugRequest req) {
        return Result.ok(bugService.updateBug(id, req));
    }

    @PatchMapping("/{id}/status")
    public Result<Map<String, String>> changeStatus(@PathVariable Long id,
                                                     @Valid @RequestBody ChangeBugStatusRequest req) {
        bugService.changeStatus(id, req);
        return Result.ok(Map.of("message", "status changed successfully"));
    }

    @GetMapping("/{id}/history")
    public Result<List<BugStatusHistory>> history(@PathVariable Long id) {
        return Result.ok(bugService.getBugHistory(id));
    }
}
```

- [ ] **Step 4: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 5: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add bug domain (CRUD + state machine)"
```

---

### Task 16: 静态资源与 SPA fallback

**Files:**
- Create: `backend-java/src/main/java/com/management/common/config/SpaFallbackController.java`

- [ ] **Step 1: 编写 SpaFallbackController.java**

```java
package com.management.common.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpaFallbackController {

    /** SPA 首页 */
    @GetMapping("/")
    @ResponseBody
    public String index() {
        if (new ClassPathResource("static/index.html").exists()) {
            return "forward:/index.html";
        }
        return "Management API is running. Frontend not built yet.";
    }
}
```

- [ ] **Step 2: mvn compile 验证**

```bash
cd backend-java && mvn compile
```

- [ ] **Step 3: Commit**

```bash
cd backend-java && git add -A && git commit -m "feat: add SPA fallback controller"
```

---

### Task 17: Controller 集成测试

**Files:**
- Create: `backend-java/src/test/java/com/management/AuthControllerTest.java`
- Create: `backend-java/src/test/java/com/management/TaskControllerTest.java`
- Create: `backend-java/src/test/resources/application-test.yml`

- [ ] **Step 1: 编写 application-test.yml**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
  jackson:
    property-naming-strategy: SNAKE_CASE
app:
  jwt-secret: test-secret-key-for-jwt-testing-purposes-only
  notification:
    email-enable: false
    nanobot-enable: false
    async: false
```

- [ ] **Step 2: 编写 AuthControllerTest.java**

```java
package com.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.auth.LoginRequest;
import com.management.auth.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void registerAndLogin_shouldReturnTokenAndUser() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("testpm");
        reg.setPassword("test123");
        reg.setRole("pm");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user_id").isNumber());

        LoginRequest login = new LoginRequest();
        login.setName("testpm");
        login.setPassword("test123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.name").value("testpm"));
    }

    @Test
    void registerDuplicate_shouldReturn409() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("dupuser");
        reg.setPassword("test123");
        reg.setRole("pm");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").value("username already exists"));
    }

    @Test
    void loginWithWrongPassword_shouldReturn401() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("wrongpw");
        reg.setPassword("test123");
        reg.setRole("pm");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setName("wrongpw");
        login.setPassword("badpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").value("invalid username or password"));
    }
}
```

- [ ] **Step 3: 编写 TaskControllerTest.java**

```java
package com.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.auth.LoginRequest;
import com.management.auth.RegisterRequest;
import com.management.task.dto.CreateTaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        // 注册并登录 admin
        RegisterRequest reg = new RegisterRequest();
        reg.setName("admin_t");
        reg.setPassword("test123");
        reg.setRole("pm");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setName("admin_t");
        login.setPassword("test123");
        String resp = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();
        adminToken = objectMapper.readTree(resp).get("data").get("token").asText();
    }

    @Test
    void listTasks_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createTask_shouldReturnCreatedTask() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("测试任务");
        req.setProjectId(1L);
        req.setDevLeadId(1L);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("测试任务"))
                .andExpect(jsonPath("$.data.status").value("pending"));
    }

    @Test
    void getTask_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/tasks/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()) // 200 with error
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
```

- [ ] **Step 4: 运行测试**

```bash
cd backend-java && mvn test -Dtest="AuthControllerTest,TaskControllerTest"
```

- [ ] **Step 5: 修复编译问题后 Commit**

```bash
cd backend-java && git add -A && git commit -m "test: add controller integration tests"
```

---

### Task 18: 最终验证与文档

- [ ] **Step 1: 运行全部测试**

```bash
cd backend-java && mvn clean test
```

- [ ] **Step 2: 构建打包**

```bash
cd backend-java && mvn clean package -DskipTests
```

- [ ] **Step 3: Commit**

```bash
cd backend-java && git add -A && git commit -m "chore: final verification and polish"
```

---

## 实施顺序总结

| 序号 | 任务 | 依赖 |
|------|------|------|
| 1 | 项目骨架 | — |
| 2 | 通用基础组件 | 1 |
| 3 | JWT 安全链路 | 2 |
| 4 | 配置类 | 1 |
| 5 | Entity: User/Project/Group | 1 |
| 6 | Entity: Task/Bug | 1 |
| 7 | Workflow 规则 | 1 |
| 4b | 启动初始化器 | 5+7 |
| 8 | Auth 领域 | 3+5 |
| 9 | User 领域 | 3+5 |
| 10 | Project 领域 | 3+5 |
| 11 | Group 领域 | 3+5 |
| 12 | 通知系统 | 1 |
| 13 | Task 查询操作 | 3+5+6+7 |
| 14 | Task 写操作 | 13+12 |
| 15 | Bug 领域 | 3+6+7+12 |
| 16 | SPA fallback | 4 |
| 17 | 集成测试 | 8-15 |
| 18 | 最终验证 | 17 |
