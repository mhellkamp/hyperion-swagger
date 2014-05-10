package com.dottydingo.hyperion.module.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
public class SwaggerRequestHandler  implements HttpRequestHandler
{
    protected SwaggerSpecBuilder swaggerSpecBuilder;
    protected ObjectMapper objectMapper = new ObjectMapper();

    public void setSwaggerSpecBuilder(SwaggerSpecBuilder swaggerSpecBuilder)
    {
        this.swaggerSpecBuilder = swaggerSpecBuilder;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
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
            handleResourceListing(httpServletResponse);
            return;
        }

        String[] split = path.split("/");
        if(split.length == 2)
        {
            handleApiListing(httpServletResponse, split[1]);
        }
        else
        {
            httpServletResponse.sendError(404);
        }
    }

    protected void handleResourceListing(HttpServletResponse httpServletResponse) throws IOException
    {
        ResourceListing listing = swaggerSpecBuilder.buildResourceListing();
        objectMapper.writeValue(httpServletResponse.getOutputStream(),listing);
    }

    protected void handleApiListing(HttpServletResponse httpServletResponse, String endpoint) throws IOException
    {
        ApiDeclaration declaration = swaggerSpecBuilder.buildEndpoint(endpoint);
        if(declaration == null)
        {
            httpServletResponse.sendError(404);
            return;
        }
        objectMapper.writeValue(httpServletResponse.getOutputStream(),declaration);
    }
}
