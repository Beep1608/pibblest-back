package com.nss.pibblest.modules.security.internal.web.filters;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nss.pibblest.modules.security.internal.core.JwtService;
import com.nss.pibblest.modules.tenant.SessionTrackerService;
import com.nss.pibblest.modules.tenant.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

@Component
//TODO: Creo que este filtro se aplicá dos veces
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final SessionTrackerService sessionTrackerService;

    public JwtAuthenticationFilter(JwtService jwtService, SessionTrackerService sessionTrackerService){
        this.jwtService = jwtService;
        this.sessionTrackerService = sessionTrackerService;
    }

    @Override
    protected void doFilterInternal(
        @NotNull HttpServletRequest request, 
        @NotNull HttpServletResponse response, 
        @NotNull FilterChain filterChain
        ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        final String userEmail;


        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Caso A: Petición REST clásica desde Angular
            jwt = authHeader.substring(7);
        } else if (request.getRequestURI().contains("/api/stores/stream/storePreview")) {
            // Caso B: Petición SSE nativa (el token viene en ?token=...)
            jwt = request.getParameter("token");
        }

        if (jwt == null || jwt.trim().isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        

        try {

            userEmail = jwtService.extractUsername(jwt);
            String owner = jwtService.extractOwner(jwt);
            boolean isValid = jwtService.isTokenValid(jwt, userEmail);
            String userId = jwtService.extractUserId(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
                boolean isTokenActive = sessionTrackerService.isSessionValid(userId, jwtService.extractTokenId(jwt));
  

                if(jwtService.isTokenValid(jwt, userEmail) && isTokenActive){

                    if (owner != null ){
                        TenantContext.setCurrentTenant(owner);
                    }

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userId, 
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("miau"))
                    );

                    System.out.println("Siii");
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                }
            }
            System.out.println("No fue validado");
            
        } catch (Exception e) {
            System.err.println("Error procesando JWT: " + e.getMessage());
        }finally{

            try{
                filterChain.doFilter(request, response);
            }finally{
                TenantContext.clear();
            }
        }
      
    }
}
