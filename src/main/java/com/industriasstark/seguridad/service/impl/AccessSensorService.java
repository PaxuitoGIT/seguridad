package com.industriasstark.seguridad.service.impl;

import com.industriasstark.seguridad.model.Sensor;
import com.industriasstark.seguridad.model.SensorEvent;
import com.industriasstark.seguridad.model.SensorType;
import com.industriasstark.seguridad.repository.SensorEventRepository;
import com.industriasstark.seguridad.repository.SensorRepository;
import com.industriasstark.seguridad.service.NotificationService;
import com.industriasstark.seguridad.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para procesar sensores de control de acceso
 */
@Service("accessSensorService")
@RequiredArgsConstructor
public class AccessSensorService implements SensorService {

    private final SensorRepository sensorRepository;
    private final SensorEventRepository eventRepository;
    private final NotificationService notificationService;

    @Override
    @Async("sensorTaskExecutor")
    @Transactional
    public CompletableFuture<SensorEvent> processData(String sensorId, Object data) {
        System.out.println("🚪 [ACCESS] Procesando sensor: " + sensorId +
                " en hilo: " + Thread.currentThread().getName());

        simulateProcessingDelay();

        Sensor sensor = sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new RuntimeException("Sensor no encontrado: " + sensorId));

        @SuppressWarnings("unchecked")
        Map<String, Object> accessData = (Map<String, Object>) data;
        String userId = (String) accessData.get("userId");
        Boolean authorized = (Boolean) accessData.get("authorized");

        SensorEvent event = new SensorEvent();
        event.setSensor(sensor);
        event.setEventType("ACCESS_ATTEMPT");
        event.setDescription(String.format("%s - Usuario: %s en %s",
                authorized ? "✓ Acceso autorizado" : "⛔ Acceso denegado",
                userId, sensor.getLocation()));
        event.setValue(authorized ? 1.0 : 0.0);
        event.setCritical(isCritical(data));
        event.setDetectedAt(LocalDateTime.now());

        SensorEvent savedEvent = eventRepository.save(event);

        sensor.setLastCheck(LocalDateTime.now());
        sensorRepository.save(sensor);

        if (savedEvent.getCritical()) {
            notificationService.sendAlert(savedEvent);
        }

        return CompletableFuture.completedFuture(savedEvent);
    }

    @Override
    public boolean isCritical(Object data) {
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> accessData = (Map<String, Object>) data;
            Boolean authorized = (Boolean) accessData.get("authorized");
            return authorized != null && !authorized;
        }
        return false;
    }

    @Override
    public String getSensorType() {
        return SensorType.ACCESS.name();
    }

    private void simulateProcessingDelay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
