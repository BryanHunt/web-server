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

package net.springfieldusa.security;

import java.security.Principal;
import java.util.Set;

import net.springfieldusa.credentials.UnencryptedCredential;

public interface SecurityService
{
  Principal authenticate(UnencryptedCredential credentials) throws SecurityException;
  boolean authorizeForRole(Principal principal, String role) throws SecurityException;
  Set<String> getRoles(Principal principal) throws SecurityException;
}
