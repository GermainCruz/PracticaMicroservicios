package com.example.Service2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/service2/equipos")
public class Service2EquiposController {

    private final RestTemplate restTemplate;
    private static final String SERVICE1_URL = "http://Service1/api/equipos";

    public Service2EquiposController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<?> listarEquiposDesdeService1() {
        try {
            Object response = restTemplate.getForObject(SERVICE1_URL, Object.class);
            return ResponseEntity.ok(response);
        } catch (ResourceAccessException ex) {
            return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service1 no se encuentra disponible actualmente.");
        } catch (HttpServerErrorException ex) {
            return buildErrorResponse(HttpStatus.BAD_GATEWAY, "Error reportado por Service1: " + ex.getStatusCode());
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error al comunicarse con Service1: " + ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerEquipoPorIdDesdeService1(@PathVariable Long id) {
        try {
            Object response = restTemplate.getForObject(SERVICE1_URL + "/" + id, Object.class);
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException.NotFound ex) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Equipo no encontrado con ID: " + id);
        } catch (HttpClientErrorException ex) {
            return buildErrorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getStatusText());
        } catch (ResourceAccessException ex) {
            return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service1 no se encuentra disponible actualmente.");
        } catch (Exception ex) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error al comunicarse con Service1: " + ex.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        error.put("source", "Service2");
        return ResponseEntity.status(status).body(error);
    }
}
