package com.dottydingo.hyperion.module.swagger.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.ObjectVisitor;
import com.fasterxml.jackson.module.jsonSchema.types.AnySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import java.util.Map;

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
