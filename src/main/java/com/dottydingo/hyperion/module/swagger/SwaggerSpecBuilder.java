package com.dottydingo.hyperion.module.swagger;

import com.dottydingo.hyperion.api.DeleteResponse;
import com.dottydingo.hyperion.api.EntityResponse;
import com.dottydingo.hyperion.api.HistoryResponse;
import com.dottydingo.hyperion.service.configuration.ApiVersionPlugin;
import com.dottydingo.hyperion.service.configuration.EntityPlugin;
import com.dottydingo.hyperion.service.configuration.HyperionEndpointConfiguration;
import com.dottydingo.hyperion.service.configuration.ServiceRegistry;
import com.dottydingo.hyperion.service.context.HttpMethod;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 */
public class SwaggerSpecBuilder
{
    private Logger logger = LoggerFactory.getLogger(SwaggerSpecBuilder.class);
    private ServiceRegistry serviceRegistry;
    private HyperionEndpointConfiguration endpointConfiguration;
    private String basePath;
    private ObjectMapper objectMapper;
    private List<Resource> additionalResources = Collections.emptyList();
    private String resourceBundleBase = "com.dottydingo.hyperion.module.swagger.Messages";

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setEndpointConfiguration(HyperionEndpointConfiguration endpointConfiguration)
    {
        this.endpointConfiguration = endpointConfiguration;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public void setAdditionalResources(List<Resource> additionalResources)
    {
        this.additionalResources = additionalResources;
    }

    public void setResourceBundleBase(String resourceBundleBase)
    {
        this.resourceBundleBase = resourceBundleBase;
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
        resources.addAll(additionalResources);
        listing.setApis(resources);
        return listing;
    }

    public ApiDeclaration buildEndpoint(String endpoint)
    {
        EntityPlugin plugin = serviceRegistry.getPluginForName(endpoint);
        if(plugin == null)
            return null;

        ResourceBundle resourceBundle = getResourceBundle(plugin.getEndpointName());

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
            apis.add(buildIdApi(plugin,resourceBundle));
        }

        if(plugin.isMethodAllowed(HttpMethod.GET) || plugin.isMethodAllowed(HttpMethod.POST))
            apis.add(buildApi(plugin,resourceBundle));

        TypeFactory typeFactory = objectMapper.getTypeFactory();

        if(plugin.isHistoryEnabled())
        {
            apis.add(buildHistory(plugin,resourceBundle));
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

    private Api buildHistory(EntityPlugin plugin,ResourceBundle resourceBundle)
    {


        Api api = new Api();
        api.setPath(String.format("/%s/history/{id}",plugin.getEndpointName()));
        api.setDescription(plugin.getEndpointName());
        List<Operation> operations = new ArrayList<Operation>();
        api.setOperations(operations);

        Operation operation = new Operation();
        operation.setMethod("GET");
        operation.setNickname(String.format("get%s", plugin.getEndpointName()));
        operation.setSummary(resourceBundle.getString("history.summary"));
        operation.setNotes(resourceBundle.getString("history.notes"));
        operation.setType(String.format("%sHistoryResponse", plugin.getEndpointName()));
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("id",resourceBundle.getString("history.param.id.description"),
                "path",
                "string",
                true));
        parameters.add(buildParameter("start",resourceBundle.getString("history.param.start.description"),"query","string"));
        parameters.add(buildParameter("limit",resourceBundle.getString("history.param.limit.description"),"query","string"));
        if (endpointConfiguration.isAllowTrace())
            parameters.add(buildParameter(endpointConfiguration.getTraceParameterName(),
                    resourceBundle.getString("history.param.trace.description"),
                    "query",
                    "string"));

        operations.add(operation);

        return api;
    }

    private Api buildApi(EntityPlugin plugin,ResourceBundle resourceBundle)
    {
        Api api = new Api();
        api.setPath(String.format("/%s",plugin.getEndpointName()));
        api.setDescription(plugin.getEndpointName());
        List<Operation> operations = new ArrayList<Operation>();
        api.setOperations(operations);
        if(plugin.isMethodAllowed(HttpMethod.GET))
            operations.add(buildQueryOperation(plugin,resourceBundle));
        if(plugin.isMethodAllowed(HttpMethod.POST))
            operations.add(buildCreateOperation(plugin,resourceBundle));
        return api;
    }

    private Operation buildCreateOperation(EntityPlugin plugin,ResourceBundle resourceBundle)
    {
        Operation operation = new Operation();
        operation.setMethod("POST");
        operation.setNickname(String.format("create%s", plugin.getEndpointName()));
        operation.setSummary(resourceBundle.getString("create.summary"));
        operation.setNotes(resourceBundle.getString("create.notes"));
        operation.setType(plugin.getEndpointName());
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("fields",resourceBundle.getString("create.param.fields.description"),"query","string"));
        parameters.add(buildParameter(endpointConfiguration.getVersionParameterName(),
                resourceBundle.getString("create.param.version.description"),
                "query",
                "string",
                endpointConfiguration.isRequireVersion()));
        parameters.add(buildParameter(null,resourceBundle.getString("create.param.body.description"),"body",plugin.getEndpointName()));
        if (endpointConfiguration.isAllowTrace())
            parameters.add(buildParameter(endpointConfiguration.getTraceParameterName(),
                    resourceBundle.getString("create.param.trace.description"),
                    "query",
                    "string"));
        return operation;
    }

    private Operation buildQueryOperation(EntityPlugin plugin,ResourceBundle resourceBundle)
    {
        Operation operation = new Operation();
        operation.setMethod("GET");
        operation.setNickname(String.format("get%s", plugin.getEndpointName()));
        operation.setSummary(resourceBundle.getString("query.summary"));
        operation.setNotes(resourceBundle.getString("query.notes"));
        operation.setType(String.format("%sEntityResponse", plugin.getEndpointName()));
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("fields", resourceBundle.getString("query.param.fields.description"), "query", "string"));
        parameters.add(buildParameter("sort", resourceBundle.getString("query.param.sort.description"),"query","string"));
        parameters.add(buildParameter("start", resourceBundle.getString("query.param.start.description"),"query","string"));
        parameters.add(buildParameter("limit", resourceBundle.getString("query.param.limit.description"),"query","string"));
        parameters.add(buildParameter("query", resourceBundle.getString("query.param.query.description"),"query","string"));
        parameters.add(buildParameter(endpointConfiguration.getVersionParameterName(),
                resourceBundle.getString("create.param.version.description"),
                "query",
                "string",
                endpointConfiguration.isRequireVersion()));
        if (endpointConfiguration.isAllowTrace())
            parameters.add(buildParameter(endpointConfiguration.getTraceParameterName(),
                    resourceBundle.getString("query.param.trace.description"),
                    "query",
                    "string"));
        return operation;
    }

    private Api buildIdApi(EntityPlugin plugin,ResourceBundle resourceBundle)
    {
        Api api = new Api();
        api.setPath(String.format("/%s/{id}",plugin.getEndpointName()));
        api.setDescription(plugin.getEndpointName());
        List<Operation> operations = new ArrayList<Operation>();
        api.setOperations(operations);
        if(plugin.isMethodAllowed(HttpMethod.GET))
            operations.add(buildGetOperation(plugin,resourceBundle));
        if(plugin.isMethodAllowed(HttpMethod.DELETE))
            operations.add(buildDeleteOperation(plugin,resourceBundle));
        if(plugin.isMethodAllowed(HttpMethod.PUT))
            operations.add(buildUpdateOperation(plugin,resourceBundle));

        return api;
    }

    private Operation buildUpdateOperation(EntityPlugin plugin,ResourceBundle resourceBundle)
    {
        Operation operation = new Operation();
        operation.setMethod("PUT");
        operation.setNickname(String.format("update%s", plugin.getEndpointName()));
        operation.setSummary(resourceBundle.getString("update.summary"));
        operation.setNotes(resourceBundle.getString("update.notes"));
        operation.setType(plugin.getEndpointName());
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("id",
                resourceBundle.getString("update.param.id.description"),
                "path",
                "string",
                true));
        parameters.add(buildParameter("fields",resourceBundle.getString("update.param.fields.description"),"query","string"));
        parameters.add(buildParameter(endpointConfiguration.getVersionParameterName(),
                resourceBundle.getString("create.param.version.description"),
                "query",
                "string",
                endpointConfiguration.isRequireVersion()));
        parameters.add(buildParameter(null,resourceBundle.getString("update.param.body.description"),"body",plugin.getEndpointName()));
        if (endpointConfiguration.isAllowTrace())
            parameters.add(buildParameter(endpointConfiguration.getTraceParameterName(),
                    resourceBundle.getString("update.param.trace.description"),
                    "query",
                    "string"));
        return operation;
    }

    private Operation buildDeleteOperation(EntityPlugin plugin,ResourceBundle resourceBundle)
    {
        Operation operation = new Operation();
        operation.setMethod("DELETE");
        operation.setNickname(String.format("delete%s",plugin.getEndpointName()));
        operation.setSummary(resourceBundle.getString("delete.summary"));
        operation.setNotes(resourceBundle.getString("delete.notes"));
        operation.setType(String.format("%sDeleteResponse",plugin.getEndpointName()));
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("id",
                resourceBundle.getString("delete.param.id.description"),
                "path",
                "string",
                true));
        if (endpointConfiguration.isAllowTrace())
            parameters.add(buildParameter(endpointConfiguration.getTraceParameterName(),
                    resourceBundle.getString("delete.param.trace.description"),
                    "query",
                    "string"));
        return operation;
    }

    private Operation buildGetOperation(EntityPlugin plugin,ResourceBundle resourceBundle)
    {
        Operation operation = new Operation();
        operation.setMethod("GET");
        operation.setNickname(String.format("get%s",plugin.getEndpointName()));
        operation.setSummary(resourceBundle.getString("get.summary"));
        operation.setNotes(resourceBundle.getString("get.notes"));
        operation.setType(String.format("%sEntityResponse",plugin.getEndpointName()));
        List<Parameter> parameters = new ArrayList<Parameter>();
        operation.setParameters(parameters);

        parameters.add(buildParameter("id",
                resourceBundle.getString("get.param.id.description"),
                "path",
                "string",
                true));
        parameters.add(buildParameter("fields",resourceBundle.getString("get.param.fields.description"),"query","string"));
        parameters.add(buildParameter(endpointConfiguration.getVersionParameterName(),
                resourceBundle.getString("create.param.version.description"),
                "query",
                "string",
                endpointConfiguration.isRequireVersion()));
        if (endpointConfiguration.isAllowTrace())
            parameters.add(buildParameter(endpointConfiguration.getTraceParameterName(),
                    resourceBundle.getString("get.param.trace.description"),
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

    protected ResourceBundle getResourceBundle(String endpoint)
    {
        return ResourceBundle.getBundle(resourceBundleBase,new Locale(endpoint));
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
