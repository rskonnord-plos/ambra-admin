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
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.AuthenticationId;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.models.UserPreference;
import org.topazproject.ambra.models.UserPreferences;
import org.topazproject.ambra.models.UserProfile;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 2/3/12
 */
public class AdminUserAlertsActionTest extends AdminWebTest {


  @Autowired
  protected AdminUserAlertsAction action;

  @Qualifier("applicationId")
  @Autowired
  protected String appId;


  @DataProvider(name = "userAlerts")
  public Object[][] getUserWithBadAlerts() {
    UserAccount userAccount = new UserAccount();
    userAccount.setProfile(new UserProfile());
    userAccount.getProfile().setDisplayName("some fake display name");
    userAccount.getProfile().setEmail(URI.create("mailto:foo@bar.org"));

    userAccount.setAuthIds(new HashSet<AuthenticationId>(1));
    userAccount.getAuthIds().add(new AuthenticationId("foo"));

    UserPreferences userPreferences = new UserPreferences();
    userPreferences.setAppId(appId);
    userPreferences.setPrefs(new HashSet<UserPreference>(2));

    UserPreference preference = new UserPreference();
    preference.setName("alertsJournals");
    preference.setValues(new String[]{"journal_monthly", "journal_weekly", "journal1_monthly"});
    userPreferences.getPrefs().add(preference);

    userAccount.setPreferences(new HashSet<UserPreferences>(1));
    userAccount.getPreferences().add(userPreferences);
    dummyDataStore.store(userAccount);
    return new Object[][]{
        {userAccount.getId().toString(), new String[]{"journal"}, new String[]{"journal", "journal1"}}
    };
  }

  @Test(dataProvider = "userAlerts")
  public void testRetrieveAlerts(String userId, String[] expectedWeeklyPreferences, String[] expectedMonthlyPreferences) throws Exception {
    action.setTopazId(userId);
    String result = action.retrieveAlerts();

    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");

    assertEquals(action.getWeeklyAlerts(), expectedWeeklyPreferences, "Action didn't have correct weekly alerts");
    assertEquals(action.getMonthlyAlerts(), expectedMonthlyPreferences, "Action didn't have correct alert alerts");
  }

  @Test(dataProvider = "userAlerts", dependsOnMethods = {"testRetrieveAlerts"})
  public void testRemoveAlerts(String userId, String[] expectedWeeklyPreferences, String[] expectedMonthlyPreferences) throws Exception {
    action.setTopazId(userId);
    action.setMonthlyAlerts(new String[0]);
    action.setWeeklyAlerts(new String[0]);

    String result = action.saveAlerts();

    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    //check what got saved to the database
    Set<UserPreference> storedPreferences = dummyDataStore.get(UserAccount.class, URI.create(userId))
        .getPreferences(appId).getPrefs();

    //There should be one 'Preference' with many 'values'
    assertEquals(storedPreferences.size(), 1, "Action stored incorrect number of preference objects");
    UserPreference pref = (UserPreference) storedPreferences.toArray()[0];
    assertEquals(pref.getName(), "alertsJournals", "Preference had incorrect type");
    assertEquals(pref.getValues(), new String[0], "Preference values didn't get cleared");
  }

  @Test
  public void testAddAlerts() throws Exception {
    UserAccount userAccount = new UserAccount();
    userAccount.setProfile(new UserProfile());
    userAccount.setAuthIds(new HashSet<AuthenticationId>(1));
    userAccount.getAuthIds().add(new AuthenticationId("foobar"));
    String userId = dummyDataStore.store(userAccount);
    String[] monthlyAlerts = {"journal"};
    String[] weeklyAlerts = {"journal", "journal1"};

    action.setTopazId(userId);
    action.setMonthlyAlerts(monthlyAlerts);
    action.setWeeklyAlerts(weeklyAlerts);

    String result = action.saveAlerts();

    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check what got saved to the database
    Set<UserPreference> storedPreferences = dummyDataStore.get(UserAccount.class, URI.create(userId))
        .getPreferences(appId).getPrefs();

    //There should be one 'Preference' with many 'values'
    assertEquals(storedPreferences.size(), 1, "Action stored incorrect number of preference objects");
    UserPreference pref = (UserPreference) storedPreferences.toArray()[0];
    assertEquals(pref.getName(), "alertsJournals", "Preference had incorrect type");
    List<String> valuesAsList = Arrays.asList(pref.getValues());

    for (String alert : monthlyAlerts) {
      assertTrue(valuesAsList.contains(alert + "_monthly"), "Didn't store monthly alert: " + alert);
    }
    for (String alert : weeklyAlerts) {
      assertTrue(valuesAsList.contains(alert + "_weekly"), "Didn't store weekly alert: " + alert);
    }
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
