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

package org.ambraproject.admin.service;

import org.ambraproject.ApplicationException;
import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.admin.DummyOnCrossPublishListener;
import org.ambraproject.article.service.ArticleService;
import org.ambraproject.model.article.ArticleInfo;
import org.ambraproject.model.article.ArticleType;
import org.ambraproject.models.Article;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Journal;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.models.Volume;
import org.ambraproject.permission.service.PermissionsService;
import org.ambraproject.views.TOCArticleGroup;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudick  1/3/12
 */
public class AdminServiceTest extends AdminBaseTest {

  @Autowired
  protected AdminService adminService;

  @Autowired
  private DummyOnCrossPublishListener onCrossPublishListener;

  //get the configuration autowired to be sure it's been intitialized by the time we test article types
  @Autowired
  protected Configuration configuraion;

  @Test
  public void testUpdateIssue() {
    //set up an issue to update
    Issue issue = new Issue("id:issueToUpdate");
    issue.setTitle("title should get overwritten");
    issue.setDescription("description should get overwritten");
    issue.setDisplayName("old display name");
    issue.setRespectOrder(false);
    issue.setArticleDois(Arrays.asList(
        "id:issue-article-1",
        "id:issue-article-2",
        "id:issue-article-3"
    ));
    dummyDataStore.store(issue);
    Article imageArticle = new Article();
    imageArticle.setDoi("id:foo-doi-for-updating-issue");
    imageArticle.setDescription("This description should overwrite what's on the issue");
    imageArticle.setTitle("This title should overwrite what's on the issue");
    dummyDataStore.store(imageArticle);

    List<String> reorderedArticleList = new ArrayList<String>(issue.getArticleDois());
    reorderedArticleList.add(reorderedArticleList.get(1));
    reorderedArticleList.remove(1);

    String displayName = "new display name";
    adminService.updateIssue(issue.getIssueUri(), imageArticle.getDoi(),
        displayName, true, reorderedArticleList);

    //check the properties on the issue from the db
    Issue storedIssue = dummyDataStore.get(Issue.class, issue.getID());
    assertEquals(storedIssue.getIssueUri(), issue.getIssueUri(), "changed issue uri");
    assertEquals(storedIssue.getImageUri(), imageArticle.getDoi(), "issue had incorrect image uri");

    assertEquals(storedIssue.getDescription(), imageArticle.getDescription(), "issue didn't get description updated from image article");
    assertEquals(storedIssue.getTitle(), imageArticle.getTitle(), "issue didn't get title updated from article");
    assertEquals(storedIssue.getDisplayName(), displayName, "issue had incorrect display name");

    assertEquals(storedIssue.isRespectOrder(), true, "issue had incorrect respectOrder attribute");
    assertEquals(storedIssue.getArticleDois(), reorderedArticleList, "Issue had incorrect article list");
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testUpdateIssueDoesNotAllowAddArticle() {
    Issue issue = new Issue("id:issueForUpdateAddArticle");
    dummyDataStore.store(issue);
    adminService.updateIssue(issue.getIssueUri(), "imageUri", "display name", true, Arrays.asList("newDoi"));
  }


  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testUpdateIssueDoesNotAllowRemoveArticle() {
    Issue issue = new Issue("id:issueForUpdateRemoveArticle");
    issue.setArticleDois(Arrays.asList("doi1", "doi2", "doi3"));
    dummyDataStore.store(issue);
    adminService.updateIssue(issue.getIssueUri(), "imageUri", "display name", true, issue.getArticleDois().subList(0, 1));
  }

  @Test
  public void testAddIssueToVolume() {
    Article imageArticle = new Article();
    imageArticle.setDoi("id:doi-for-creating-issue");
    imageArticle.setTitle("Once Upon a Time");
    imageArticle.setDescription("Centers on a woman with a troubled past who is drawn into a small town in " +
        "Maine where the magic and mystery of Fairy Tales just may be real. ");
    dummyDataStore.store(imageArticle);

    Volume volume = new Volume("id:volumeAddIssueToVolume");
    dummyDataStore.store(volume);

    Issue issue = new Issue("id:newIssueUriToCreate");
    issue.setDisplayName("display name for new issue to create");
    issue.setArticleDois(Arrays.asList(
        "newDoi1",
        "newDoi2",
        "newDoi3"
    ));
    issue.setImageUri(imageArticle.getDoi());

    Long issueID = adminService.addIssueToVolume(volume.getVolumeUri(), issue);
    assertNotNull(issue, "returned null issue id");

    Issue storedIssue = dummyDataStore.get(Issue.class, issueID);
    assertNotNull(storedIssue, "Issue didn't get stored to the db");
    assertEquals(storedIssue.getArticleDois().toArray(), issue.getArticleDois().toArray(), "storedIssue had incorrect article list");
    assertEquals(storedIssue.getImageUri(), issue.getImageUri(), "storedIssue had incorrect image uri");

    assertEquals(storedIssue.getDescription(), imageArticle.getDescription(), "storedIssue didn't get description updated from image article");
    assertEquals(storedIssue.getTitle(), imageArticle.getTitle(), "storedIssue didn't get title updated from article");
    assertEquals(storedIssue.getDisplayName(), storedIssue.getDisplayName(), "storedIssue had incorrect display name");

    assertEquals(dummyDataStore.get(Volume.class, volume.getID()).getIssues().size(), 1,
        "Issue didn't get added to volume");
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testCreateIssueWithNoIssueUri() {
    Volume volume = new Volume("id:volumeForIssueWithNoUri");
    dummyDataStore.store(volume);

    adminService.addIssueToVolume(volume.getVolumeUri(), new Issue(""));
  }

  @DataProvider(name = "journalAndArticle")
  public Object[][] getCrossPubArticle() {
    Journal journal = new Journal();
    journal.setJournalKey("PLoSFakeJournal");
    dummyDataStore.store(journal);

    Article article = new Article();
    article.setDoi("id:article-to-x-pub");
    article.seteIssn(defaultJournal.geteIssn());
    article.setJournals(new HashSet<Journal>());
    article.getJournals().add(defaultJournal);
    dummyDataStore.store(article);

    return new Object[][]{
        {journal.getJournalKey(), article}
    };
  }

  @Test(dataProvider = "journalAndArticle")
  public void testAddXPubArticles(String journalName, Article article) throws Exception {
    int originalInvocationCount = onCrossPublishListener.getInvocationCount();
    adminService.crossPubArticle(article.getDoi(), journalName);
    boolean foundMatch = false;
    for (Journal journal : dummyDataStore.get(Article.class, article.getID()).getJournals()) {
      if (journalName.equals(journal.getJournalKey())) {
        foundMatch = true;
        break;
      }
    }
    assertTrue(foundMatch, "Didn't add journal to article");
    assertTrue(onCrossPublishListener.getInvocationCount() == originalInvocationCount + 1, "Didn't invoke onCrossPublishListener");
  }

  @Test(dataProvider = "journalAndArticle", dependsOnMethods = {"testAddXPubArticles"}, alwaysRun = false)
  public void testRemoveXPubArticle(String journalName, Article article) throws Exception {
    int originalInvocationCount = onCrossPublishListener.getInvocationCount();
    adminService.removeArticleFromJournal(article.getDoi(), journalName);
    boolean foundMatch = false;
    for (Journal journal : dummyDataStore.get(Article.class, article.getID()).getJournals()) {
      if (journalName.equals(journal.getJournalKey())) {
        foundMatch = true;
        break;
      }
    }
    assertFalse(foundMatch, "Didn't remove journal from article");
    assertTrue(onCrossPublishListener.getInvocationCount() == originalInvocationCount + 1, "Didn't invoke onCrossPublishListener");
  }

  @Test
  public void testGetCrosspubbedArticles() {
    Journal journal = new Journal("crossPubJournal");
    journal.seteIssn("testEissnCrossPub");
    dummyDataStore.store(journal);
    List<String> dois = new ArrayList<String>(3);
    for (int i = 0; i < 3; i++) {
      Article article = new Article("id:crosspubbed-article-" + i);
      article.seteIssn(defaultJournal.geteIssn());
      article.setJournals(new HashSet<Journal>(2));
      article.getJournals().add(defaultJournal);
      article.getJournals().add(journal);
      dummyDataStore.store(article);
      dois.add(article.getDoi());
    }

    Article nonCrossPubbedArticle = new Article("id:non-crosspubbed-article");
    nonCrossPubbedArticle.seteIssn(defaultJournal.geteIssn());
    nonCrossPubbedArticle.setJournals(new HashSet<Journal>(1));
    nonCrossPubbedArticle.getJournals().add(defaultJournal);
    dummyDataStore.store(nonCrossPubbedArticle);

    Article articlePubbedInJournal = new Article("id:articlePubbedInJournal");
    articlePubbedInJournal.seteIssn(journal.geteIssn());
    articlePubbedInJournal.setJournals(new HashSet<Journal>(1));
    articlePubbedInJournal.getJournals().add(journal);
    dummyDataStore.store(articlePubbedInJournal);

    List<String> results = adminService.getCrossPubbedArticles(journal);
    assertNotNull(results, "returned null doi list");
    assertEqualsNoOrder(results.toArray(), dois.toArray(), "returned incorrect dois");
  }

  @Test
  public void testCreateVolume() throws URISyntaxException {
    Journal journal = new Journal();
    journal.setJournalKey("test journal for createVolume");
    dummyDataStore.store(journal);

    String volumeUri = "id:volume_to_create";
    String displayName = "Some Fake Display Name";

    Volume result = adminService.createVolume(journal.getJournalKey(), volumeUri, displayName);

    assertNotNull(result, "returned null result");
    assertNotNull(result.getID(), "result had null id");
    assertEquals(result.getVolumeUri(), volumeUri, "result didn't have correct uri");
    assertEquals(result.getDisplayName(), displayName, "result didn't have correct display name");

    Volume storedVolume = dummyDataStore.get(Volume.class, result.getID());
    assertNotNull(storedVolume, "volume didn't get stored to the database");
    assertEquals(storedVolume.getVolumeUri(), volumeUri, "stored volume didn't have correct uri");
    assertEquals(storedVolume.getDisplayName(), displayName, "stored volume didn't have correct display name");

    List<Volume> storedVolumeList = dummyDataStore.get(Journal.class, journal.getID()).getVolumes();
    assertEquals(storedVolumeList.size(), 1, "volume didn't get added to journal");
    assertEquals(storedVolumeList.get(0).getVolumeUri(), volumeUri, "stored volume didn't have correct uri");
    assertEquals(storedVolumeList.get(0).getDisplayName(), displayName, "stored volume didn't have correct display name");

    //trying to create again should return null
    assertNull(adminService.createVolume(journal.getJournalKey(), volumeUri, "foo"),
        "Creating duplicate volume should return null");
    assertEquals(dummyDataStore.get(Journal.class, journal.getID()).getVolumes().size(), 1, "Duplicate volume got added to journal");
    assertEquals(dummyDataStore.get(Volume.class, result.getID()).getDisplayName(), displayName, "storing duplicate volume changed display name");
  }

  @Test
  public void testSetCurrentIssue() {
    Journal journal = new Journal("journal for setCurrentIssue");
    journal.setCurrentIssue(new Issue("id:oldCurrentIssue"));
    journal.getCurrentIssue().setDescription("old current issue description");
    journal.getCurrentIssue().setDisplayName("old current issue display name");
    journal.getCurrentIssue().setTitle("old current issue title");
    journal.getCurrentIssue().setArticleDois(Arrays.asList("oldDoi1", "oldDoi2"));
    dummyDataStore.store(journal.getCurrentIssue());
    dummyDataStore.store(journal);

    Issue issue = new Issue("id:newCurrentIssue");
    issue.setDescription("new current issue description");
    issue.setDisplayName("new current issue display name");
    issue.setTitle("new current issue title");
    issue.setArticleDois(Arrays.asList("newDoi1", "newDoi2", "newDoi3"));
    dummyDataStore.store(issue);


    adminService.setCurrentIssue(journal.getJournalKey(), issue.getIssueUri());
    Journal storedJournal = dummyDataStore.get(Journal.class, journal.getID());
    assertNotNull(storedJournal, "journal got removed from the db");
    assertNotNull(storedJournal.getCurrentIssue(), "stored journal had no current issue");
    assertEquals(storedJournal.getCurrentIssue(), issue, "stored journal had incorrect current issue");
  }


  @Test
  public void testGetVolumes() {
    //create a journal and some volumes
    Journal journal = new Journal();
    journal.setJournalKey("Fake Journal for get Volumes");
    journal.setVolumes(new ArrayList<Volume>(3));

    Volume volume1 = new Volume();
    volume1.setDisplayName("Display name for volume 1");
    volume1.setImageUri("id:image-for-vol1");
    volume1.setVolumeUri("id:testGetVolumes-volume1");
    dummyDataStore.store(volume1);
    journal.getVolumes().add(volume1);

    Volume volume2 = new Volume();
    volume2.setDisplayName("Display name for volume 2");
    volume2.setImageUri("id:image-for-vol2");
    volume2.setVolumeUri("id:testGetVolumes-volume2");
    dummyDataStore.store(volume2);
    journal.getVolumes().add(volume2);

    Volume volume3 = new Volume();
    volume3.setDisplayName("Display name for volume 3");
    volume3.setImageUri("id:image-for-vol3");
    volume3.setVolumeUri("id:testGetVolumes-volume3");
    dummyDataStore.store(volume3);
    journal.getVolumes().add(volume3);

    dummyDataStore.store(journal);

    List<Volume> result = adminService.getVolumes(journal.getJournalKey());
    assertNotNull(result, "returned null list of volumes");
    assertEquals(result.size(), 3, "returned incorrect number of volumes");

    assertEquals(result.get(0).getDisplayName(), volume1.getDisplayName(), "Volume 1 had incorrect display name");
    assertEquals(result.get(0).getImageUri(), volume1.getImageUri(), "Volume 1 had incorrect image article");

    assertEquals(result.get(1).getDisplayName(), volume2.getDisplayName(), "Volume 2 had incorrect display name");
    assertEquals(result.get(1).getImageUri(), volume2.getImageUri(), "Volume 2 had incorrect image article");

    assertEquals(result.get(2).getDisplayName(), volume3.getDisplayName(), "Volume 3 had incorrect display name");
    assertEquals(result.get(2).getImageUri(), volume3.getImageUri(), "Volume 3 had incorrect image article");
  }

  @Test
  public void testGetJournal() {
    //save a journal
    Journal journal = new Journal();
    journal.setJournalKey("journal key for testGetJournal");
    journal.setDescription("description for testGetJournal");
    journal.seteIssn("eIssn for testGetJournal");
    journal.setTitle("title for testGetJournal");

    dummyDataStore.store(journal);

    Journal result = adminService.getJournal(journal.getJournalKey());
    assertNotNull(result, "returned null result");
    assertEquals(result, journal, "returned incorrect journal");
  }

  @DataProvider(name = "volume")
  public Object[][] getVolume() {
    Volume volume = new Volume();
    volume.setVolumeUri("id:volume-for-get-volume");
    volume.setImageUri("id:test-image-article");
    volume.setDisplayName("Test Volume");
    volume.setIssues(Arrays.asList(
        new Issue("id:issue-in-vol1"),
        new Issue("id:issue-in-vol2"),
        new Issue("id:issue-in-vol3")
    ));
    for (Issue issue : volume.getIssues()) {
      issue.setArticleDois(new ArrayList<String>(0));
    }

    dummyDataStore.store(volume.getIssues());
    dummyDataStore.store(volume);

    return new Object[][]{
        {volume}
    };
  }

  @Test(dataProvider = "volume")
  public void testGetVolume(Volume volume) {
    Volume result = adminService.getVolume(volume.getVolumeUri());
    assertNotNull(result, "returned null volume");
    assertEquals(result.getVolumeUri(), volume.getVolumeUri(), "returned incorrect volume");
    assertEquals(result.getDisplayName(), volume.getDisplayName(),
        "Volume didn't have correct display name");
    assertEquals(result.getImageUri(), volume.getImageUri(),
        "Volume didn't have correct image uri");

    assertNull(adminService.getVolume("non-existent-volume"), "didn't return null for non-existent volume");
  }

  @Test(dataProvider = "volume")
  public void testGetIssues(Volume volume) {
    List<Issue> issues = adminService.getIssues(volume.getVolumeUri());
    assertNotNull(issues, "returned null issue list");
    assertEquals(issues.toArray(), volume.getIssues().toArray(), "returned incorrect issues");
  }

  @Test(dataProvider = "volume")
  public void testGetIssuesCSV(Volume volume) {
    List<String> issueUris = new ArrayList<String>(volume.getIssues().size());
    for (Issue issue : volume.getIssues()) {
      issueUris.add(issue.getIssueUri());
    }
    String expectedCsv = StringUtils.join(issueUris, ",");

    String actualCsv = adminService.formatIssueCsv(volume.getIssues());
    assertNotNull(actualCsv, "returned null csv");
    assertEquals(actualCsv, expectedCsv, "returned incorrect csv");
  }

  @Test(dataProvider = "volume",
      dependsOnMethods = {"testGetVolume", "testGetIssues", "testGetIssuesCSV"})
  public void testUpdateVolume(Volume volume) throws URISyntaxException {
    String newDisplayName = "Updated Display Name";

    List<Issue> newIssueList = new ArrayList<Issue>(
        dummyDataStore.get(Volume.class, volume.getID()).getIssues());
    newIssueList.add(newIssueList.get(1));
    newIssueList.remove(1);


    adminService.updateVolume(volume.getVolumeUri(), newDisplayName, adminService.formatIssueCsv(newIssueList));
    Volume storedVolume = dummyDataStore.get(Volume.class, volume.getID());
    assertEquals(storedVolume.getDisplayName(), newDisplayName, "volume didn't get display name updated");
    assertEquals(storedVolume.getIssues(), newIssueList, "Volume didn't have correct issue list");
  }

  @Test(dataProvider = "volume", expectedExceptions = {IllegalArgumentException.class})
  public void testUpdateVolumeDoesNotAllowAddIssue(Volume volume) throws URISyntaxException {
    String issueCsv = adminService.formatIssueCsv(volume.getIssues());
    issueCsv += ",id:some-new-issue";
    adminService.updateVolume(volume.getVolumeUri(), "foo", issueCsv);
  }

  @Test(dataProvider = "volume", expectedExceptions = {IllegalArgumentException.class})
  public void testUpdateVolumeDoesNotAllowRemoveIssue(Volume volume) throws URISyntaxException {
    String issueCsv = adminService.formatIssueCsv(volume.getIssues());
    issueCsv = issueCsv.substring(0, issueCsv.lastIndexOf(","));
    adminService.updateVolume(volume.getVolumeUri(), "foo", issueCsv);
  }

  @Test
  public void testDeleteVolumes() {
    Journal journal = new Journal("test journal for delete volumes");
    Volume volume1 = new Volume("id:volumeToDelete1");
    Volume volume2 = new Volume("id:volumeToDelete2");
    Volume volume3 = new Volume("id:volumeToDelete3");
    Volume volume4 = new Volume("id:volumeToDelete4");
    journal.setVolumes(Arrays.asList(volume1, volume2, volume3, volume4));
    dummyDataStore.store(journal);

    String[] deletedVolumes = adminService.deleteVolumes(journal.getJournalKey(), volume1.getVolumeUri(),
        volume3.getVolumeUri(), volume4.getVolumeUri());
    assertEquals(deletedVolumes, new String[]{volume1.getVolumeUri(), volume3.getVolumeUri(), volume4.getVolumeUri()},
        "Didn't return correct array of deleted volumes");

    Journal storedJournal = dummyDataStore.get(Journal.class, journal.getID());

    assertNotNull(storedJournal, "journal got removed from the db");
    assertEquals(storedJournal.getVolumes().size(), 1, "didn't remove volumes from journal list");
    assertEquals(storedJournal.getVolumes().get(0), volume2, "removed incorrect volumes from journal list");

    for (Volume volume : Arrays.asList(volume1, volume3, volume4)) {
      assertNull(dummyDataStore.get(Volume.class, volume.getID()),
          "didn't remove volume '" + volume.getVolumeUri() + "' from the database");
    }
  }

  @DataProvider(name = "issue")
  public Object[][] getIssue() {
    Issue issue = new Issue("id:test-issue-id");
    issue.setTitle("The Thing You Love Most");
    issue.setDescription("Regina does everything in her power to force Emma out of Storybrooke and out of her " +
        "and Henry's lives forever. Meanwhile, the chilling circumstances of how the Evil Queen released the " +
        "curse upon the fairytale world is revealed.");
    issue.setArticleDois(new ArrayList<String>(3));

    for (int i = 1; i <= 3; i++) {
      Article article = new Article();
      article.setDoi("id:doi-for-issue-test" + i);
      dummyDataStore.store(article);
      issue.getArticleDois().add(article.getDoi());
    }

    dummyDataStore.store(issue);

    Volume volume = new Volume();
    volume.setVolumeUri("id:test-vol-for-issues1");
    volume.setIssues(Arrays.asList(issue));
    dummyDataStore.store(volume);

    return new Object[][]{
        {issue, volume}
    };
  }

  @Test(dataProvider = "issue")
  public void testGetIssue(Issue expectedIssue, Volume volume) {
    Issue issue = adminService.getIssue(expectedIssue.getIssueUri());
    assertNotNull(issue, "returned null issue");
    assertEquals(issue, expectedIssue, "returned incorrect issue");
  }

  @Test(dataProvider = "issue", dependsOnMethods = {"testGetIssue"}, alwaysRun = true)
  public void testRemoveArticle(Issue issue, Volume volume) {
    Issue storedIssue = dummyDataStore.get(Issue.class, issue.getID());
    String[] doisToRemove = storedIssue.getArticleDois().subList(1, 3).toArray(new String[2]);
    int currentNumArticles = storedIssue.getArticleDois().size();

    adminService.removeArticlesFromIssue(storedIssue.getIssueUri(), doisToRemove);
    storedIssue = dummyDataStore.get(Issue.class, issue.getID());

    assertEquals(storedIssue.getArticleDois().size(), currentNumArticles - 2, "didn't remove articles from issue");
    for (String doi : doisToRemove) {
      assertFalse(storedIssue.getArticleDois().contains(doi), "article doi didn't get removed from issue");
    }
  }

  @Test(dataProvider = "issue", dependsOnMethods = {"testGetIssue"}, alwaysRun = true)
  public void testAddArticlesToIssue(Issue issue, Volume volume) {
    int currentNumDois = dummyDataStore.get(Issue.class, issue.getID()).getArticleDois().size();
    //create an article and add it to the issue
    Article article = new Article("id:articleWhichWillBeAddedToAnIssue");
    dummyDataStore.store(article);


    String nonExistentDoi = "id:nonExistentArticle";
    adminService.addArticlesToIssue(issue.getIssueUri(),
        article.getDoi(), nonExistentDoi, issue.getArticleDois().get(0)); //it shouldn't add the dup doi to list

    List<String> storedDois = dummyDataStore.get(Issue.class, issue.getID()).getArticleDois();
    assertEquals(storedDois.size(), currentNumDois + 2, "Didn't add dois to article list");
    assertEquals(storedDois.get(storedDois.size() - 2), article.getDoi(), "doi for existing article didn't get added to issue");
    assertEquals(storedDois.get(storedDois.size() - 1), nonExistentDoi, "doi for nonexistent article didn't get added to issue");
  }

  @DataProvider
  public Object[][] issueArticleGroups() {
    //set up issue with articles of each type
    Issue issue = new Issue("id:testIssueForArticleGroupList");
    issue.setRespectOrder(false);
    issue.setArticleDois(new ArrayList<String>());

    Issue manualOrderIssue = new Issue("id:testIssueForArticleGroupListRespectOrder");
    manualOrderIssue.setRespectOrder(true);
    manualOrderIssue.setArticleDois(new ArrayList<String>());

    Calendar oldDate = Calendar.getInstance();
    oldDate.add(Calendar.YEAR, -1);

    //put in articles of each type except for the last one
    Map<ArticleType, List<Article>> expectedArticles = new HashMap<ArticleType, List<Article>>();
    for (int j = 0; j < ArticleType.getOrderedListForDisplay().size() - 1; j++) {
      ArticleType articleType = ArticleType.getOrderedListForDisplay().get(j);
      expectedArticles.put(articleType, new ArrayList<Article>(2));
      for (int i = 1; i <= 2; i++) {
        Article article = new Article("id:articleForType" + articleType.getHeading().replaceAll(" ", "_") + i);
        article.setTitle("title " + i + " for article of type " + articleType.getHeading());
        article.setTypes(new HashSet<String>(2));
        article.getTypes().add(articleType.getUri().toString());
        article.getTypes().add("foo");
        if (i == 1) {
          article.setDate(Calendar.getInstance().getTime());
        } else {
          article.setDate(oldDate.getTime());
        }

        issue.getArticleDois().add(article.getDoi());
        dummyDataStore.store(article);
        expectedArticles.get(articleType).add(article);
      }
    }

    String nonExistentDoi = "id:nonexistentArticle";
    issue.getArticleDois().add(nonExistentDoi);

    Article unknownTypeArticle = new Article("id:articleWithUnknownType");
    unknownTypeArticle.setTypes(new HashSet<String>());
    unknownTypeArticle.getTypes().add("id:someStrangeType");
    dummyDataStore.store(unknownTypeArticle);
    issue.getArticleDois().add(unknownTypeArticle.getDoi());

    dummyDataStore.store(issue);

    manualOrderIssue.setArticleDois(new ArrayList<String>());
    manualOrderIssue.getArticleDois().addAll(issue.getArticleDois());
    Collections.sort(manualOrderIssue.getArticleDois(), new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return -1 * o1.compareTo(o2);
      }
    });

    Map<ArticleType, List<Article>> manualOrderExpectedArticles = new HashMap<ArticleType, List<Article>>();
    for (ArticleType type : expectedArticles.keySet()) {
      List<Article> list = new ArrayList<Article>();
      list.addAll(expectedArticles.get(type));
      Collections.sort(list, new Comparator<Article>() {
        @Override
        public int compare(Article o1, Article o2) {
          return -1 * o1.getDoi().compareTo(o2.getDoi());
        }
      });
      manualOrderExpectedArticles.put(type, list);
    }

    dummyDataStore.store(manualOrderIssue);

    return new Object[][]{
        {issue, expectedArticles, nonExistentDoi, unknownTypeArticle},
        {manualOrderIssue, manualOrderExpectedArticles, nonExistentDoi, unknownTypeArticle}
    };
  }

  @Test(dataProvider = "issueArticleGroups")
  public void testGetArticleGroupList(Issue issue, Map<ArticleType, List<Article>> expectedArticles, String nonExistentDoi, Article unknownTypeArticle) {
    List<TOCArticleGroup> articleGroupList = adminService.getArticleGroupList(issue);
    assertNotNull(articleGroupList, "returned null article group list");

    //there should be an extra group for orphaned articles, but we didn't include any articles for the last article type
    assertEquals(articleGroupList.size(), ArticleType.getOrderedListForDisplay().size(),
        "Returned incorrect number of article groups");

    for (int i = 0; i < ArticleType.getOrderedListForDisplay().size() - 1; i++) {
      TOCArticleGroup group = articleGroupList.get(i);
      ArticleType type = ArticleType.getOrderedListForDisplay().get(i);
      assertEquals(group.getArticleType(), type,
          "Returned types in incorrect order; element " + (i + 1) + " had incorrect article type");
      assertEquals(group.getHeading(), type.getHeading(), "Element " + (i + 1) + " had incorrect heading");
      assertEquals(group.getPluralHeading(), type.getPluralHeading(),
          "Element " + (i + 1) + " had incorrect plural heading");
      assertEquals(group.getCount(), expectedArticles.get(type).size(), "Element " + (i + 1) + " had incorrect count");
      assertEquals(group.getArticles().size(), expectedArticles.get(type).size(), "Element " + (i + 1) + " had incorrect number of articles");

      //check the articles in the group
      for (int j = 0; j < group.getArticles().size(); j++) {
        ArticleInfo actualArticle = group.getArticles().get(j);
        Article expectedArticle = expectedArticles.get(type).get(j);
        assertEquals(actualArticle.getDoi(), expectedArticle.getDoi(),
            "Article " + (j + 1) + " for type '" + type.getHeading() + "' had incorrect doi");
        assertEquals(actualArticle.getTitle(), expectedArticle.getTitle(),
            "Article " + (j + 1) + " for type '" + type.getHeading() + "' had incorrect title");
      }

    }
    //should have added a group for orphans last
    TOCArticleGroup lastGroup = articleGroupList.get(articleGroupList.size() - 1);
    assertEquals(lastGroup.getPluralHeading(), "Orphaned Articles", "Orphaned article group had incorrect heading");
    assertEquals(lastGroup.getArticles().size(), 2, "Orphaned article group didn't include all orphans");
    boolean foundMatchForNonExistentArticle = false;
    boolean foundMatchForUnknownTypeArticle = false;
    for (ArticleInfo article : lastGroup.getArticles()) {
      if (nonExistentDoi.equals(article.getDoi())) {
        foundMatchForNonExistentArticle = true;
      } else if (unknownTypeArticle.getDoi().equals(article.getDoi())) {
        foundMatchForUnknownTypeArticle = true;
      }
    }
    assertTrue(foundMatchForNonExistentArticle, "Didn't return doi for non-existent article in orphan list");
    assertTrue(foundMatchForUnknownTypeArticle, "Didn't return doi for article with unknown type in orphan list");
  }

  //be sure getArticleGroupList() still works when the issue doesn't have orphans and doesn't have dois in all article types
  @Test
  public void testGetArticleGroupListRunsOutOfDois() {
    Issue issue = new Issue("id:issueThatRunsOutOfDois");
    Calendar oldDate = Calendar.getInstance();
    oldDate.add(Calendar.YEAR, -1);
    Article article1 = new Article("id:doiForIssueThatRunsOutOfDois1");
    article1.setTypes(new HashSet<String>(2));
    article1.getTypes().add(ArticleType.getOrderedListForDisplay().get(0).getUri().toString());
    article1.setDate(Calendar.getInstance().getTime());
    dummyDataStore.store(article1);

    Article article2 = new Article("id:doiForIssueThatRunsOutOfDois2");
    article2.setTypes(new HashSet<String>(2));
    article2.getTypes().add(ArticleType.getOrderedListForDisplay().get(0).getUri().toString());
    article2.setDate(oldDate.getTime());
    dummyDataStore.store(article2);

    issue.setArticleDois(Arrays.asList(article1.getDoi(), article2.getDoi()));
    dummyDataStore.store(issue);

    List<TOCArticleGroup> groups = adminService.getArticleGroupList(issue);
    assertNotNull(groups, "returned null groups");
    //should be a group for orphans too
    assertEquals(groups.size(), 2, "returned incorrect number of groups");
    assertEquals(groups.get(0).getArticleType(), ArticleType.getOrderedListForDisplay().get(0),
        "returned group for incorrect article type");
    assertEquals(groups.get(0).getArticles().size(), 2, "returned incorrect number of articles");
    assertEquals(groups.get(0).getArticles().get(0).getDoi(), article1.getDoi(),
        "returned incorrect article");
    assertEquals(groups.get(0).getArticles().get(1).getDoi(), article2.getDoi(),
        "returned incorrect article");
  }

  @Test
  public void testGetArticleGroupListWithEmptyIssue() {
    Issue issue = new Issue("id:emptyIssueForGetArticleGroupList");
    issue.setArticleDois(new ArrayList<String>(0));
    dummyDataStore.store(issue);
    List<TOCArticleGroup> articleGroupList = adminService.getArticleGroupList(issue);
    assertNotNull(articleGroupList, "Returned null article group list");
    assertTrue(articleGroupList.isEmpty(), "Returned non-null article group list");
  }

  @Test(dataProvider = "issueArticleGroups", dependsOnMethods = {"testGetArticleGroupList"})
  public void testFormatArticleList(Issue issue, Map<ArticleType, List<Article>> expectedArticles, String nonExistentDoi, Article unknownTypeArticle) {
    List<TOCArticleGroup> groups = adminService.getArticleGroupList(issue);
    String articleCsv = adminService.formatArticleCsv(groups);
    assertNotNull(articleCsv, "returned null csv");
    String[] splitCsv = articleCsv.split(",");
    int i = 0;
    for (TOCArticleGroup group : groups) {
      for (ArticleInfo articleInfo : group.getArticles()) {
        assertEquals(splitCsv[i], articleInfo.getDoi(), "Doi in position " + (i + 1) + " was incorrect");
        i++;
      }
    }
    assertEquals(splitCsv.length, i, "returned csv with incorrect number of dois");
  }

  @Test
  public void testFormatArticleCsvWithEmpyGroups() {
    String csv = adminService.formatArticleCsv(new ArrayList<TOCArticleGroup>(0));
    assertNotNull(csv, "returned null result");
    assertTrue(csv.isEmpty(), "returned non empty csv");
  }

  @Test
  public void testDeleteIssue() {
    Volume volume = new Volume("id:volumeForDeleteIssue");
    volume.setIssues(Arrays.asList(
        new Issue("id:testDeleteIssue1"),
        new Issue("id:testDeleteIssue2"),
        new Issue("id:testDeleteIssue3")
    ));
    dummyDataStore.store(volume);
    for (Issue issue : volume.getIssues()) {
      issue.setArticleDois(new ArrayList<String>(0));
    }

    Issue issueToDelete = volume.getIssues().get(1);

    adminService.deleteIssue(issueToDelete.getIssueUri());
    assertNull(dummyDataStore.get(Issue.class, issueToDelete.getID()), "Issue didn't get removed from the database");

    Volume storedVolume = dummyDataStore.get(Volume.class, volume.getID());
    assertEquals(storedVolume.getIssues().size(), 2, "Issue didn't get removed from volume list");
    assertEquals(storedVolume.getIssues().get(0), volume.getIssues().get(0), "Issues got reordered in volume; first issue changed");
    assertEquals(storedVolume.getIssues().get(1), volume.getIssues().get(2), "Issues got reordered in volume; last issue changed");
  }

  @DataProvider
  public Object[][] publishableArticles() {
    Journal journal = new Journal("Journal for testGetPublishableArticles");
    journal.seteIssn("Journal for testGetPublishableArticles");
    dummyDataStore.store(journal);

    Calendar lastYear = Calendar.getInstance();
    lastYear.add(Calendar.YEAR, -1);

    Article article1 = new Article("z-id:publishableArticle1");
    article1.setState(Article.STATE_UNPUBLISHED);
    article1.seteIssn(journal.geteIssn());
    article1.setJournals(new HashSet<Journal>(1));
    article1.getJournals().add(journal);
    article1.setDate(lastYear.getTime());
    dummyDataStore.store(article1);

    Article article2 = new Article("a-id:publishableArticle2");
    article2.setState(Article.STATE_UNPUBLISHED);
    article2.seteIssn(journal.geteIssn());
    article2.setJournals(new HashSet<Journal>(1));
    article2.getJournals().add(journal);
    article2.setDate(Calendar.getInstance().getTime());
    dummyDataStore.store(article2);

    Article article3 = new Article("id:notPublishableArticle");
    article3.setState(Article.STATE_ACTIVE);
    article3.seteIssn(journal.geteIssn());
    article3.setJournals(new HashSet<Journal>(1));
    article3.getJournals().add(journal);
    dummyDataStore.store(article3);

    Article article4 = new Article("id:articleInOtherJournal");
    article4.setState(Article.STATE_UNPUBLISHED);
    article4.seteIssn(defaultJournal.geteIssn());
    article4.setJournals(new HashSet<Journal>(1));
    article4.getJournals().add(defaultJournal);
    dummyDataStore.store(article4);


    return new Object[][]{
        {journal.geteIssn(), AdminService.ORDER_BY_FIELD_ARTICLE_DOI, true, Arrays.asList(article2, article1)},
        {journal.geteIssn(), AdminService.ORDER_BY_FIELD_ARTICLE_DOI, false, Arrays.asList(article1, article2)},
        {journal.geteIssn(), AdminService.ORDER_BY_FIELD_DATE_PUBLISHED, true, Arrays.asList(article1, article2)},
        {journal.geteIssn(), AdminService.ORDER_BY_FIELD_DATE_PUBLISHED, false, Arrays.asList(article2, article1)}
    };
  }

  @Test(dataProvider = "publishableArticles")
  public void testGetPublishableArticles(String eIssn, String orderField, boolean isOrderAscending, List<Article> expectedArticles) throws ApplicationException {
    List<ArticleInfo> results = adminService.getPublishableArticles(eIssn, orderField, isOrderAscending);
    assertNotNull(results, "returned null list of results");
    assertEquals(results.size(), expectedArticles.size(), "returned incorrect number of results");
    for (int i = 0; i < results.size(); i++) {
      assertEquals(results.get(i).getDoi(), expectedArticles.get(i).getDoi(), "Article " + (i + 1) + " had incorrect doi");
    }
  }
}
