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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriInfo;

public interface WebResourceUsageLogService
{
  void recordPost(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime);
  void recordGet(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime);
  void recordPut(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime);
  void recordPatch(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime);
  void recordDelete(HttpServletRequest request, UriInfo uri, Principal user, int responseCode, long processTime);
}
