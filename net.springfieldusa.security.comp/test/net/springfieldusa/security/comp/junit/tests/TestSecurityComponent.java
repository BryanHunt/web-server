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

package net.springfieldusa.security.comp.junit.tests;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import net.springfieldusa.credentials.CredentialException;
import net.springfieldusa.credentials.CredentialsService;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.groups.GroupException;
import net.springfieldusa.groups.GroupService;
import net.springfieldusa.password.EncryptionException;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.comp.SecurityComponent;

public class TestSecurityComponent
{
  private String email;
  private String password;
  private Principal principal;
  private CredentialsService credentialsService;
  private GroupService groupService;
  private SecurityComponent securityComponent;
  
  @Before
  public void setUp() throws EncryptionException
  {
    securityComponent = new SecurityComponent();
    
    principal = mock(Principal.class);
    credentialsService = mock(CredentialsService.class);
    groupService = mock(GroupService.class);
    securityComponent.bindCredentialService(credentialsService);
    securityComponent.bindGroupService(groupService);
    
    when(principal.getName()).thenReturn("junit");
  }
  
  @Test
  public void testAuthenticateWithValidCredentials() throws SecurityException, CredentialException
  {
    when(credentialsService.authenticate(any(UnencryptedCredential.class))).thenReturn(principal);
    assertThat(securityComponent.authenticate(new UnencryptedCredential(email, password)), is(principal));
  }
  
  @Test
  public void testAuthenticateWithInvalidCredentials() throws SecurityException, CredentialException
  {
    when(credentialsService.authenticate(any(UnencryptedCredential.class))).thenReturn(null);
    assertThat(securityComponent.authenticate(new UnencryptedCredential(email, password)), is(nullValue()));
  }

  @Test(expected = SecurityException.class)
  public void testAuthenticateWithCredentialException() throws SecurityException, CredentialException
  {
    when(credentialsService.authenticate(any(UnencryptedCredential.class))).thenThrow(new CredentialException());
    securityComponent.authenticate(new UnencryptedCredential(email, password));
  }
  
  @Test
  public void testAuthorizeForRole() throws SecurityException, GroupException
  {
    Set<String> users = new HashSet<>();
    users.add("junit");
    when(groupService.getUsersInGroup("admin")).thenReturn(users);
    when(groupService.getUsersInGroup("root")).thenReturn(Collections.emptySet());
    
    assertTrue(securityComponent.authorizeForRole(principal, "admin"));
    assertFalse(securityComponent.authorizeForRole(principal, "root"));
  }
  
  @Test(expected = SecurityException.class)
  public void testAuthorizeForRoleWithGroupException() throws SecurityException, GroupException
  {
    when(groupService.getUsersInGroup("admin")).thenThrow(new GroupException());
    
    securityComponent.authorizeForRole(principal, "admin");
  }

  @Test
  public void testGetRoles() throws SecurityException, GroupException
  {
    Set<String> groups = new HashSet<>();
    
    when(groupService.getGroupsFor("junit")).thenReturn(groups);
    assertThat(securityComponent.getRoles(principal), is(groups));
  }

  @Test(expected = SecurityException.class)
  public void testGetRolesWithGroupException() throws SecurityException, GroupException
  {
    when(groupService.getGroupsFor("junit")).thenThrow(new GroupException());
    securityComponent.getRoles(principal);
  }
}
