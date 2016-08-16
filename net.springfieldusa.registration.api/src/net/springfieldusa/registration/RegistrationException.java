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

package net.springfieldusa.registration;

public class RegistrationException extends Exception
{
  private static final long serialVersionUID = 8754976822232183915L;

  public RegistrationException()
  {}

  public RegistrationException(String message)
  {
    super(message);
  }

  public RegistrationException(Throwable cause)
  {
    super(cause);
  }

  public RegistrationException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public RegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
