package com.dottydingo.hyperion.module.swagger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;

import java.io.IOException;

/**
 */
@JsonSerialize(using = ReferenceSchema.ReferenceSerializer.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class ReferenceSchema extends ObjectSchema
{

    public static class ReferenceSerializer extends StdScalarSerializer<ReferenceSchema>
    {
        public ReferenceSerializer()
        {
            super(ReferenceSchema.class);
        }

        @Override
        public void serializeWithType(ReferenceSchema value, JsonGenerator jgen, SerializerProvider provider, TypeSerializer typeSer)
                throws IOException, JsonGenerationException
        {
            serialize(value, jgen, provider);
        }

        @Override
        public void serialize(ReferenceSchema value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException
        {
            jgen.writeStartObject();
            jgen.writeStringField("$ref",value.get$ref());
            jgen.writeEndObject();
        }
    }
}
