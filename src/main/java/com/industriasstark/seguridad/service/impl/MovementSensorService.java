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
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para procesar sensores de movimiento
 *
 * @Service: Define este bean como un servicio
 * @RequiredArgsConstructor: Inyección de dependencias por constructor (Lombok)
 * @Async: Permite procesamiento asíncrono
 * @Transactional: Gestión automática de transacciones
 *
 * Demuestra múltiples conceptos del Tema 1:
 * - Spring Beans y anotaciones
 * - Inyección de dependencias
 * - Procesamiento asíncrono
 * - Gestión de transacciones
 */
@Service("movementSensorService")
@RequiredArgsConstructor
public class MovementSensorService implements SensorService {

    private final SensorRepository sensorRepository;
    private final SensorEventRepository eventRepository;
    private final NotificationService notificationService;

    @Override
    @Async("sensorTaskExecutor")
    @Transactional
    public CompletableFuture<SensorEvent> processData(String sensorId, Object data) {
        System.out.println("🏃 [MOVEMENT] Procesando sensor: " + sensorId +
                " en hilo: " + Thread.currentThread().getName());

        // Simular procesamiento
        simulateProcessingDelay();

        Sensor sensor = sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new RuntimeException("Sensor no encontrado: " + sensorId));

        Boolean movementDetected = (Boolean) data;

        SensorEvent event = new SensorEvent();
        event.setSensor(sensor);
        event.setEventType("MOVEMENT_DETECTED");
        event.setDescription(movementDetected ?
                "⚠️ Movimiento detectado en " + sensor.getLocation() :
                "✓ Sin movimiento");
        event.setValue(movementDetected ? 1.0 : 0.0);
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
        return data instanceof Boolean && (Boolean) data;
    }

    @Override
    public String getSensorType() {
        return SensorType.MOVEMENT.name();
    }

    private void simulateProcessingDelay() {
        try {
            Thread.sleep(1000); // Simula procesamiento de 1 segundo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
