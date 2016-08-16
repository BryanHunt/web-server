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

package net.springfieldusa.web.json.api;

import java.util.Collection;

public class JsonApiErrorsWrapper extends JsonApi
{
  private Collection<JsonApiError> errors;

  public Collection<JsonApiError> getErros()
  {
    return errors;
  }

  public void setErrors(Collection<JsonApiError> errors)
  {
    this.errors = errors;
  }
}
