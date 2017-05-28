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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;

import net.springfieldusa.data.ApplicationDataService;
import net.springfieldusa.data.ApplicationException;
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

public abstract class StorageResource extends WebResource
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
  
  public Response createResource(HttpServletRequest request, UriInfo uriInfo, SecurityContext securityContext, String collection, JsonApiDataWrapper resource)
  {
    long startTime = System.currentTimeMillis();
    
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
      {
        recordGet(request, uriInfo, securityContext.getUserPrincipal(), 401, System.currentTimeMillis() - startTime);
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());
      }
      
      // --- Business Logic ------------------------------------------------------------------------
      
      EntityObject entity = new EntityObject((String) resource.getData().getId(), resource.getData().camelizedAttributes(), decodeRelationships(resource), resource.getMeta());

      entity = applicationDataService.create(securityContext.getUserPrincipal(), collection, entity);

      // --- JSON API ------------------------------------------------------------------------------

      JsonApi jsonApi = new JsonApiDataWrapper(new JsonApiData(entity.getId(), collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));
      
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 202, System.currentTimeMillis() - startTime);
      return Response.created(new URI(uriInfo.getAbsolutePath().toString() + "/" + entity.getId())).entity(jsonApi).build();
    }
    catch (ApplicationException e)
    {
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 400, System.currentTimeMillis() - startTime);
      throw new BadRequestException(e.getMessage(), e);
    }
    catch (AuthorizationException e)
    {
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 403, System.currentTimeMillis() - startTime);
      throw new ForbiddenException(e);    
    }
    catch (DuplicateIdException e)
    {
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 409, System.currentTimeMillis() - startTime);
      throw new ClientErrorException("Duplicate ID", Status.CONFLICT);
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 500, System.currentTimeMillis() - startTime);
      throw new InternalServerErrorException(e);
    }
  }

  public JsonApiDataCollectionWrapper retrieveResources(@Context HttpServletRequest request, @Context SecurityContext securityContext, @Context UriInfo uriInfo, String collection, Integer skip, Integer offset, Integer limit, String filter)
  {
    long startTime = System.currentTimeMillis();

    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
      {
        recordGet(request, uriInfo, securityContext.getUserPrincipal(), 200, System.currentTimeMillis() - startTime);
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());
      }
      
      // --- Business Logic ------------------------------------------------------------------------

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
      {
        recordGet(request, uriInfo, securityContext.getUserPrincipal(), 200, System.currentTimeMillis() - startTime);
        return new JsonApiDataCollectionWrapper(Collections.emptyList());
      }
      
      // --- JSON API ------------------------------------------------------------------------------

      Collection<JsonApiData> jsonData = new ArrayList<>();

      entities.forEach((entity) -> {
        jsonData.add(new JsonApiData(entity.getId(), collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));
      });

      recordGet(request, uriInfo, securityContext.getUserPrincipal(), 200, System.currentTimeMillis() - startTime);
      return new JsonApiDataCollectionWrapper(jsonData);
    }
    catch (ApplicationException e)
    {
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 400, System.currentTimeMillis() - startTime);
      throw new BadRequestException(e.getMessage(), e);
    }
    catch (AuthorizationException e)
    {
      recordGet(request, uriInfo, securityContext.getUserPrincipal(), 403, System.currentTimeMillis() - startTime);
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      recordGet(request, uriInfo, securityContext.getUserPrincipal(), 500, System.currentTimeMillis() - startTime);
      throw new InternalServerErrorException(e);
    }
  }

  public JsonApiDataWrapper retrieveResource(HttpServletRequest request, SecurityContext securityContext, UriInfo uriInfo, String collection, String id)
  {
    long startTime = System.currentTimeMillis();

    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
      {
        recordGet(request, uriInfo, securityContext.getUserPrincipal(), 401, System.currentTimeMillis() - startTime);
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());
      }
      
      // --- Business Logic ------------------------------------------------------------------------

      EntityObject entity = applicationDataService.retrieve(securityContext.getUserPrincipal(), collection, id);

      if (entity == null)
      {
        recordGet(request, uriInfo, securityContext.getUserPrincipal(), 404, System.currentTimeMillis() - startTime);
        throw new NotFoundException();
      }
      
      // --- JSON API ------------------------------------------------------------------------------

      recordGet(request, uriInfo, securityContext.getUserPrincipal(), 200, System.currentTimeMillis() - startTime);
      return new JsonApiDataWrapper(new JsonApiData(id, collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));
    }
    catch (ApplicationException e)
    {
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 400, System.currentTimeMillis() - startTime);
      throw new BadRequestException(e.getMessage(), e);
    }
    catch (AuthorizationException e)
    {
      recordGet(request, uriInfo, securityContext.getUserPrincipal(), 403, System.currentTimeMillis() - startTime);
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      recordGet(request, uriInfo, securityContext.getUserPrincipal(), 500, System.currentTimeMillis() - startTime);
      throw new InternalServerErrorException(e);
    }
  }

  public Response updateResource(HttpServletRequest request, SecurityContext securityContext, UriInfo uriInfo, String collection, String id, JsonApiDataWrapper resource)
  {
    long startTime = System.currentTimeMillis();

    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
      {
        recordPut(request, uriInfo, securityContext.getUserPrincipal(), 401, System.currentTimeMillis() - startTime);
       throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());
      }
      
      // --- Business Logic ------------------------------------------------------------------------

      EntityObject entity = new EntityObject(id, resource.getData().camelizedAttributes(), decodeRelationships(resource), resource.getMeta());

      if(applicationDataService.update(securityContext.getUserPrincipal(), collection, entity) != 1)
      {
        recordPut(request, uriInfo, securityContext.getUserPrincipal(), 404, System.currentTimeMillis() - startTime);
        throw new NotFoundException();
      }
      
      recordPut(request, uriInfo, securityContext.getUserPrincipal(), 200, System.currentTimeMillis() - startTime);
      return Response.noContent().build();
    }
    catch (ApplicationException e)
    {
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 400, System.currentTimeMillis() - startTime);
      throw new BadRequestException(e.getMessage(), e);
    }
    catch (AuthorizationException e)
    {
      recordPut(request, uriInfo, securityContext.getUserPrincipal(), 403, System.currentTimeMillis() - startTime);
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      recordPut(request, uriInfo, securityContext.getUserPrincipal(), 500, System.currentTimeMillis() - startTime);
      throw new InternalServerErrorException(e);
    }
  }

  public Response patchResource(HttpServletRequest request, SecurityContext securityContext, UriInfo uriInfo, String collection, String id, JsonApiDataWrapper resource)
  {
    long startTime = System.currentTimeMillis();
    
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
      {
        recordPatch(request, uriInfo, securityContext.getUserPrincipal(), 401, System.currentTimeMillis() - startTime);
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());
      }
      
      // --- Business Logic ------------------------------------------------------------------------

      EntityObject entity = new EntityObject(id, resource.getData().camelizedAttributes(), decodeRelationships(resource), resource.getMeta());
      
      if(applicationDataService.patch(securityContext.getUserPrincipal(), collection, entity) != 1)
      {
        recordPatch(request, uriInfo, securityContext.getUserPrincipal(), 404, System.currentTimeMillis() - startTime);
        throw new NotFoundException();
      }
      
      recordPatch(request, uriInfo, securityContext.getUserPrincipal(), 200, System.currentTimeMillis() - startTime);
      return Response.noContent().build();
    }
    catch (ApplicationException e)
    {
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 400, System.currentTimeMillis() - startTime);
      throw new BadRequestException(e.getMessage(), e);
    }
    catch (AuthorizationException e)
    {
      recordPatch(request, uriInfo, securityContext.getUserPrincipal(), 403, System.currentTimeMillis() - startTime);
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      recordPatch(request, uriInfo, securityContext.getUserPrincipal(), 500, System.currentTimeMillis() - startTime);
      throw new InternalServerErrorException(e);
    }
  }

  public Response deleteResource(HttpServletRequest request, SecurityContext securityContext, UriInfo uriInfo, String collection, String id)
  {
    long startTime = System.currentTimeMillis();
    
    try
    {
      //--- Authentication ------------------------------------------------------------------------
      
      if(securityContext.getUserPrincipal() == null)
      {
        recordDelete(request, uriInfo, securityContext.getUserPrincipal(), 401, System.currentTimeMillis() - startTime);
        throw new NotAuthorizedException(authenticationHandler.getAuthenticationScheme());
      }
      
      // --- Business Logic ------------------------------------------------------------------------

      if(applicationDataService.delete(securityContext.getUserPrincipal(), collection, id) != 1)
      {
        recordDelete(request, uriInfo, securityContext.getUserPrincipal(), 404, System.currentTimeMillis() - startTime);
        throw new NotFoundException();
      }
      
      recordDelete(request, uriInfo, securityContext.getUserPrincipal(), 200, System.currentTimeMillis() - startTime);
      return Response.noContent().build();
    }
    catch (ApplicationException e)
    {
      recordPost(request, uriInfo, securityContext.getUserPrincipal(), 400, System.currentTimeMillis() - startTime);
      throw new BadRequestException(e.getMessage(), e);
    }
    catch (AuthorizationException e)
    {
      recordDelete(request, uriInfo, securityContext.getUserPrincipal(), 403, System.currentTimeMillis() - startTime);
      throw new ForbiddenException(e);    
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      recordDelete(request, uriInfo, securityContext.getUserPrincipal(), 500, System.currentTimeMillis() - startTime);
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
      
      try
      {
        relationships.add(new Relationship(entry.getKey(), relationshipData.get("data")));
      }
      catch (IllegalArgumentException e)
      {
        // IllegalArgumentException is thrown if the relationship does not have an ID.
        // We ignore this exception for the case where the UI doesn't have the ID yet.
        // It is up to the UI to re-save the entity when the ID is known.  The primary
        // use caese is a many-to-many relationship.
      }
    }

    return relationships;
  }
}
