package com.dottydingo.hyperion.module.swagger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
public abstract class ResourceHandler
{
    private String path;
    private String description;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public abstract void renderResourceListing(HttpServletRequest httpServletRequest,
                                               HttpServletResponse httpServletResponse) throws IOException;

}
