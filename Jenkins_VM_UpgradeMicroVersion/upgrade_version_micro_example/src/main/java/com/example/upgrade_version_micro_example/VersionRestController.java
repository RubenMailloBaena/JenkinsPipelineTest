package com.example.upgrade_version_micro_example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class VersionRestController{

    @Value("${app.version}")
    private String version;

    @GetMapping("/version")
    public String getMethodName() {
        return version;
    }
    
}