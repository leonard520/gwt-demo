package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the GWT Demo backend application.
 * Replaces the legacy GWT + Spring 2.5.6 WAR deployment
 * with a modern Spring Boot 3.x executable JAR.
 */
@SpringBootApplication
public class GwtDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GwtDemoApplication.class, args);
    }
}
