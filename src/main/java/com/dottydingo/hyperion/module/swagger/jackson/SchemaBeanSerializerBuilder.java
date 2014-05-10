package com.dottydingo.hyperion.module.swagger.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;

/**
 */
public class SchemaBeanSerializerBuilder extends BeanSerializerBuilder
{
    private final static BeanPropertyWriter[] NO_PROPERTIES = new BeanPropertyWriter[0];

    public SchemaBeanSerializerBuilder(BeanDescription beanDesc)
    {
        super(beanDesc);
    }

    public SchemaBeanSerializerBuilder(BeanSerializerBuilder src)
    {
        super(src);
    }

    public JsonSerializer<?> build()
    {
        BeanPropertyWriter[] properties;
        // No properties or any getter? No real serializer; caller gets to handle
        if (_properties == null || _properties.isEmpty()) {
            if (_anyGetter == null) {
                return null;
            }
            properties = NO_PROPERTIES;
        } else {
            properties = _properties.toArray(new BeanPropertyWriter[_properties.size()]);

        }
        return new SchemaBeanSerializer(_beanDesc.getType(), this,
                properties, _filteredProperties);
    }
}
