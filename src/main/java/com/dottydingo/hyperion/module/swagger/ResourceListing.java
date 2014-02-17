package com.dottydingo.hyperion.module.swagger;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 */
@JsonPropertyOrder({"apiVersion","swaggerVersion","apis"})
public class ResourceListing
{
    private String apiVersion;
    private String swaggerVersion;
    private List<ApiResource> apis;

    public String getApiVersion()
    {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion)
    {
        this.apiVersion = apiVersion;
    }

    public String getSwaggerVersion()
    {
        return swaggerVersion;
    }

    public void setSwaggerVersion(String swaggerVersion)
    {
        this.swaggerVersion = swaggerVersion;
    }

    public List<ApiResource> getApis()
    {
        return apis;
    }

    public void setApis(List<ApiResource> apis)
    {
        this.apis = apis;
    }
}
