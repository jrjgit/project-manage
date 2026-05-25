package com.management.common.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpaFallbackController {

    @GetMapping("/")
    @ResponseBody
    public String index() {
        if (new ClassPathResource("static/index.html").exists()) {
            return "forward:/index.html";
        }
        return "Management API is running. Frontend not built yet.";
    }
}
