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

package net.springfieldusa.password.comp.junit.tests;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;

import net.springfieldusa.password.EncryptionException;
import net.springfieldusa.password.comp.PasswordComponent;

import org.junit.Before;
import org.junit.Test;

public class TestPasswordComponent
{
  private PasswordComponent passwordComponent;
  
  @Before
  public void setUp() throws NoSuchAlgorithmException
  {
    passwordComponent = new PasswordComponent();
    passwordComponent.activate();
  }
  
  @Test
  public void testCreateSalt() throws EncryptionException
  {
    byte[] salt = passwordComponent.createSalt();
    assertThat(salt.length, is(8));
    
    // Verify that calling salt again creates a unique value
    
    assertThat(passwordComponent.createSalt(), is(not(salt)));
  }
  
  @Test
  public void testEncryptPassword() throws EncryptionException
  {
    String password = "password";
    byte[] salt = { 1 };
    byte[] encryptedPassword = passwordComponent.encryptPassword(password, salt);

    // Verify that the encrypted password is actually encrypted
    
    assertThat(encryptedPassword, is(not(password.getBytes())));
    
    // Verify that encrypt password is idempotent
    
    assertThat(passwordComponent.encryptPassword(password, salt), is(encryptedPassword));
    
    // Verify that the encrypted password is different if we change the salt
    
    salt[0] = 2;
    assertThat(passwordComponent.encryptPassword(password, salt), is(not(encryptedPassword)));
    
    // Verify that a different password encrypts to a different value
    
    assertThat(passwordComponent.encryptPassword("letmein", salt), is(not(encryptedPassword)));
  }
  
  @Test
  public void testValidatePassword() throws EncryptionException
  {
    String password = "password";
    byte[] salt = { 1 };
    byte[] encryptedPassword = passwordComponent.encryptPassword(password, salt);

    assertTrue(passwordComponent.validatePassword(password, encryptedPassword, salt));
    assertFalse(passwordComponent.validatePassword("letmein", encryptedPassword, salt));
  }
}
