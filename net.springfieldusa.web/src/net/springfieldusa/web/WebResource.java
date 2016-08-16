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

  protected void recordPost(UriInfo uri, Principal user)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordPost(uri, user);
  }
  
  protected void recordGet(UriInfo uri, Principal user)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordGet(uri, user);    
  }
  
  protected void recordPut(UriInfo uri, Principal user)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordPut(uri, user);
  }
  
  protected void recordPatch(UriInfo uri, Principal user)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordPatch(uri, user);
  }
  
  protected void recordDelete(UriInfo uri, Principal user)
  {
    WebResourceUsageLogService usageLogService = usageLogServiceReference.get();
    
    if(usageLogService == null)
      return;
    
    usageLogService.recordDelete(uri, user);
  }
}
