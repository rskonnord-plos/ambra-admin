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

package org.ambraproject.user.action;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
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
import static org.testng.Assert.assertTrue;

/**
 * @author Joe Osowski
 */
public class EditRolesActionTest extends AdminWebTest {

  @Autowired
  protected EditRolesAction editRolesAction;

  @DataProvider(name = "userNoRoles")
  public Object[][] getUserNoRoles() {
    UserProfile up = new UserProfile();

    up.setAuthId("AUTHID");
    up.setDisplayName("user name 1");
    up.setEmail("test@ambraproject.org");
    up.setPassword("pass");

    dummyDataStore.store(up);

    return new Object[][]{
      { up.getAuthId(), up.getID() }
    };
  }

  @Test(dataProvider = "userNoRoles")
  public void testAssignNoRolesAction(String authId, Long userProfileId) throws Exception {
    editRolesAction.setUserAuthId(authId);
    editRolesAction.setRoleIDs(new Long[]{});
    editRolesAction.assignRoles();

    UserProfile storedUser = dummyDataStore.get(UserProfile.class, userProfileId);

    //Test action get methods
    assertEquals(editRolesAction.getUserAuthId(), authId);
    assertEquals(editRolesAction.getDisplayName(), "user name 1");
    assertEquals(editRolesAction.getEmail(), "test@ambraproject.org");

    //All user roles should be removed
    assertEquals(storedUser.getRoles().size(), 0, "Roles not removed correctly");
  }

  @DataProvider(name = "userTwoRoles")
  public Object[][] getUserTwoRoles() {
    UserProfile up = new UserProfile();
    up.setDisplayName("user name 2");
    up.setAuthId("TESTUSER2");
    up.setEmail("twoRoles@example.org");
    up.setPassword("pass");
    dummyDataStore.store(up);

    UserRole ur1 = new UserRole();
    ur1.setRoleName("Role 1");
    dummyDataStore.store(ur1);

    UserRole ur2 = new UserRole();
    ur2.setRoleName("Role 2");
    dummyDataStore.store(ur2);

    return new Object[][]{
      { up.getID(), up.getAuthId(), new Long[] { ur1.getID(), ur2.getID() } }
    };
  }

  @Test(dataProvider = "userTwoRoles")
  public void testAssignTwoRolesAction(Long userProfileId, String authId, Long[] roleIDs) throws Exception {
    editRolesAction.setUserAuthId(authId);
    editRolesAction.setRoleIDs(roleIDs);
    editRolesAction.assignRoles();

    UserProfile storedUser = dummyDataStore.get(UserProfile.class, userProfileId);

    //All user should have two roles
    assertEquals(storedUser.getRoles().size(), 2, "Roles not assigned correctly");

    for(Long roleId : roleIDs) {
      boolean found = false;

      for(UserRole ur : storedUser.getRoles()) {
        if(ur.getID().equals(roleId)) {
          found = true;
          break;
        }
      }

      assertTrue(found, "Roles not assigned correctly");
    }
  }

  @DataProvider(name = "userAssignedThreeRoles")
  public Object[][] getUserAssignedThreeRoles() {
    Set<UserRole> roles = new HashSet<UserRole>();
    UserRole ur1 = new UserRole();
    ur1.setRoleName("Assigned Role 1");
    dummyDataStore.store(ur1);
    roles.add(ur1);

    UserRole ur2 = new UserRole();
    ur2.setRoleName("Assigned Role 2");
    dummyDataStore.store(ur2);
    roles.add(ur2);

    UserRole ur3 = new UserRole();
    ur3.setRoleName("Assigned Role 3");
    dummyDataStore.store(ur3);
    roles.add(ur3);

    UserRole ur4 = new UserRole();
    ur4.setRoleName("Unassigned Role 4");
    dummyDataStore.store(ur4);

    UserRole ur5 = new UserRole();
    ur5.setRoleName("Unassigned Role 5");
    dummyDataStore.store(ur5);

    UserProfile up = new UserProfile();
    up.setDisplayName("user name 3");
    up.setAuthId("USERAUTH5");
    up.setEmail("threeRoles@example.com");
    up.setPassword("pass");
    up.setRoles(roles);
    dummyDataStore.store(up);

    return new Object[][]{
      { up.getID(), up.getAuthId(),
        new Long[] { ur1.getID(), ur2.getID(), ur3.getID() },
        new Long[] { ur4.getID(), ur5.getID() } }
    };
  }

  @Test(dataProvider = "userAssignedThreeRoles")
  public void testAssignRoles2Action(Long userProfileId, String authId,
    Long[] assignedRoles, Long[] roleIDsToAssign) throws Exception {

    editRolesAction.setUserAuthId(authId);
    editRolesAction.execute();

    List<UserRoleView> assignedRoleViews = editRolesAction.getUserRoles();

    for(UserRoleView urv : assignedRoleViews) {
      if(urv.getAssigned()) {
        boolean found = false;

        for(Long roleId : assignedRoles) {
          if(urv.getID().equals(roleId)) {
            found = true;
            break;
          }
        }

        assertTrue(found, "Roles not assigned correctly");
      }
    }

    editRolesAction.setRoleIDs(roleIDsToAssign);
    editRolesAction.assignRoles();

    //All user should have two roles
    UserProfile up = (UserProfile)dummyDataStore.get(UserProfile.class, userProfileId);

    assertEquals(up.getRoles().size(), 2, "Roles not assigned correctly");

    for(Long roleId : roleIDsToAssign) {
      boolean found = false;

      for(UserRole ur : up.getRoles()) {
        if(ur.getID().equals(roleId)) {
          found = true;
          break;
        }
      }

      assertTrue(found, "Roles not assigned correctly");
    }
  }


  @Override
  protected BaseActionSupport getAction() {
    return editRolesAction;
  }
}
