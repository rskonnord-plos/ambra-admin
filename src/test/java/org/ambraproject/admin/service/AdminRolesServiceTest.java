/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.admin.service;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.admin.views.UserRoleView;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for the adminRolesService
 *
 * @author Joe Osowski
 */
public class AdminRolesServiceTest extends AdminBaseTest {

  @Autowired
  protected AdminRolesService adminRolesService;

  @DataProvider(name = "userProfileAndRoles1")
  public Object[][] roleSet1()
  {
    UserRole ur1 = new UserRole();
    ur1.setRoleName("Role1");

    dummyDataStore.store(ur1);

    UserRole ur2 = new UserRole();
    ur2.setRoleName("Role2");

    dummyDataStore.store(ur2);

    HashSet<UserRole> roles = new HashSet<UserRole>();

    roles.add(ur1);
    roles.add(ur2);

    UserProfile up = new UserProfile();
    up.setDisplayName("Test User");
    up.setRoles(roles);

    dummyDataStore.store(up);

    return new Object[][] {
      { up, roles }
    };
  }

  @Test(dataProvider = "userProfileAndRoles1")
  public void testAssignRole(UserProfile up, HashSet<UserRole> roles)
  {
    UserProfile resultUp = (UserProfile)dummyDataStore.get(UserProfile.class, up.getID());

    Set<UserRole> resultRows = resultUp.getRoles();

    assertEquals(resultRows.size(), 2);

    UserRole ur3 = new UserRole();
    ur3.setRoleName("Role3");

    dummyDataStore.store(ur3);

    assertNotNull(ur3.getID());

    adminRolesService.grantRole(up.getID(), ur3.getID());

    resultUp = (UserProfile)dummyDataStore.get(UserProfile.class, up.getID());

    resultRows = resultUp.getRoles();

    assertEquals(resultRows.size(), 3);
    assertTrue(resultRows.contains(ur3));
  }

  @DataProvider(name = "userProfileAndRoles2")
  public Object[][] roleSet2()
  {
    UserRole ur1 = new UserRole();
    ur1.setRoleName("Role3");

    dummyDataStore.store(ur1);

    UserRole ur2 = new UserRole();
    ur2.setRoleName("Role4");

    dummyDataStore.store(ur2);

    HashSet<UserRole> roles = new HashSet<UserRole>();

    roles.add(ur1);
    roles.add(ur2);

    UserProfile up = new UserProfile();
    up.setDisplayName("Test User2");
    up.setRoles(roles);

    dummyDataStore.store(up);

    return new Object[][] {
      { up, roles }
    };
  }

  @Test(dataProvider = "userProfileAndRoles2")
  public void testRemoveRoles(UserProfile up, HashSet<UserRole> roles)
  {
    assertEquals(up.getRoles().size(), 2);

    adminRolesService.revokeAllRoles(up.getID());

    UserProfile resultUp = (UserProfile)dummyDataStore.get(UserProfile.class, up.getID());

    Set<UserRole> resultRows = resultUp.getRoles();

    assertEquals(resultRows.size(), 0);
  }

  @DataProvider(name = "userRoles")
  public Object[][] roles()
  {
    UserProfile up = new UserProfile();
    up.setDisplayName("Test User5");

    dummyDataStore.store(up);

    UserRole ur1 = new UserRole();
    ur1.setRoleName("Role5");

    dummyDataStore.store(ur1);

    UserRole ur2 = new UserRole();
    ur2.setRoleName("Role6");

    dummyDataStore.store(ur2);

    HashSet<UserRole> roles = new HashSet<UserRole>();

    roles.add(ur1);
    roles.add(ur2);

    return new Object[][] {
      { roles, up.getID() }
    };
  }

  @Test(dataProvider = "userRoles")
  public void testGetAllUserRoles(HashSet<UserRole> roles, Long userProfileID)
  {
    List<UserRoleView> rolesFromDB = adminRolesService.getAllRoles(userProfileID);

    //A lot of unit tests insert data, so I just test if something is returned
    assertTrue(rolesFromDB.size() > 0);

    //None of these roles should be assigned
    for(UserRoleView uv : rolesFromDB) {
      assertFalse(uv.getAssigned());
    }
  }

  @DataProvider(name = "userAssignedRoles")
  public Object[][] userAssignedRoles()
  {
    UserRole ur1 = new UserRole();
    ur1.setRoleName("Role7");

    dummyDataStore.store(ur1);

    UserRole ur2 = new UserRole();
    ur2.setRoleName("Role8");

    dummyDataStore.store(ur2);

    HashSet<UserRole> roles = new HashSet<UserRole>();

    roles.add(ur1);
    roles.add(ur2);

    UserProfile up = new UserProfile();
    up.setDisplayName("Test User6");
    up.setRoles(roles);

    dummyDataStore.store(up);

    return new Object[][] {
      { roles, up.getID() }
    };
  }

  @Test(dataProvider = "userAssignedRoles")
  public void testUserAssignedRoles(HashSet<UserRole> roles, Long userProfileID)
  {
    List<UserRoleView> viewsFromDB = adminRolesService.getAllRoles(userProfileID);

    //A lot of unit tests insert data, so I just test if something is returned
    assertTrue(viewsFromDB.size() > 0);

    for(UserRoleView uv : viewsFromDB) {
      if(uv.getAssigned()) {
        boolean found = false;

        for(UserRole ur : roles) {
          if(uv.getID().equals(ur.getID())) {
            found = true;
            break;
          }
        }

        assertTrue(found);
      } else {
        boolean found = false;

        for(UserRole ur : roles) {
          if(uv.getID().equals(ur.getID())) {
            found = true;
            break;
          }
        }

        assertFalse(found);
      }
    }
  }

  @DataProvider(name = "userProfileAndRoles3")
  public Object[][] roleSet3()
  {
    UserRole ur1 = new UserRole();
    ur1.setRoleName("Role5");

    dummyDataStore.store(ur1);

    UserRole ur2 = new UserRole();
    ur2.setRoleName("Role6");

    dummyDataStore.store(ur2);

    HashSet<UserRole> roles = new HashSet<UserRole>();

    roles.add(ur1);
    roles.add(ur2);

    UserProfile up = new UserProfile();
    up.setDisplayName("Test User3");
    up.setRoles(roles);

    dummyDataStore.store(up);

    return new Object[][] {
      { up, roles }
    };
  }

  @Test(dataProvider = "userProfileAndRoles3")
  public void testGetUserRoles(UserProfile up, HashSet<UserRole> roles)
  {
    Set<UserRoleView> rolesFromDB = adminRolesService.getUserRoles(up.getID());

    assertEquals(roles.size(), rolesFromDB.size());

    for(UserRoleView urv : rolesFromDB) {
      boolean found = false;

      for(UserRole role : roles) {
        if(urv.getID().equals(role.getID())) {
          found = true;
          break;
        }
      }

      assertTrue(found);
    }
  }
}
