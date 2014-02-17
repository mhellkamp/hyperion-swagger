package com.dottydingo.hyperion.module.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class EmbeddedResourcesRequestHandler extends SwaggerRequestHandler
{
    public static final int BUFFER_SIZE = 8192;

    private Logger logger = LoggerFactory.getLogger(EmbeddedResourcesRequestHandler.class);

    private List<EmbeddedResource> embeddedResources = Collections.emptyList();
    private Map<String,EmbeddedResource> embeddedResourceMap = new HashMap<String, EmbeddedResource>();

    public void setEmbeddedResources(List<EmbeddedResource> embeddedResources)
    {
        this.embeddedResources = embeddedResources;
    }

    @Override
    protected void handleResourceListing(HttpServletResponse httpServletResponse) throws IOException
    {
        ResourceListing listing = swaggerSpecBuilder.buildResourceListing();

        List<ApiResource> apis = listing.getApis();
        for (EmbeddedResource embeddedResource : embeddedResources)
        {
            ApiResource resource = new ApiResource();
            resource.setPath(String.format("/%s",embeddedResource.getPath()));
            resource.setDescription(embeddedResource.getDescription());
            apis.add(resource);
            embeddedResourceMap.put(embeddedResource.getPath(),embeddedResource);
        }

        objectMapper.writeValue(httpServletResponse.getOutputStream(), listing);
    }

    @Override
    protected void handleApiListing(HttpServletResponse httpServletResponse, String endpoint) throws IOException
    {
        EmbeddedResource embeddedResource = embeddedResourceMap.get(endpoint);
        if(embeddedResource == null)
        {
            super.handleApiListing(httpServletResponse, endpoint);
            return;
        }

        try
        {
            InputStream inputStream = EmbeddedResourcesRequestHandler.class.getResourceAsStream(embeddedResource.getLocation());
            if(inputStream != null)
                copy(inputStream,httpServletResponse.getOutputStream());
            else
                httpServletResponse.sendError(404);
        }
        catch (Exception e)
        {
            logger.error(String.format("Error reading embedded resource %s",embeddedResource.getLocation()),e);
            httpServletResponse.sendError(404);
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }
}
