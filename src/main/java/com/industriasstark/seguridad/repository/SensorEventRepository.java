package com.industriasstark.seguridad.repository;

import com.industriasstark.seguridad.model.Sensor;
import com.industriasstark.seguridad.model.SensorEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestionar Eventos de Sensores
 */
@Repository
public interface SensorEventRepository extends JpaRepository<SensorEvent, Long> {

    List<SensorEvent> findBySensor(Sensor sensor);

    List<SensorEvent> findByCriticalTrue();

    List<SensorEvent> findByProcessedFalse();

    @Query("SELECT e FROM SensorEvent e WHERE e.detectedAt BETWEEN :start AND :end")
    List<SensorEvent> findEventsBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(e) FROM SensorEvent e WHERE e.critical = true AND e.processed = false")
    Long countUnprocessedCriticalEvents();
}
