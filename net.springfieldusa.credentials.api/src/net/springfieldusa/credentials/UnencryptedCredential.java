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

package net.springfieldusa.credentials;

/**
 * @author bhunt
 * 
 */
public class UnencryptedCredential
{
  private String userId;
  private String password;

  /**
   * Constructs an empty Credential instance.
   */
  public UnencryptedCredential()
  {
    super();
  }

  /**
   * Constructs a Credential instance from an id and password.
   * 
   * @param userid
   * @param password
   */
  public UnencryptedCredential(String userId, String password)
  {
    this.userId = userId;
    this.password = password;
  }

  /**
   * Retrieves the user id portion of the credential.
   * 
   * @return the user id
   */
  public String getUserId()
  {
    return userId;
  }

  /**
   * Retrieves the password portion of the credential.
   * 
   * @return the password
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Changes the credential id field.
   * 
   * @param userId
   *          the user id to set
   */
  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  /**
   * Changes the credential password field.
   * 
   * @param password
   *          the password to set
   */
  public void setPassword(String password)
  {
    this.password = password;
  }
}
