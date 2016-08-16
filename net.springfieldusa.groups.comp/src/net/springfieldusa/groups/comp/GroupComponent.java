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

package net.springfieldusa.groups.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import net.springfieldusa.comp.AbstractComponent;
import net.springfieldusa.groups.Group;
import net.springfieldusa.groups.GroupException;
import net.springfieldusa.groups.GroupService;
import net.springfieldusa.storage.StorageService;

@Component(service = GroupService.class)
public class GroupComponent extends AbstractComponent implements GroupService
{
  private static final String GROUPS = "groups";

  private volatile StorageService storageService;

  @Activate
  public void activate() throws Exception
  {
    try
    {
      if (storageService.retrieve(GROUPS, Group.KEY_NAME, "admin", Group::new) == null)
      {
        Collection<String> admins = new ArrayList<>();
        admins.add("admin");
        Group group = new Group();
        group.setName("admin");
        group.setMembers(admins);
        storageService.create(GROUPS, group);
      }
    }
    catch (Exception e)
    {
      log(LogService.LOG_ERROR, "Failed to initialize group service", e);
      throw e;
    }
  }

  @Override
  public String addGroup(Group group) throws GroupException
  {
    try
    {
      return storageService.create(GROUPS, group).getId();
    }
    catch (Exception e)
    {
      throw new GroupException(e);
    }
  }

  @Override
  public Group getGroup(String groupId) throws GroupException
  {
    try
    {
      return storageService.retrieve(GROUPS, Group.KEY_NAME, groupId, Group::new);
    }
    catch (Exception e)
    {
      throw new GroupException(e);
    }
  }

  @Override
  public void removeGroup(String group) throws GroupException
  {
    try
    {
      storageService.delete(GROUPS, group);
    }
    catch (Exception e)
    {
      throw new GroupException(e);
    }
  }

  @Override
  public Set<String> getGroupsFor(String user) throws GroupException
  {
    try
    {
      Map<String, Object> query = new HashMap<>();
      query.put(Group.KEY_MEMBERS, user);
      Collection<Group> groups = storageService.find(GROUPS, query, Group::new);

      Set<String> groupNames = new HashSet<>();
      groups.forEach((group) ->
      {
        groupNames.addAll(group.getMembers());
      });

      return groupNames;
    }
    catch (Exception e)
    {
      throw new GroupException(e);
    }
  }

  @Override
  public Set<String> getUsersInGroup(String groupName) throws GroupException
  {
    try
    {
      Group group = storageService.retrieve(GROUPS, Group.KEY_NAME, groupName, Group::new);
      return new HashSet<String>(group.getMembers());
    }
    catch (Exception e)
    {
      throw new GroupException(e);
    }
  }

  @Reference(unbind = "-")
  public void bindStorageService(StorageService storageService)
  {
    this.storageService = storageService;
  }
}
