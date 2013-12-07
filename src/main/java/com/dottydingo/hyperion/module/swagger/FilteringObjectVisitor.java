package com.dottydingo.hyperion.module.swagger;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.ObjectVisitor;
import com.fasterxml.jackson.module.jsonSchema.types.AnySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

/**
 */
public class FilteringObjectVisitor extends ObjectVisitor
{
    public FilteringObjectVisitor(SerializerProvider provider, ObjectSchema schema)
    {
        super(provider, schema);
    }

    @Override
    protected JsonSchema propertySchema(BeanProperty writer) throws JsonMappingException
    {
        if(writer.getAnnotation(com.dottydingo.hyperion.module.swagger.AnySchema.class) != null)
            return new AnySchema();

        return super.propertySchema(writer);
    }
}
