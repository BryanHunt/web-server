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

package net.springfieldusa.storage;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import net.springfieldusa.entity.EntityObject;

public interface StorageService
{
  <T extends EntityObject> T create(String collection, T data) throws DuplicateIdException;
  
  EntityObject retrieve(String collection, String id);
  EntityObject retrieve(String collection, String key, String value);
  
  <T extends EntityObject> T retrieve(String collection, String id, Supplier<T> factory);
  <T extends EntityObject> T retrieve(String collection, String key, String value, Supplier<T> factory);
  
  Collection<EntityObject> find(String collection, Map<String, Object> query);
  Collection<EntityObject> find(String collection, String query);
  
  <T extends EntityObject> Collection<T> find(String collection, Map<String, Object> query, Supplier<T> factory);
  <T extends EntityObject> Collection<T> find(String collection, String query, Supplier<T> factory);
  
  Collection<EntityObject> find(String collection, Map<String, Object> query, int skip, int limit);
  Collection<EntityObject> find(String collection, String query, int skip, int limit);

  <T extends EntityObject> Collection<T> find(String collection, Map<String, Object> query, int skip, int limit, Supplier<T> factory);
  <T extends EntityObject> Collection<T> find(String collection, String query, int skip, int limit, Supplier<T> factory);

  <T extends EntityObject> long update(String collection, T data);
  <T extends EntityObject> long update(String collection, String query, T data);
  
  <T extends EntityObject> long patch(String collection, T data);
  <T extends EntityObject> long patch(String collection, String query, T data);

  long delete(String collection, String id);
  long delete(String collection, String key, String value);
}
