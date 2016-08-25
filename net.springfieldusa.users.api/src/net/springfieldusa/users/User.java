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

package net.springfieldusa.users;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import net.springfieldusa.entity.EntityObject;

public class User extends EntityObject
{
  public static final String COLLECTION = "users";
  public static final String KEY_EMAIL = "email";
  public static final String KEY_FIRST_NAME = "firstName";
  public static final String KEY_LAST_NAME = "lastName";
  public static final String KEY_PASSWORD = "password";
  public static final String KEY_CREATED_ON = "createdOn";

  public User()
  {
    super();
  }
  
  public User(EntityObject wrappedObject)
  {
    super(wrappedObject);
  }
  
  public User(Map<String, Object> values)
  {
    super(values, Collections.emptyList());
  }
  
  public User(String id, Map<String, Object> values)
  {
    super(id, values, Collections.emptyList());
  }
  
  public User(String email)
  {
    if(email == null)
      throw new IllegalArgumentException("name cannot be null");
    
    put(KEY_EMAIL, email);
  }

  public String getEmail()
  {
    return get(KEY_EMAIL).toString();
  }
  
  public void setEmail(String email)
  {
    put(KEY_EMAIL, email);
  }
  
  public String getPassword()
  {
    return get(KEY_PASSWORD).toString();
  }
  
  public void setPassword(String password)
  {
    put(KEY_PASSWORD, password);
  }
  
  public String clearPassword()
  {
    return (String) getAttributes().remove(KEY_PASSWORD);
  }
  
  public Date getCreatedOn()
  {
    return (Date) get(KEY_CREATED_ON);
  }
  
  public void setCreatedOn(Date createdOn)
  {
    put(KEY_CREATED_ON, createdOn);
  }
}
