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
import org.ambraproject.ApplicationException;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 9/12/11
 */
public class SearchUserActionTest extends AdminWebTest {

  @Autowired
  protected SearchUserAction searchUserAction;


  @DataProvider(name = "savedUser")
  public Object[][] getSavedUser() throws Exception {
    UserProfile userProfile = new UserProfile();
    userProfile.setEmail("test@SearchUserActionTest.org");
    userProfile.setAuthId("authIdForSearchUserActionTest");
    userProfile.setGivenNames("John P.");
    userProfile.setSurname("Smith");
    userProfile.setRealName("John P. Smith");
    userProfile.setDisplayName("jps");
    userProfile.setPositionType("doctor");
    userProfile.setOrganizationType("university");
    userProfile.setPostalAddress("123 fake st");
    userProfile.setBiography("born; lived; died");
    userProfile.setInterests("interesting stuff");
    userProfile.setResearchAreas("areas to research");
    userProfile.setCity("city");
    userProfile.setCountry("country");
    userProfile.setPassword("pass");

    dummyDataStore.store(userProfile);


    return new Object[][]{
        {userProfile}
    };
  }


  @Test(dataProvider = "savedUser")
  public void testSearchUserByAuthId(UserProfile storedUser) throws Exception {
    String authId = storedUser.getAuthId();
    searchUserAction.setUserAuthId(authId);
    assertEquals(searchUserAction.executeFindUserByAuthId(), Action.SUCCESS,
        "searchUserAction.executeFindUserByAuthId() didn't return success");

    final UserProfile[] ambraUserIdList = searchUserAction.getUsers();
    assertTrue(ambraUserIdList.length == 1, "Didn't return correct number of users");
    compareUsers(ambraUserIdList[0], storedUser);
    searchUserAction.setUserAuthId(null);
  }

  @Test(dataProvider = "savedUser")
  public void testSearchUserByEmail(UserProfile storedUser) throws Exception {
    String emailAddress = storedUser.getEmail();
    searchUserAction.setEmailAddress(emailAddress);

    assertEquals(searchUserAction.executeFindUserByEmailAddress(), Action.SUCCESS,
        "searchUserAction.executeFindUserByEmailAddress() didn't return success");

    final UserProfile[] ambraUserIdList = searchUserAction.getUsers();
    assertTrue(ambraUserIdList.length == 1, "didn't return correct number of user profiles");
    compareUsers(ambraUserIdList[0], storedUser);
    searchUserAction.setEmailAddress(null);
  }

  @Test(dataProvider = "savedUser")
  public void testSearchUserByName(UserProfile storedUser) throws Exception {
    searchUserAction.setName(storedUser.getDisplayName());

    assertEquals(searchUserAction.executeFindUserByName(), Action.SUCCESS,
        "searchUserAction.executeFindUserByName() didn't return success");

    final UserProfile[] ambraUserIdList = searchUserAction.getUsers();
    assertTrue(ambraUserIdList.length == 1, "didn't return correct number of user profiles");
    compareUsers(ambraUserIdList[0], storedUser);
    searchUserAction.setName(null);
  }

  /**
   * Checks that the values for the user stored with the given id are the constants from this class
   *
   * @throws org.ambraproject.ApplicationException
   *          if there's an error retrieving the user
   */
  private void compareUsers(UserProfile expected, UserProfile expectedUser) throws ApplicationException {
    assertEquals(expected.getEmail(), expectedUser.getEmail(), "saved user didn't have correct email");
    assertEquals(expected.getRealName(), expectedUser.getRealName(), "saved user didn't have correct real name");
    assertEquals(expected.getDisplayName(), expectedUser.getDisplayName(), "saved user didn't have correct display name");
    assertEquals(expected.getGivenNames(), expectedUser.getGivenNames(), "saved user didn't have correct given names");
    assertEquals(expected.getSurname(), expectedUser.getSurname(), "saved user didn't have correct surnames");
    assertEquals(expected.getPositionType(), expectedUser.getPositionType(), "saved user didn't have correct position type");
    assertEquals(expected.getOrganizationType(), expectedUser.getOrganizationType(), "saved user didn't have correct organization type");
    assertEquals(expected.getPostalAddress(), expectedUser.getPostalAddress(), "saved user didn't have correct postal address");
    assertEquals(expected.getBiography(), expectedUser.getBiography(), "saved user didn't have correct biography text");
    assertEquals(expected.getInterests(), expectedUser.getInterests(), "saved user didn't have correct interests text");
    assertEquals(expected.getResearchAreas(), expectedUser.getResearchAreas(), "saved user didn't have correct research areas text");
    assertEquals(expected.getCity(), expectedUser.getCity(), "saved user didn't have correct city");
    assertEquals(expected.getCountry(), expectedUser.getCountry(), "saved user didn't have correct country");
  }

  @Override
  protected BaseActionSupport getAction() {
    return searchUserAction;
  }
}

