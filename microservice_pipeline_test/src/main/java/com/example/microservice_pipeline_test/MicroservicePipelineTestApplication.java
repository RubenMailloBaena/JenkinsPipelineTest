package com.example.microservice_pipeline_test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@RestController
public class MicroservicePipelineTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicePipelineTestApplication.class, args);
	}

	@GetMapping("/ping")
	public String getPint(){
		return "Microservice_Pipeline_TEST";
	}
}
