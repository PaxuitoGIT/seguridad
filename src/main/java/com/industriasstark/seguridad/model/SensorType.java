package com.industriasstark.seguridad.model;

/**
 * Enumeración de los tipos de sensores disponibles
 */
public enum SensorType {
    MOVEMENT("Sensor de Movimiento"),
    TEMPERATURE("Sensor de Temperatura"),
    ACCESS("Sensor de Control de Acceso");

    private final String description;

    SensorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}