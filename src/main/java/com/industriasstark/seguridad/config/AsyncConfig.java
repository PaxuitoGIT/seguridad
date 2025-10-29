package com.industriasstark.seguridad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono y concurrente
 * Define un ExecutorService personalizado para manejar tareas concurrentes.
 * Permite procesar múltiples sensores simultáneamente.
 * Demuestra el uso de @Bean y configuración programática del Tema 1.
 */
@Configuration
public class AsyncConfig {

    /**
     * Bean Singleton que proporciona un Executor para tareas asíncronas
     *
     * @return Executor configurado para el sistema de seguridad
     */
    @Bean(name = "sensorTaskExecutor")
    public Executor sensorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Configuración del pool de hilos
        executor.setCorePoolSize(10);  // Hilos base
        executor.setMaxPoolSize(20);   // Máximo de hilos
        executor.setQueueCapacity(500); // Cola de espera
        executor.setThreadNamePrefix("SensorThread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        System.out.println("Executor Service configurado: " +
                executor.getCorePoolSize() + " hilos base");

        return executor;
    }
}
