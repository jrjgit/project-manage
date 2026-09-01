package com.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.auth.LoginRequest;
import com.management.auth.RegisterRequest;
import com.management.requirement.dto.CreateRequirementRequest;
import com.management.task.dto.CreateTaskRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 需求发布校验：其下存在未完成任务时禁止发布
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RequirementReleaseGuardTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    private long projectId;

    @BeforeAll
    void loginAsPm() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("发布校验PM");
        reg.setAccount("release_guard_pm");
        reg.setPassword("test123");
        reg.setRole("pm");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setAccount("release_guard_pm");
        login.setPassword("test123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        token = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();

        projectId = objectMapper.readTree(mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"发布校验项目\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    private long createRequirement(String description) throws Exception {
        CreateRequirementRequest req = new CreateRequirementRequest();
        req.setDescription(description);
        MvcResult result = mockMvc.perform(post("/api/requirements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        if (result.getResponse().getStatus() != 201) {
            throw new IllegalStateException("创建需求失败 status=" + result.getResponse().getStatus()
                    + " body=" + result.getResponse().getContentAsString());
        }
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    @Test
    void release_shouldBeRejected_whenOpenTaskExists() throws Exception {
        long reqId = createRequirement("发布校验-存在未完成任务");

        CreateTaskRequest task = new CreateTaskRequest();
        task.setTitle("未完成任务");
        task.setDescription("发布前应拦截");
        task.setProjectId(projectId);
        task.setRequirementId(reqId);
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated());

        // 状态流转入口
        mockMvc.perform(patch("/api/requirements/" + reqId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"released\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("未完成的任务")));

        // 编辑入口同样拦截（避免绕过）
        mockMvc.perform(put("/api/requirements/" + reqId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"released\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("未完成的任务")));
    }

    @Test
    void release_shouldSucceed_whenNoTasks() throws Exception {
        long reqId = createRequirement("发布校验-无任务可直接发布");

        mockMvc.perform(patch("/api/requirements/" + reqId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"released\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("status changed"));
    }
}
