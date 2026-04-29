package com.nss.pibblest.modules.tenant.filters;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nss.pibblest.modules.security.internal.core.JwtService;
import com.nss.pibblest.modules.tenant.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public TenantFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {


        String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                System.out.println("Token: " + token);
                String schema = jwtService.extractOwner(token);
          
                System.out.println("Schema :"+schema);
                TenantContext.setCurrentTenant(schema);
            }
            chain.doFilter(request, response);
        } finally {

            TenantContext.clear();
        }

    }



}
