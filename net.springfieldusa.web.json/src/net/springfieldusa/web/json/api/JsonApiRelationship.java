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

public class JsonApiRelationship
{
  private String type;
  private String id;
  
  public JsonApiRelationship()
  {}
  
  @SuppressWarnings("unchecked")
  public JsonApiRelationship(Object mapping)
  {
    if(!(mapping instanceof Map))
      throw new IllegalStateException();
    
    Map<String, String> data = (Map<String, String>) mapping;
    this.id = data.get("id");
    this.type = data.get("type");
  }
  
  public JsonApiRelationship(String id, String type)
  {
    this.id = id;
    this.type = type;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

}
