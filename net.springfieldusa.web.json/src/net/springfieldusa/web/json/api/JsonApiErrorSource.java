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

public class JsonApiErrorSource
{
  private String pointer;
  private String parameter;

  public String getPointer()
  {
    return pointer;
  }

  public void setPointer(String pointer)
  {
    this.pointer = pointer;
  }

  public String getParameter()
  {
    return parameter;
  }

  public void setParameter(String parameter)
  {
    this.parameter = parameter;
  }
}
