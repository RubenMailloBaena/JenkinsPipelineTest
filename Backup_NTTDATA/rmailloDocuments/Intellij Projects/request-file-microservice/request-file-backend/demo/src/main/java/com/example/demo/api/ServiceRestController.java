package com.example.demo.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ServiceRestController {

    @GetMapping("/ping")
    public String getPing() {
        return new String("Hellow World!");
    }
    
}
