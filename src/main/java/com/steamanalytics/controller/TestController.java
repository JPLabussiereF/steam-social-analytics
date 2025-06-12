package com.steamanalytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class TestController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/health")
    public Map<String, Object> health() {
        boolean dbConnected = false;
        boolean redisConnected = false;

        // Testar conexão com PostgreSQL
        try (Connection connection = dataSource.getConnection()) {
            dbConnected = connection.isValid(5);
        } catch (Exception e) {
            // Conexão falhou
        }

        // Testar conexão com Redis
        try {
            redisTemplate.opsForValue().set("test:health", "ok");
            String value = (String) redisTemplate.opsForValue().get("test:health");
            redisConnected = "ok".equals(value);
        } catch (Exception e) {
            // Conexão com Redis falhou
        }

        return Map.of(
                "status", (dbConnected && redisConnected) ? "UP" : "PARTIAL",
                "application", applicationName,
                "database", dbConnected ? "UP" : "DOWN",
                "redis", redisConnected ? "UP" : "DOWN",
                "message", "Steam Social Analytics is running!"
        );
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "application", applicationName,
                "version", "1.0.0-SNAPSHOT",
                "environment", "development",
                "description", "Steam Social Analytics - Sistema de análise social para Steam"
        );
    }

    @GetMapping("/database-test")
    public Map<String, Object> testDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            String databaseName = connection.getCatalog();
            String url = connection.getMetaData().getURL();

            return Map.of(
                    "status", "SUCCESS",
                    "database", databaseName,
                    "url", url,
                    "valid", connection.isValid(5)
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }

    @GetMapping("/redis-test")
    public Map<String, Object> testRedis() {
        try {
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "Hello Redis!";

            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            return Map.of(
                    "status", "SUCCESS",
                    "sent", testValue,
                    "received", retrievedValue,
                    "match", testValue.equals(retrievedValue)
            );
        } catch (Exception e) {
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }
}