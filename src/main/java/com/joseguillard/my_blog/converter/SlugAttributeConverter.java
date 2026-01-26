package com.joseguillard.my_blog.converter;

import com.joseguillard.my_blog.model.vo.Slug;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SlugAttributeConverter implements AttributeConverter<Slug, String> {

    @Override
    public String convertToDatabaseColumn(Slug slug) {
        return slug != null ?  slug.getValue() : null;
    }

    @Override
    public Slug convertToEntityAttribute(String dbData) {
        return dbData != null ? Slug.of(dbData) : null;
    }
}
