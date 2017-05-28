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

package net.springfieldusa.data.auth.comp;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ObjectSecurity
{
  public static final String KEY_OWNER = "owner";
  public static final String KEY_ADMIN_GROUPS = "adminGroups";
  public static final String KEY_READ_GROUPS = "readGroups";
  public static final String KEY_WRITE_GROUPS = "writeGroups";
  public static final String KEY_DELETE_GROUPS = "deleteGroups";

  private Map<String, Object> attributes;
  
  public ObjectSecurity()
  {
    attributes = new HashMap<>();
  }
  
  public ObjectSecurity(Map<String, Object> attributes)
  {
    this.attributes = attributes;
  }

  public void addReadGroup(String groupName)
  {
    getPermissionGroup(KEY_READ_GROUPS).add(groupName);
  }
  
  public void addWriteGroup(String groupName)
  {
    getPermissionGroup(KEY_WRITE_GROUPS).add(groupName);
  }
  
  public void addAdminGroup(String groupName)
  {
    getPermissionGroup(KEY_ADMIN_GROUPS).add(groupName);
  }
  
  public void addDeleteGroup(String groupName)
  {
    getPermissionGroup(KEY_DELETE_GROUPS).add(groupName);
  }

  public void removeReadGroup(String groupName)
  {
    getPermissionGroup(KEY_READ_GROUPS).remove(groupName);
  }
  
  public void removeWriteGroup(String groupName)
  {
    getPermissionGroup(KEY_WRITE_GROUPS).remove(groupName);
  }
  
  public void removeAdminGroup(String groupName)
  {
    getPermissionGroup(KEY_ADMIN_GROUPS).remove(groupName);
  }
  
  public void removeDeleteGroup(String groupName)
  {
    getPermissionGroup(KEY_DELETE_GROUPS).remove(groupName);    
  }
  
  public void setOwner(String owner)
  {
    put(KEY_OWNER, owner);
  }
  
  public boolean isOwner(String targetOwner)
  {
    if (targetOwner == null)
      throw new IllegalArgumentException("Target owner must not be null");

    return targetOwner.equals(get(KEY_OWNER));
  }

  public boolean isReadAllowedFor(Collection<String> groups)
  {
    return checkPermission(KEY_READ_GROUPS, groups);
  }

  public boolean isWriteAllowedFor(Collection<String> groups)
  {
    return checkPermission(KEY_WRITE_GROUPS, groups);
  }

  public boolean isDeleteAllowedFor(Collection<String> groups)
  {
    return checkPermission(KEY_DELETE_GROUPS, groups);
  }

  public boolean isAdminAllowedFor(Collection<String> groups)
  {
    return checkPermission(KEY_ADMIN_GROUPS, groups);
  }

  public Object get(String key)
  {
    return attributes.get(key);
  }
  
  public Object put(String key, Object value)
  {
    return attributes.put(key, value);
  }

  public Map<String, Object> getAttributes()
  {
    return attributes;
  }
  
  private boolean checkPermission(String groupKey, Collection<String> groups)
  {
    HashSet<String> permissionGroup = new HashSet<>(getPermissionGroup(groupKey));
    permissionGroup.retainAll(groups);
    return !permissionGroup.isEmpty();
  }
  
  @SuppressWarnings("unchecked")
  private Collection<String> getPermissionGroup(String groupKey)
  {
    Collection<String> permissionGroup = (Collection<String>) get(groupKey);
    
    if(permissionGroup == null)
    {
      permissionGroup = new HashSet<>();
      put(groupKey, permissionGroup);
    }
    
    return permissionGroup;    
  }
}
