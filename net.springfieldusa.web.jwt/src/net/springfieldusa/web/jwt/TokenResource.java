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

package net.springfieldusa.web.jwt;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.jwt.TokenService;
import net.springfieldusa.web.WebResource;

@Path("/auth")
@Api(value = "auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component(service = TokenResource.class)
public class TokenResource extends WebResource
{
  private volatile TokenService tokenService;

  @POST
  @ApiOperation(value = "Create an authentication token")
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Credentials were invalid") })
  public Token createToken(UnencryptedCredential credentials)
  {
    try
    {
      String token = tokenService.createToken(credentials);

      if (token == null)
        throw new NotAuthorizedException("Token");

      return new Token(credentials.getUserId(), token);
    }
    catch (WebApplicationException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindTokenService(TokenService tokenService, Map<String, Object> properties)
  {
    this.tokenService = tokenService;
  }
}
