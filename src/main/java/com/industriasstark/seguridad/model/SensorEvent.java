package com.industriasstark.seguridad.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un Evento detectado por un sensor
 */
@Entity
@Table(name = "sensor_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SensorEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)  // CAMBIO: EAGER en lugar de LAZY
    @JoinColumn(name = "sensor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sensor sensor;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(name = "event_value")  // CAMBIO: nombre explícito
    private Double value;

    @Column(nullable = false)
    private Boolean critical = false;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;
}