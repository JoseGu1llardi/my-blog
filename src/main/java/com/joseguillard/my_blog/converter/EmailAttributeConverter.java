package com.joseguillard.my_blog.converter;

import com.joseguillard.my_blog.model.vo.Email;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailAttributeConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email email) {
        return email != null ? email.address() : null;
    }

    @Override
    public Email convertToEntityAttribute(String dbData) {
        return dbData != null ? Email.of(dbData) : null;
    }
}
