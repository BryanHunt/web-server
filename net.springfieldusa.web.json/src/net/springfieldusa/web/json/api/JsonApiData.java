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

package net.springfieldusa.web.json.api;

import java.util.Map;

public class JsonApiData
{
  private Object id;
  private String type;
  private Map<String, Object> attributes;
  private Map<String, Object> relationships;
  private Map<String, Object> meta;

  public JsonApiData() {}
  
  public JsonApiData(Object id, String type, Map<String, Object> camelizedAttributes, Map<String, Object> camelizedRelationships, Map<String, Object> meta)
  {
    this.id = id;
    this.type = type;
    this.attributes = JsonApi.dasherize(camelizedAttributes);
    this.relationships = JsonApi.dasherize(camelizedRelationships);
    this.meta = meta;
  }

  public Object getId()
  {
    return id;
  }

  public void setId(Object id)
  {
    this.id = id;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public Map<String, Object> getMeta()
  {
    return meta;
  }
  
  public void setMeta(Map<String, Object> meta)
  {
    this.meta = meta;
  }
  
  public Map<String, Object> getAttributes()
  {
    return attributes;
  }
  
  public Map<String, Object> camelizedAttributes()
  {
    return JsonApi.camelize(attributes);
  }

  public void setAttributes(Map<String, Object> dasherizedAttributes)
  {
    this.attributes = dasherizedAttributes;
  }

  public Map<String, Object> getRelationships()
  {
    return relationships;
  }
 
  public Map<String, Object> camelizedRelationships()
  {
    return JsonApi.camelize(relationships);
  }

  public void setRelationships(Map<String, Object> dasherizedRelationships)
  {
    this.relationships = dasherizedRelationships;
  }  
}
