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

package net.springfieldusa.registration.comp.junit.tests;

import static org.mockito.Mockito.mock;

import org.junit.Before;

import net.springfieldusa.password.EncryptionException;
import net.springfieldusa.registration.comp.UserRegistrationComponent;
import net.springfieldusa.storage.StorageService;

public class TestUserRegistrationComponent
{
  private UserRegistrationComponent userRegistrationComponent;
  private StorageService storageService;
  private String email;
  private String password;
  private byte[] encryptedPassword;
  private byte[] salt;

  @Before
  public void setUp() throws EncryptionException
  {
    email = "nobody@nowhere.org";
    password = "password";
    salt = new byte[] {0, 1, 2};
    encryptedPassword = "encryptedPassword".getBytes();
    storageService = mock(StorageService.class);
    
    userRegistrationComponent = new UserRegistrationComponent();
    userRegistrationComponent.bindStorageService(storageService);
  }
}
