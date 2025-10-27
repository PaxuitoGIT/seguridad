package com.industriasstark.seguridad.config;

import com.industriasstark.seguridad.model.Sensor;
import com.industriasstark.seguridad.model.SensorType;
import com.industriasstark.seguridad.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Inicializa datos de prueba al arrancar la aplicación
 *
 * CommandLineRunner se ejecuta después de que Spring Boot arranque
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SensorRepository sensorRepository;

    @Override
    public void run(String... args) {
        System.out.println("📊 Inicializando datos de prueba...");

        List<Sensor> sensors = Arrays.asList(
                createSensor("MOV-001", SensorType.MOVEMENT, "Entrada Principal"),
                createSensor("MOV-002", SensorType.MOVEMENT, "Laboratorio Stark"),
                createSensor("MOV-003", SensorType.MOVEMENT, "Sala de Servidores"),

                createSensor("TEMP-001", SensorType.TEMPERATURE, "Sala de Servidores"),
                createSensor("TEMP-002", SensorType.TEMPERATURE, "Armería"),
                createSensor("TEMP-003", SensorType.TEMPERATURE, "Hangar de Trajes"),

                createSensor("ACC-001", SensorType.ACCESS, "Puerta Principal"),
                createSensor("ACC-002", SensorType.ACCESS, "Ascensor Privado"),
                createSensor("ACC-003", SensorType.ACCESS, "Bóveda de Prototipos")
        );

        sensorRepository.saveAll(sensors);

        System.out.println("✅ " + sensors.size() + " sensores inicializados");
        sensors.forEach(s -> System.out.println("   - " + s.getSensorId() +
                " [" + s.getType() + "] en " +
                s.getLocation()));
    }

    private Sensor createSensor(String sensorId, SensorType type, String location) {
        Sensor sensor = new Sensor();
        sensor.setSensorId(sensorId);
        sensor.setType(type);
        sensor.setLocation(location);
        sensor.setActive(true);
        sensor.setCreatedAt(LocalDateTime.now());
        return sensor;
    }
}
