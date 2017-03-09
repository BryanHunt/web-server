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

package net.springfieldusa.web.ping;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.springfieldusa.web.WebResource;
import net.springfieldusa.web.WebResourceUsageLogService;

@Api(value = "ping")
@Path("/ping")
@Produces("text/plain")
@Component(service = PingResource.class)
public class PingResource extends WebResource
{
  @GET
  @ApiOperation(value = "Endpoint to check server health")
  public String ping(@Context HttpServletRequest request, @Context SecurityContext securityContext, @Context UriInfo uriInfo)
  {
    recordGet(request, uriInfo, securityContext.getUserPrincipal(), 200, 0);
    return "Hello";
  }

  @Override
  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void bindWebResourceUsageLogService(WebResourceUsageLogService webResourceUsageLogService)
  {
    super.bindWebResourceUsageLogService(webResourceUsageLogService);
  }

  @Override
  public void unbindWebResourceUsageLogService(WebResourceUsageLogService webResourceUsageLogService)
  {
    super.unbindWebResourceUsageLogService(webResourceUsageLogService);
  }
}
