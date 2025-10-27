package com.industriasstark.seguridad.service;

import com.industriasstark.seguridad.model.Sensor;
import com.industriasstark.seguridad.model.SensorEvent;
import com.industriasstark.seguridad.model.SensorType;
import com.industriasstark.seguridad.repository.SensorEventRepository;
import com.industriasstark.seguridad.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio coordinador del sistema de seguridad
 *
 * Utiliza @Qualifier para resolver conflictos entre múltiples implementaciones
 * de SensorService, demostrando resolución de conflictos del Tema 1.
 */
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final SensorRepository sensorRepository;
    private final SensorEventRepository eventRepository;

    @Qualifier("movementSensorService")
    private final SensorService movementSensorService;

    @Qualifier("temperatureSensorService")
    private final SensorService temperatureSensorService;

    @Qualifier("accessSensorService")
    private final SensorService accessSensorService;

    /**
     * Procesa datos de un sensor según su tipo
     */
    public CompletableFuture<SensorEvent> processSensorData(String sensorId,
                                                            SensorType type,
                                                            Object data) {
        SensorService service = switch (type) {
            case MOVEMENT -> movementSensorService;
            case TEMPERATURE -> temperatureSensorService;
            case ACCESS -> accessSensorService;
        };

        return service.processData(sensorId, data);
    }

    /**
     * Obtiene todos los eventos críticos sin procesar
     */
    public List<SensorEvent> getCriticalEvents() {
        return eventRepository.findByCriticalTrue();
    }

    /**
     * Obtiene estadísticas del sistema
     */
    public SystemStats getSystemStats() {
        long totalSensors = sensorRepository.count();
        long activeSensors = sensorRepository.findByActiveTrue().size();
        long totalEvents = eventRepository.count();
        long criticalEvents = eventRepository.countUnprocessedCriticalEvents();

        return new SystemStats(totalSensors, activeSensors, totalEvents, criticalEvents);
    }

    /**
     * Record para estadísticas del sistema (Java 17+)
     */
    public record SystemStats(
            long totalSensors,
            long activeSensors,
            long totalEvents,
            long unprocessedCriticalEvents
    ) {}
}
