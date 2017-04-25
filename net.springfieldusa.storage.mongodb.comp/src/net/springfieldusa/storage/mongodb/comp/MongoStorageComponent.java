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

package net.springfieldusa.storage.mongodb.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipselabs.emongo.MongoProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.combine;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.entity.ObjectReference;
import net.springfieldusa.entity.Relationship;
import net.springfieldusa.entity.TypeConverter;
import net.springfieldusa.storage.DuplicateIdException;
import net.springfieldusa.storage.StorageService;

@Component(service = StorageService.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MongoStorageComponent extends AbstractComponent implements StorageService
{
  private volatile MongoProvider mongoProvider;

  private static final String ID = "_id";
  private static final String META = "_meta";
  private static final String RELATIONSHIPS = "_relationships";
  private static TypeConverter typeConverter = new MongoTypeConverter();

  @Override
  public <T extends EntityObject> T create(String collection, T data) throws DuplicateIdException
  {
    if (data.getId() == null)
      data.setId(UUID.randomUUID().toString());

    Document dbObject = new Document(data.getAttributes());
    dbObject.put(ID, data.getId());
    dbObject.put(META, data.getMeta());
    dbObject.put(RELATIONSHIPS, createRelationships(data));

    log(LogService.LOG_DEBUG, "Adding object: '" + data.getId() + "'");

    try
    {
      getCollection(collection).insertOne(dbObject);
      return data;
    }
    catch (MongoWriteException e)
    {
      if (e.getCode() == 11000)
        throw new DuplicateIdException();

      throw e;
    }
  }

  @Override
  public EntityObject retrieve(String collection, String id)
  {
    return retrieve(collection, id, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> T retrieve(String collection, String id, Supplier<T> factory)
  {
    return retrieve(collection, ID, id, factory);
  }

  @Override
  public EntityObject retrieve(String collection, String key, String value)
  {
    return retrieve(collection, key, value, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> T retrieve(String collection, String key, String value, Supplier<T> factory)
  {
    Document result = getCollection(collection).find(new Document(key, value)).first();

    if (result == null)
      return null;

    return createObject(factory, result);
  }

  @Override
  public <T extends EntityObject> long update(String collection, T data)
  {
    MongoCollection<Document> mongoCollection = getCollection(collection);
    Document document = new Document(data.getAttributes());
    document.put(ID, data.getId());
    document.put(META, data.getMeta());
    document.put(RELATIONSHIPS, createRelationships(data));
    UpdateResult result = mongoCollection.replaceOne(eq(ID, data.getId()), document);
    return result.getMatchedCount();
  }

  @Override
  public <T extends EntityObject> long update(String collection, String query, T data)
  {
    Document jsonQuery = Document.parse(query);
    MongoCollection<Document> mongoCollection = getCollection(collection);
    Document document = new Document(data.getAttributes());
    document.put(ID, data.getId());
    document.put(META, data.getMeta());
    document.put(RELATIONSHIPS, createRelationships(data));
    UpdateResult result = mongoCollection.updateMany(jsonQuery, document);
    return result.getMatchedCount();
  }

  @Override
  public <T extends EntityObject> long patch(String collection, T data)
  {
    MongoCollection<Document> mongoCollection = getCollection(collection);
    List<Bson> updates = new ArrayList<>();

    if (data.getAttributes() != null)
    {
      for (Entry<String, Object> entry : data.getAttributes().entrySet())
        updates.add(set(entry.getKey(), entry.getValue()));
    }

    if (data.getMeta() != null)
    {
      for (Entry<String, Object> entry : data.getMeta().entrySet())
        updates.add(set(META + "." + entry.getKey(), entry.getValue()));
    }

    if(data.getRelationships() != null)
    {
      for (Relationship relationship : data.getRelationships())
      {
        if (relationship.isMany())
        {
          Collection<Document> dbReferences = new ArrayList<>();
          updates.add(set(RELATIONSHIPS + "." + relationship.getType(), dbReferences));
          
          relationship.getObjectReferences().forEach((reference) -> {
            dbReferences.add(createReference(reference));
          });
        }
        else
        {
          updates.add(set(RELATIONSHIPS + "." + relationship.getType(), createReference(relationship.getObjectReference())));
        }
      }
    }
    
    UpdateResult result = mongoCollection.updateOne(eq(ID, data.getId()), combine(updates));
    return result.getMatchedCount();
  }

  @Override
  public <T extends EntityObject> long patch(String collection, String query, T data)
  {
    return update(collection, query, data); // FIXME
  }

  @Override
  public long delete(String collection, String id)
  {
    DeleteResult result = getCollection(collection).deleteOne(eq(ID, id));
    return result.getDeletedCount();
  }

  @Override
  public long delete(String collection, String key, String value)
  {
    DeleteResult result = getCollection(collection).deleteMany(eq(key, value));
    return result.getDeletedCount();
  }

  @Override
  public Collection<EntityObject> find(String collection, Map<String, Object> query)
  {
    return find(collection, query, EntityObject::new);
  }

  @Override
  public Collection<EntityObject> find(String collection, Map<String, Object> query, int skip, int limit)
  {
    return find(collection, query, skip, limit, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> Collection<T> find(String collection, String query, Supplier<T> factory)
  {
    if (query == null)
      query = "{}";

    return find(collection, Document.parse(query), factory);
  }

  @Override
  public <T extends EntityObject> Collection<T> find(String collection, String query, int skip, int limit, Supplier<T> factory)
  {
    if (query == null)
      query = "{}";

    return find(collection, Document.parse(query), skip, limit, factory);
  }

  @Override
  public Collection<EntityObject> find(String collection, String query)
  {
    return find(collection, query, EntityObject::new);
  }

  @Override
  public Collection<EntityObject> find(String collection, String query, int skip, int limit)
  {
    return find(collection, query, skip, limit, EntityObject::new);
  }

  @Override
  public <T extends EntityObject> Collection<T> find(String collection, Map<String, Object> query, Supplier<T> factory)
  {
    if (query == null)
      query = Collections.emptyMap();

    return find(collection, new Document(query), 0, 0, factory);
  }

  @Override
  public <T extends EntityObject> Collection<T> find(String collection, Map<String, Object> query, int skip, int limit, Supplier<T> factory)
  {
    if (query == null)
      query = Collections.emptyMap();

    return find(collection, new Document(query), skip, limit, factory);
  }

  @Reference(unbind = "-")
  public void bindMongoProvider(MongoProvider mongoProvider)
  {
    this.mongoProvider = mongoProvider;
  }

  protected MongoCollection<Document> getCollection(String name)
  {
    return mongoProvider.getMongoDatabase().getCollection(name);
  }

  private <T extends EntityObject> Collection<T> find(String collection, Document query, int skip, int limit, Supplier<T> factory)
  {
    FindIterable<Document> cursor = getCollection(collection).find(query);

    if (skip > 0)
      cursor.skip(skip);

    if (limit > 0)
      cursor.limit(limit);

    Collection<T> items = new ArrayList<>();

    for (Document value : cursor)
      items.add(createObject(factory, value));

    return items;
  }

  private <T extends EntityObject> T createObject(Supplier<T> factory, Document result)
  {
    T object = factory.get();
    object.setId(result.remove(ID).toString());
    object.setTypeConverter(typeConverter);

    Document dbMeta = (Document) result.remove(META);
    object.setMeta(dbMeta);

    Document dbRelationships = (Document) result.remove(RELATIONSHIPS);
    Collection<Relationship> relationships = new ArrayList<>();

    if (dbRelationships != null)
    {
      for (Entry<String, Object> dbRelationship : dbRelationships.entrySet())
        relationships.add(new Relationship(dbRelationship.getKey(), dbRelationship.getValue()));
    }

    object.setRelationships(relationships);
    object.setAttributes(result);
    return object;
  }

  private <T extends EntityObject> Document createRelationships(T data)
  {
    Document dbRelationships = new Document();

    for (Relationship relationship : data.getRelationships())
      buildRelationship(dbRelationships, relationship);

    return dbRelationships;
  }

  private void buildRelationship(Document dbRelationships, Relationship relationship)
  {
    if (relationship.isMany())
    {
      Collection<Document> dbReferences = new ArrayList<>();
      dbRelationships.put(relationship.getType(), dbReferences);

      relationship.getObjectReferences().forEach((reference) -> {
        dbReferences.add(createReference(reference));
      });
    }
    else
    {
      dbRelationships.put(relationship.getType(), createReference(relationship.getObjectReference()));
    }
  }

  private Document createReference(ObjectReference reference)
  {
    return new Document("id", reference.getId()).append("type", reference.getType());
  }
}
