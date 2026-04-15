package com.nss.pibblest.modules.owners.internal.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/owners").permitAll()
                .requestMatchers("/api/owners").permitAll() // Tu endpoint público
            // --- BLOQUE DE SWAGGER ---
            .requestMatchers(
                "/api/swagger-ui",      // Tu ruta personalizada en properties
                "/swagger-ui.html",     // Ruta legacy de redirección
                "/swagger-ui/**",       // Archivos estáticos (HTML, CSS, JS)
                "/v3/api-docs",         // El JSON principal de OpenAPI
                "/v3/api-docs/**"       // Archivos de configuración interna de Swagger
            ).permitAll()
                .anyRequest().permitAll()
            )
            .httpBasic(basic ->basic.disable());

        return http.build();
    }
    
}
