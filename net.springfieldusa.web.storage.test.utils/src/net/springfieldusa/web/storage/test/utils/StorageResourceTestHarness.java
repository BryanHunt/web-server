package net.springfieldusa.web.storage.test.utils;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.junit.BeforeClass;

import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.entity.ObjectReference;
import net.springfieldusa.entity.Relationship;
import net.springfieldusa.web.json.api.JsonApiData;
import net.springfieldusa.web.json.api.JsonApiDataWrapper;
import net.springfieldusa.web.json.api.JsonApiRelationship;
import net.springfieldusa.web.json.api.JsonApiRelationshipCollectionWrapper;
import net.springfieldusa.web.json.api.JsonApiRelationshipWrapper;

public abstract class StorageResourceTestHarness
{
  private static Client client;
  private static String port;
  private String apiRoot = "/api";

  @BeforeClass
  public static void globalSetup()
  {
    ClientConfig config = new ClientConfig();
    config.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
    client = ClientBuilder.newClient(config);
    port = System.getProperty("org.osgi.service.http.port", "8080");
  }
  
  @SuppressWarnings("unchecked")
  protected String getJsonWebToken(String userId, String password)
  {
    WebTarget authTarget = buildTarget("/auth");

    Map<String, Object> body = new HashMap<>();
    body.put("userId", userId);
    body.put("password", password);

    Response response = authTarget.request().post(Entity.json(body));

    try
    {
      assertThat(response.getStatus(), is(200));

      Map<String, Object> data = response.readEntity(Map.class);
      assertThat(data, is(notNullValue()));

      String token = (String) data.get("token");
      assertThat(token, not(isEmptyOrNullString()));

      return token;
    }
    finally
    {
      response.close();
    }
  }

  protected abstract String getAuthorizationHeader();

  protected String getBaseUri()
  {
    return "http://localhost:" + port;
  }

  protected Builder authenticate(Builder builder)
  {
    return builder.header("Authorization", getAuthorizationHeader());
  }

  protected String buildUri(String path)
  {
    return getBaseUri() + apiRoot + path;
  }

  protected WebTarget buildTarget(String path)
  {
    return client.target(buildUri(path));
  }

  protected Builder buildRequest(WebTarget target)
  {
    return authenticate(target.request());
  }

  protected EntityObject createEntity(Map<String, Object> attributes)
  {
    return createEntity(attributes, Collections.emptyList());
  }

  protected EntityObject createEntity(Map<String, Object> attributes, Collection<Relationship> relationships)
  {
    return new EntityObject(attributes, relationships);
  }

  protected EntityObject createEntity(String id, Map<String, Object> attributes, Collection<Relationship> relationships, Map<String, Object> meta)
  {
    return new EntityObject(id, attributes, relationships, meta);
  }

  protected EntityObject createDocument(String collection, EntityObject entity)
  {
    WebTarget target = buildTarget("/storage/" + collection);
    JsonApiDataWrapper data = new JsonApiDataWrapper(new JsonApiData(entity.getId(), collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));

    Response response = buildRequest(target).post(Entity.entity(data, "application/vnd.api+json"));

    try
    {
      assertThat(response.getStatus(), isOneOf(201, 401, 409));

      JsonApiDataWrapper responseData = response.readEntity(JsonApiDataWrapper.class);
      EntityObject document = new EntityObject((String) responseData.getData().getId(), responseData.getData().camelizedAttributes(), decodeRelationships(responseData), responseData.getMeta());
      assertThat((String) entity.getId(), not(isEmptyOrNullString()));
      
      return document;
    }
    finally
    {
      response.close();
    }
  }

  protected EntityObject getDocument(String collection, String id)
  {
    WebTarget target = buildTarget("/storage/" + collection + "/" + id);
    Response response = buildRequest(target).accept("application/vnd.api+json").get();
    
    try
    {
      assertThat(response.getStatus(), isOneOf(200, 401, 404));
      
      if(response.getStatus() == 401)
        throw new NotAuthorizedException(response);
      
      if(response.getStatus() == 404)
        throw new NotFoundException(response);

      JsonApiDataWrapper responseData = response.readEntity(JsonApiDataWrapper.class);
      return new EntityObject((String) responseData.getData().getId(), responseData.getData().camelizedAttributes(), decodeRelationships(responseData), responseData.getMeta());
    }
    finally
    {
      response.close();
    }
  }
  
  protected void updateDocument(String collection, EntityObject entity)
  {
    WebTarget target = buildTarget("/storage/" + collection + "/" + entity.getId());
    JsonApiDataWrapper data = new JsonApiDataWrapper(new JsonApiData(entity.getId(), collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));
    Response response = buildRequest(target).put(Entity.entity(data, "application/vnd.api+json"));
    
    try
    {
      assertThat(response.getStatus(), isOneOf(204, 401, 404));

      if(response.getStatus() == 401)
        throw new NotAuthorizedException(response);
      
      if(response.getStatus() == 404)
        throw new NotFoundException(response);
    }
    finally
    {
      response.close();
    }
  }

  protected void patchDocument(String collection, EntityObject entity)
  {
    WebTarget target = buildTarget("/storage/" + collection + "/" + entity.getId());
    JsonApiDataWrapper data = new JsonApiDataWrapper(new JsonApiData(entity.getId(), collection, entity.getAttributes(), encodeRelationships(entity), entity.getMeta()));
    Response response = buildRequest(target).method("PATCH", Entity.entity(data, "application/vnd.api+json"));
    
    try
    {
      assertThat(response.getStatus(), isOneOf(204, 401, 404));
      
      if(response.getStatus() == 401)
        throw new NotAuthorizedException(response);
      
      if(response.getStatus() == 404)
        throw new NotFoundException(response);
    }
    finally
    {
      response.close();
    }
  }

  protected void deleteDocument(String collection, String id)
  {
    WebTarget target = buildTarget("/storage/" + collection + "/" + id);
    Response response = buildRequest(target).delete();
    
    try
    {
      assertThat(response.getStatus(), isOneOf(204, 401, 404));

      if(response.getStatus() == 401)
        throw new NotAuthorizedException(response);
      
      if(response.getStatus() == 404)
        throw new NotFoundException(response);
    }
    finally
    {
      response.close();
    }    
  }
  
  private Map<String, Object> encodeRelationships(EntityObject storedResource) // TODO refactor with
                                                                               // code from
                                                                               // StorageResource
                                                                               // into a utility
                                                                               // class
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