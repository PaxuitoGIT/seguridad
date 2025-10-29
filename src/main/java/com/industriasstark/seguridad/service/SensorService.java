package com.industriasstark.seguridad.service;

import com.industriasstark.seguridad.model.SensorEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Interfaz para servicios de procesamiento de sensores
 * Define el contrato para todos los tipos de sensores.
 * Demuestra el principio de Inversión de Control (IoC) del Tema 1.
 */
public interface SensorService {

    /**
     * Procesa datos del sensor de forma asíncrona
     *
     * @param sensorId ID del sensor
     * @param data Datos a procesar
     * @return Future con el evento generado
     */
    CompletableFuture<SensorEvent> processData(String sensorId, Object data);

    /**
     * Valida si los datos requieren una alerta crítica
     */
    boolean isCritical(Object data);

    /**
     * Retorna el tipo de sensor que maneja este servicio
     */
    String getSensorType();
}
