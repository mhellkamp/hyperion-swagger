package com.dottydingo.hyperion.module.swagger.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;

/**
 */
public class SchemaBeanSerializerFactory extends BeanSerializerFactory
{
    public SchemaBeanSerializerFactory(SerializerFactoryConfig config)
    {
        super(config);
    }

    @Override
    protected BeanSerializerBuilder constructBeanSerializerBuilder(BeanDescription beanDesc)
    {
        return new SchemaBeanSerializerBuilder(beanDesc);
    }
}
