package com.example.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GeminiMcpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GeminiMcpServerApplication.class, args);
        System.out.println("GeminiMcpServerApplication Started!!!");
    }
}
