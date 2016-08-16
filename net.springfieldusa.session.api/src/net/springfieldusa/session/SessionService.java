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

package net.springfieldusa.session;

import java.security.Principal;

import net.springfieldusa.credentials.UnencryptedCredential;

public interface SessionService {
  Principal getPrincipal(String sessionToken) throws SessionException;
  String createSessionToken(UnencryptedCredential credential) throws SessionException;
  void expireSessionToken(String sessionToken) throws SessionException;
}
