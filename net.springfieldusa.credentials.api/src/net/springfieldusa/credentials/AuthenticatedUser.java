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

package net.springfieldusa.credentials;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AuthenticatedUser implements Principal
{
  private String userId;
  private Set<String> roles;

  public AuthenticatedUser(String userId)
  {
    this(userId, null);
  }

  public AuthenticatedUser(String userId, Collection<String> roles)
  {
    this.userId = userId;
    this.roles = roles != null ? new HashSet<>(roles) : Collections.emptySet();
  }

  @Override
  public String getName()
  {
    return userId;
  }

  public boolean isUserInRole(String role)
  {
    return roles.contains(role);
  }
}
