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

package net.springfieldusa.web.documents;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.springfieldusa.data.ApplicationDataService;
import net.springfieldusa.web.json.api.JsonApiDataCollectionWrapper;
import net.springfieldusa.web.json.api.JsonApiDataWrapper;
import net.springfieldusa.web.storage.PATCH;
import net.springfieldusa.web.storage.StorageResource;

@Api(value = "storage")
@Path("/storage/{collection}")
@Consumes("application/vnd.api+json")
@Produces("application/vnd.api+json")
@Component(service = DocumentResource.class)
public class DocumentResource extends StorageResource
{
  @POST
  @ApiOperation(value = "Persist a new document to the database", notes = "The document id is optional.  If it is supplied, it must unique.  If it is not supplied, the server will generate one.")
  @ApiResponses(value = { @ApiResponse(code = 201, message = "Document was persisted to the database", reference = "#/definitions/JsonApiDataWrapper"), 
                          @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 409, message = "A document with the supplied id already exists in the database"),
                          @ApiResponse(code = 403, message = "You are not authorized to create the document") })
  public Response createResource(@Context HttpServletRequest request, @Context UriInfo uriInfo, @Context SecurityContext securityContext,
      @ApiParam(value = "the database collection in which to store the document", required = true) @PathParam("collection") String collection,
      @ApiParam(value = "the document to store", required = true) JsonApiDataWrapper resource)
  {
    return super.createResource(request, uriInfo, securityContext, collection, resource);
  }

  @GET
  @ApiOperation(value = "Query and retrieve documents from the database", notes = "The skip and offset parameters have the same function and only one needs to be specified if you want to skip documents.  If both skip and offset are specified, skip has precedence.")
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to retrieve the documents") })  
  public JsonApiDataCollectionWrapper retrieveResources(@Context HttpServletRequest request, @Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection to query for the documents", required = true) @PathParam("collection") String collection,
      @ApiParam(value = "the number of documents to skip", required = false) @QueryParam("skip") Integer skip, 
      @ApiParam(value = "the number of documents to skip", required = false) @QueryParam("offset") Integer offset, 
      @ApiParam(value = "the maximum number of documents to return", required = false) @QueryParam("limit") Integer limit, 
      @ApiParam(value = "the native database query", required = false) @QueryParam("filter") String filter)
  {
    return super.retrieveResources(request, securityContext, uriInfo, collection, skip, offset, limit, filter);
  }

  @GET
  @Path("{id}")
  @ApiOperation(value = "Retrieve the specified document from the database")
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to retrieve the document"), 
                          @ApiResponse(code = 404, message = "The document with the specified id was not found in the database") })  
  public JsonApiDataWrapper retrieveResource(@Context HttpServletRequest request, @Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection from wich to retrieve the document", required = true) @PathParam("collection") String collection,
      @ApiParam(value = "the document id", required = true) @PathParam("id") String id)
  {
    return super.retrieveResource(request, securityContext, uriInfo, collection, id);
  }

  @PUT
  @Path("{id}")
  @ApiOperation(value = "Replace a document in the database")
  @ApiResponses(value = { @ApiResponse(code = 204, message = "Update was successful"),
                          @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to update the document"), 
                          @ApiResponse(code = 404, message = "The document with the specified id was not found in the database") })  
  public Response updateResource(@Context HttpServletRequest request, @Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection containing the document to update", required = true) @PathParam("collection") String collection,
      @ApiParam(value = "the document id", required = true) @PathParam("id") String id, 
      @ApiParam(value = "the document to store", required = true) JsonApiDataWrapper resource)
  {
    return super.updateResource(request, securityContext, uriInfo, collection, id, resource);
  }

  @PATCH
  @Path("{id}")
  @ApiOperation(httpMethod = "PATCH", value = "Patch a document in the database", notes = "Only the document attributes specified in the payload are modified in the database. Any new attributes will be added to the document in the database.  Document attributes may not be removed using PATCH.")
  @ApiResponses(value = { @ApiResponse(code = 204, message = "Patch was successful"),
                          @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to update the document"), 
                          @ApiResponse(code = 404, message = "The document with the specified id was not found in the database") })  
  public Response patchResource(@Context HttpServletRequest request, @Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection containing the document to update") @PathParam("collection") String collection,
      @ApiParam(value = "the document id", required = true) @PathParam("id") String id, 
      @ApiParam(value = "the document attributes to update", required = true) JsonApiDataWrapper resource)
  {
    return super.patchResource(request, securityContext, uriInfo, collection, id, resource);
  }

  @DELETE
  @Path("{id}")
  @ApiOperation(value = "Delete a document from the database")
  @ApiResponses(value = { @ApiResponse(code = 204, message = "Delete was successful"),
                          @ApiResponse(code = 401, message = "Authentication is required"),
                          @ApiResponse(code = 403, message = "You are not authorized to delete the document"), 
                          @ApiResponse(code = 404, message = "The document with the specified id was not found in the database") })  
  public Response deleteResource(@Context HttpServletRequest request, @Context SecurityContext securityContext, @Context UriInfo uriInfo, 
      @ApiParam(value = "the database collection containing the document to delete") @PathParam("collection") String collection,
      @ApiParam(value = "the document id", required = true) @PathParam("id") String id)
  {
    return super.deleteResource(request, securityContext, uriInfo, collection, id);
  }

  @Reference(unbind = "-", target = "(secure=true)")
  public void bindApplicationDataService(ApplicationDataService applicationDataService)
  {
    super.bindApplicationDataService(applicationDataService);
  }

  @Reference(unbind = "-")
  public void bindAuthenticationHandler(AuthenticationHandler authenticationHandler)
  {
    super.bindAuthenticationHandler(authenticationHandler);
  }
}
