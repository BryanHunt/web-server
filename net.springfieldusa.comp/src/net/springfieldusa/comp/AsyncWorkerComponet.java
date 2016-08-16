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

package net.springfieldusa.comp;

import java.util.Map;

import org.osgi.service.log.LogService;

public abstract class AsyncWorkerComponet extends AbstractComponent implements Runnable
{
  public static final String PROP_WORKER_STARTUP_DELAY = "worker.startupDelay";
  public static final String PROP_WORKER_NAME = "worker.name";
  
  private long startupDelay;
  private Thread thread;
  private volatile boolean done = false;

  public void activate(Map<String, Object> properties)
  {
    String workerName = (String) properties.get(PROP_WORKER_NAME);
    
    if(workerName == null)
      workerName = "Unnamed worker";
    
    Long startupDelayProp = (Long) properties.get(PROP_WORKER_STARTUP_DELAY);
    
    if(startupDelayProp != null)
      startupDelay = startupDelayProp.longValue();
    else
      startupDelay = 0;
    
    thread = new Thread(this, workerName);
    thread.start();
  }

  public void deactivate()
  {
    done = true;
    thread.interrupt();
  }

  @Override
  public void run()
  {
    log(LogService.LOG_INFO, "Worker [" + thread.getName() +  "] is starting");

    try
    {
      Thread.sleep(startupDelay);
    }
    catch (InterruptedException e)
    {}

    while (!done)
    {
      try
      {
        doWork();
      }
      catch(Exception e)
      {
        log(LogService.LOG_ERROR, "Worker [" + thread.getName() +  "] threw unexpected exception", e);
      }
    }

    log(LogService.LOG_INFO, "Worker [" + thread.getName() +  "] is terminating");
  }

  protected abstract void doWork();
}
