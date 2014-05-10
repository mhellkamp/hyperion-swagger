package com.dottydingo.hyperion.module.swagger.jackson;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.FormatVisitorFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import java.util.Map;

/**
 */
public class FilteringVisitorFactory extends FormatVisitorFactory
{

    public FilteringVisitorFactory()
    {
    }

    @Override
    public JsonObjectFormatVisitor objectFormatVisitor(SerializerProvider provider, ObjectSchema objectSchema)
    {
        return new FilteringObjectVisitor(provider,objectSchema);
    }
}
