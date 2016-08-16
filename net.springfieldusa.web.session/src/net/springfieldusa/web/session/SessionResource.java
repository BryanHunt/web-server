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

package net.springfieldusa.web.session;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.session.SessionService;
import net.springfieldusa.web.WebResource;

@Path("/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component(service = SessionResource.class)
public class SessionResource extends WebResource
{
  private static final String SESSION_COOKIE = "session";

  private volatile SessionService sessionService;

  @POST
  public Response createSession(UnencryptedCredential credential)
  {
    try
    {
      String sessionToken = sessionService.createSessionToken(credential);

      if (sessionToken == null)
        throw new NotAuthorizedException("Form");

      URI location = new URI(sessionToken);
      return Response.created(location).cookie(new NewCookie(SESSION_COOKIE, sessionToken)).build();
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException();
    }
  }

  @DELETE
  public Response expireSession(@CookieParam(SESSION_COOKIE) String sessionToken)
  {
    try
    {
      sessionService.expireSessionToken(sessionToken);
      return Response.ok().cookie(new NewCookie(SESSION_COOKIE, "")).build();
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException();
    }
  }

  @Reference(unbind = "-")
  public void bindSessionService(SessionService sessionService)
  {
    this.sessionService = sessionService;
  }
}
