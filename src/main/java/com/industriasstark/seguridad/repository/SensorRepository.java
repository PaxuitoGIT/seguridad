package com.industriasstark.seguridad.repository;

import com.industriasstark.seguridad.model.Sensor;
import com.industriasstark.seguridad.model.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar Sensores
 *
 * Spring Data JPA genera automáticamente la implementación.
 * Demuestra la simplificación del acceso a datos del Tema 1.
 */
@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {

    Optional<Sensor> findBySensorId(String sensorId);

    List<Sensor> findByType(SensorType type);

    List<Sensor> findByActiveTrue();

    List<Sensor> findByLocation(String location);
}
