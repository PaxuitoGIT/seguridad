package com.industriasstark.seguridad.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationFilter customAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración de usuarios en memoria
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {

        UserDetails admin = User.builder()
                .username("tony.stark")
                .password(passwordEncoder.encode("jarvis123"))
                .roles("ADMIN", "SECURITY_OFFICER")
                .build();

        UserDetails securityOfficer = User.builder()
                .username("pepper.potts")
                .password(passwordEncoder.encode("stark123"))
                .roles("SECURITY_OFFICER")
                .build();

        UserDetails viewer = User.builder()
                .username("happy.hogan")
                .password(passwordEncoder.encode("driver123"))
                .roles("VIEWER")
                .build();

        System.out.println("✅ Usuarios configurados:");
        System.out.println("   - Admin: tony.stark / jarvis123");
        System.out.println("   - Officer: pepper.potts / stark123");
        System.out.println("   - Viewer: happy.hogan / driver123");

        return new InMemoryUserDetailsManager(admin, securityOfficer, viewer);
    }

    /**
     * Configuración CORS para permitir solicitudes desde cualquier origen
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configuración de seguridad HTTP
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Agregar el filtro personalizado ANTES del filtro de autenticación
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/", "/index.html", "/*.css", "/*.js", "/favicon.ico").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Rutas protegidas por roles
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/sensors/**").hasAnyRole("ADMIN", "SECURITY_OFFICER")
                        .requestMatchers("/api/events/**").hasAnyRole("ADMIN", "SECURITY_OFFICER", "VIEWER")

                        // Todas las demás requieren autenticación
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}