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

import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

/**
 * @author bhunt
 * 
 */
public abstract class AbstractComponent
{
	private AtomicReference<LogService> logServiceReference = new AtomicReference<>();

	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	public void bindLogService(LogService logService)
	{
		logServiceReference.set(logService);
	}

	public void unbindLogService(LogService logService)
	{
		logServiceReference.compareAndSet(logService, null);
	}

	protected void log(int level, String message)
	{
		log(level, message, null);
	}

	protected void log(int level, String message, Exception exception)
	{
		LogService logService = logServiceReference.get();

		if (logService != null)
			logService.log(level, message, exception);
	}
}
