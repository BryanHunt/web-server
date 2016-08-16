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

public abstract class AsyncSeasonalWorkerComponet extends AsyncWorkerComponet
{
  public static final String PROP_WORKER_INTERVAL = "worker.interval";

  private long workInterval;

  public void activate(Map<String, Object> properties)
  {
    Long workIntervalProp = (Long) properties.get(PROP_WORKER_INTERVAL);

    if (workIntervalProp != null)
      workInterval = workIntervalProp.longValue();
    else
      workInterval = 30000;

    super.activate(properties);
  }

  @Override
  protected final void doWork()
  {
    doSeasonalWork();

    try
    {
      Thread.sleep(workInterval);
    }
    catch (InterruptedException e)
    {}
  }

  protected abstract void doSeasonalWork();
}
