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

import org.osgi.annotation.versioning.ProviderType;

import net.springfieldusa.entity.EntityObject;

@ProviderType
public interface DataProcessor
{
  <T extends EntityObject> T handleCreate(Principal principal, T data) throws ApplicationException;
  <T extends EntityObject> T handleRetrieve(Principal principal, T data) throws ApplicationException;
  <T extends EntityObject> Collection<T> handleRetrieve(Principal principal, Collection<T> data) throws ApplicationException;
  <T extends EntityObject> T handleUpdate(Principal principal, T data) throws ApplicationException;
  <T extends EntityObject> T handlePatch(Principal principal, T data) throws ApplicationException;
  <T extends EntityObject> void handleDelete(Principal principal, String id) throws ApplicationException;
  <T extends EntityObject> void handleDelete(Principal principal, String key, String value) throws ApplicationException;
}
