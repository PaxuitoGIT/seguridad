package com.industriasstark.seguridad.service;

import com.industriasstark.seguridad.model.SensorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones en tiempo real
 *
 * @Slf4j: Lombok genera automáticamente un logger
 */
@Service
@Slf4j
public class NotificationService {

    /**
     * Envía alerta de forma asíncrona
     * En producción, esto enviaría WebSocket, email, SMS, etc.
     */
    @Async("sensorTaskExecutor")
    public void sendAlert(SensorEvent event) {
        log.warn("🚨 ALERTA CRÍTICA: {} - {}",
                event.getEventType(),
                event.getDescription());

        // Simular envío de notificación
        System.out.println("📧 Email enviado a security@starkindustries.com");
        System.out.println("📱 Push notification enviada a dispositivos móviles");
        System.out.println("🔔 Alerta sonora activada en la ubicación: " +
                event.getSensor().getLocation());
    }

    public void logInfo(String message) {
        log.info("ℹ️  {}", message);
    }
}
