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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JsonApi
{
  private Map<String, Object> meta;

  public Map<String, Object> getMeta()
  {
    return meta;
  }

  public void setMeta(Map<String, Object> meta)
  {
    this.meta = meta;
  }
  
  public static String dasherize(String camelized)
  {
    return camelized.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
  }
  
  public static String camelize(String dasherized)
  {
    String[] elements = dasherized.split("-");
    StringBuilder buffer = new StringBuilder(elements[0]);
    
    for(int i = 1; i < elements.length; i++)
    {
      char[] characters = elements[i].toCharArray();
      characters[0] = Character.toUpperCase(characters[0]);
      buffer.append(characters);
    }
    
    return buffer.toString();
  }
  
  public static <T> Map<String, T> dasherize(Map<String, T> camelized)
  {
    Map<String, T> dasherized = new HashMap<>(camelized.size());
    
    for(Entry<String, T> entry : camelized.entrySet())
      dasherized.put(dasherize(entry.getKey()), entry.getValue());
    
    return dasherized;
  }  

  public static <T> Map<String, T> camelize(Map<String, T> dasherized)
  {
    if(dasherized == null)
      return Collections.emptyMap();
    
    Map<String, T> camelized = new HashMap<>(dasherized.size());
    
    for(Entry<String, T> entry : dasherized.entrySet())
      camelized.put(camelize(entry.getKey()), entry.getValue());
    
    return camelized;
  }  
}
