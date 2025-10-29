package com.industriasstark.seguridad.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint de login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());

            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("username", user.getUsername());
                response.put("fullName", getFullName(user.getUsername()));
                response.put("role", user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            // Usuario no encontrado o error
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Credenciales inválidas"));
    }

    // Metodo para obtener el nombre completo basado en el nombre de usuario
    private String getFullName(String username) {
        switch (username) {
            case "tony.stark": return "Tony Stark";
            case "pepper.potts": return "Pepper Potts";
            case "happy.hogan": return "Happy Hogan";
            default: return username;
        }
    }

    // Clase interna para la solicitud de login
    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
