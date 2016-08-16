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

package net.springfieldusa.security.comp;

import java.security.Principal;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.credentials.CredentialException;
import net.springfieldusa.credentials.CredentialsService;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.groups.GroupException;
import net.springfieldusa.groups.GroupService;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

@Component(service = SecurityService.class)
public class SecurityComponent extends AbstractComponent implements SecurityService
{
  private volatile CredentialsService credentialsService;
  private volatile GroupService groupService;

  @Override
  public Principal authenticate(UnencryptedCredential credentials) throws SecurityException
  {
    log(LogService.LOG_DEBUG, "Authenticating user: '" + credentials.getUserId() + "'");

    try
    {
      return credentialsService.authenticate(credentials);
    }
    catch (CredentialException e)
    {
      log(LogService.LOG_ERROR, "Exception occured when attempting to authenticate user: '" + credentials.getUserId() + "'");
      throw new SecurityException(e);
    }
  }

  @Override
  public boolean authorizeForRole(Principal principal, String role) throws SecurityException
  {
    try
    {
      Set<String> users = groupService.getUsersInGroup(role);
      return users.contains(principal.getName());
    }
    catch (GroupException e)
    {
      log(LogService.LOG_ERROR, "Exception occured when attempting to authorize user: '" + principal.getName() + "' for role: '" + role + "'");
      throw new SecurityException(e);
    }
  }

  @Override
  public Set<String> getRoles(Principal principal) throws SecurityException
  {
    try
    {
      return groupService.getGroupsFor(principal.getName());
    }
    catch (GroupException e)
    {
      log(LogService.LOG_ERROR, "Exception occured when attempting to get roles for user: '" + principal.getName() + "'");
      throw new SecurityException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindCredentialService(CredentialsService credentialsService)
  {
    this.credentialsService = credentialsService;
  }

  @Reference(unbind = "-")
  public void bindGroupService(GroupService groupService)
  {
    this.groupService = groupService;
  }
}
