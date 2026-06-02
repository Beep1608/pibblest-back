package com.nss.pibblest.modules.security.internal.infrastructure.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nss.pibblest.modules.security.internal.web.filters.JwtAuthenticationFilter;
import com.nss.pibblest.modules.tenant.filters.TenantFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter; 
    private final TenantFilter tenantFilter;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, TenantFilter tenantFilter){
        this.jwtAuthFilter = jwtAuthFilter;
        this.tenantFilter = tenantFilter;
    } 

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
        .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/login", "/api/owners/register",
                    "/api/owners/verify","/api/events/retry-incomplete",
                    "/api/owners/resend-token"
                 ).permitAll()
            .requestMatchers(
                "/api/swagger-ui",      // Tu ruta personalizada en properties
                "/swagger-ui.html",     // Ruta legacy de redirección
                "/swagger-ui/**",       // Archivos estáticos (HTML, CSS, JS)
                "/v3/api-docs",         // El JSON principal de OpenAPI
                "/v3/api-docs/**"       // Archivos de configuración interna de Swagger
            ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(tenantFilter, jwtAuthFilter.getClass())
            .httpBasic(basic ->basic.disable());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
    @Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 1. ¿A quién dejamos pasar? A tu Angular
    configuration.setAllowedOrigins(List.of("http://localhost:4200")); 
    
    // 2. ¿Qué métodos permitimos? (El OPTIONS es VITAL para que no falle el preflight)
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    
    // 3. ¿Qué headers permitimos? 
    // Como vi que tienes un "tenantFilter", seguramente mandas un header de tenant, asegúrate de agregarlo aquí si es así.
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Tenant-ID")); 
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // Aplica para todas tus rutas
    
    return source;
}
}
