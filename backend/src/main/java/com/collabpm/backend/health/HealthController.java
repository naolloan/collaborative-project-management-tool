package com.collabpm.backend.health;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "service", "collaborative-project-management-backend",
            "timestamp", Instant.now().toString());
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of(
            "name", "Collaborative Project Management Tool API",
            "version", "0.0.1",
            "authentication", "Keycloak");
    }
}
