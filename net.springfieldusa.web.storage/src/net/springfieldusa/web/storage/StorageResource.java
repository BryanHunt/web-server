/*******************************************************************************
 * Copyright (c) 2016 Bryan Hunt.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Hunt - initial API and implementation
 *******************************************************************************/

package net.springfieldusa.web.storage;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import net.springfieldusa.data.ApplicationDataService;
import net.springfieldusa.data.AuthorizationException;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.entity.ObjectReference;
import net.springfieldusa.entity.Relationship;
import net.springfieldusa.storage.DuplicateIdException;
import net.springfieldusa.web.WebResource;
import net.springfieldusa.web.json.api.JsonApi;
import net.springfieldusa.web.json.api.JsonApiData;
import net.springfieldusa.web.json.api.JsonApiDataCollectionWrapper;
import net.springfieldusa.web.json.api.JsonApiDataWrapper;
import net.springfieldusa.web.json.api.JsonApiRelationship;
import net.springfieldusa.web.json.api.JsonApiRelationshipCollectionWrapper;
import net.springfieldusa.web.json.api.JsonApiRelationshipWrapper;

@Api(value = "storage")
@Path("/storage/{collection}")
@Consumes("application/vnd.api+json")
@Produces("application/vnd.api+json")
@Component(service = StorageResource.class)
public class StorageResource extends WebResource
{
  private volatile ApplicationDataService applicationDataService;
  private volatile AuthenticationHandler authenticationHandler;
  private Set<String> reservedQueryParameters;

  public StorageResource()
  {
    reservedQueryParameters = new HashSet<>();
    reservedQueryParameters.add("skip");
    reservedQueryParameters.add("limit");
    reservedQueryParameters.add("offset");
  }
  
  @POST
  @ApiOperation(value = "Persist a new document to the database", notes = "The document id is optional.  If it is supplied, it must unique.  If it is not supplied, the server will generate one.")
  @ApiResponses(value = { @ApiResponse(code = 201, message = "Document was persisted to the database", reference = "#/definitions/JsonApiDataWrapper"), 
                          @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 409, message = "A document with the supplied id already exists in the database"),
                          @ApiResponse(code = 403, message = "You are not authorized to create the document") })
  public Response createResource(@Context UriInfo uriInfo, @Context SecurityContext securityContext, 
      @ApiParam(value = "the database collection in which to store the document", required = true) @PathParam("collection") String collection,
      @ApiParam(value = "the document to store", required = true) JsonApiDataWrapper resource)
  {
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());

      // --- Business Logic ------------------------------------------------------------------------
      
      recordPost(uriInfo, securityContext.getUserPrincipal());
      EntityObject entity = new EntityObject((String) resource.getData().getId(), resource.getData().camelizedAttributes(), decodeRelationships(resource), resource.getMeta());

      entity = applicationDataService.create(securityContext.getUserPrincipal(), collection, entity);

      // --- JSON API ------------------------------------------------------------------------------

      JsonApi jsonApi = new JsonApiDataWrapper(new JsonApiData(entity.getId(), collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));
      return Response.created(new URI(uriInfo.getAbsolutePath().toString() + "/" + entity.getId())).entity(jsonApi).build();
    }
    catch (DuplicateIdException e)
    {
      throw new ClientErrorException("Duplicate ID", Status.CONFLICT);
    }
    catch (AuthorizationException e)
    {
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException(e);
    }
  }

  @GET
  @ApiOperation(value = "Query and retrieve documents from the database", notes = "The skip and offset parameters have the same function and only one needs to be specified if you want to skip documents.  If both skip and offset are specified, skip has precedence.")
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to retrieve the documents") })  
  public JsonApiDataCollectionWrapper retrieveResources(@Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection to query for the documents", required = true) @PathParam("collection") String collection,
      @ApiParam(value = "the number of documents to skip", required = false) @QueryParam("skip") Integer skip, 
      @ApiParam(value = "the number of documents to skip", required = false) @QueryParam("offset") Integer offset, 
      @ApiParam(value = "the maximum number of documents to return", required = false) @QueryParam("limit") Integer limit, 
      @ApiParam(value = "the native database query", required = false) @QueryParam("filter") String filter)
  {
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());

      // --- Business Logic ------------------------------------------------------------------------

      recordGet(uriInfo, securityContext.getUserPrincipal());

      if (skip == null)
        skip = offset != null ? offset : 0;

      if (limit == null)
        limit = 0;

      Collection<EntityObject> entities = null;
      
      if(filter != null)
      {
        entities = applicationDataService.find(securityContext.getUserPrincipal(), collection, filter, skip, limit);
      }
      else
      {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        Map<String, Object> query = new HashMap<>();
        
        for(String key : queryParameters.keySet())
        {
          if(!reservedQueryParameters.contains(key))
            query.put(key, queryParameters.get(key).get(0)); // FIXME make $or ??
        }
        
        entities = applicationDataService.find(securityContext.getUserPrincipal(), collection, query, skip, limit);
      }
      

      if (entities == null || entities.isEmpty())
        return new JsonApiDataCollectionWrapper(Collections.emptyList());

      // --- JSON API ------------------------------------------------------------------------------

      Collection<JsonApiData> jsonData = new ArrayList<>();

      entities.forEach((entity) -> {
        jsonData.add(new JsonApiData(entity.getId(), collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));
      });

      return new JsonApiDataCollectionWrapper(jsonData);
    }
    catch (AuthorizationException e)
    {
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException(e);
    }
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "Retrieve the specified document from the database")
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to retrieve the document"), 
                          @ApiResponse(code = 404, message = "The document with the specified id was not found in the database") })  
  public JsonApiDataWrapper retrieveResource(@Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection from wich to retrieve the document", required = true) @PathParam("collection") String collection,
      @ApiParam(value = "the document id", required = true) @PathParam("id") String id)
  {
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());

      // --- Business Logic ------------------------------------------------------------------------

      recordGet(uriInfo, securityContext.getUserPrincipal());

      EntityObject entity = applicationDataService.retrieve(securityContext.getUserPrincipal(), collection, id);

      if (entity == null)
        throw new NotFoundException();

      // --- JSON API ------------------------------------------------------------------------------

      return new JsonApiDataWrapper(new JsonApiData(id, collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));
    }
    catch (AuthorizationException e)
    {
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException(e);
    }
  }

  @PUT
  @Path("{id}")
  @ApiOperation(value = "Replace a document in the database")
  @ApiResponses(value = { @ApiResponse(code = 204, message = "Update was successful"),
                          @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to update the document"), 
                          @ApiResponse(code = 404, message = "The document with the specified id was not found in the database") })  
  public Response updateResource(@Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection containing the document to update", required = true) @PathParam("collection") String collection,
      @ApiParam(value = "the document id", required = true) @PathParam("id") String id, 
      @ApiParam(value = "the document to store", required = true) JsonApiDataWrapper resource)
  {
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());

      // --- Business Logic ------------------------------------------------------------------------

      recordPut(uriInfo, securityContext.getUserPrincipal());

      EntityObject entity = new EntityObject(id, resource.getData().camelizedAttributes(), decodeRelationships(resource), resource.getMeta());

      if(applicationDataService.update(securityContext.getUserPrincipal(), collection, entity) != 1)
        throw new NotFoundException();

      return Response.noContent().build();
    }
    catch (AuthorizationException e)
    {
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException(e);
    }
  }

  @PATCH
  @Path("{id}")
  @ApiOperation(httpMethod = "PATCH", value = "Patch a document in the database", notes = "Only the document attributes specified in the payload are modified in the database. Any new attributes will be added to the document in the database.  Document attributes may not be removed using PATCH.")
  @ApiResponses(value = { @ApiResponse(code = 204, message = "Patch was successful"),
                          @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to update the document"), 
                          @ApiResponse(code = 404, message = "The document with the specified id was not found in the database") })  
  public Response patchResource(@Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection containing the document to update") @PathParam("collection") String collection,
      @ApiParam(value = "the document id", required = true) @PathParam("id") String id, 
      @ApiParam(value = "the document attributes to update", required = true) JsonApiDataWrapper resource)
  {
    
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());

      // --- Business Logic ------------------------------------------------------------------------

      recordPatch(uriInfo, securityContext.getUserPrincipal());
      
      EntityObject entity = new EntityObject(id, resource.getData().camelizedAttributes(), decodeRelationships(resource), resource.getMeta());
      
      if(applicationDataService.patch(securityContext.getUserPrincipal(), collection, entity) != 1)
        throw new NotFoundException();

      return Response.noContent().build();
    }
    catch (AuthorizationException e)
    {
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException(e);
    }
  }

  @DELETE
  @Path("{id}")
  @ApiOperation(value = "Delete a document from the database")
  @ApiResponses(value = { @ApiResponse(code = 204, message = "Delete was successful"),
                          @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to delete the document"), 
                          @ApiResponse(code = 404, message = "The document with the specified id was not found in the database") })  
  public Response deleteResource(@Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection containing the document to delete") @PathParam("collection") String collection,
      @ApiParam(value = "the document id", required = true) @PathParam("id") String id)
  {
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());

      // --- Business Logic ------------------------------------------------------------------------

      recordDelete(uriInfo, securityContext.getUserPrincipal());
      
      if(applicationDataService.delete(securityContext.getUserPrincipal(), collection, id) != 1)
        throw new NotFoundException();
      
      return Response.noContent().build();
    }
    catch (AuthorizationException e)
    {
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException(e);
    }
  }

  @Reference(unbind = "-", target = "(secure=true)")
  public void bindApplicationDataService(ApplicationDataService applicationDataService)
  {
    this.applicationDataService = applicationDataService;
  }

  @Reference(unbind = "-")
  public void bindAuthenticationHandler(AuthenticationHandler authenticationHandler)
  {
    this.authenticationHandler = authenticationHandler;
  }
  
  private Map<String, Object> encodeRelationships(EntityObject storedResource)
  {
    Map<String, Object> jsonApiRelationshipMapping = new HashMap<>();

    for (Relationship relationship : storedResource.getRelationships())
    {
      if (relationship.isMany())
      {
        Collection<JsonApiRelationship> jsonApiRelationships = new ArrayList<>();
        jsonApiRelationshipMapping.put(relationship.getType(), new JsonApiRelationshipCollectionWrapper(jsonApiRelationships));

        relationship.getObjectReferences().forEach((reference) -> {
          jsonApiRelationships.add(new JsonApiRelationship(reference.getId(), reference.getType()));
        });
      }
      else
      {
        ObjectReference reference = relationship.getObjectReference();
        jsonApiRelationshipMapping.put(relationship.getType(), new JsonApiRelationshipWrapper(new JsonApiRelationship(reference.getId(), reference.getType())));
      }
    }

    return jsonApiRelationshipMapping;
  }

  private Collection<Relationship> decodeRelationships(JsonApiDataWrapper resource)
  {
    Collection<Relationship> relationships = new ArrayList<>();

    for (Entry<String, Object> entry : resource.getData().camelizedRelationships().entrySet())
    {
      @SuppressWarnings("unchecked")
      Map<String, Object> relationshipData = (Map<String, Object>) entry.getValue();
      relationships.add(new Relationship(entry.getKey(), relationshipData.get("data")));
    }

    return relationships;
  }
}
