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

package net.springfieldusa.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityObject
{
  public static final String KEY_META_SECURITY = "security";

  private TypeConverter typeConverter;
  private Map<String, Object> attributes;
  private Collection<Relationship> relationships;

  private String id;
  private Map<String, Object> meta;
  private ObjectSecurity security;

  public EntityObject()
  {
    attributes = new HashMap<>();
    relationships = new ArrayList<>();
  }

  public EntityObject(EntityObject wrappedObject)
  {
    this.id = wrappedObject.id;
    this.attributes = wrappedObject.attributes;
    this.relationships = wrappedObject.relationships;
    setMeta(wrappedObject.meta);
  }

  public EntityObject(Map<String, Object> attributes, Collection<Relationship> relationships)
  {
    this(UUID.randomUUID().toString(), attributes, relationships);
  }

  public EntityObject(String id, Map<String, Object> attributes, Collection<Relationship> relationships)
  {
    this(id, attributes, relationships, null);
  }

  public EntityObject(String id, Map<String, Object> attributes, Collection<Relationship> relationships, Map<String, Object> meta)
  {
    this.attributes = new HashMap<>(attributes);
    this.relationships = new ArrayList<>(relationships);
    this.id = id;
    this.meta = meta;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    if (this.id != null)
      throw new IllegalStateException("An entity's ID cannot be changed");

    this.id = id;
  }

  public TypeConverter getTypeConverter()
  {
    return typeConverter;
  }

  public void setTypeConverter(TypeConverter typeConverter)
  {
    this.typeConverter = typeConverter;
  }

  public Map<String, Object> getAttributes()
  {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes)
  {
    this.attributes = new HashMap<>(attributes);
  }

  public Collection<Relationship> getRelationships()
  {
    return relationships;
  }

  public void setRelationships(Collection<Relationship> relationships)
  {
    this.relationships = new ArrayList<>(relationships);
  }

  public Map<String, Object> getMeta()
  {
    return meta;
  }
  
  public Object getMetaObject(String key)
  {
    return meta != null ? meta.get(key) : null;
  }

  public void putMetaObject(String key, Object value)
  {
    if(meta == null)
      meta = new HashMap<>();
    
    meta.put(key, value);
  }
  
  public void setMeta(Map<String, Object> meta)
  {
    this.meta = meta;
  }

  @SuppressWarnings("unchecked")
  public ObjectSecurity getSecurity()
  {
    if(security == null)
    {
      Map<String, Object> data = (Map<String, Object>) getMetaObject(KEY_META_SECURITY);
      
      if(data != null)
        security = new ObjectSecurity(data);
    }
    
    return security;
  }

  public void setSecurity(ObjectSecurity security)
  {
    putMetaObject(KEY_META_SECURITY, security);
  }

  @Override
  public boolean equals(Object object)
  {
    if (object != null && object instanceof EntityObject)
    {
      String targetId = ((EntityObject) object).id;

      if (id != null)
        return id.equals(targetId);

      if (targetId == null)
        return this == object;
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    if (id == null)
      throw new IllegalStateException("Entities with no ID cannot be hashed");

    return id.hashCode();
  }

  @Override
  public String toString()
  {
    return id;
  }

  protected Object get(String key)
  {
    return attributes.get(key);
  }

  protected void put(String key, Object value)
  {
    attributes.put(key, value);
  }
}
