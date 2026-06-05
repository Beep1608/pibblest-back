package com.nss.pibblest.modules.security.internal.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authZ")
public class PermissionChecker {

    public boolean check(Long storeId, String moduleCode, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // El formato que inyectamos en el JWT
        String requiredAuthority = "STORE_" + storeId + "_" + moduleCode + "_" + action.toUpperCase();

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals(requiredAuthority));
    }
}
