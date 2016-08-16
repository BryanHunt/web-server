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

package net.springfieldusa.groups.comp.junit.tests;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import net.springfieldusa.groups.comp.GroupComponent;
import net.springfieldusa.storage.StorageService;

public class TestGroupComponent
{
  private GroupComponent groupsComponent;
  private StorageService storageService;
  @Before
  public void setUp()
  {
    storageService = mock(StorageService.class);
    
    groupsComponent = new GroupComponent();
    groupsComponent.bindStorageService(storageService);
  }
  
  @Test
  public void testAddGroup()
  {
  }

  @Test
  public void testGetGroup()
  {
  }

  @Test
  public void testRemoveGroup()
  {
  }

  @Test
  public void testGetGroupsfor()
  {
//    fail("Not implemented");
  }

  @Test
  public void testGetUsersInGroup()
  {
  }
}
