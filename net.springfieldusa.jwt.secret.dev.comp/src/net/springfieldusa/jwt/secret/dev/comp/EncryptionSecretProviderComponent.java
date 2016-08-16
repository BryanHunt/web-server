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

package net.springfieldusa.jwt.secret.dev.comp;

import org.osgi.service.component.annotations.*;

import net.springfieldusa.jwt.EncryptionSecretProvider;

@Component(service = EncryptionSecretProvider.class)
public class EncryptionSecretProviderComponent implements EncryptionSecretProvider
{
  @Override
  public String getSecret()
  {
    return "secret";
  }
}
