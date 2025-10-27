package com.industriasstark.seguridad.controller;

import com.industriasstark.seguridad.model.Sensor;
import com.industriasstark.seguridad.model.SensorEvent;
import com.industriasstark.seguridad.model.SensorType;
import com.industriasstark.seguridad.repository.SensorRepository;
import com.industriasstark.seguridad.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador REST para gestión de sensores
 */
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SensorController {

    private final SensorRepository sensorRepository;
    private final SecurityService securityService;

    /**
     * Obtiene todos los sensores
     */
    @GetMapping
    public ResponseEntity<List<Sensor>> getAllSensors() {
        return ResponseEntity.ok(sensorRepository.findAll());
    }

    /**
     * Obtiene sensores por tipo
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Sensor>> getSensorsByType(@PathVariable SensorType type) {
        return ResponseEntity.ok(sensorRepository.findByType(type));
    }

    /**
     * Crea un nuevo sensor
     */
    @PostMapping
    public ResponseEntity<Sensor> createSensor(@RequestBody Sensor sensor) {
        Sensor saved = sensorRepository.save(sensor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Procesa datos de un sensor de forma concurrente
     */
    @PostMapping("/{sensorId}/process")
    public ResponseEntity<Map<String, Object>> processSensorData(
            @PathVariable String sensorId,
            @RequestBody Map<String, Object> requestData) {

        String typeStr = (String) requestData.get("type");
        Object data = requestData.get("data");

        SensorType type = SensorType.valueOf(typeStr.toUpperCase());

        CompletableFuture<SensorEvent> future =
                securityService.processSensorData(sensorId, type, data);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Procesamiento iniciado para sensor: " + sensorId);
        response.put("sensorId", sensorId);
        response.put("type", type.name());

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Procesa múltiples sensores simultáneamente
     */
    @PostMapping("/process-batch")
    public ResponseEntity<Map<String, Object>> processBatch(
            @RequestBody List<Map<String, Object>> sensorDataList) {

        List<CompletableFuture<SensorEvent>> futures = sensorDataList.stream()
                .map(data -> {
                    String sensorId = (String) data.get("sensorId");
                    SensorType type = SensorType.valueOf(
                            ((String) data.get("type")).toUpperCase());
                    Object sensorData = data.get("data");

                    return securityService.processSensorData(sensorId, type, sensorData);
                })
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Procesamiento concurrente iniciado");
        response.put("sensorsProcessed", futures.size());

        return ResponseEntity.accepted().body(response);
    }
}