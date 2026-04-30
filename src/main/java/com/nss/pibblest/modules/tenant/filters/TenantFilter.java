package com.nss.pibblest.modules.tenant.filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.nss.pibblest.modules.security.internal.core.JwtService;
import com.nss.pibblest.modules.tenant.TenantContext;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final HandlerExceptionResolver exceptionResolver;

    public TenantFilter(JwtService jwtService, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.jwtService = jwtService;
        this.exceptionResolver = exceptionResolver;
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
        }catch(ExpiredJwtException ex){
            exceptionResolver.resolveException(request, response, null, ex);
        }finally {

            TenantContext.clear();
        }

    }



}
