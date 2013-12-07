package com.dottydingo.hyperion.module.swagger;

import com.dottydingo.hyperion.api.DeleteResponse;
import com.dottydingo.hyperion.api.EntityResponse;
import com.dottydingo.hyperion.api.HistoryResponse;
import com.dottydingo.hyperion.service.configuration.ApiVersionPlugin;
import com.dottydingo.hyperion.service.configuration.EntityPlugin;
import com.dottydingo.hyperion.service.configuration.ServiceRegistry;
import com.dottydingo.hyperion.service.context.HttpMethod;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;

/**
 */
public class SwaggerSpecBuilder
{
    private Logger logger = LoggerFactory.getLogger(SwaggerSpecBuilder.class);
    private ServiceRegistry serviceRegistry;
    private String basePath;
    private ObjectMapper objectMapper;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public ResourceListing buildResourceListing()
    {
        ResourceListing listing = new ResourceListing();
        listing.setApiVersion("1.0");
        listing.setSwaggerVersion("1.2");

        List<Resource> resources = new ArrayList<Resource>();
        for (EntityPlugin plugin : serviceRegistry.getEntityPlugins())
        {
            Resource resource = new Resource();
            resource.setDescription(plugin.getEndpointName());
            resource.setPath(String.format("/%s",plugin.getEndpointName()));
            resources.add(resource);
        }
        listing.setApis(resources);
        return listing;
    }

    public ApiDeclaration buildEndpoint(String endpoint)
    {
        EntityPlugin plugin = serviceRegistry.getPluginForName(endpoint);
        if(plugin == null)
            return null;

        ApiDeclaration api = new ApiDeclaration();
        Map<String,Model> models = new HashMap<String, Model>();
        api.setModels(models);

        ApiVersionPlugin pluginVersion = plugin.getApiVersionRegistry().getPluginForVersion(null);

        api.setApiVersion(pluginVersion.getVersion().toString());
        api.setSwaggerVersion("1.2");
        api.setBasePath(basePath);
        api.setResourcePath(String.format("/%s",plugin.getEndpointName()));

        List<Api> apis = new ArrayList<Api>();
        if(plugin.isMethodAllowed(HttpMethod.GET) || plugin.isMethodAllowed(HttpMethod.PUT) ||
                plugin.isMethodAllowed(HttpMethod.DELETE))
        {
            apis.add(buildIdApi(plugin));
        }

        if(plugin.isMethodAllowed(HttpMethod.GET) || plugin.isMethodAllowed(HttpMethod.POST))
            apis.add(buildApi(plugin));

        TypeFactory typeFactory = objectMapper.getTypeFactory();

        if(plugin.isHistoryEnabled())
        {
            apis.add(buildHistory(plugin));
            models.putAll(buildModels(String.format("%sHistoryResponse", plugin.getEndpointName()),
                    typeFactory.constructParametricType(HistoryResponse.class, Serializable.class, pluginVersion.getApiClass())));
        }

        api.setApis(apis);


        models.putAll(buildModels(endpoint, typeFactory.constructType(pluginVersion.getApiClass())));

        if(plugin.isMethodAllowed(HttpMethod.GET))
            models.putAll(buildModels(String.format("%sEntityResponse", plugin.getEndpointName()),
                    typeFactory.constructParametricType(EntityResponse.class, pluginVersion.getApiClass())));

        if(plugin.isMethodAllowed(HttpMethod.DELETE))
            models.putAll(buildModels(String.format("%sDeleteResponse", plugin.getEndpointName()),
                    typeFactory.constructType(DeleteResponse.class)));

        return api;
    }

    private Api buildHistory(EntityPlugin plugin)
    {
        Api api = new Api();
        api.setPath(String.format("/%s/history/{id}",plugin.getEndpointName()));
        api.setDescription(plugin.getEndpointName());
        List<Operation> operations = new ArrayList<Operation>();
        api.setOperations(operations);

        Operation operation = new Operation();
        operation.setMethod("GET");
        operation.setNickname(String.format("get%s",plugin.getEndpointName()));
        operation.setSummary("Retrieve history for an entity.");
        operation.setType(String.format("%sHistoryResponse",plugin.getEndpointName()));
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("id",
                "The ID of the item to retrieve history for.",
                "path",
                "string",
                true));
        parameters.add(buildParameter("start","The starting position for the results. This defaults to 1. Used for paging.","query","string"));
        parameters.add(buildParameter("limit","The maximum number of results to return in the query. This defaults to 500. Used for paging.","query","string"));
        operations.add(operation);

        return api;
    }

    private Api buildApi(EntityPlugin plugin)
    {
        Api api = new Api();
        api.setPath(String.format("/%s",plugin.getEndpointName()));
        api.setDescription(plugin.getEndpointName());
        List<Operation> operations = new ArrayList<Operation>();
        api.setOperations(operations);
        if(plugin.isMethodAllowed(HttpMethod.GET))
            operations.add(buildQueryOperation(plugin));
        if(plugin.isMethodAllowed(HttpMethod.POST))
            operations.add(buildCreateOperation(plugin));        
        return api;
    }

    private Operation buildCreateOperation(EntityPlugin plugin)
    {
        Operation operation = new Operation();
        operation.setMethod("POST");
        operation.setNickname(String.format("create%s", plugin.getEndpointName()));
        operation.setSummary("Create an item.");
        operation.setType(plugin.getEndpointName());
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("fields","A comma separated list of fields to return.","query","string"));
        parameters.add(buildParameter("version",
                "An optional API version. If omitted the latest API version is used.",
                "query",
                "string"));
        parameters.add(buildParameter(null,"New entity","body",plugin.getEndpointName()));
        return operation;
    }

    private Operation buildQueryOperation(EntityPlugin plugin)
    {
        Operation operation = new Operation();
        operation.setMethod("GET");
        operation.setNickname(String.format("get%s", plugin.getEndpointName()));
        operation.setSummary("Query items.");
        operation.setType(String.format("%sEntityResponse", plugin.getEndpointName()));
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("fields", "A comma separated list of fields to return.", "query", "string"));
        parameters.add(buildParameter("sort","An optional comma separated list of sort fields. Descending Creative may be specified by using the form fieldName:desc","query","string"));
        parameters.add(buildParameter("start","The starting position for the results. This defaults to 1. Used for paging.","query","string"));
        parameters.add(buildParameter("limit","The maximum number of results to return in the query. This defaults to 500. Used for paging.","query","string"));
        parameters.add(buildParameter("query","An optional query specified in RSQL.","query","string"));
        parameters.add(buildParameter("version",
                "An optional API version. If omitted the latest API version is used.",
                "query",
                "string"));
        return operation;
    }

    private Api buildIdApi(EntityPlugin plugin)
    {
        Api api = new Api();
        api.setPath(String.format("/%s/{id}",plugin.getEndpointName()));
        api.setDescription(plugin.getEndpointName());
        List<Operation> operations = new ArrayList<Operation>();
        api.setOperations(operations);
        if(plugin.isMethodAllowed(HttpMethod.GET))
            operations.add(buildGetOperation(plugin));
        if(plugin.isMethodAllowed(HttpMethod.DELETE))
            operations.add(buildDeleteOperation(plugin));
        if(plugin.isMethodAllowed(HttpMethod.PUT))
            operations.add(buildUpdateOperation(plugin));

        return api;
    }

    private Operation buildUpdateOperation(EntityPlugin plugin)
    {
        Operation operation = new Operation();
        operation.setMethod("PUT");
        operation.setNickname(String.format("update%s", plugin.getEndpointName()));
        operation.setSummary("Update an item.");
        operation.setType(plugin.getEndpointName());
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("id",
                "The id of the item being updated. If the id is also in the payload it must match this id.",
                "path",
                "string",
                true));
        parameters.add(buildParameter("fields","A comma separated list of fields to return.","query","string"));
        parameters.add(buildParameter("version",
                "An optional API version. If omitted the latest API version is used.",
                "query",
                "string"));
        parameters.add(buildParameter(null,"Updated entity","body",plugin.getEndpointName()));
        return operation;
    }

    private Operation buildDeleteOperation(EntityPlugin plugin)
    {
        Operation operation = new Operation();
        operation.setMethod("DELETE");
        operation.setNickname(String.format("delete%s",plugin.getEndpointName()));
        operation.setSummary("Update an item.");
        operation.setType(String.format("%sDeleteResponse",plugin.getEndpointName()));
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("id",
                "One or more comma separated ids to delete.",
                "path",
                "string",
                true));
        return operation;
    }

    private Operation buildGetOperation(EntityPlugin plugin)
    {
        Operation operation = new Operation();
        operation.setMethod("GET");
        operation.setNickname(String.format("get%s",plugin.getEndpointName()));
        operation.setSummary("Find items by ID.");
        operation.setType(String.format("%sEntityResponse",plugin.getEndpointName()));
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("id",
                "One or more comma separated ids to retrieve.",
                "path",
                "string",
                true));
        parameters.add(buildParameter("fields","A comma separated list of fields to return.","query","string"));
        parameters.add(buildParameter("version",
                "An optional API version. If omitted the latest API version is used.",
                "query",
                "string"));
        return operation;
    }


    private Parameter buildParameter(String name,String description,String paramType,String dataType)
    {
        return buildParameter(name, description, paramType, dataType,false);
    }

    private Parameter buildParameter(String name,String description,String paramType,String dataType,boolean required)
    {
        Parameter parameter = new Parameter();
        parameter.setName(name);
        parameter.setDescription(description);
        parameter.setParamType(paramType);
        parameter.setDataType(dataType);
        parameter.setRequired(required);
        return parameter;
    }

    private Model buildModel(String name, JavaType type)
    {
        try
        {
            SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();
            objectMapper.acceptJsonFormatVisitor(type, visitor);
            JsonSchema jsonSchema = visitor.finalSchema();

            Model model = new Model();
            model.setId(name);
            model.setProperties(((ObjectSchema) jsonSchema).getProperties());
            return model;
        }
        catch (JsonMappingException e)
        {
            logger.warn("Error generating model for type {}",type);
        }
        return null;
    }

    protected Map<String,Model> buildModels(String name,JavaType type)
    {
        ObjectSchema schema = buildSchema(type);
        if(schema != null)
            return buildModels(name, schema);
        return Collections.emptyMap();
    }

    protected ObjectSchema buildSchema(JavaType type)
    {
        try
        {
            TypeIdSchemaFactoryWrapper visitor = new TypeIdSchemaFactoryWrapper();
            objectMapper.acceptJsonFormatVisitor(type, visitor);
            JsonSchema jsonSchema = visitor.finalSchema();

            return jsonSchema.asObjectSchema();
        }
        catch (JsonMappingException e)
        {
            logger.warn("Error generating model for type {}",type);
        }
        return null;
    }


    protected Map<String,Model> buildModels(String name, ObjectSchema objectSchema)
    {
        Map<String,Model> modelMap = new HashMap<String, Model>();
        Map<String,JsonSchema> properties = new LinkedHashMap<String, JsonSchema>();

        for (Map.Entry<String, JsonSchema> entry : objectSchema.getProperties().entrySet())
        {
            JsonSchema schema = entry.getValue();
            if(schema.isObjectSchema())
            {
                JsonSchema temp = new ReferenceSchema();
                temp.set$ref(schema.getId());
                properties.put(entry.getKey(),temp);
                modelMap.putAll(buildModels(schema.getId(),schema.asObjectSchema()));
            }
            else if(schema.isArraySchema() && schema.asArraySchema().getItems().isSingleItems() && schema.asArraySchema().getItems().asSingleItems().getSchema().isObjectSchema())
            {
                ObjectSchema entrySchema = (ObjectSchema) schema.asArraySchema().getItems().asSingleItems().getSchema();

                ArraySchema arraySchema = new ArraySchema();
                JsonSchema temp = new ReferenceSchema();
                temp.set$ref(entrySchema.getId());
                arraySchema.setItemsSchema(temp);
                properties.put(entry.getKey(),arraySchema);

                modelMap.putAll(buildModels(entrySchema.getId(), entrySchema));
            }
            else
            {
                properties.put(entry.getKey(),entry.getValue());
            }
        }

        Model model = new Model();
        model.setId(name);
        model.setProperties(properties);
        modelMap.put(name,model);

        return modelMap;
    }

}
