package com.nss.pibblest.modules.owners.internal.utils;

import java.util.UUID;

import java.text.Normalizer;
import java.util.Locale;


public class IdentifierGenerator {

    /**
     * Genera un código tipo JIRA (ej: GOOGLE-42X)
     */
    public static String generateOrganizationCode(String company) {
        String cleanName = sanitize(company);
        // Tomamos las primeras 3 o 4 letras en mayúsculas
        String prefix = cleanName.length() > 4 ? cleanName.substring(0, 4) : cleanName;
        // Añadimos un sufijo alfanumérico corto para unicidad
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        
        return prefix.toUpperCase() + "-" + suffix;
    }

    /**
     * Genera un nombre de esquema seguro para DB (ej: google_6f2a_schema)
     */
    public static String generateSchemaName(String company) {
        String cleanName = sanitize(company).toLowerCase();
        // El nombre del esquema suele ser mejor en snake_case
        String uniqueId = UUID.randomUUID().toString().substring(0, 6);
        
        return cleanName + "_" + uniqueId + "_schema";
    }

    // Método auxiliar para limpiar el texto (quita acentos y caracteres raros)
    private static String sanitize(String input) {
        if (input == null) return "ORG";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "") // Quita acentos
                         .replaceAll("[^a-zA-Z0-9]", "") // Quita espacios y símbolos
                         .toUpperCase();
    }
}