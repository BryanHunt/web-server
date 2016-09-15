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

package net.springfieldusa.jwt;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

import net.springfieldusa.credentials.UnencryptedCredential;

public interface TokenService
{
  String createToken(ContainerRequestContext context, HttpServletRequest request, UnencryptedCredential credentials) throws TokenException;
  Map<String, Object> verifyToken(String token) throws TokenException;
}
