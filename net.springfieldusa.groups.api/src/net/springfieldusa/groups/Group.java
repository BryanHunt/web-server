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

package net.springfieldusa.groups;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.springfieldusa.entity.EntityObject;

public class Group extends EntityObject
{
  public static final String KEY_NAME = "name";
  public static final String KEY_MEMBERS = "members";

  public Group()
  {
    super();
  }

  public Group(Map<String, Object> values)
  {
    super(values, Collections.emptyList());
  }

  public Group(String id, Map<String, Object> values)
  {
    super(id, values, Collections.emptyList());
  }

  public String getName()
  {
    return get(KEY_NAME).toString();
  }
  
  public void setName(String name)
  {
    put(KEY_NAME, name);
  }
  
  @SuppressWarnings("unchecked")
  public Collection<String> getMembers()
  {
    return (Collection<String>) get(KEY_MEMBERS);
  }
  
  public void setMembers(Collection<String> members)
  {
    put(KEY_MEMBERS, members);
  }
}
