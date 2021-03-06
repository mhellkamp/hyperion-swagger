package com.dottydingo.hyperion.module.swagger.jackson;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.ArrayVisitor;
import com.fasterxml.jackson.module.jsonSchema.factories.ObjectVisitor;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.factories.WrapperFactory;import java.lang.Override;
import java.util.*;

/**
 */
public class TypeIdSchemaFactoryWrapper extends SchemaFactoryWrapper
{

    private WrapperFactory wrapperFactory = new WrapperFactory() {
        @Override
        public SchemaFactoryWrapper getWrapper(SerializerProvider p) {
            SchemaFactoryWrapper wrapper = new TypeIdSchemaFactoryWrapper();
            wrapper.setProvider(p);
            return wrapper;
        };
    };

    public TypeIdSchemaFactoryWrapper()
    {
        super();
        this.visitorFactory = new FilteringVisitorFactory();
    }

    @Override
    public JsonArrayFormatVisitor expectArrayFormat(JavaType convertedType)
    {
        ArrayVisitor visitor = ((ArrayVisitor)super.expectArrayFormat(convertedType));
        visitor.setWrapperFactory(wrapperFactory);
        return visitor;
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType convertedType)
    {
        ObjectVisitor visitor = ((ObjectVisitor)super.expectObjectFormat(convertedType));
        visitor.getSchema().setProperties(new LinkedHashMap<String, JsonSchema>());
        visitor.setWrapperFactory(wrapperFactory);
        visitor.getSchema().setId(convertedType.getRawClass().getSimpleName());
        return visitor;
    }
}
