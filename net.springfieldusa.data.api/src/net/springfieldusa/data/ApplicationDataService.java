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

package net.springfieldusa.data;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import org.osgi.annotation.versioning.ProviderType;

import net.springfieldusa.entity.EntityObject;
import net.springfieldusa.storage.DuplicateIdException;

@ProviderType
public interface ApplicationDataService
{
  <T extends EntityObject> T create(Principal principal, String collection, T data) throws ApplicationException, AuthorizationException, AuthorizationException, DuplicateIdException;
  
  EntityObject retrieve(Principal principal, String collection, String id) throws ApplicationException, AuthorizationException;
  EntityObject retrieve(Principal principal, String collection, String key, String value) throws ApplicationException, AuthorizationException;
  
  <T extends EntityObject> T retrieve(Principal principal, String collection, String id, Supplier<T> factory) throws ApplicationException, AuthorizationException;
  <T extends EntityObject> T retrieve(Principal principal, String collection, String key, String value, Supplier<T> factory) throws ApplicationException, AuthorizationException;
  
  Collection<EntityObject> find(Principal principal, String collection, Map<String, Object> query) throws ApplicationException, AuthorizationException;
  Collection<EntityObject> find(Principal principal, String collection, String query) throws ApplicationException, AuthorizationException;
  
  <T extends EntityObject> Collection<T> find(Principal principal, String collection, Map<String, Object> query, Supplier<T> factory) throws ApplicationException, AuthorizationException;
  <T extends EntityObject> Collection<T> find(Principal principal, String collection, String query, Supplier<T> factory) throws ApplicationException, AuthorizationException;
  
  Collection<EntityObject> find(Principal principal, String collection, Map<String, Object> query, int skip, int limit) throws ApplicationException, AuthorizationException;
  Collection<EntityObject> find(Principal principal, String collection, String query, int skip, int limit) throws ApplicationException, AuthorizationException;

  <T extends EntityObject> Collection<T> find(Principal principal, String collection, Map<String, Object> query, int skip, int limit, Supplier<T> factory) throws ApplicationException, AuthorizationException;
  <T extends EntityObject> Collection<T> find(Principal principal, String collection, String query, int skip, int limit, Supplier<T> factory) throws ApplicationException, AuthorizationException;

  <T extends EntityObject> long update(Principal principal, String collection, T data) throws ApplicationException, AuthorizationException;
  <T extends EntityObject> long update(Principal principal, String collection, String query, T data) throws ApplicationException, AuthorizationException;
  
  <T extends EntityObject> long patch(Principal principal, String collection, T data) throws ApplicationException, AuthorizationException;
  <T extends EntityObject> long patch(Principal principal, String collection, String query, T data) throws ApplicationException, AuthorizationException;

  long delete(Principal principal, String collection, String id) throws ApplicationException, AuthorizationException;
  long delete(Principal principal, String collection, String key, String value) throws ApplicationException, AuthorizationException;
}
