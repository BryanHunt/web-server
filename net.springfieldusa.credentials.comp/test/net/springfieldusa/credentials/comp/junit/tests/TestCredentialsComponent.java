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

package net.springfieldusa.credentials.comp.junit.tests;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import net.springfieldusa.credentials.CredentialException;
import net.springfieldusa.credentials.EncryptedCredential;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.credentials.comp.CredentialsComponent;
import net.springfieldusa.entity.TypeConverter;
import net.springfieldusa.password.EncryptionException;
import net.springfieldusa.password.PasswordService;
import net.springfieldusa.storage.DuplicateIdException;
import net.springfieldusa.storage.StorageService;

public class TestCredentialsComponent
{
  private CredentialsComponent credentialsComponent;
  private PasswordService passwordService;
  private StorageService storageService;
  private TypeConverter typeConverter;
  private String email;
  private String password;
  private byte[] encryptedPassword;
  private byte[] salt;
  
  @Before
  public void setUp()
  {
    email = "nobody@nowhere.org";
    password = "password";
    salt = new byte[] {0, 1, 2};
    encryptedPassword = "encryptedPassword".getBytes();
    passwordService = mock(PasswordService.class);
    storageService = mock(StorageService.class);
    typeConverter = mock(TypeConverter.class);
        
    credentialsComponent = new CredentialsComponent();
    credentialsComponent.bindStorageService(storageService);
    credentialsComponent.bindPasswordService(passwordService);    
  }
  
  @Test
  public void testAddUnencryptedCredential() throws EncryptionException, CredentialException, DuplicateIdException
  {
    UnencryptedCredential credential = new UnencryptedCredential(email, password);
    
    when(passwordService.createSalt()).thenReturn(salt);
    when(passwordService.encryptPassword(password, salt)).thenReturn(encryptedPassword);
    
    credentialsComponent.addCredential(credential);
    
    ArgumentCaptor<EncryptedCredential> argument = ArgumentCaptor.forClass(EncryptedCredential.class);
    verify(storageService).create(eq("credentials"), argument.capture());
    argument.getValue().setTypeConverter(typeConverter);
    when(typeConverter.toByteArray(eq(encryptedPassword))).thenReturn(encryptedPassword);
    assertThat(argument.getValue().getPassword(), is(encryptedPassword));
  }
  
  @Test
  public void testAddEncryptedCredential() throws EncryptionException, CredentialException, DuplicateIdException
  {
    EncryptedCredential credential = new EncryptedCredential(email, encryptedPassword, salt);
    credential.setTypeConverter(typeConverter);
    when(typeConverter.toByteArray(eq(encryptedPassword))).thenReturn(encryptedPassword);
    credentialsComponent.addCredential(credential);
    
    ArgumentCaptor<EncryptedCredential> argument = ArgumentCaptor.forClass(EncryptedCredential.class);
    verify(storageService).create(eq("credentials"), argument.capture());
    assertThat(argument.getValue().getPassword(), is(encryptedPassword));
  }

  @Test
  public void testAuthenticateGoodPassword() throws EncryptionException, CredentialException
  {
    EncryptedCredential credential = new EncryptedCredential(email, encryptedPassword, salt);
    credential.setTypeConverter(typeConverter);
    when(typeConverter.toByteArray(eq(encryptedPassword))).thenReturn(encryptedPassword);
    when(typeConverter.toByteArray(eq(salt))).thenReturn(salt);
    
    when(storageService.retrieve(eq("credentials"), eq("userId"), eq(email), any())).thenReturn(credential);
    when(passwordService.validatePassword(password, encryptedPassword, salt)).thenReturn(Boolean.TRUE);
    
    Principal principal = credentialsComponent.authenticate(new UnencryptedCredential(email, password));
    
    assertThat(principal, is(notNullValue()));
    assertThat(principal.getName(), is(email));
  }
  
  @Test
  public void testAuthenticateBadPassword() throws EncryptionException, CredentialException
  {
    EncryptedCredential credential = new EncryptedCredential(email, encryptedPassword, salt);
    credential.setTypeConverter(typeConverter);
    when(typeConverter.toByteArray(eq(encryptedPassword))).thenReturn(encryptedPassword);
    when(typeConverter.toByteArray(eq(salt))).thenReturn(salt);
    
    when(storageService.retrieve(eq("credentials"), eq("userId"), eq(email), any())).thenReturn(credential);
    when(passwordService.validatePassword(password, encryptedPassword, salt)).thenReturn(Boolean.FALSE);
    
    Principal principal = credentialsComponent.authenticate(new UnencryptedCredential(email, password));
    
    assertThat(principal, is(nullValue()));
  }
}