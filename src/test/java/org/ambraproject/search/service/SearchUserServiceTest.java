/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.search.service;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.models.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick 2/17/12
 */
public class SearchUserServiceTest extends AdminBaseTest {

  @Autowired
  protected SearchUserService searchUserService;

  @DataProvider(name = "usersByDisplayName")
  public Object[][] getUsersByDisplayName() {
    UserProfile user1 = new UserProfile();
    user1.setAuthId("authId1ForSearchByDisplayName");
    user1.setEmail("email1@SearchByDisplayName.org");
    user1.setDisplayName("search_me");
    dummyDataStore.store(user1);

    UserProfile user2 = new UserProfile();
    user2.setAuthId("authId2ForSearchByDisplayName");
    user2.setEmail("email2@SearchByDisplayName.org");
    user2.setDisplayName("search_me1234");
    dummyDataStore.store(user2);

    UserProfile user3 = new UserProfile();
    user3.setAuthId("authId3ForSearchByDisplayName");
    user3.setEmail("email3@SearchByDisplayName.org");
    user3.setDisplayName("search_me12foo");
    dummyDataStore.store(user3);

    return new Object[][]{
        {"non-existent-displayName", new UserProfile[]{}},
        {"search_me", new UserProfile[]{user1}},
        {"search", new UserProfile[]{user1, user2, user3}},
        {"search_me12", new UserProfile[]{user2, user3}},
        {"search_me1234", new UserProfile[]{user2}}
    };
  }

  @Test(dataProvider = "usersByDisplayName")
  public void testFindUsersByDisplayName(String displayNameString, UserProfile[] expectedUsers) {
    List<UserProfile> results = searchUserService.findUsersByDisplayName(displayNameString);

    assertNotNull(results, "Returned null results list");
    assertEquals(results.size(), expectedUsers.length, "Returned incorrect number of results");
    for (UserProfile expected : expectedUsers) {
      UserProfile match = null;
      for (UserProfile user : results) {
        if (expected.getID().equals(user.getID())) {
          match = user;
          break;
        }
      }
      assertNotNull(match, "Didn't find a match for expected user: " + expected);
      assertEquals(match.getDisplayName(), expected.getDisplayName(), "Returned user with incorrect display name");
      assertEquals(match.getEmail(), expected.getEmail(), "Returned user with incorrect email");
      assertEquals(match.getAuthId(), expected.getAuthId(), "Returned user with incorrect auth id");
    }
  }

  @DataProvider(name = "usersByEmail")
  public Object[][] getUsersByEmail() {
    UserProfile user1 = new UserProfile();
    user1.setAuthId("authId1ForFindByEmail");
    user1.setEmail("search_email1@FindByEmail.org");
    user1.setDisplayName("displayName1ForFindByEmail");
    dummyDataStore.store(user1);

    UserProfile user2 = new UserProfile();
    user2.setAuthId("authId2ForFindByEmail");
    user2.setEmail("search_email2@FindByEmail.org");
    user2.setDisplayName("displayName2ForFindByEmail");
    dummyDataStore.store(user2);

    UserProfile user3 = new UserProfile();
    user3.setAuthId("authId3ForFindByEmail");
    user3.setEmail("search_email3@FindByEmail.org");
    user3.setDisplayName("displayName3ForFindByEmail");
    dummyDataStore.store(user3);

    return new Object[][]{
        {"non-existent-email", new UserProfile[]{}},
        {"@FindByEmail.org", new UserProfile[]{user1, user2, user3}},
        {"search_email", new UserProfile[]{user1, user2, user3}},
        {"search_email2@FindByEmail.org", new UserProfile[]{user2}},
        {"search_email1", new UserProfile[]{user1}}
    };
  }

  @Test(dataProvider = "usersByEmail")
  public void testFindUsersByEmail(String emailString, UserProfile[] expectedUsers) {
    List<UserProfile> results = searchUserService.findUsersByEmail(emailString);

    assertNotNull(results, "Returned null results list");
    assertEquals(results.size(), expectedUsers.length, "Returned incorrect number of results");
    for (UserProfile expected : expectedUsers) {
      UserProfile match = null;
      for (UserProfile user : results) {
        if (expected.getID().equals(user.getID())) {
          match = user;
          break;
        }
      }
      assertNotNull(match, "Didn't find a match for expected user: " + expected);
      assertEquals(match.getDisplayName(), expected.getDisplayName(), "Returned user with incorrect display name");
      assertEquals(match.getEmail(), expected.getEmail(), "Returned user with incorrect email");
      assertEquals(match.getAuthId(), expected.getAuthId(), "Returned user with incorrect auth id");
    }
  }

}
