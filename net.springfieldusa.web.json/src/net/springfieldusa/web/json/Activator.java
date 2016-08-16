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

package net.springfieldusa.web.json;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator
{
  @SuppressWarnings("rawtypes")
  private ServiceRegistration registration;

  @Override
  public void start( BundleContext context ) throws Exception {
    JacksonFeature feature = new JacksonFeature();
    registration = context.registerService( JacksonFeature.class.getName(), feature, null );
  }

  @Override
  public void stop( BundleContext context ) throws Exception {
    if( registration != null ) {
      registration.unregister();
    }
  }
}
