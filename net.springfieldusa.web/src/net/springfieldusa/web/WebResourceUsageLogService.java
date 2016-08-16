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

import javax.ws.rs.core.UriInfo;

public interface WebResourceUsageLogService
{
  void recordPost(UriInfo uri, Principal user);
  void recordGet(UriInfo uri, Principal user);
  void recordPut(UriInfo uri, Principal user);
  void recordPatch(UriInfo uri, Principal user);
  void recordDelete(UriInfo uri, Principal user);
}
