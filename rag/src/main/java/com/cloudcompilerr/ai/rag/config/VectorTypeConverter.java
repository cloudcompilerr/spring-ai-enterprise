package com.cloudcompilerr.ai.rag.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;

/**
 * JPA converter for PostgreSQL vector type.
 * Converts between Java float[] and PostgreSQL vector.
 */
@Converter
public class VectorTypeConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }
        
        // Convert float array to PostgreSQL vector format: [1.0,2.0,3.0]
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(attribute[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        // Parse PostgreSQL vector format: [1.0,2.0,3.0] to float array
        String cleaned = dbData.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        if (cleaned.isEmpty()) {
            return new float[0];
        }
        
        String[] parts = cleaned.split(",");
        float[] result = new float[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        
        return result;
    }
}