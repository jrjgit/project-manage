package com.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.auth.LoginRequest;
import com.management.auth.RegisterRequest;
import com.management.task.dto.CreateTaskRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void registerAndLogin_shouldReturnTokenAndUser() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("测试PM");
        reg.setAccount("testpm");
        reg.setPassword("test123");
        reg.setRole("pm");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user_id").isNumber());

        LoginRequest login = new LoginRequest();
        login.setAccount("testpm");
        login.setPassword("test123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.name").value("测试PM"));
    }

    @Test
    void registerDuplicate_shouldReturn409() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("dupuser");
        reg.setAccount("dupuser");
        reg.setPassword("test123");
        reg.setRole("pm");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").value("账号已存在"));
    }

    @Test
    void loginWithWrongPassword_shouldReturn401() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("wrongpw");
        reg.setAccount("wrongpw");
        reg.setPassword("test123");
        reg.setRole("pm");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)));

        LoginRequest login = new LoginRequest();
        login.setAccount("wrongpw");
        login.setPassword("badpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").value("账号或密码错误"));
    }

    @Test
    void unauthenticatedAccess_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }
}
