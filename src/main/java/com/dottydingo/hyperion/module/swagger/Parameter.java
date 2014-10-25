package com.dottydingo.hyperion.module.swagger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 */
@JsonPropertyOrder({"paramType","name","description","dataType","format","required","minimum","maximum","defaultValue","enum"})
public class Parameter
{
    private String paramType;
    private String name;
    private String description;
    private String dataType;
    private String format;
    private boolean required;
    private Integer minimum;
    private Integer maximum;
    private String defaultValue;
    private List<String> possibleValues;

    public String getParamType()
    {
        return paramType;
    }

    public void setParamType(String paramType)
    {
        this.paramType = paramType;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDataType()
    {
        return dataType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public Integer getMinimum()
    {
        return minimum;
    }

    public void setMinimum(Integer minimum)
    {
        this.minimum = minimum;
    }

    public Integer getMaximum()
    {
        return maximum;
    }

    public void setMaximum(Integer maximum)
    {
        this.maximum = maximum;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    public List<String> getPossibleValues()
    {
        return possibleValues;
    }

    @JsonProperty("enum")
    public void setPossibleValues(List<String> possibleValues)
    {
        this.possibleValues = possibleValues;
    }
}
