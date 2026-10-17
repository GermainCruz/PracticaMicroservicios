package com.example.Pasarela.controller;

import com.example.Pasarela.filter.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitResetController {

    private final RateLimiterService rateLimiterService;

    public RateLimitResetController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPost() {
        return doReset();
    }

    @GetMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetGet() {
        return doReset();
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("maxRequestsAllowed", rateLimiterService.getMaxRequests());
        response.put("windowSeconds", rateLimiterService.getWindowSeconds());
        response.put("activeTrackedClients", rateLimiterService.getTrackedClientsCount());
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> doReset() {
        rateLimiterService.reset();
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "Contadores del limitador de tasa reiniciados correctamente para nuevas pruebas.");
        return ResponseEntity.ok(response);
    }
}
