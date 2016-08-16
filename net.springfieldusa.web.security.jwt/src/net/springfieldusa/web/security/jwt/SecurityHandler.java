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

package net.springfieldusa.web.security.jwt;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;

import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.container.ContainerRequestContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;
import com.eclipsesource.jaxrs.provider.security.AuthorizationHandler;

import net.springfieldusa.credentials.AuthenticatedUser;
import net.springfieldusa.jwt.TokenException;
import net.springfieldusa.jwt.TokenService;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;

@Component(service = { AuthenticationHandler.class, AuthorizationHandler.class })
public class SecurityHandler implements AuthenticationHandler, AuthorizationHandler
{
  private volatile SecurityService securityService;
  private volatile TokenService tokenService;

  @Override
  public boolean isUserInRole(Principal user, String role)
  {
    AuthenticatedUser authenticatedUser = (AuthenticatedUser) user;

    if (authenticatedUser.isUserInRole(role))
      return true;

    try
    {
      return securityService.authorizeForRole(user, role);
    }
    catch (SecurityException e)
    {
      throw new ServiceUnavailableException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Principal authenticate(ContainerRequestContext requestContext)
  {
    String authHeader = requestContext.getHeaderString("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer "))
      return null;

    try
    {
      Map<String, Object> claims = tokenService.verifyToken(authHeader.substring(7));

      if (claims == null || claims.isEmpty())
        return null;

      return new AuthenticatedUser((String) claims.get("userId"), (Collection<String>) claims.get("roles"));
    }
    catch (TokenException e)
    {
      throw new ServiceUnavailableException();
    }
  }

  @Override
  public String getAuthenticationScheme()
  {
    return "Bearer";
  }

  @Reference(unbind = "-")
  public void bindSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }

  @Reference(unbind = "-")
  public void bindTokenService(TokenService tokenService)
  {
    this.tokenService = tokenService;
  }
}
