package br.com.aulas.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StatusUsuarioConverter implements AttributeConverter<Boolean, String> {
    @Override
    public String convertToDatabaseColumn(Boolean ativo) {
        return Boolean.TRUE.equals(ativo) ? "Ativo" : "Inativo";
    }

    @Override
    public Boolean convertToEntityAttribute(String status) {
        return "Ativo".equalsIgnoreCase(status);
    }
}