package com.dottydingo.hyperion.module.swagger.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class SchemaBeanSerializer extends BeanSerializer
{
    private static final ThreadLocal<Set<JavaType>>  visitedThreadLocal = new ThreadLocal<Set<JavaType>>(){
        @Override
        protected Set<JavaType> initialValue()
        {
            return new HashSet<JavaType>();
        }
    };

    protected SchemaBeanSerializer(BeanSerializerBase src)
    {
        super(src);
    }

    public SchemaBeanSerializer(JavaType type, BeanSerializerBuilder builder, BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties)
    {
        super(type, builder, properties, filteredProperties);
    }

    protected SchemaBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter)
    {
        super(src, objectIdWriter);
    }

    protected SchemaBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId)
    {
        super(src, objectIdWriter, filterId);
    }

    protected SchemaBeanSerializer(BeanSerializerBase src, String[] toIgnore)
    {
        super(src, toIgnore);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException
    {
        //deposit your output format
        if (visitor == null) {
            return;
        }


        JsonObjectFormatVisitor objectVisitor = visitor.expectObjectFormat(typeHint);
        if (objectVisitor == null) {
            return;
        }

        Set<JavaType> visited = visitedThreadLocal.get();

        if(!visited.add(typeHint))
            return;

        if (_propertyFilterId != null)
        {
            PropertyFilter filter = findPropertyFilter(visitor.getProvider(),
                    _propertyFilterId, null);
            for (int i = 0; i < _props.length; i++)
            {
                filter.depositSchemaProperty(_props[i], objectVisitor, visitor.getProvider());
            }
        }
        else
        {
            for (int i = 0; i < _props.length; i++)
            {
                _props[i].depositSchemaProperty(objectVisitor);
            }
        }

        visited.clear();
    }
}
