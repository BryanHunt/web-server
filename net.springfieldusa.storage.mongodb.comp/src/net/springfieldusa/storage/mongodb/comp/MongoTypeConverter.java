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

package net.springfieldusa.storage.mongodb.comp;

import org.bson.types.Binary;

import net.springfieldusa.entity.TypeConverter;

public class MongoTypeConverter implements TypeConverter
{
  @Override
  public byte[] toByteArray(Object data)
  {
    return ((Binary) data).getData();
  }
}
