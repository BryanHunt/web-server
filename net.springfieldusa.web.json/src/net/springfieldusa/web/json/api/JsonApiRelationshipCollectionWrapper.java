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

import java.util.Collection;

public class JsonApiRelationshipCollectionWrapper extends JsonApi
{
  private Collection<JsonApiRelationship> data;

  public JsonApiRelationshipCollectionWrapper() 
  {}
  
  public JsonApiRelationshipCollectionWrapper(Collection<JsonApiRelationship> data)
  {
    this.data = data;
  }
  
  public Collection<JsonApiRelationship> getData()
  {
    return data;
  }

  public void setData(Collection<JsonApiRelationship> data)
  {
    this.data = data;
  }
}
