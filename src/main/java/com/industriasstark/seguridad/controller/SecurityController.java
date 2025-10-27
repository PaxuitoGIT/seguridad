package com.industriasstark.seguridad.controller;

import com.industriasstark.seguridad.model.SensorEvent;
import com.industriasstark.seguridad.repository.SensorEventRepository;
import com.industriasstark.seguridad.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para eventos de seguridad y estadísticas
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class SecurityController {

    private final SensorEventRepository eventRepository;
    private final SecurityService securityService;

    /**
     * Obtiene todos los eventos
     */
    @GetMapping
    public ResponseEntity<List<SensorEvent>> getAllEvents() {
        return ResponseEntity.ok(eventRepository.findAll());
    }

    /**
     * Obtiene eventos críticos
     */
    @GetMapping("/critical")
    public ResponseEntity<List<SensorEvent>> getCriticalEvents() {
        return ResponseEntity.ok(securityService.getCriticalEvents());
    }

    /**
     * Obtiene estadísticas del sistema
     */
    @GetMapping("/stats")
    public ResponseEntity<SecurityService.SystemStats> getStats() {
        return ResponseEntity.ok(securityService.getSystemStats());
    }

    /**
     * Marca un evento como procesado
     */
    @PatchMapping("/{eventId}/process")
    public ResponseEntity<SensorEvent> markAsProcessed(@PathVariable Long eventId) {
        SensorEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        event.setProcessed(true);
        SensorEvent updated = eventRepository.save(event);

        return ResponseEntity.ok(updated);
    }
}
