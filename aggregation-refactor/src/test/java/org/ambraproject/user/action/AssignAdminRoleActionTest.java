/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.user.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.permission.service.PermissionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alex Kudlick 9/12/11
 */
public class AssignAdminRoleActionTest extends AdminWebTest {

  @Autowired
  protected AssignAdminRoleAction assignAdminRoleAction;


  @DataProvider(name = "savedUser")
  public Object[][] getSavedUser() {
    UserProfile userProfile = new UserProfile();
    userProfile.setEmail("email@AssignAdminRoleActionTest.org");
    userProfile.setDisplayName("displayNameForAssignAdminRoleActionTest.org");
    userProfile.setAuthId("authIdForAssignAdminRoleActionTest.org");
    dummyDataStore.store(userProfile);

    return new Object[][]{
        {userProfile.getID()}
    };
  }

  @Test(dataProvider = "savedUser")
  public void testAssignAdminRoleAction(Long userId) throws Exception {
    assignAdminRoleAction.setUserId(userId);
    assertEquals(assignAdminRoleAction.execute(), Action.SUCCESS, "execute didn't return success");
    assertEquals(assignAdminRoleAction.getActionErrors().size(), 0, "action had error messages");
    assertEquals(assignAdminRoleAction.getActionMessages().size(), 1, "Action didn't return a success message");

    UserProfile storedUser = dummyDataStore.get(UserProfile.class, userId);
    assertEquals(storedUser.getRoles().size(), 1, "User didn't get a role added");
    UserRole role = storedUser.getRoles().iterator().next();
    assertEquals(role.getRoleName(), PermissionsService.ADMIN_ROLE, "Role didn't have correct name");
  }

  @Override
  protected BaseActionSupport getAction() {
    return assignAdminRoleAction;
  }
}
