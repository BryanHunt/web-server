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

package net.springfieldusa.registration;

/**
 * @author bhunt
 * 
 */
public interface UserRegistrationService
{
  String KEY_EMAIL = "email";
  String KEY_PASSWORD = "password";
  String KEY_ENCRYPTED_PASSWORD = "encryptedPassword";
  String KEY_SALT = "salt";
  String KEY_REGISTERED_ON = "registeredOn";
  
  void verifyUser(String id) throws RegistrationException;
}
