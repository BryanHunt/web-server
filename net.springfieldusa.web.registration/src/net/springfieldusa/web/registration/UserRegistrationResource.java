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

package net.springfieldusa.web.registration;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.springfieldusa.registration.RegistrationException;
import net.springfieldusa.registration.UserRegistrationService;
import net.springfieldusa.web.WebResource;

/**
 * @author bhunt
 * 
 */
@Path("/registrations")
@Api(value = "registration")
@Component(service = UserRegistrationResource.class)
public class UserRegistrationResource extends WebResource
{
  private volatile UserRegistrationService userRegistrationService;

  @GET
  @Path("/{id}")
  @ApiOperation(value = "Verify a registration")
  public Response verifyUser(@ApiParam(value = "id of the registration", required = true) @PathParam("id") String id)
  {
    try
    {
      userRegistrationService.verifyUser(id);
      return Response.noContent().build();
    }
    catch (RegistrationException e)
    {
      log(LogService.LOG_ERROR, "Failed to verify user", e);
      throw new BadRequestException("Failed to verify user");
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Unexpected exception", e);
      throw new InternalServerErrorException("Unexpected server exception", e);
    }
  }

  @Reference(unbind = "-")
  public void bindUserRegistrationService(UserRegistrationService userRegistrationService)
  {
    this.userRegistrationService = userRegistrationService;
  }
}
