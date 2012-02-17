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

package org.ambraproject.user.action;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import org.ambraproject.Constants;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 2/17/12
 */
public class AdminUserProfileActionTest extends AdminWebTest {

  @Autowired
  protected AdminUserProfileAction action;

  @DataProvider(name = "user")
  public Object[][] getUser() {
    UserProfile user = new UserProfile();
    user.setEmail("userActionTest@topazproject.org");
    user.setRealName("Test User");
    user.setDisplayName("TEST_USERNAME");
    user.setGivenNames("my GIVENNAMES");
    user.setSurname("my Surnames");
    user.setPositionType("my POSITION_TYPE");
    user.setOrganizationType("my organizationType");
    user.setPostalAddress("my postalAddress");
    user.setBiography("my biographyText");
    user.setInterests("my interestsText");
    user.setResearchAreas("my researchAreasText");
    user.setCity("my city");
    user.setCountry("my country");
    user.setAuthId("auth-id-for-AdminUserActionTest");
    dummyDataStore.store(user);

    return new Object[][]{
        {user}
    };
  }

  @Override
  public void setDefaultRequest() {
    super.setDefaultRequest();
    //add the email to session so the action doesn't try and talk to cas
    UserProfile user = (UserProfile) getUser()[0][0];
    ActionContext.getContext().getSession().put(Constants.SINGLE_SIGNON_EMAIL_KEY, user.getEmail());
    action.setUserAuthId(user.getAuthId());

  }


  @BeforeMethod
  public void resetAction() {
//    action.setEmail(null);
    action.setRealName(null);
    action.setDisplayName(null);
    action.setGivenNames(null);
    action.setSurnames(null);
    action.setPositionType(null);
    action.setOrganizationType(null);
    action.setPostalAddress(null);
    action.setBiographyText(null);
    action.setInterestsText(null);
    action.setResearchAreasText(null);
    action.setCity(null);
    action.setCountry(null);
    action.setWeblog(null);
    action.setHomePage(null);
  }

  /**
   * Regression test for a bug that never made it into production in which updating a profile would overwrite the user's roles
   *
   * @param user
   */
  @Test(dataProvider = "user")
  public void testUpdateDoesNotOverwriteRoles(UserProfile user) throws Exception {
    UserRole role = new UserRole("test role for UserProfileActionTest");
    dummyDataStore.store(role);
    UserProfile storedUser = dummyDataStore.get(UserProfile.class, user.getID());
    storedUser.getRoles().add(role);
    dummyDataStore.update(storedUser);

    action.executeSaveUser();

    storedUser = dummyDataStore.get(UserProfile.class, user.getID());
    assertTrue(storedUser.getRoles().size() > 0, "Roles got erased");
  }

  @Test(dataProvider = "user")
  public void testUpdateDoesNotOverwriteAccountUri(UserProfile user) throws Exception {
    UserProfile storedUser = dummyDataStore.get(UserProfile.class, user.getID());
    if (storedUser.getAccountUri() == null) {
      storedUser.setAccountUri("id:accountUriForTestingOverwrite");
      dummyDataStore.update(storedUser);
    }

    action.executeSaveUser();

    storedUser = dummyDataStore.get(UserProfile.class, user.getID());
    assertNotNull(storedUser.getAccountUri(), "Account uri got erased");
  }

  @Test(dataProvider = "user")
  public void testExecuteWithExistingUser(UserProfile user) throws Exception {
    assertEquals(action.execute(), Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages: " + StringUtils.join(action.getActionErrors(), ";"));

    assertEquals(action.getEmail(), user.getEmail(), "action didn't have correct email");
    assertEquals(action.getRealName(), user.getRealName(), "action didn't have correct real name");
    assertEquals(action.getDisplayName(), user.getDisplayName(), "action didn't have correct display name");
    assertEquals(action.getGivenNames(), user.getGivenNames(), "action didn't have correct given names");
    assertEquals(action.getSurnames(), user.getSurname(), "action didn't have correct surnames");
    assertEquals(action.getPositionType(), user.getPositionType(), "action didn't have correct position type");
    assertEquals(action.getOrganizationType(), user.getOrganizationType(), "action didn't have correct organization type");
    assertEquals(action.getPostalAddress(), user.getPostalAddress(), "action didn't have correct postal address");
    assertEquals(action.getBiographyText(), user.getBiography(), "action didn't have correct biography text");
    assertEquals(action.getInterestsText(), user.getInterests(), "action didn't have correct interests text");
    assertEquals(action.getResearchAreasText(), user.getResearchAreas(), "action didn't have correct research areas text");
    assertEquals(action.getCity(), user.getCity(), "action didn't have correct city");
    assertEquals(action.getCountry(), user.getCountry(), "action didn't have correct country");
  }

  @Test(dataProvider = "user", dependsOnMethods = {"testExecuteWithExistingUser"})
  public void testEditAlreadySavedUser(UserProfile user) throws Exception {
    long testStart = Calendar.getInstance().getTime().getTime();
    String newOrganizationType = "new organization type";
    String newPositionType = "new position type";

    action.setPositionType(newPositionType);
    action.setOrganizationType(newOrganizationType);
    action.setOrgVisibility("public");

    action.setEmail(user.getEmail());
    action.setRealName(user.getRealName());
    action.setDisplayName(user.getDisplayName());
    action.setGivenNames(user.getGivenNames());
    action.setSurnames(user.getSurname());
    action.setPostalAddress(user.getPostalAddress());
    action.setBiographyText(user.getBiography());
    action.setInterestsText(user.getInterests());
    action.setResearchAreasText(user.getResearchAreas());
    action.setCity(user.getCity());
    action.setCountry(user.getCountry());

    assertEquals(action.executeSaveUser(), Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages: " + StringUtils.join(action.getActionErrors(), ";"));

    UserProfile savedUser = dummyDataStore.get(UserProfile.class, user.getID());

    assertEquals(savedUser.getOrganizationType(), newOrganizationType, "user didn't get organization type set");
    assertEquals(savedUser.getPositionType(), newPositionType, "user didn't get position type set on it");
    assertTrue(savedUser.getOrganizationVisibility(), "user didn't get organization visibility set");

    //make sure none of the other properties got changed
    assertEquals(savedUser.getEmail(), user.getEmail(), "user's email changed");
    assertEquals(savedUser.getRealName(), user.getRealName(), "user's real name changed");
    assertEquals(savedUser.getDisplayName(), user.getDisplayName(), "user's display name changed");
    assertEquals(savedUser.getGivenNames(), user.getGivenNames(), "user's given names changed");
    assertEquals(savedUser.getSurname(), user.getSurname(), "user's surnames changed");
    assertEquals(savedUser.getPostalAddress(), user.getPostalAddress(), "user's postal address changed");
    assertEquals(savedUser.getBiography(), user.getBiography(), "user's biography text changed");
    assertEquals(savedUser.getInterests(), user.getInterests(), "user's interests text changed");
    assertEquals(savedUser.getResearchAreas(), user.getResearchAreas(), "user's research areas text changed");
    assertEquals(savedUser.getCity(), user.getCity(), "user's city changed");
    assertEquals(savedUser.getCountry(), user.getCountry(), "user's country changed");

    assertTrue(savedUser.getLastModified().getTime() >= testStart, "user didn't have lastmodified date updated");
  }

  @Test
  public void testSaveWithNullDisplayName() throws Exception {
    action.setDisplayName(null);
    action.setGivenNames("foo");
    action.setSurnames("foo");

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("displayName"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithNullGivenNames() throws Exception {
    action.setDisplayName("testdisplayname");
    action.setGivenNames(null);
    action.setSurnames("foo");

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("givenNames"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithNullSurnames() throws Exception {
    action.setDisplayName("testdisplayname");
    action.setGivenNames("foo");
    action.setSurnames(null);

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("surnames"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithShortDisplayName() throws Exception {
    action.setDisplayName("a");
    action.setGivenNames("foo");
    action.setSurnames("foo");

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("displayName"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithLongDisplayName() throws Exception {
    action.setDisplayName("Meet_Mary_Margaret_Shes_a_charitable_sensitive_elementary_school_teacher_in_Storybrooke_Maine");
    action.setGivenNames("foo");
    action.setSurnames("foo");

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("displayName"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveDisplayNameWithBadCharacters() throws Exception {
    action.setDisplayName("@#$(U^(#&$)(&U");
    action.setGivenNames("foo");
    action.setSurnames("foo");

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("displayName"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithProfantiy() throws Exception {
    action.setDisplayName("fooadmin");
    action.setGivenNames("foo");
    action.setSurnames("foo");
    action.setBiographyText("ass");
    action.setHomePage(null);
    action.setWeblog(null);

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("biographyText"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithBadHomePage() throws Exception {
    action.setDisplayName("testDisplayName");
    action.setGivenNames("foo");
    action.setSurnames("foo");
    action.setHomePage("hello this is a bad home page");

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("homePage"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test
  public void testSaveWithBadBlog() throws Exception {
    action.setDisplayName("testDisplayName");
    action.setGivenNames("foo");
    action.setSurnames("foo");
    action.setWeblog("hello this is a bad blog");

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("weblog"), "action didn't add field errors");
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpected field errors for fields: "
        + StringUtils.join(action.getFieldErrors().keySet(), ","));
  }

  @Test(dataProvider = "user")
  public void testSaveWithDuplicateDisplayName(UserProfile userProfile) throws Exception {
    action.setUserAuthId("new-auth-key-for-dup-displayName");
    action.setDisplayName(userProfile.getDisplayName());
    action.setGivenNames(userProfile.getGivenNames());
    action.setSurnames(userProfile.getSurname());

    String result = action.executeSaveUser();
    assertEquals(result, Action.INPUT, "Action didn't return input");
    assertNotNull(action.getFieldErrors().get("displayName"));
    assertEquals(action.getFieldErrors().size(), 1, "action added unexpeceted field errors");
  }


  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
