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
import org.ambraproject.admin.AdminWebTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.Volume;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

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


}
