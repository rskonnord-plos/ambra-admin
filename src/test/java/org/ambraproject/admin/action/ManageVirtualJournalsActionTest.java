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
import org.ambraproject.web.VirtualJournalContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Volume;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 1/30/12
 */
public class ManageVirtualJournalsActionTest extends AdminWebTest {

  @Autowired
  protected ManageVirtualJournalsAction action;

  @DataProvider(name = "basicInfo")
  public Object[][] getCurrentIssueAndVolumes() {
    Journal journal = new Journal();
    journal.setId(URI.create("id:test-journal-for-VolumeManagementAction"));
    journal.setKey("journalForTestManageJournals");
    journal.seteIssn("fakeEIssn");
    journal.setVolumes(new ArrayList<URI>(3));

    Issue currentIssue = new Issue();
    currentIssue.setId(URI.create("id:current-issue-for-test-volume"));
    dummyDataStore.store(currentIssue);
    journal.setCurrentIssue(currentIssue.getId());

    List<Volume> volumes = new ArrayList<Volume>(3);

    for (int i = 1; i <= 3; i++) {
      Volume volume = new Volume();
      volume.setDisplayName("200" + i);
      volume.setId(URI.create("id:fake-volume-for-manage-journals" + i));
      dummyDataStore.store(volume);
      journal.getVolumes().add(volume.getId());
      volumes.add(volume);
    }

    dummyDataStore.store(journal);

    return new Object[][]{
        {journal, currentIssue.getId().toString(), volumes}
    };
  }

  @Test(dataProvider = "basicInfo")
  public void testExecute(Journal journal, String currentIssue, List<Volume> volumes) throws Exception {
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");

    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default execute");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");


    assertEquals(action.getJournal().getCurrentIssue(), currentIssue, "action didn't get correct issue");
    assertEquals(action.getVolumes().size(), volumes.size(), "Action returned incorrect number of volumes");
    for (int i = 0; i < volumes.size(); i++) {
      Volume actual = action.getVolumes().get(i);
      Volume expected = volumes.get(i);
      assertEquals(actual.getId(), expected.getId(), "Volume " + (i + 1) + " didn't have correct uri");
      assertEquals(actual.getDisplayName(), expected.getDisplayName(),
          "Volume " + (i + 1) + " didn't have correct display name");
      assertEquals(actual.getId(), expected.getId(), "Volume " + (i + 1) + " didn't have correct uri");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testCreateVolume(Journal journal, String currentIssue, List<Volume> volumes) throws Exception {
    int initialNumberOfVolumes = dummyDataStore.get(journal.getId(), Journal.class).getVolumes().size();
    String volumeUri = "id:new-volume-for-create-volume";
    String volumeDisplayName = "That Still Small Voice";
    //set properties on the action
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("CREATE_VOLUME");
    action.setVolumeURI(volumeUri);
    action.setDisplayName(volumeDisplayName);

    //run the action
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");

    //check action's return values
    assertEquals(action.getVolumes().size(), initialNumberOfVolumes + 1, "action didn't add new volume to list");
    Volume actualVolume = action.getVolumes().get(action.getVolumes().size() - 1);
    assertEquals(actualVolume.getId().toString(), volumeUri, "Volume didn't have correct id");
    assertEquals(actualVolume.getDisplayName(), volumeDisplayName, "Volume didn't have correct id");

    assertTrue(action.getActionMessages().size() > 0, "Action didn't return a message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check values stored to the database
    Journal storedJournal = dummyDataStore.get(journal.getId(), Journal.class);
    assertEquals(storedJournal.getVolumes().size(), initialNumberOfVolumes + 1,
        "journal didn't get volume added in the database");

    assertEquals(storedJournal.getVolumes().get(storedJournal.getVolumes().size() - 1), URI.create(volumeUri),
        "Journal didn't have volume added in the db");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testRemoveVolumes(Journal journal, String currentIssue, List<Volume> volumes) throws Exception {
    List<URI> initialVolumes = dummyDataStore.get(journal.getId(), Journal.class).getVolumes();
    String[] volumesToDelete = new String[]{initialVolumes.get(0).toString(), initialVolumes.get(1).toString()};


    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("REMOVE_VOLUMES");
    action.setVolsToDelete(volumesToDelete);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");

    //check the return values on the action
    assertEquals(action.getVolumes().size(), initialVolumes.size() - 2, "action didn't remove volumes");
    assertTrue(action.getActionMessages().size() > 0, "Action didn't add message for deleting volumes");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");


    List<URI> storedVolumes = dummyDataStore.get(journal.getId(), Journal.class).getVolumes();
    for (String deletedVol : volumesToDelete) {
      assertFalse(storedVolumes.contains(URI.create(deletedVol)), "Volume " + deletedVol + " didn't get removed from journal");
      assertNull(dummyDataStore.get(URI.create(deletedVol), Volume.class), "Volume didn't get removed from the database");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testSetCurrentIssue(Journal journal, String currentIssue, List<Volume> volumes) throws Exception {
    String currentIssueURI = "id:new-issue-uri-to-set";
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("UPDATE_ISSUE");
    action.setCurrentIssueURI(currentIssueURI);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertTrue(action.getActionErrors().size() == 0, "action returned error messages");
    assertTrue(action.getActionMessages().size() > 0, "action didn't return a message indicating success");

    assertEquals(action.getJournal().getCurrentIssue(), currentIssueURI, "action didn't have correct issue uri");

    String storedIssueUri = dummyDataStore.get(journal.getId(), Journal.class).getCurrentIssue().toString();

    assertEquals(storedIssueUri, currentIssueURI, "issue uri didn't get stored to the database");

  }


  private VirtualJournalContext makeVirtualJournalContext(Journal journal) {
    return new VirtualJournalContext(
        journal.getKey(),
        "dfltJournal",
        "http",
        80,
        "localhost",
        "ambra-webapp",
        new ArrayList<String>());
  }

}
