package com.example.Pasarela.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewaySecurityAndRateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Value("${gateway.api-key:admin123}")
    private String configuredApiKey;

    public GatewaySecurityAndRateLimitFilter(RateLimiterService rateLimiterService, ObjectMapper objectMapper) {
        this.rateLimiterService = rateLimiterService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // Excluir endpoints de administración del limitador de tasa
        if (path.startsWith("/api/rate-limit")) {
            filterChain.doFilter(request, response);
            return;
        }

        // =========================================================================
        // RESTRICCIÓN 2: Proteger operaciones de escritura (POST, PUT, PATCH, DELETE)
        // =========================================================================
        if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method)) {
            String apiKeyHeader = request.getHeader("X-API-KEY");

            if (apiKeyHeader == null || !apiKeyHeader.trim().equals(configuredApiKey.trim())) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED,
                        "Acceso no autorizado: Se requiere una cabecera 'X-API-KEY' válida para operaciones de escritura (" + method + ").",
                        path);
                return;
            }
        }

        // =========================================================================
        // RESTRICCIÓN 1: Limitar solicitudes GET (Máximo 2 por cliente y ruta en 60s)
        // =========================================================================
        if ("GET".equals(method)) {
            String clientIp = request.getRemoteAddr();
            String clientKey = clientIp + ":" + path;

            if (!rateLimiterService.isAllowed(clientKey)) {
                long remaining = rateLimiterService.getSecondsRemaining(clientKey);
                Map<String, Object> extra = new HashMap<>();
                extra.put("reintenteEnSegundos", remaining);

                sendErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS,
                        "Límite de solicitudes excedido. Se permite un máximo de "
                                + rateLimiterService.getMaxRequests()
                                + " solicitudes GET a esta ruta en un intervalo de "
                                + rateLimiterService.getWindowSeconds() + " segundos.",
                        path, extra);
                return;
            }
        }

        // Continuar el flujo normal hacia el enrutador de Spring Cloud Gateway
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        sendErrorResponse(response, status, message, path, null);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message, String path, Map<String, Object> extra) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);

        if (extra != null) {
            body.putAll(extra);
        }

        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }
}
