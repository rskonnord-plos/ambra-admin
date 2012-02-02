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
import org.ambraproject.article.action.TOCArticleGroup;
import org.ambraproject.article.service.ArticleService;
import org.ambraproject.article.service.NoSuchArticleIdException;
import org.ambraproject.model.article.ArticleInfo;
import org.ambraproject.model.article.ArticleType;
import org.ambraproject.models.Article;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.Volume;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Alex Kudlick 2/1/12
 */
public class IssueManagementActionTest extends AdminWebTest {

  @Autowired
  protected IssueManagementAction action;

  @Autowired
  protected ArticleService articleService; //just using this to get articles by doi and check that they didn't get deleted

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @DataProvider(name = "basicInfo")
  public Object[][] getBasicInfo() {
    Volume volume = new Volume();
    volume.setId(URI.create("id:testVolumeForIssue"));
    volume.setIssueList(new ArrayList<URI>(1));

    Issue issue = new Issue();
    issue.setId(URI.create("id:testIssueForIssueManagement"));
    issue.setDisplayName("Malbolge");
    issue.setImage(URI.create("id:testImageForIssueManagement"));
    issue.setArticleList(new ArrayList<URI>(8));

    List<TOCArticleGroup> articleGroupList = new ArrayList<TOCArticleGroup>(3);

    for (ArticleType type : ArticleType.getOrderedListForDisplay()) {
      TOCArticleGroup group = new TOCArticleGroup(type);

      for (int i = 1; i < 3; i++) {
        Article article = new Article();
        article.setDoi("id:" + type.getHeading().replaceAll(" ", "_") + "-article" + i);
        article.setTitle("Title for Article " + i + " in group " + type.getHeading());
        article.setTypes(new HashSet<String>(1));
        article.getTypes().add(type.getUri().toString());

        //insure that articles are ordered in time
        Calendar date = Calendar.getInstance();
        date.add(Calendar.SECOND, -1 * (type.getHeading().hashCode() + i));
        article.setDate(date.getTime());

        dummyDataStore.store(article);
        issue.getArticleList().add(URI.create(article.getDoi()));

        //add an articleInfo to TOC group
        ArticleInfo info = new ArticleInfo();
        info.setId(URI.create(article.getDoi()));
        info.setAt(article.getTypes());
        info.setTitle(article.getTitle());
        group.addArticle(info);
      }
      articleGroupList.add(group);
    }

    List<URI> orphans = new ArrayList<URI>(2);
    Article orphan1 = new Article();
    orphan1.setDoi("id:orphanned-article-for-IssueManagementAction1");
    orphan1.setTypes(new HashSet<String>(1));
    orphan1.getTypes().add("id:definitelyNotARealArticleType");
    dummyDataStore.store(orphan1);
    orphans.add(URI.create(orphan1.getDoi()));

    orphans.add(URI.create("id:non-existent-article-orphan"));

    issue.getArticleList().addAll(orphans);

    dummyDataStore.store(issue);
    volume.getIssueList().add(issue.getId());
    dummyDataStore.store(volume);

    return new Object[][]{
        {volume.getId().toString(), issue, articleGroupList, orphans}
    };
  }

  @Test(dataProvider = "basicInfo")
  public void testExecute(String volumeURI, Issue issue, List<TOCArticleGroup> articleGroupList,
                          List<URI> orphans) throws Exception {
    action.setVolumeURI(volumeURI);
    action.setIssueURI(issue.getId().toString());


    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");

    assertEquals(action.getIssue().getId(), issue.getId(), "Action returned incorrect issue");
    assertEquals(action.getIssue().getDisplayName(), issue.getDisplayName(),
        "Action returned issue with incorrect display name");
    assertEquals(action.getIssue().getImage(), issue.getImage(), "Action returned issue with incorrect image URI");
    assertEquals(action.getArticleOrderCSV(), StringUtils.join(issue.getArticleList(), ","),
        "Action returned incorrect article csv");

    assertEquals(action.getArticleGrps().size(), articleGroupList.size(),
        "Action returned incorrect number of article groups");
    //article groups
    for (int i = 0; i < articleGroupList.size(); i++) {
      TOCArticleGroup actualGroup = action.getArticleGrps().get(i);
      TOCArticleGroup expectedGroup = articleGroupList.get(i);
      assertEquals(actualGroup.getHeading(), expectedGroup.getHeading(),
          "Article group " + (i + 1) + " had incorrect heading");
      assertEquals(actualGroup.getArticles().size(), expectedGroup.getArticles().size(),
          "Article group " + (i + 1) + " had incorrect number of articles");
      //articles in the group
      for (int j = 0; j < actualGroup.getArticles().size(); j++) {
        ArticleInfo actualArticle = actualGroup.getArticles().get(j);
        ArticleInfo expectedArticle = expectedGroup.getArticles().get(j);
        assertEquals(actualArticle.getId(), expectedArticle.getId(),
            "Article " + (j + 1) + " in group " + actualGroup.getHeading() + " had incorrect doi");
        assertEquals(actualArticle.getTitle(), expectedArticle.getTitle(),
            "Article " + (j + 1) + " in group " + actualGroup.getHeading() + " had incorrect title");
      }
    }

    assertEquals(action.getOrphans().toArray(), orphans.toArray(), "Action returned incorrect orphans");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testUpdateIssue(String volumeURI, Issue issue, List<TOCArticleGroup> articleGroupList,
                              List<URI> orphans) throws Exception {
    //execute the action to get the current CSV
    action.setCommand("foo");
    action.setVolumeURI(volumeURI);
    action.setIssueURI(issue.getId().toString());
    action.execute();
    clearMessages();

    List<URI> existingArticles = dummyDataStore.get(Issue.class, issue.getId()).getArticleList();

    String reorderedArticleCsv = action.getArticleOrderCSV();
    String articleToReorder = reorderedArticleCsv.substring(0, reorderedArticleCsv.indexOf(","));
    reorderedArticleCsv = reorderedArticleCsv.replaceFirst(articleToReorder + ",", "");
    reorderedArticleCsv += ("," + articleToReorder);
    String[] orderedArticlesForDb = reorderedArticleCsv.split(",");

    //when we tell the action to order the articles in the issue, it does, but then they are grouped by types
    List<String> orderedArticlesForCSV = new ArrayList<String>(existingArticles.size());
    for (ArticleType type : ArticleType.getOrderedListForDisplay()) {
      for (String article : orderedArticlesForDb) {
        try {
          if (articleService.getArticle(article, DEFAULT_ADMIN_AUTHID)
              .getTypes().contains(type.getUri().toString())) {
            orderedArticlesForCSV.add(article);
          }
        } catch (NoSuchArticleIdException e) {
          //suppress
        }
      }
    }
    for (String article : orderedArticlesForDb) {
      if (!orderedArticlesForCSV.contains(article)) {
        orderedArticlesForCSV.add(article);
      }
    }


    String imageUri = "id:newImageURIToSetOnIssue";
    String displayName = "Cheech and Chong";

    action.setCommand("UPDATE_ISSUE");
    action.setVolumeURI(volumeURI);
    action.setIssueURI(issue.getId().toString());
    action.setArticleListCSV(reorderedArticleCsv);
    action.setDisplayName(displayName);
    action.setImageURI(imageUri);
    action.setRespectOrder(true);


    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
//    assertTrue(action.getActionMessages().size() > 0, "Action didn't return messages indicating success");

    //check properties on action
    assertEquals(action.getIssue().getId(), issue.getId(), "action changed issues after update");
    assertEquals(action.getDisplayName(), displayName, "action had incorrect display name");
    assertEquals(action.getImageURI(), imageUri, "Action had incorrect image uri");
    assertEquals(action.getArticleOrderCSV(), StringUtils.join(orderedArticlesForCSV, ","), "action didn't have correct article csv");
    assertEquals(action.getIssue().getArticleList().size(), existingArticles.size(), "Action had incorrect number of articles");

    for (int i = 0; i < orderedArticlesForDb.length; i++) {
      assertEquals(action.getIssue().getArticleList().get(i).toString(), orderedArticlesForDb[i],
          "Action had articles in incorrect order; article " + (i + 1) + " was incorrect");
    }

    //check what got stored to the database
    Issue storedIssue = dummyDataStore.get(Issue.class, issue.getId());

    assertEquals(storedIssue.getDisplayName(), displayName, "Issue got saved to the database with incorrect display name");
    assertEquals(storedIssue.getImage(), URI.create(imageUri), "Issue got saved to the database with incorrect image uri");
    assertEquals(storedIssue.getArticleList().size(), existingArticles.size(),
        "Articles got removed or added from the issue on reordering");

    //The db, however, stores articles in the order we told it
    for (int i = 0; i < orderedArticlesForDb.length; i++) {
      assertEquals(storedIssue.getArticleList().get(i).toString(), orderedArticlesForDb[i],
          "Articles didn't get ordered correctly in the database");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowAddingArticleToCsv(String volumeURI, Issue issue, List<TOCArticleGroup> articleGroupList,
                                                       List<URI> orphans) throws Exception {
    //execute the action to get the original csv
    action.setVolumeURI(volumeURI);
    action.setIssueURI(issue.getId().toString());
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String incorrectCsv = originalCsv + ",id:some-fake-new-article";

    action.setCommand("UPDATE_ISSUE");
    action.setArticleListCSV(incorrectCsv);
    action.setRespectOrder(true);
    action.setDisplayName(issue.getDisplayName());
    action.setImageURI(issue.getImage().toString());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 1, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowRemovingArticleFromCsv(String volumeURI, Issue issue, List<TOCArticleGroup> articleGroupList,
                                                           List<URI> orphans) throws Exception {
    //execute the action to get the original csv
    action.setVolumeURI(volumeURI);
    action.setIssueURI(issue.getId().toString());
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String articleToRemove = originalCsv.substring(0, originalCsv.indexOf(","));
    String incorrectCsv = originalCsv.replace(articleToRemove + ",", "");

    action.setCommand("UPDATE_ISSUE");
    action.setArticleListCSV(incorrectCsv);
    action.setRespectOrder(true);
    action.setDisplayName(issue.getDisplayName());
    action.setImageURI(issue.getImage().toString());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 1, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowChangingArticlesInCsv(String volumeURI, Issue issue, List<TOCArticleGroup> articleGroupList,
                                                          List<URI> orphans) throws Exception {
    //execute the action to get the original csv
    action.setVolumeURI(volumeURI);
    action.setIssueURI(issue.getId().toString());
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String changedCsv = originalCsv.substring(originalCsv.indexOf(",") + 1);
    changedCsv = "id:this-article-was-not-in-original-csv," + changedCsv;

    assertEquals(changedCsv.split(",").length, originalCsv.split(",").length,
        "test added or removed articles instead of just changing one");


    action.setCommand("UPDATE_ISSUE");
    action.setArticleListCSV(changedCsv);
    action.setRespectOrder(true);
    action.setDisplayName(issue.getDisplayName());
    action.setImageURI(issue.getImage().toString());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 1, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testAddArticle(String volumeURI, Issue issue, List<TOCArticleGroup> articleGroupList,
                             List<URI> orphans) throws Exception {
    String articlesToAddCsv = "id:new-article-for-adding-to-issue1,id:new-article-for-adding-to-issue2";
    Article article = new Article();
    article.setDoi(articlesToAddCsv.split(",")[0]);
    article.setTypes(new HashSet<String>(1));
    article.getTypes().add(ArticleType.getOrderedListForDisplay().get(0).getUri().toString());
    article.setDate(Calendar.getInstance().getTime());
    dummyDataStore.store(article);


    action.setCommand("ADD_ARTICLE");
    action.setIssueURI(issue.getId().toString());
    action.setVolumeURI(volumeURI);
    action.setArticleURI(articlesToAddCsv);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 2, "Action didn't return messages indicating success");

    for (String doi : articlesToAddCsv.split(",")) {
      assertTrue(action.getArticleOrderCSV().contains(doi), "Article " + doi + " didn't get added to action's csv");
      assertTrue(action.getIssue().getArticleList().contains(URI.create(doi)),
          "Article " + doi + " didn't get added to action's issue");
    }
    //the first article should be in the first TOC group
    boolean foundMatch = false;
    for (ArticleInfo articleInfo : action.getArticleGrps().get(0).getArticles()) {
      if (articleInfo.getId().toString().equals(article.getDoi())) {
        foundMatch = true;
        break;
      }
    }
    assertTrue(foundMatch, "New article didn't get added to correct group");
    //the second article should be an orphan
    assertTrue(action.getOrphans().contains(URI.create(articlesToAddCsv.split(",")[1])),
        "Non existent article didn't get added to orphan list");

    //check the values that got stored to the database
    List<URI> storedArticles = dummyDataStore.get(Issue.class, issue.getId()).getArticleList();
    for (String doi : articlesToAddCsv.split(",")) {
      assertTrue(storedArticles.contains(URI.create(doi)), "Article " + doi + " didn't get added to the issue in the database");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testRemoveArticles(String volumeURI, Issue issue, List<TOCArticleGroup> articleGroupList,
                                 List<URI> orphans) throws Exception {
    List<URI> articlesToDelete = dummyDataStore.get(Issue.class, issue.getId()).getArticleList().subList(0, 3);
    String[] articlesToDeleteArray = new String[3];
    for (int i = 0; i < 3; i++) {
      articlesToDeleteArray[i] = articlesToDelete.get(i).toString();
    }


    action.setCommand("REMOVE_ARTICLES");
    action.setVolumeURI(volumeURI);
    action.setIssueURI(issue.getId().toString());
    action.setArticlesToRemove(articlesToDeleteArray);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 3, "Action didn't return messages indicating success");

    for (URI doi : articlesToDelete) {
      assertFalse(action.getIssue().getArticleList().contains(doi),
          "Article " + doi + " didn't get removed from action's issue list");
      assertFalse(action.getArticleOrderCSV().contains(doi.toString()),
          "Article " + doi + " didn't get removed from action's csv");
    }

    //check the values in the db
    List<URI> storedArticleList = dummyDataStore.get(Issue.class, issue.getId()).getArticleList();
    for (URI doi : articlesToDelete) {
      assertFalse(storedArticleList.contains(doi), "Article " + doi + " didn't get removed from issue in the database");
      try {
        articleService.getArticle(doi.toString(), DEFAULT_ADMIN_AUTHID);
      } catch (NoSuchArticleIdException e) {
        fail("Article " + doi + " got deleted from the database instead of just being removed from the issue");
      }
    }
  }
}
