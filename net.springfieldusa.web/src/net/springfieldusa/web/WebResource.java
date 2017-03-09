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

package net.springfieldusa.web;

import java.security.Principal;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import net.springfieldusa.comp.AbstractComponent;

public abstract class WebResource extends AbstractComponent
{
  private AtomicReference<WebResourceUsageLogService> usageLogServiceReference = new AtomicReference<>();

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  public void bindWebResourceUsageLogService(WebResourceUsageLogService webResourceUsageLogService)
  {
    usageLogServiceReference.set(webResourceUsageLogService);
  }

  public void unbindWebResourceUsageLogService(WebResourceUsageLogService webResourceUsageLogService)
  {
    usageLogServiceReference.compareAndSet(webResourceUsageLogService, null);
  }

  protected void recordPost(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordPost(request, uri, user, responseCode, processTime);
  }
  
  protected void recordGet(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordGet(request, uri, user, responseCode, processTime);    
  }
  
  protected void recordPut(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordPut(request, uri, user, responseCode, processTime);
  }
  
  protected void recordPatch(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordPatch(request, uri, user, responseCode, processTime);
  }
  
  protected void recordDelete(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordDelete(request, uri, user, responseCode, processTime);
  }
}
