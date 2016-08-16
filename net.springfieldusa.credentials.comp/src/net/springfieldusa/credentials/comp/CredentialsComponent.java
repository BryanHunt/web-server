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

package net.springfieldusa.credentials.comp;

import java.security.Principal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.credentials.AuthenticatedUser;
import net.springfieldusa.credentials.CredentialException;
import net.springfieldusa.credentials.CredentialsService;
import net.springfieldusa.credentials.EncryptedCredential;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.entity.ObjectSecurity;
import net.springfieldusa.password.EncryptionException;
import net.springfieldusa.password.PasswordService;
import net.springfieldusa.storage.StorageService;

/**
 * @author bhunt
 * 
 */
@Component(service = CredentialsService.class)
public class CredentialsComponent extends AbstractComponent implements CredentialsService
{
  private static final String CREDENTIALS = "credentials";
  private static final String ADMIN_ID = "admin";
  private static final String ADMIN_DEFAULT_PASSWORD = "admin";

  private volatile PasswordService passwordService;
  private volatile StorageService storageService;

  @Activate
  public void activate() throws CredentialException
  {
    try
    {
      if (storageService.retrieve(CREDENTIALS, EncryptedCredential.KEY_USER_ID, ADMIN_ID, EncryptedCredential::new) == null)
        addCredential(new UnencryptedCredential(ADMIN_ID, ADMIN_DEFAULT_PASSWORD));
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Failed to initialize credential service", e);
      throw new CredentialException(e);
    }
  }

  @Override
  public void addCredential(UnencryptedCredential credential) throws CredentialException
  {
    // TODO : See if the user is already registered or has a registration
    // pending

    try
    {
      byte[] salt = passwordService.createSalt();
      addCredential(new EncryptedCredential(credential.getUserId(), passwordService.encryptPassword(credential.getPassword(), salt), salt));
    }
    catch (EncryptionException e)
    {
      log(LogService.LOG_ERROR, "Failed to add credential", e);
      throw new CredentialException(e);
    }
  }

  @Override
  public void addCredential(EncryptedCredential encryptedCredential) throws CredentialException
  {
    try
    {
      ObjectSecurity security = new ObjectSecurity();
      security.setOwner(encryptedCredential.getUserId());
      encryptedCredential.setSecurity(security);
      storageService.create(CREDENTIALS, encryptedCredential);
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Failed to add credential", e);
      throw new CredentialException(e);
    }
  }

  @Override
  public void updateCredential(UnencryptedCredential credential) throws CredentialException
  {
    // TODO : See if the user is already registered or has a registration
    // pending

    try
    {
      byte[] salt = passwordService.createSalt();
      updateCredential(new EncryptedCredential(credential.getUserId(), passwordService.encryptPassword(credential.getPassword(), salt), salt));
    }
    catch (EncryptionException e)
    {
      log(LogService.LOG_ERROR, "Failed to update credential", e);
      throw new CredentialException(e);
    }
  }

  @Override
  public void updateCredential(EncryptedCredential encryptedCredential) throws CredentialException
  {
    try
    {
      storageService.update(CREDENTIALS, encryptedCredential);
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Failed to update credential", e);
      throw new CredentialException(e);
    }
  }

  @Override
  public void removeCredential(String userId) throws CredentialException
  {
    try
    {
      storageService.delete(CREDENTIALS, EncryptedCredential.KEY_USER_ID, userId);
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Failed to remove credential", e);
      throw new CredentialException(e);
    }
  }

  @Override
  public Principal authenticate(UnencryptedCredential credential) throws CredentialException
  {
    try
    {
      EncryptedCredential encryptedCredential = storageService.retrieve(CREDENTIALS, EncryptedCredential.KEY_USER_ID, credential.getUserId(), EncryptedCredential::new);

      if (encryptedCredential == null)
        return null;

      if (passwordService.validatePassword(credential.getPassword(), encryptedCredential.getPassword(), encryptedCredential.getSalt()))
        return new AuthenticatedUser(credential.getUserId());

      return null;
    }
    catch (EncryptionException e)
    {
      log(LogService.LOG_ERROR, "Exception during authentication", e);
      throw new CredentialException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindPasswordService(PasswordService passwordService)
  {
    this.passwordService = passwordService;
  }

  @Reference(unbind = "-")
  public void bindStorageService(StorageService storageService)
  {
    this.storageService = storageService;
  }
}
