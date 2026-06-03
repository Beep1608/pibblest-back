package com.nss.pibblest.modules.tenant;

import java.util.Map;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
public class CurrentTenantResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null || currentTenant.trim().isEmpty()) {
            return "identity"; 
        }
        System.out.println("CurrentTenantResolver: "+currentTenant);
        return currentTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

}
