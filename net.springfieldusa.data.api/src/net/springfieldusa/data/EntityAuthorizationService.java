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

import net.springfieldusa.entity.EntityObject;

public interface EntityAuthorizationService
{
  boolean isCreateAuthorizedFor(Principal principal, String collection, EntityObject object);
  boolean isRetrieveAuthorizedFor(Principal principal, String collection, EntityObject object);
  boolean isRetrieveAuthorizedFor(Principal principal, String collection, Collection<? extends EntityObject> objects);
  boolean isUpdateAuthorizedFor(Principal principal, String collection, EntityObject updatedObject, EntityObject storedObject);
  boolean isDeleteAuthorizedFor(Principal principal, String collection, EntityObject object);  
}
