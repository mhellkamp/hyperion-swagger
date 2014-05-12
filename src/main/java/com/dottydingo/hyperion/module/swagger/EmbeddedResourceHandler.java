package com.dottydingo.hyperion.module.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 */
public class EmbeddedResourceHandler extends ResourceHandler
{
    public static final int BUFFER_SIZE = 8192;

    private Logger logger = LoggerFactory.getLogger(EmbeddedResourceHandler.class);

    private String location;

    public void setLocation(String location)
    {
        this.location = location;
    }

    @Override
    public void renderResourceListing(HttpServletRequest httpServletRequest,
                                      HttpServletResponse httpServletResponse) throws IOException
    {
        try
        {
            InputStream inputStream = EmbeddedResourceHandler.class.getResourceAsStream(location);
            if(inputStream != null)
                copy(inputStream,httpServletResponse.getOutputStream());
            else
                httpServletResponse.sendError(404);
        }
        catch (Exception e)
        {
            logger.error(String.format("Error reading embedded resource %s",location),e);
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
