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

public interface CredentialsService
{
  void addCredential(UnencryptedCredential credential) throws CredentialException;
  void addCredential(EncryptedCredential credential) throws CredentialException;
  void updateCredential(UnencryptedCredential credential) throws CredentialException;
  void updateCredential(EncryptedCredential credential) throws CredentialException;
  void removeCredential(String userId) throws CredentialException;
  
  Principal authenticate(UnencryptedCredential credential) throws CredentialException;
}
