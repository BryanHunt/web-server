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

package net.springfieldusa.password;

/**
 * @author bhunt
 * 
 */
public interface PasswordService
{
	byte[] createSalt() throws EncryptionException;

	byte[] encryptPassword(String password, byte[] salt) throws EncryptionException;

	boolean validatePassword(String password, byte[] targetPassword, byte[] salt) throws EncryptionException;
}