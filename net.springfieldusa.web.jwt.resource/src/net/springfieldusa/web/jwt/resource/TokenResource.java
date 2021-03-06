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

package net.springfieldusa.web.jwt.resource;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.springfieldusa.credentials.UnencryptedCredential;
import net.springfieldusa.jwt.TokenService;
import net.springfieldusa.security.SecurityService;
import net.springfieldusa.web.WebResource;
import net.springfieldusa.web.jwt.ClaimsProvider;

@Path("/auth")
@Api(value = "auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Component(service = TokenResource.class)
public class TokenResource extends WebResource
{
  private volatile SecurityService securityService;
  private volatile TokenService tokenService;
  private Set<ClaimsProvider> claimsProviders = new CopyOnWriteArraySet<>();

  @POST
  @ApiOperation(value = "Create an authentication token")
  @ApiResponses(value = { @ApiResponse(code = 401, message = "Credentials were invalid") })
  public Token createToken(@Context ContainerRequestContext context, @Context HttpServletRequest request, UnencryptedCredential credentials)
  {
    try
    {
      Principal principal = securityService.authenticate(credentials);
      
      if (principal == null)
        throw new NotAuthorizedException("Token");

      Map<String, Object> claims = new HashMap<>();
      
      for(ClaimsProvider claimsProvider : claimsProviders)
        claimsProvider.addClaims(claims, context, principal);
      
      return new Token(credentials.getUserId(), tokenService.createToken(principal, claims));
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
  public void bindSecurityService(SecurityService securityService)
  {
    this.securityService = securityService;
  }
  
  @Reference(unbind = "-")
  public void bindTokenService(TokenService tokenService, Map<String, Object> properties)
  {
    this.tokenService = tokenService;
  }
  
  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void bindClaimsProvider(ClaimsProvider claimsProvider)
  {
    this.claimsProviders.add(claimsProvider);
  }
  
  public void unbindClaimsProvider(ClaimsProvider claimsProvider)
  {
    this.claimsProviders.remove(claimsProvider);
  }
}
