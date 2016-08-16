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

package net.springfieldusa.web.jwt;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Token
{
  private String user;
  private String token;
  
  public Token()
  {}

  public Token(String user, String token)
  {
    this.user = user;
    this.token = token;
  }

  public String getUser()
  {
    return user;
  }
  
  public String getToken()
  {
    return token;
  }

  public void setUser(String user)
  {
    this.user = user;
  }
  
  public void setToken(String token)
  {
    this.token = token;
  }
}
