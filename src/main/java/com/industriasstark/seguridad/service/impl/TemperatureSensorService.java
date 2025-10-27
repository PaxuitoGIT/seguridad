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
 * Servicio para procesar sensores de temperatura
 */
@Service("temperatureSensorService")
@RequiredArgsConstructor
public class TemperatureSensorService implements SensorService {

    private final SensorRepository sensorRepository;
    private final SensorEventRepository eventRepository;
    private final NotificationService notificationService;

    private static final double TEMPERATURE_THRESHOLD = 50.0; // Celsius

    @Override
    @Async("sensorTaskExecutor")
    @Transactional
    public CompletableFuture<SensorEvent> processData(String sensorId, Object data) {
        System.out.println("🌡️  [TEMPERATURE] Procesando sensor: " + sensorId +
                " en hilo: " + Thread.currentThread().getName());

        simulateProcessingDelay();

        Sensor sensor = sensorRepository.findBySensorId(sensorId)
                .orElseThrow(() -> new RuntimeException("Sensor no encontrado: " + sensorId));

        Double temperature = ((Number) data).doubleValue();

        SensorEvent event = new SensorEvent();
        event.setSensor(sensor);
        event.setEventType("TEMPERATURE_READ");
        event.setDescription(String.format("Temperatura: %.2f°C en %s",
                temperature, sensor.getLocation()));
        event.setValue(temperature);
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
        if (data instanceof Number) {
            double temp = ((Number) data).doubleValue();
            return temp > TEMPERATURE_THRESHOLD || temp < 0;
        }
        return false;
    }

    @Override
    public String getSensorType() {
        return SensorType.TEMPERATURE.name();
    }

    private void simulateProcessingDelay() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
