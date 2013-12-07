package com.dottydingo.hyperion.module.swagger;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.module.jsonSchema.factories.FormatVisitorFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

/**
 */
public class FilteringVisitorFactory extends FormatVisitorFactory
{
    @Override
    public JsonObjectFormatVisitor objectFormatVisitor(SerializerProvider provider, ObjectSchema objectSchema)
    {
        return new FilteringObjectVisitor(provider,objectSchema);
    }
}
