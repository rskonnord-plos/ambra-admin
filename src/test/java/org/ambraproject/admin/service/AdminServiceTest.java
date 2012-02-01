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

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.models.Article;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.Volume;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Alex Kudick  1/3/12
 */
public class AdminServiceTest extends AdminBaseTest {

  @Autowired
  protected AdminService adminService;

  @DataProvider(name = "issueToUpdate")
  public Object[][] getIssuesToUpdate() {
    URI blankIssue = URI.create(dummyDataStore.store(new Issue()));

    Article imageArticle = new Article();
    imageArticle.setDoi("id:foo-doi-for-updating-issue");
    imageArticle.setDescription("This description should overwrite what's on the issue");
    imageArticle.setTitle("This title should overwrite what's on the issue");
    dummyDataStore.store(imageArticle);

    Issue existingIssue = new Issue();
    existingIssue.setTitle("title should get overwritten");
    existingIssue.setDescription("description should get overwritten");
    existingIssue.setDisplayName("old display name");
    existingIssue.setArticleList(new ArrayList<URI>());
    existingIssue.getArticleList().add(URI.create("id:issue-article-1"));
    existingIssue.getArticleList().add(URI.create("id:issue-article-2"));
    existingIssue.getArticleList().add(URI.create("id:issue-article-3"));

    dummyDataStore.store(existingIssue);

    List<URI> addedArticleList = new ArrayList<URI>(existingIssue.getArticleList());
    addedArticleList.add(URI.create("id:issue-article-4"));

    List<URI> removedArticleList = new ArrayList<URI>(existingIssue.getArticleList());
    removedArticleList.remove(1);

    return new Object[][]{
        {blankIssue, imageArticle, "brand new display name", new ArrayList<URI>(0), false},
        {blankIssue, imageArticle, "added an article", addedArticleList, true},
        {blankIssue, imageArticle, "removed an article", removedArticleList, true}
    };
  }

  @Test(dataProvider = "issueToUpdate")
  public void testUpdateIssue(URI issueId, Article imageArticle, String displayName, List<URI> articleList, boolean respectOrder) throws URISyntaxException {
    Issue result = adminService.updateIssue(issueId, URI.create(imageArticle.getDoi()),
        displayName, articleList, respectOrder);
    assertNotNull(result, "returned null issue");
    assertEquals(result.getId(), issueId, "returned issue with incorrect id");
    assertEquals(result.getArticleList().toArray(), articleList.toArray(), "issue had incorrect article list");
    assertEquals(result.getImage(), URI.create(imageArticle.getDoi()), "issue had incorrect image uri");
    assertEquals(result.getDescription(), imageArticle.getDescription(), "issue didn't get description updated from image article");
    assertEquals(result.getTitle(), imageArticle.getTitle(), "issue didn't get title updated from article");
    assertEquals(result.getDisplayName(), displayName, "issue had incorrect display name");
    assertEquals(result.getRespectOrder(), respectOrder, "issue had incorrect respectOrder attribute");

    //check the properties on the issue from the db
    Issue storedIssue = dummyDataStore.get(Issue.class, issueId);
    assertEquals(storedIssue.getArticleList().toArray(), articleList.toArray(), "issue with incorrect id");
    assertEquals(storedIssue.getImage(), URI.create(imageArticle.getDoi()), "issue had incorrect image uri");
    assertEquals(storedIssue.getDescription(), imageArticle.getDescription(), "issue didn't get description updated from image article");
    assertEquals(storedIssue.getTitle(), imageArticle.getTitle(), "issue didn't get title updated from article");
    assertEquals(storedIssue.getDisplayName(), displayName, "issue had incorrect display name");
    assertEquals(storedIssue.getRespectOrder(), respectOrder, "issue had incorrect respectOrder attribute");
  }

  @DataProvider(name = "createIssue")
  public Object[][] getIssueToCreate() {
    Article article = new Article();
    article.setDoi("id:doi-for-creating-issue");
    article.setTitle("Once Upon a Time");
    article.setDescription("Centers on a woman with a troubled past who is drawn into a small town in " +
        "Maine where the magic and mystery of Fairy Tales just may be real. ");
    dummyDataStore.store(article);
    Volume volume = new Volume();
    dummyDataStore.store(volume);

    List<URI> expectedArticleList = new ArrayList<URI>(3);
    expectedArticleList.add(URI.create("id:for-creating-issue1"));
    expectedArticleList.add(URI.create("id:for-creating-issue2"));
    expectedArticleList.add(URI.create("id:for-creating-issue3"));

    String articleCsv = StringUtils.join(expectedArticleList, ",");

    return new Object[][]{
        {volume, URI.create("id:new-issue-uri"), article, "some new display name", articleCsv, expectedArticleList}
    };
  }

  @Test(dataProvider = "createIssue")
  public void testCreateIssue(Volume vol, URI issueURI, Article imageArticle,
                              String displayName, String articleListCsv, List<URI> expectedArticleList) {
    Issue issue = adminService.createIssue(vol, issueURI, URI.create(imageArticle.getDoi()), displayName, articleListCsv);
    assertNotNull(issue, "created null issue");
    assertEquals(issue.getId(), issueURI, "issue had incorrect id");
    assertEquals(issue.getArticleList().toArray(), expectedArticleList.toArray(), "issue had incorrect article list");
    assertEquals(issue.getImage(), URI.create(imageArticle.getDoi()), "issue had incorrect image uri");
    assertEquals(issue.getDescription(), imageArticle.getDescription(), "issue didn't get description updated from image article");
    assertEquals(issue.getTitle(), imageArticle.getTitle(), "issue didn't get title updated from article");
    assertEquals(issue.getDisplayName(), displayName, "issue had incorrect display name");

    Volume storedVolume = dummyDataStore.get(Volume.class, vol.getId());
    assertTrue(storedVolume.getIssueList().contains(issue.getId()), "issue didn't get added to volume");

    Issue storedIssue = dummyDataStore.get(Issue.class, issueURI);
    assertEquals(storedIssue.getArticleList().toArray(), expectedArticleList.toArray(), "storedIssue had incorrect article list");
    assertEquals(storedIssue.getImage(), URI.create(imageArticle.getDoi()), "storedIssue had incorrect image uri");
    assertEquals(storedIssue.getDescription(), imageArticle.getDescription(), "storedIssue didn't get description updated from image article");
    assertEquals(storedIssue.getTitle(), imageArticle.getTitle(), "storedIssue didn't get title updated from article");
    assertEquals(storedIssue.getDisplayName(), displayName, "storedIssue had incorrect display name");
  }

  @DataProvider(name = "issueParents")
  public Object[][] issueParents() {
    URI issue1 = URI.create(dummyDataStore.store(new Issue()));

    Volume volume1 = new Volume();
    volume1.setIssueList(new ArrayList<URI>(1));
    volume1.getIssueList().add(issue1);
    URI volume1Uri = URI.create(dummyDataStore.store(volume1));

    Volume volume2 = new Volume();
    volume2.setIssueList(new ArrayList<URI>(1));
    volume2.getIssueList().add(issue1);
    URI volume2Uri = URI.create(dummyDataStore.store(volume2));


    return new Object[][]{
        {issue1, new URI[]{volume1Uri, volume2Uri}}
    };
  }

  @Test(dataProvider = "issueParents")
  public void testGetIssueParents(URI issueUri, URI[] expectedVolumeUris) {
    List<Volume> volumes = adminService.getIssueParents(issueUri);
    assertNotNull(volumes, "returned null list of volumes");
    assertEquals(volumes.size(), expectedVolumeUris.length, "returned incorrect number of volumes");
    URI[] actualVolumeUris = new URI[volumes.size()];
    for (int i = 0; i < volumes.size(); i++) {
      actualVolumeUris[i] = volumes.get(i).getId();
    }
    assertEqualsNoOrder(actualVolumeUris, expectedVolumeUris, "Returned incorrect volumes");
  }

  @DataProvider(name = "journalAndArticle")
  public Object[][] getCrossPubArticle() {
    Journal journal = new Journal();
    journal.setKey("PLoSFakeJournal");
    journal.setId(URI.create("id:journal-for-x-pub"));
    dummyDataStore.store(journal);

    Article article = new Article();
    article.setDoi("id:article-to-x-pub");
    dummyDataStore.store(article);

    return new Object[][]{
        {journal.getKey(), journal.getId(), URI.create(article.getDoi())}
    };
  }

  @Test(dataProvider = "journalAndArticle")
  public void testAddXPubArticles(String journalName, URI journalId, URI doi) throws Exception {
    adminService.addXPubArticle(journalName, doi);
    Journal journal = dummyDataStore.get(Journal.class, journalId);
    assertTrue(journal.getSimpleCollection().contains(doi), "doi didn't get added to journal");
  }

  @Test(dataProvider = "journalAndArticle", dependsOnMethods = {"testAddXPubArticles"}, alwaysRun = false)
  public void testRemoveXPubArticle(String journalName, URI journalId, URI doi) throws Exception {
    adminService.removeXPubArticle(journalName, doi);
    Journal journal = dummyDataStore.get(Journal.class, journalId);
    assertFalse(journal.getSimpleCollection().contains(doi), "doi didn't get removed from journal");
  }

  @Test
  public void testCreateVolume() throws URISyntaxException {
    URI volumeUri = URI.create("id:volume_to_create");
    String displayName = "Some Fake Display Name";
    URI[] issueList = new URI[]{
        URI.create("id:test-article1-for-vol"),
        URI.create("id:test-article2-for-vol"),
        URI.create("id:test-article3-for-vol")};

    adminService.createVolume(
        "journal", //default journal created by BaseTest
        volumeUri,
        displayName,
        StringUtils.join(issueList, ",")
    );

    Volume volume = dummyDataStore.get(Volume.class, volumeUri);
    assertNotNull(volume, "volume didn't get created");
    assertEquals(volume.getId(), volumeUri, "volume didn't have correct uri");
    assertEquals(volume.getDisplayName(), displayName, "volume didn't have correct display name");
    assertEquals(volume.getIssueList().toArray(), issueList, "volume had incorrect issue list");
  }

  @Test(dataProvider = "journalAndArticle")
  public void testSetJrnlIssueURI(String journalName, URI journalId, URI doi) {
    adminService.setJrnlIssueURI(journalName, doi);
    Journal journal = dummyDataStore.get(Journal.class, journalId);
    assertEquals(journal.getCurrentIssue(), doi, "journal didn't get current issue set");
  }
  
  
  @Test
  public void testGetVolumes() {
    //create a journal and some volumes
    Journal journal = new Journal();
    journal.setKey("Fake Journal for get Volumes");
    journal.setVolumes(new ArrayList<URI>());
    
    Volume volume1 = new Volume();
    volume1.setDisplayName("Display name for volume 1");
    volume1.setImage(URI.create("id:image-for-vol1"));
    volume1.setId(URI.create(dummyDataStore.store(volume1)));
    journal.getVolumes().add(volume1.getId());
    
    Volume volume2 = new Volume();
    volume2.setDisplayName("Display name for volume 2");
    volume2.setImage(URI.create("id:image-for-vol2"));
    volume2.setId(URI.create(dummyDataStore.store(volume2)));
    journal.getVolumes().add(volume2.getId());
    
    Volume volume3 = new Volume();
    volume3.setDisplayName("Display name for volume 3");
    volume3.setImage(URI.create("id:image-for-vol3"));
    volume3.setId(URI.create(dummyDataStore.store(volume3)));
    journal.getVolumes().add(volume3.getId());

    dummyDataStore.store(journal);

    List<Volume> result = adminService.getVolumes(journal.getKey());
    assertNotNull(result, "returned null list of volumes");
    assertEquals(result.size(), 3, "returned incorrect number of volumes");

    assertEquals(result.get(0).getDisplayName(), volume1.getDisplayName(), "Volume 1 had incorrect display name");
    assertEquals(result.get(0).getImage(), volume1.getImage(), "Volume 1 had incorrect image article");

    assertEquals(result.get(1).getDisplayName(), volume2.getDisplayName(), "Volume 2 had incorrect display name");
    assertEquals(result.get(1).getImage(), volume2.getImage(), "Volume 2 had incorrect image article");

    assertEquals(result.get(2).getDisplayName(), volume3.getDisplayName(), "Volume 3 had incorrect display name");
    assertEquals(result.get(2).getImage(), volume3.getImage(), "Volume 3 had incorrect image article");

  }

  @DataProvider(name = "volume")
  public Object[][] getVolume() {
    Volume volume = new Volume();
    volume.setId(URI.create("id:volume-for-get-volume"));
    volume.setImage(URI.create("id:test-image-article"));
    volume.setDisplayName("Test Volume");
    volume.setIssueList(new ArrayList<URI>(3));
    volume.getIssueList().add(URI.create("id:issue-in-vol1"));
    volume.getIssueList().add(URI.create("id:issue-in-vol2"));
    volume.getIssueList().add(URI.create("id:issue-in-vol3"));
    dummyDataStore.store(volume);

    for (URI issueUri : volume.getIssueList()) {
      Issue issue = new Issue();
      issue.setId(issueUri);
      dummyDataStore.store(issue);
    }

    Journal journal = new Journal();
    journal.setId(URI.create("id:journal-for-testing-volumes"));
    journal.setKey("Journal for testing volumes");
    journal.setVolumes(new ArrayList<URI>(1));
    journal.getVolumes().add(volume.getId());
    dummyDataStore.store(journal);

    return new Object[][]{
        {journal, volume}
    };
  }

  @Test(dataProvider = "volume")
  public void testGetVolume(Journal journal, Volume expectedVolume) {
    Volume actualVolume = adminService.getVolume(expectedVolume.getId());
    assertNotNull(actualVolume, "returned null volume");
    assertEquals(actualVolume.getId(), expectedVolume.getId(), "returned incorrect volume");
    assertEquals(actualVolume.getDisplayName(), expectedVolume.getDisplayName(),
        "Volume didn't have correct display name");
    assertEquals(actualVolume.getImage(), expectedVolume.getImage(),
        "Volume didn't have correct image uri");
  }

  @Test(dataProvider = "volume")
  public void testGetIssuesByUri(Journal journal, Volume volume) {
    List<Issue> issues = adminService.getIssues(volume.getId());
    List<URI> issueUris = new ArrayList<URI>(issues.size());
    for (Issue issue : issues) {
      issueUris.add(issue.getId());
    }
    assertEquals(issueUris.toArray(), volume.getIssueList().toArray(), "returned incorrect issue list");
  }

  @Test(dataProvider = "volume")
  public void testGetIssues(Journal journal, Volume volume) {
    List<Issue> issues = adminService.getIssues(volume);
    List<URI> issueUris = new ArrayList<URI>(issues.size());
    for (Issue issue : issues) {
      issueUris.add(issue.getId());
    }
    assertEquals(issueUris.toArray(), volume.getIssueList().toArray(), "returned incorrect issue list");
  }

  @Test(dataProvider = "volume")
  public void testGetIssuesCSV(Journal journal, Volume volume) {
    String issueCsv = adminService.getIssuesCSV(volume);
    List<URI> issueUris = new ArrayList<URI>(volume.getIssueList().size());
    for (String issue : issueCsv.split(",")) {
      issueUris.add(URI.create(issue));
    }
    assertEquals(issueUris.toArray(), volume.getIssueList().toArray(), "returned incorrect issue list");
  }

  @Test(dataProvider = "volume")
  public void testGetIssuesCSVByUri(Journal journal, Volume volume) {
    String issueCsv = adminService.getIssuesCSV(volume.getId());
    List<URI> issueUris = new ArrayList<URI>(volume.getIssueList().size());
    for (String issue : issueCsv.split(",")) {
      issueUris.add(URI.create(issue));
    }
    assertEquals(issueUris.toArray(), volume.getIssueList().toArray(), "returned incorrect issue list");
  }

  @Test(dataProvider = "volume",
      dependsOnMethods = {"testGetVolume", "testGetIssues", "testGetIssuesByUri",
      "testGetIssuesCSV", "testGetIssuesCSVByUri"})
  public void testUpdateVolume(Journal journal, Volume volume) throws URISyntaxException {
    String newDisplayName = "Updated Display Name";
    List<URI> newIssueList = new ArrayList<URI>(3);
    newIssueList.add(URI.create("id:new-issue-for-update-volume1"));
    newIssueList.add(URI.create("id:new-issue-for-update-volume2"));
    newIssueList.add(URI.create("id:new-issue-for-update-volume3"));

    adminService.updateVolume(volume, newDisplayName, newIssueList);
    Volume storedVolume = dummyDataStore.get(Volume.class, volume.getId());
    assertEquals(storedVolume.getDisplayName(), newDisplayName, "volume didn't get display name updated");
    assertEquals(storedVolume.getIssueList().toArray(), newIssueList.toArray(), "Volume didn't have correct issue list");
  }


  @Test(dataProvider = "volume", dependsOnMethods = {"testGetVolume", "testGetIssues",
      "testUpdateVolume", "testGetIssuesByUri"}, alwaysRun = true)
  public void testDeleteVolume(Journal journal, Volume volume) {
    adminService.deleteVolume(journal.getKey(), volume.getId());
    assertNull(dummyDataStore.get(Volume.class, volume.getId()), "Volume didn't get deleted from the database");
    Journal storedJournal = dummyDataStore.get(Journal.class, journal.getId());
    assertFalse(storedJournal.getVolumes().contains(volume.getId()), "Volume URI didn't get removed from journal list");
  }

  @DataProvider(name = "issue")
  public Object[][] getIssue() {
    Issue issue = new Issue();
    issue.setId(URI.create("id:test-issue-id"));
    issue.setTitle("The Thing You Love Most");
    issue.setDescription("Regina does everything in her power to force Emma out of Storybrooke and out of her " +
        "and Henry's lives forever. Meanwhile, the chilling circumstances of how the Evil Queen released the " +
        "curse upon the fairytale world is revealed.");
    issue.setArticleList(new ArrayList<URI>(1));
    
    Article article = new Article();
    article.setDoi("id:doi-for-issue-test");
    dummyDataStore.store(article);

    issue.getArticleList().add(URI.create(article.getDoi()));
    dummyDataStore.store(issue);

    Volume volume = new Volume();
    volume.setId(URI.create("id:test-vol-for-issues1"));
    volume.setIssueList(new ArrayList<URI>(1));
    volume.getIssueList().add(issue.getId());
    dummyDataStore.store(volume);

    return new Object[][]{
        {issue, volume}
    };
  }

  @Test(dataProvider = "issue")
  public void testGetIssue(Issue expectedIssue, Volume volume) {
    Issue issue = adminService.getIssue(expectedIssue.getId());
    assertNotNull(issue, "returned null issue");
    assertEquals(issue.getTitle(), expectedIssue.getTitle(), "issue had incorrect title");
    assertEquals(issue.getDescription(), expectedIssue.getDescription(), "issue had incorrect description");
  }

  @Test(dataProvider = "issue")
  public void testRemoveArticle(Issue issue, Volume volume) {
    URI articleDoi = issue.getArticleList().get(0);
    //make sure the doi is in there to begin
    Issue storedIssue = dummyDataStore.get(Issue.class, issue.getId());
    assertTrue(storedIssue.getArticleList().contains(articleDoi),"Doi wasn't in issue list to start with");

    adminService.removeArticle(issue, articleDoi);
    storedIssue = dummyDataStore.get(Issue.class, issue.getId());
    assertFalse(storedIssue.getArticleList().contains(articleDoi), "article doi didn't get removed from issue");
  }
  
  @Test(dataProvider = "issue")
  public void testAddArticle(Issue issue, Volume volume) {
    //create an article and add it to the issue
    URI articleDoi = URI.create("id:articleWhichWillBeAddedToAnIssue");
    Article article = new Article();
    article.setDoi(articleDoi.toString());
    dummyDataStore.store(article);

    adminService.addArticle(issue, articleDoi);

    //make sure the article got added
    Issue storedIssue = dummyDataStore.get(Issue.class, issue.getId());
    assertTrue(storedIssue.getArticleList().contains(articleDoi), "article doi didn't get added toissue");
  }

  @Test(dataProvider = "issue", dependsOnMethods = {"testGetIssue","testRemoveArticle"}, alwaysRun = true)
  public void testDeleteIssue(Issue issue, Volume volume) {
    adminService.deleteIssue(issue);
    assertNull(dummyDataStore.get(Issue.class, issue.getId()), "Issue didn't get removed from the database");
    Volume storedVolume = dummyDataStore.get(Volume.class, volume.getId());
    assertFalse(storedVolume.getIssueList().contains(issue.getId()), "issue didn't get removed from volume");
  }

  @Test(dataProvider = "issue", dependsOnMethods = {"testGetIssue","testRemoveArticle"}, alwaysRun = true)
  public void testDeleteIssueByUri(Issue issue, Volume volume) {
    adminService.deleteIssue(issue.getId());
    assertNull(dummyDataStore.get(Issue.class, issue.getId()), "Issue didn't get removed from the database");
    Volume storedVolume = dummyDataStore.get(Volume.class, volume.getId());
    assertFalse(storedVolume.getIssueList().contains(issue.getId()), "issue didn't get removed from volume");
  }
}
