package com.industriasstark.seguridad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EntityScan("com.industriasstark.seguridad.model")
@EnableJpaRepositories("com.industriasstark.seguridad.repository")
public class SeguridadApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeguridadApplication.class, args);
		System.out.println("========================================");
		System.out.println("🛡️  STARK SECURITY SYSTEM ACTIVADO 🛡️");
		System.out.println("========================================");
	}
}