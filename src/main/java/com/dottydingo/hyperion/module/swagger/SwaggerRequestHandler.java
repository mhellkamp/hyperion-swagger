package com.dottydingo.hyperion.module.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class SwaggerRequestHandler  implements HttpRequestHandler,InitializingBean
{
    protected SwaggerSpecBuilder swaggerSpecBuilder;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected Map<String,ResourceHandler> additionalResourcesMap = new LinkedHashMap<String, ResourceHandler>();

    public void setSwaggerSpecBuilder(SwaggerSpecBuilder swaggerSpecBuilder)
    {
        this.swaggerSpecBuilder = swaggerSpecBuilder;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper.copy();
    }

    public void setAdditionalResources(List<ResourceHandler> additionalResources)
    {
        if(additionalResources != null)
        {
            for (ResourceHandler additionalResource : additionalResources)
            {
                additionalResourcesMap.put(additionalResource.getPath(),
                        additionalResource);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if(objectMapper == null)
            objectMapper = new ObjectMapper();

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException
    {
        String path = httpServletRequest.getPathInfo();
        if(path == null || path.length() == 0)
        {
            httpServletResponse.sendError(404);
            return;
        }

        if(path.equals("/"))
        {
            handleResourceListing(httpServletRequest,httpServletResponse);
            return;
        }

        String[] split = path.split("/");
        if(split.length == 2)
        {
            handleApiListing(httpServletRequest,httpServletResponse, split[1]);
        }
        else
        {
            httpServletResponse.sendError(404);
        }
    }

    protected void handleResourceListing(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
    {
        ResourceListing listing = swaggerSpecBuilder.buildResourceListing();

        List<ApiResource> apis = listing.getApis();

        for (ResourceHandler resourceHandler : additionalResourcesMap.values())
        {
            ApiResource resource = new ApiResource();
            resource.setPath(String.format("/%s",resourceHandler.getPath()));
            resource.setDescription(resourceHandler.getDescription());
            apis.add(resource);
        }

        objectMapper.writeValue(httpServletResponse.getOutputStream(), listing);
    }

    protected void handleApiListing(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    String endpoint) throws IOException
    {
        ResourceHandler handler = additionalResourcesMap.get(endpoint);
        if(handler != null)
        {
            handler.renderResourceListing(httpServletRequest,httpServletResponse);
            return;
        }

        ApiDeclaration declaration = swaggerSpecBuilder.buildEndpoint(endpoint);
        if(declaration == null)
        {
            httpServletResponse.sendError(404);
            return;
        }
        objectMapper.writeValue(httpServletResponse.getOutputStream(),declaration);
    }
}
