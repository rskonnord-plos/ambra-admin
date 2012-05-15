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

package org.ambraproject.admin.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.admin.service.AdminService;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Volume;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Alex Kudlick 1/31/12
 */
public class VolumeManagementActionTest extends AdminWebTest {

  @Autowired
  protected VolumeManagementAction action;

  @Autowired
  protected AdminService adminService;

  @DataProvider(name = "basicInfo")
  public Object[][] getBasicInfo() {
    Volume volume = new Volume();
    volume.setVolumeUri("id:volume-for-VolumeManagementAction");
    volume.setDisplayName("2009");
    volume.setIssues(new ArrayList<Issue>(3));

    String issuesCSV = "";
    for (int i = 1; i <= 3; i++) {
      Issue issue = new Issue();
      issue.setIssueUri("id:issue-for-VolumeManagementAction" + i);
      issue.setDisplayName("Month " + i);
      dummyDataStore.store(issue);
      volume.getIssues().add(issue);
      issuesCSV += issue.getIssueUri();
      if (i < 3) {
        issuesCSV += ',';
      }
    }
    dummyDataStore.store(volume);

    return new Object[][]{
        {volume, volume.getIssues(), issuesCSV}
    };
  }


  @Test(dataProvider = "basicInfo")
  public void testExecute(Volume expectedVolume, List<Issue> expectedIssues, String issuesCSV) throws Exception {
    action.setVolumeURI(expectedVolume.getVolumeUri());

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");

    assertNotNull(action.getVolume(), "action had null volume");
    assertEquals(action.getVolume().getVolumeUri(), expectedVolume.getVolumeUri(), "Action returned incorrect volume");
    assertEquals(action.getVolume().getDisplayName(), expectedVolume.getDisplayName(),
        "Action returned volume with incorrect display name");

    assertEquals(action.getIssuesCSV(), issuesCSV, "Action returned incorrect issue csv list");
    assertEquals(action.getIssues().size(), expectedIssues.size(), "Action returned incorrect number of issues");
    for (int i = 0; i < expectedIssues.size(); i++) {
      Issue actualIssue = action.getIssues().get(i);
      Issue expectedIssue = expectedIssues.get(i);
      assertEquals(actualIssue.getIssueUri(), expectedIssue.getIssueUri(), "Issue " + (i + 1) + " had incorrect URI");
      assertEquals(actualIssue.getDisplayName(), expectedIssue.getDisplayName(), "Issue " + (i + 1) + " had incorrect display name");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testCreateIssue(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {
    String issueURI = "id:new-issue-to-create";
    String displayName = "New Display Name";
    String imageURI = "id:image-article-for-create-issue";

    List<Issue> existingIssues = dummyDataStore.get(Volume.class, volume.getID()).getIssues();
    String expectedIssuesCSV = adminService.formatIssueCsv(existingIssues) + "," + issueURI;

    action.setVolumeURI(volume.getVolumeUri());
    action.setCommand("CREATE_ISSUE");
    action.setIssueURI(issueURI);
    action.setDisplayName(displayName);
    action.setImageURI(imageURI);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");

    //check properties on action
    assertNotNull(action.getVolume(), "Action had null volume");
    assertEquals(action.getVolume().getVolumeUri(), volume.getVolumeUri(), "Action changed volumes after creating issue");
    assertEquals(action.getIssuesCSV(), expectedIssuesCSV, "Action didn't return correct issues csv");
    assertEquals(action.getIssues().size(), existingIssues.size() + 1, "action didn't add issue to list");

    for (int i = 0; i < existingIssues.size(); i++) {
      assertEquals(action.getIssues().get(i), existingIssues.get(i),
          "Action reordered issues; issue " + (i + 1) + " changed");
    }
    Issue lastIssue = action.getIssues().get(action.getIssues().size() - 1);
    assertEquals(lastIssue.getIssueUri(), issueURI, "Issue didn't get created with correct URI");
    assertEquals(lastIssue.getDisplayName(), displayName, "Issue didn't get created with correct display name");
    assertEquals(lastIssue.getImageUri(), imageURI, "Issue didn't get created with correct image URI");

    List<Issue> storedIssues = dummyDataStore.get(Volume.class, volume.getID()).getIssues();
    assertEquals(storedIssues.size(), existingIssues.size() + 1, "issue didn't get added to volume in the database");
    for (int i = 0; i < existingIssues.size(); i++) {
      assertEquals(storedIssues.get(i), existingIssues.get(i),
          "Issues got reordered in the database; issue " + (i + 1) + " changed");
    }

    assertEquals(storedIssues.get(storedIssues.size() - 1).getIssueUri(), issueURI, "Issue didn't have correct issue uri");
    assertEquals(storedIssues.get(storedIssues.size() - 1).getDisplayName(), displayName,
        "Issue didn't get stored with correct display name");
    assertEquals(storedIssues.get(storedIssues.size() - 1).getImageUri(), imageURI,
        "Issue didn't get stored with correct image URI");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testDeleteIssues(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {
    List<Issue> existingIssues = dummyDataStore.get(Volume.class, volume.getID()).getIssues();
    String[] deleteUris = new String[]{existingIssues.get(0).getIssueUri(), existingIssues.get(1).getIssueUri()};

    String expectedIssuesCSV = adminService.formatIssueCsv(existingIssues.subList(2, existingIssues.size()));

    action.setVolumeURI(volume.getVolumeUri());
    action.setCommand("REMOVE_ISSUES");
    action.setIssuesToDelete(deleteUris);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 2, "Action didn't return messages indicating success");

    //check properties on action
    assertNotNull(action.getVolume(), "Action had null volume");
    assertEquals(action.getVolume().getVolumeUri(), volume.getVolumeUri(), "Action changed volumes after deleting issue");
    assertEquals(action.getIssuesCSV(), expectedIssuesCSV, "Action didn't return correct issues csv");
    assertEquals(action.getIssues().size(), existingIssues.size() - 2, "action didn't remove issues from list");

    for (int i = 0; i < action.getIssues().size(); i++) {
      assertEquals(action.getIssues().get(i), existingIssues.get(i + 2),
          "Action reordered issues; issue " + (i + 1) + " changed");
    }

    //check what got added to the db
    List<Issue> storedIssues = dummyDataStore.get(Volume.class, volume.getID()).getIssues();
    assertEquals(storedIssues.size(), existingIssues.size() - 2, "issues didn't get removed from volume in the database");
    for (int i = 0; i < storedIssues.size(); i++) {
      assertEquals(storedIssues.get(i), existingIssues.get(i + 2),
          "Issues got reordered in the database; issue " + (i + 1) + " changed");
    }
    for (Issue deletedIssue : existingIssues.subList(0,1)) {
      assertNull(dummyDataStore.get(Issue.class, deletedIssue.getID()), "Issue didn't get deleted from the database");
      assertFalse(storedIssues.contains(deletedIssue),
          "Issue " + deletedIssue.getIssueUri() + " didn't get removed from volume in the database");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testUpdateVolume(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {
    List<Issue> existingIssues = dummyDataStore.get(Volume.class, volume.getID()).getIssues();
    String csv = adminService.formatIssueCsv(existingIssues);

    String reorderedCsv = csv;
    String issueToReorder = csv.substring(0, csv.indexOf(","));
    reorderedCsv = reorderedCsv.replaceFirst(issueToReorder + ",", "");
    reorderedCsv += ("," + issueToReorder);
    String displayName = "Fruit of the Poisonous Tree";

    action.setCommand("UPDATE_VOLUME");
    action.setDisplayName(displayName);
    action.setIssuesCSV(reorderedCsv);
    action.setVolumeURI(volume.getVolumeUri());

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message(s) indicating success");

    assertNotNull(action.getVolume(), "Action had null volume");
    assertEquals(action.getVolume().getVolumeUri(), volume.getVolumeUri(), "Action changed volume after updating");
    assertEquals(action.getIssuesCSV(), reorderedCsv, "Action didn't return correct csv");
    assertEquals(action.getVolume().getDisplayName(), displayName, "Action didn't have correct display name");

    //check results in db
    Volume storedVolume = dummyDataStore.get(Volume.class, volume.getID());
    assertEquals(storedVolume.getDisplayName(), displayName, "Volume didn't get display name changed in the db");
    List<Issue> expectedIssues = new ArrayList<Issue>(existingIssues);
    expectedIssues.add(expectedIssues.get(0));
    expectedIssues.remove(0);

    assertEquals(storedVolume.getIssues().size(), expectedIssues.size(), "Number of issues changed in the database");
    assertEquals(storedVolume.getIssues().size(), expectedIssues.size(), "Number of issues changed in the database");

    for (int i = 0; i < expectedIssues.size(); i++) {
      assertEquals(storedVolume.getIssues().get(i), expectedIssues.get(i),
          "Issues weren't in correct order; element " + (i + 1) + " was incorrect");
    }

  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowAdditionsToCsv(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {
    String existingIssuesCSV = adminService.formatIssueCsv(dummyDataStore.get(Volume.class, volume.getID()).getIssues());

    action.setCommand("UPDATE_VOLUME");
    action.setVolumeURI(volume.getVolumeUri());
    action.setIssuesCSV(existingIssuesCSV + ",id:new-issue-in-csv");

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 1, "Action didn't return error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned message");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowRemovalsFromCsv(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {
    String existingIssuesCSV = adminService.formatIssueCsv(dummyDataStore.get(Volume.class, volume.getID()).getIssues());

    action.setCommand("UPDATE_VOLUME");
    action.setVolumeURI(volume.getVolumeUri());
    action.setIssuesCSV(existingIssuesCSV.substring(existingIssuesCSV.indexOf(",") + 1));

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 1, "Action didn't return error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned message");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowChangesInCsv(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {
    String existingIssuesCSV = adminService.formatIssueCsv(dummyDataStore.get(Volume.class, volume.getID()).getIssues());
    existingIssuesCSV = existingIssuesCSV.substring(existingIssuesCSV.indexOf(",") + 1);
    existingIssuesCSV = "id:this-issue-was-not-in-list," + existingIssuesCSV;

    assertEquals(dummyDataStore.get(Volume.class, volume.getID()).getIssues().size(),
        existingIssuesCSV.split(",").length, "Test added/removed an issue instead of just changing one");

    action.setCommand("UPDATE_VOLUME");
    action.setVolumeURI(volume.getVolumeUri());
    action.setIssuesCSV(existingIssuesCSV);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 1, "Action didn't return error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned message");
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
