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

package net.springfieldusa.registration.comp;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.credentials.EncryptedCredential;
import net.springfieldusa.registration.RegistrationException;
import net.springfieldusa.registration.UserRegistrationService;
import net.springfieldusa.storage.DuplicateIdException;
import net.springfieldusa.storage.StorageService;
import net.springfieldusa.users.User;

/**
 * @author bhunt
 * 
 */
@Component(service = UserRegistrationService.class)
public class UserRegistrationComponent extends AbstractComponent implements UserRegistrationService
{
  private static final String REGISTRATIONS = "registrations";
  private static final String CREDENTIALS = "credentials";
  private static final String USERS = "users";

  private volatile StorageService storageService;

  @Override
  public void verifyUser(String id) throws RegistrationException
  {
    User user = storageService.retrieve(REGISTRATIONS, id, User::new);
    
    EncryptedCredential credential = new EncryptedCredential(user.getEmail(), user.getAttributes().get(UserRegistrationService.KEY_ENCRYPTED_PASSWORD), user.getAttributes().get(UserRegistrationService.KEY_SALT));
    try
    {
      storageService.create(CREDENTIALS, credential);
      storageService.create(USERS, sanitize(user));
      storageService.delete(REGISTRATIONS, id);
    }
    catch (DuplicateIdException e)
    {
      throw new RegistrationException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindStorageService(StorageService storageService)
  {
    this.storageService = storageService;
  }
  
  private User sanitize(User user)
  {
    user.getAttributes().remove(UserRegistrationService.KEY_REGISTERED_ON);
    user.getAttributes().remove(UserRegistrationService.KEY_ENCRYPTED_PASSWORD);
    user.getAttributes().remove(UserRegistrationService.KEY_SALT);
    return user;
  }
}
