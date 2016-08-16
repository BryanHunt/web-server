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

package net.springfieldusa.groups;

import java.util.Set;

public interface GroupService
{
  String addGroup(Group group) throws GroupException;
  Group getGroup(String groupId) throws GroupException;
  void removeGroup(String groupId) throws GroupException;
  
  Set<String> getUsersInGroup(String groupName) throws GroupException;
  Set<String> getGroupsFor(String user) throws GroupException;
}
