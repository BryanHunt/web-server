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

package net.springfieldusa.web.security.basic;

import java.security.Principal;
import java.util.Base64;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.container.ContainerRequestContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;
import com.eclipsesource.jaxrs.provider.security.AuthorizationHandler;

import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

@Component(service= {AuthenticationHandler.class, AuthorizationHandler.class})
public class SecurityHandler implements AuthenticationHandler, AuthorizationHandler
{
  private volatile SecurityService securityService;
  
  @Override
  public boolean isUserInRole(Principal user, String role)
  {
    try
    {
      return securityService.authorizeForRole(user, role);
    }
    catch (SecurityException e)
    {
      throw new ServiceUnavailableException();
    }
  }

  @Override
  public Principal authenticate(ContainerRequestContext requestContext)
  {
    try
    {
      String authHeader = requestContext.getHeaderString("Authorization");
      
      if(authHeader == null || !authHeader.startsWith("Basic "))
        throw new NotAuthorizedException("Basic");
      
      String[] credentials = new String(Base64.getDecoder().decode(authHeader.substring(6))).split(":");
      
      if(credentials.length != 2 || credentials[0].isEmpty() || credentials[1].isEmpty())
        throw new NotAuthorizedException("Basic");
      
      Principal principal = securityService.authenticate(new UnencryptedCredential(credentials[0], credentials[1]));
      
      if(principal == null)
        throw new NotAuthorizedException("Basic");
      
      return principal;
    }
    catch (SecurityException e)
    {
      throw new ServiceUnavailableException();
    }
  }

  @Override
  public String getAuthenticationScheme()
  {
    return "BASIC";
  }
  
  @Reference(unbind="-")
  public void bindSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }
}
