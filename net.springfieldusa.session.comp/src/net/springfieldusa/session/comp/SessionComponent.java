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

package net.springfieldusa.session.comp;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.security.SecurityException;
import net.springfieldusa.security.SecurityService;
import net.springfieldusa.session.SessionException;
import net.springfieldusa.session.SessionService;

@Component(service = SessionService.class)
public class SessionComponent extends AbstractComponent implements SessionService
{
  // TODO: add a timestamp to the session token so that they can be
  // expired after a configured timeout
  
  private Map<String, Principal> sessionsByToken = new ConcurrentHashMap<>();
  private Map<String, String> sessionsByUser = new ConcurrentHashMap<>();
  private volatile SecurityService securityService;
  
  @Override
  public Principal getPrincipal(String sessionToken) throws SessionException
  {
    return sessionsByToken.get(sessionToken);
  }

  @Override
  public String createSessionToken(UnencryptedCredential credential) throws SessionException
  {
    // TODO: check for a race condition when a request for a new token
    // is received for the same user while a previous request is still pending
    
    if(credential == null)
      return null;
    
    try
    {
      Principal principal = securityService.authenticate(credential);
      
      if(principal == null)
        return null;
        
      String sessionToken = UUID.randomUUID().toString();
      String previousSessionToken = sessionsByUser.put(credential.getUserId(), sessionToken);
      
      if(previousSessionToken != null)
        sessionsByToken.remove(previousSessionToken);

      sessionsByToken.put(sessionToken, principal);
      log(LogService.LOG_DEBUG, "Created session token: '" + sessionToken + "' for user: '" + credential.getUserId() + "'");    
      return sessionToken;
    }
    catch (SecurityException e)
    {
      log(LogService.LOG_ERROR, "Failed to create session token", e);
      throw new SessionException(e);
    }
  }

  @Override
  public void expireSessionToken(String sessionToken) throws SessionException
  {
    log(LogService.LOG_DEBUG, "Expiring session token: '" + sessionToken + "'");
    Principal principal = sessionsByToken.remove(sessionToken);
    
    if(principal != null)
    {
      log(LogService.LOG_DEBUG, "Session token expired for user: '" + principal.getName() + "'");
      sessionsByUser.remove(principal.getName());
    }
  }
  
  @Reference(unbind = "-")
  public void bindSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }
}
