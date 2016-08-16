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

import java.util.Date;

import net.springfieldusa.entity.EntityObject;

/**
 * @author bhunt
 * 
 */
public class EncryptedCredential extends EntityObject
{
  public static final String KEY_USER_ID = "userId";
  public static final String KEY_PASSWORD = "password";
  public static final String KEY_SALT = "salt";
  public static final String KEY_UPDATED_ON = "updatedOn";
  
  /**
	 * Constructs an empty Credential instance.
	 */
	public EncryptedCredential()
	{
		super();
	}

	/**
	 * Constructs a Credential instance from an id and password.
	 * 
	 * @param userid
	 * @param password
	 */
	public EncryptedCredential(String userId, Object password, Object salt)
	{
	  put(KEY_USER_ID, userId);
	  put(KEY_PASSWORD, password);
	  put(KEY_SALT, salt);
	  put(KEY_UPDATED_ON, new Date());
	}

	/**
	 * Retrieves the user id portion of the credential.
	 * 
	 * @return the user id
	 */
	public String getUserId()
	{
		return get(KEY_USER_ID).toString();
	}

	/**
   * Changes the credential id field.
   * 
   * @param userId the user id to set
   */
  public void setUserId(String userId)
  {
    put(KEY_USER_ID, userId);
  }

  /**
   * Retrieves the password portion of the credential.
   * 
	 * @return the password
	 */
	public byte[] getPassword()
	{
		return getTypeConverter().toByteArray(get(KEY_PASSWORD));
	}

	/**
	 * Changes the credential password field.
	 * 
	 * @param password the password to set
	 */
	public void setPassword(byte[] password)
	{
	  put(KEY_PASSWORD, password);
	}
	
	public byte[] getSalt()
	{
	  return getTypeConverter().toByteArray(get(KEY_SALT));
	}
	
	public void setSalt(byte[] salt)
	{
	  put(KEY_SALT, salt);
	}
	
	public Date getUpdatedOn()
	{
	  return (Date) get(KEY_UPDATED_ON);
	}
	
	public void setUpdatedOn(Date updatedOn)
	{
	  put(KEY_UPDATED_ON, updatedOn);
	}
}
