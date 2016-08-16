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
import java.util.Map;

public class Relationship
{
  private String type;
  private boolean many;
  private ObjectReference objectReference;
  private Collection<ObjectReference> objectReferences;
  
  @SuppressWarnings({ "unchecked" })
  public Relationship(String type, Object relationship)
  {
    this.type = type;
    
    if(relationship instanceof Collection)
    {
      many = true;
      objectReferences = new ArrayList<>();
      Collection<Object> items = (Collection<Object>) relationship;
      
      items.forEach((item) -> {
        objectReferences.add(createReference(item));        
      });
    }
    else if(relationship instanceof Map)
    {
      many = false;
      objectReference = createReference(relationship);
    }
    else
    {
      throw new IllegalArgumentException("The relationship must be a Collection<Map> or a Map");
    }
  }

  public Relationship(String type, ObjectReference reference)
  {
    many = false;
    this.type = type;
    this.objectReference = reference;
  }
  
  public Relationship(String type, Collection<ObjectReference> references)
  {
    many = true;
    this.type = type;
    this.objectReferences = new ArrayList<>(references);
  }

  public String getType()
  {
    return type;
  }

  public boolean isMany()
  {
    return many;
  }

  public ObjectReference getObjectReference()
  {
    if(many)
      throw new IllegalStateException("The reference is not singular");
    
    return objectReference;
  }

  public Collection<ObjectReference> getObjectReferences()
  {
    if(!many)
      throw new IllegalStateException("The reference is singular");
    
    return objectReferences;
  }

  @SuppressWarnings("rawtypes")
  private ObjectReference createReference(Object item)
  {
    if(!(item instanceof Map))
      throw new IllegalArgumentException("Expected a collection of references, but one or more items in the collection is not of type Map");
    
    Map data = (Map) item;
    String id = (String) data.get("id");
    String referenceType = (String) data.get("type");
    
    if(id == null || referenceType == null)
      throw new IllegalArgumentException("The reference must contain both an id and type");
    
    return new ObjectReference(id, referenceType);
  }
}
