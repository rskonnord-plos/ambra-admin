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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.Volume;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 1/31/12
 */
public class VolumeManagementActionTest extends AdminWebTest {

  @Autowired
  protected VolumeManagementAction action;

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testExecuteWithNoId() throws Exception {
    action.setVolumeURI(null);
    action.execute();
  }

  @DataProvider(name = "basicInfo")
  public Object[][] getBasicInfo() {
    Volume volume = new Volume();
    volume.setId(URI.create("id:volume-for-VolumeManagementAction"));
    volume.setDisplayName("2009");
    volume.setIssueList(new ArrayList<URI>(3));

    List<Issue> issues = new ArrayList<Issue>(3);
    String issuesCSV = "";
    for (int i = 1; i <= 3; i++) {
      Issue issue = new Issue();
      issue.setId(URI.create("id:issue-for-VolumeManagementAction" + i));
      issue.setDisplayName("Month " + i);
      dummyDataStore.store(issue);
      issues.add(issue);
      volume.getIssueList().add(issue.getId());
      issuesCSV += issue.getId().toString();
      if (i < 3) {
        issuesCSV += ',';
      }
    }
    dummyDataStore.store(volume);

    return new Object[][]{
        {volume, issues, issuesCSV}
    };
  }


  @Test(dataProvider = "basicInfo")
  public void testExecute(Volume expectedVolume, List<Issue> expectedIssues, String issuesCSV) throws Exception {
    action.setVolumeURI(expectedVolume.getId().toString());

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");

    assertEquals(action.getVolume().getId(), expectedVolume.getId(), "Action returned incorrect volume");
    assertEquals(action.getVolume().getDisplayName(), expectedVolume.getDisplayName(),
        "Action returned volume with incorrect display name");

    assertEquals(action.getIssuesCSV(), issuesCSV, "Action returned incorrect issue csv list");
    assertEquals(action.getIssues().size(), expectedIssues.size(), "Action returned incorrect number of issues");
    for (int i = 0; i < expectedIssues.size(); i++) {
      Issue actualIssue = action.getIssues().get(i);
      Issue expectedIssue = expectedIssues.get(i);
      assertEquals(actualIssue.getId(), expectedIssue.getId(), "Issue " + (i + 1) + " had incorrect URI");
      assertEquals(actualIssue.getDisplayName(), expectedIssue.getDisplayName(), "Issue " + (i + 1) + " had incorrect display name");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testCreateIssue(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {

    String issueURI = "id:new-issue-to-create";
    String displayName = "New Display Name";
    String imageURI = "id:image-article-for-create-issue";

    List<URI> existingIssues = dummyDataStore.get(Volume.class, volume.getId()).getIssueList();
    String expectedIssuesCSV = StringUtils.join(existingIssues, ",");
    expectedIssuesCSV += ("," + issueURI);

    action.setVolumeURI(volume.getId().toString());
    action.setCommand("CREATE_ISSUE");
    action.setIssueURI(issueURI);
    action.setDisplayName(displayName);
    action.setImageURI(imageURI);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");

    //check properties on action
    assertEquals(action.getVolume().getId(), volume.getId(), "Action changed volumes after creating issue");
    assertEquals(action.getIssuesCSV(), expectedIssuesCSV, "Action didn't return correct issues csv");
    assertEquals(action.getIssues().size(), existingIssues.size() + 1, "action didn't add issue to list");

    for (int i = 0; i < existingIssues.size(); i++) {
      assertEquals(action.getIssues().get(i).getId(), existingIssues.get(i),
          "Action reordered issues; issue " + (i + 1) + " changed");
    }
    Issue lastIssue = action.getIssues().get(action.getIssues().size() - 1);
    assertEquals(lastIssue.getId().toString(), issueURI, "Issue didn't get created with correct URI");
    assertEquals(lastIssue.getDisplayName(), displayName, "Issue didn't get created with correct display name");
    assertEquals(lastIssue.getImage().toString(), imageURI, "Issue didn't get created with correct image URI");

    //check what got added to the db
    Issue storedIssue = dummyDataStore.get(Issue.class, URI.create(issueURI));
    assertNotNull(storedIssue, "issue didn't get stored to the database");
    assertEquals(storedIssue.getDisplayName(), displayName, "Issue didn't get stored with correct display name");
    assertEquals(storedIssue.getImage(), URI.create(imageURI), "Issue didn't get stored with correct image URI");

    List<URI> storedIssues = dummyDataStore.get(Volume.class, volume.getId()).getIssueList();
    assertEquals(storedIssues.size(), existingIssues.size() + 1, "issue didn't get added to volume in the database");
    for (int i = 0; i < existingIssues.size(); i++) {
      assertEquals(storedIssues.get(i), existingIssues.get(i),
          "Issues got reordered in the database; issue " + (i + 1) + " changed");
    }
    assertEquals(storedIssues.get(storedIssues.size() - 1), URI.create(issueURI),
        "Issue didn't get added to volume in the db");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testDeleteIssues(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {
    List<URI> existingIssues = dummyDataStore.get(Volume.class, volume.getId()).getIssueList();
    String[] issuesToDelete = new String[]{existingIssues.get(0).toString(), existingIssues.get(1).toString()};
    String expectedIssuesCSV = StringUtils.join(existingIssues.subList(2, existingIssues.size()), ",");

    action.setVolumeURI(volume.getId().toString());
    action.setCommand("REMOVE_ISSUES");
    action.setIssuesToDelete(issuesToDelete);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");

    //check properties on action
    assertEquals(action.getVolume().getId(), volume.getId(), "Action changed volumes after deleting issue");
    assertEquals(action.getIssuesCSV(), expectedIssuesCSV, "Action didn't return correct issues csv");
    assertEquals(action.getIssues().size(), existingIssues.size() - 2, "action didn't remove issues from list");

    for (int i = 0; i < action.getIssues().size(); i++) {
      assertEquals(action.getIssues().get(i).getId(), existingIssues.get(i + 2),
          "Action reordered issues; issue " + (i + 1) + " changed");
    }

    //check what got added to the db
    List<URI> storedIssues = dummyDataStore.get(Volume.class, volume.getId()).getIssueList();
    assertEquals(storedIssues.size(), existingIssues.size() - 2, "issues didn't get removed from volume in the database");
    for (int i = 0; i < storedIssues.size(); i++) {
      assertEquals(storedIssues.get(i), existingIssues.get(i + 2),
          "Issues got reordered in the database; issue " + (i + 1) + " changed");
    }
    for (String issueURI : issuesToDelete) {
      assertNull(dummyDataStore.get(Issue.class, URI.create(issueURI)), "Issue didn't get deleted from the database");
      assertFalse(storedIssues.contains(URI.create(issueURI)),
          "Issue " + issueURI + " didn't get removed from volume in the database");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testUpdateVolume(Volume volume, List<Issue> issues, String issuesCSV) throws Exception {
    List<URI> existingIssues = dummyDataStore.get(Volume.class, volume.getId()).getIssueList();
    String csv = StringUtils.join(existingIssues, ",");

    String reorderedCsv = csv;
    String issueToReorder = csv.substring(0, csv.indexOf(","));
    reorderedCsv = reorderedCsv.replaceFirst(issueToReorder + ",", "");
    reorderedCsv += ("," + issueToReorder);
    String displayName = "Fruit of the Poisonous Tree";

    action.setCommand("UPDATE_VOLUME");
    action.setDisplayName(displayName);
    action.setIssuesToOrder(reorderedCsv);
    action.setVolumeURI(volume.getId().toString());

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertTrue(action.getActionMessages().size() > 1, "Action didn't return message(s) indicating success");

    assertEquals(action.getVolume().getId(), volume.getId(), "Action changed volume after updating");
    assertEquals(action.getIssuesCSV(), reorderedCsv, "Action didn't return correct csv");
    assertEquals(action.getVolume().getDisplayName(), displayName, "Action didn't have correct display name");

    //check results in db
    Volume storedVolume = dummyDataStore.get(Volume.class, volume.getId());
    assertEquals(storedVolume.getDisplayName(), displayName, "Volume didn't get display name changed in the db");
    String[] expectedIssues = reorderedCsv.split(",");
    assertEquals(storedVolume.getIssueList().size(), expectedIssues.length, "Number of issues changed in the database");
    assertEquals(storedVolume.getIssueList().size(), existingIssues.size(), "Number of issues changed in the database");

    for (int i = 0; i < expectedIssues.length; i++) {
      assertEquals(storedVolume.getIssueList().get(i), URI.create(expectedIssues[i]),
          "Issues weren't in correct order; element " + (i + 1) + " was incorrect");
    }

  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
