package com.nss.pibblest.modules.owners.internal.utils;

import java.util.UUID;
import java.text.Normalizer;

public class IdentifierGenerator {

    public static String generateOrganizationCode(String company) {
        String cleanName = sanitize(company);
        String prefix = cleanName.length() > 4 ? cleanName.substring(0, 4) : cleanName;
        // CORRECCIÓN: Entropía ampliada a 8 caracteres
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        return prefix.toUpperCase() + "-" + suffix;
    }

    public static String generateSchemaName(String company) {
        String cleanName = sanitize(company).toLowerCase();
        // CORRECCIÓN: Entropía ampliada a 8 caracteres
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        return cleanName + "_" + uniqueId + "_schema";
    }

    private static String sanitize(String input) {
        if (input == null) return "ORG";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "") 
                         .replaceAll("[^a-zA-Z0-9]", "") 
                         .toUpperCase();
    }
}