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

package net.springfieldusa.web.security.form;

import java.security.Principal;
import java.util.Map;

import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;
import com.eclipsesource.jaxrs.provider.security.AuthorizationHandler;

import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;
import net.springfieldusa.session.SessionException;
import net.springfieldusa.session.SessionService;

@Component(service= {AuthenticationHandler.class, AuthorizationHandler.class})
public class SecurityHandler implements AuthenticationHandler, AuthorizationHandler
{
  private static final String SESSION_COOKIE = "session";
  
  private volatile SecurityService securityService;
  private volatile SessionService sessionService;
  
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
      Map<String, Cookie> cookies = requestContext.getCookies();
      Cookie sessionCookie = cookies.get(SESSION_COOKIE);
      
      if(sessionCookie == null)
        return null;
      
      Principal principal = sessionService.getPrincipal(sessionCookie.getValue());
      
      if(principal == null)
        return null;
        
      return principal;
    }
    catch (SessionException e)
    {
      throw new ServiceUnavailableException();
    }
  }

  @Override
  public String getAuthenticationScheme()
  {
    return "FORM";
  }
  
  @Reference(unbind="-")
  public void bindSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }
  
  @Reference(unbind="-")
  public void bindSessionService(SessionService sessionService)
  {
    this.sessionService = sessionService;
  }
}
