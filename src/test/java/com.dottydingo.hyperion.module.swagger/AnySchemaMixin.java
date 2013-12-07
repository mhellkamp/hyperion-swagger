package com.dottydingo.hyperion.module.swagger;

import com.fasterxml.jackson.databind.JsonNode;

/**
 */
public interface AnySchemaMixin
{
    @AnySchema
    JsonNode getJsonNode();
}
