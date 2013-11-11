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
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.views.TOCArticleGroup;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.views.article.ArticleType;
import org.ambraproject.models.Article;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Volume;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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

  @Autowired
  protected AdminService adminService; //just using this to format expected doi strings

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @DataProvider(name = "basicInfo")
  public Object[][] getBasicInfo() {
    Issue issue = new Issue();
    issue.setRespectOrder(true);
    issue.setIssueUri("id:testIssueForIssueManagement");
    issue.setDisplayName("Malbolge");
    issue.setImageUri("id:testImageForIssueManagement");
    issue.setArticleDois(new ArrayList<String>(8));

    List<TOCArticleGroup> articleGroupList = new ArrayList<TOCArticleGroup>(4);

    for (ArticleType type : ArticleType.getOrderedListForDisplay()) {
      TOCArticleGroup group = new TOCArticleGroup(type);

      for (int i = 1; i < 3; i++) {
        Article article = new Article();
        article.setDoi("id:" + type.getHeading().replaceAll(" ", "_") + "-article" + i);
        article.setTitle("Title for Article " + i + " in group " + type.getHeading());
        article.setTypes(new HashSet<String>(1));
        article.getTypes().add(type.getUri().toString());

        //ensure that articles are ordered in time
        Calendar date = Calendar.getInstance();
        date.add(Calendar.SECOND, -1 * (type.getHeading().hashCode() + i));
        article.setDate(date.getTime());

        dummyDataStore.store(article);
        issue.getArticleDois().add(article.getDoi());

        //add an articleInfo to TOC group
        ArticleInfo info = new ArticleInfo();
        info.setDoi(article.getDoi());
        info.setAt(article.getTypes());
        info.setTitle(article.getTitle());
        group.addArticle(info);
      }
      articleGroupList.add(group);
    }

    //group for orphans
    TOCArticleGroup orphans = new TOCArticleGroup(null);
    orphans.setHeading("Orphaned Article");
    orphans.setPluralHeading("Orphaned Articles");

    Article orphan1 = new Article();
    orphan1.setDoi("id:orphaned-article-for-IssueManagementAction1");
    orphan1.setTypes(new HashSet<String>(1));
    orphan1.getTypes().add("id:definitelyNotARealArticleType");
    dummyDataStore.store(orphan1);

    ArticleInfo orphan1Info = new ArticleInfo();
    orphan1Info.setDoi(orphan1.getDoi());
    orphans.addArticle(orphan1Info);
    issue.getArticleDois().add(orphan1.getDoi());

    ArticleInfo orphan2Info = new ArticleInfo();
    orphan2Info.setDoi("id:non-existent-article-orphan");
    orphans.addArticle(orphan2Info);
    issue.getArticleDois().add(orphan2Info.getDoi());

    articleGroupList.add(orphans);

    //sort the issue article list in a weird way so we can tell that the article csv is ordered by toc groups
    Collections.sort(issue.getArticleDois(),new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return -1 * o1.compareTo(o2);
      }
    });
    for (TOCArticleGroup group : articleGroupList) {
      Collections.sort(group.getArticles(), new Comparator<ArticleInfo>() {
        @Override
        public int compare(ArticleInfo o1, ArticleInfo o2) {
          return -1 * o1.getDoi().compareTo(o2.getDoi());
        }
      });
    }
    dummyDataStore.store(issue);

    return new Object[][]{
        {issue, articleGroupList}
    };
  }

  @Test(dataProvider = "basicInfo")
  public void testExecute(Issue issue, List<TOCArticleGroup> articleGroupList) throws Exception {
    action.setIssueURI(issue.getIssueUri());
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");

    assertEquals(action.getIssue().getIssueUri(), issue.getIssueUri(), "Action returned incorrect issue");
    assertEquals(action.getIssue().getDisplayName(), issue.getDisplayName(),
        "Action returned issue with incorrect display name");
    assertEquals(action.getIssue().getImageUri(), issue.getImageUri(), "Action returned issue with incorrect image URI");

    //the action should show the article csv in order of the article groups
    assertEquals(action.getArticleOrderCSV(), adminService.formatArticleCsv(articleGroupList),
        "Action returned incorrect article csv");

    assertEquals(action.getArticleGroups().size(), articleGroupList.size(),
        "Action returned incorrect number of article groups");
    //article groups
    for (int i = 0; i < articleGroupList.size(); i++) {
      TOCArticleGroup actualGroup = action.getArticleGroups().get(i);
      TOCArticleGroup expectedGroup = articleGroupList.get(i);
      assertEquals(actualGroup.getHeading(), expectedGroup.getHeading(),
          "Article group " + (i + 1) + " had incorrect heading");
      assertEquals(actualGroup.getArticles().size(), expectedGroup.getArticles().size(),
          "Article group " + (i + 1) + " had incorrect number of articles");
      //articles in the group
      for (int j = 0; j < actualGroup.getArticles().size(); j++) {
        ArticleInfo actualArticle = actualGroup.getArticles().get(j);
        ArticleInfo expectedArticle = expectedGroup.getArticles().get(j);
        assertEquals(actualArticle.getDoi(), expectedArticle.getDoi(),
            "Article " + (j + 1) + " in group " + actualGroup.getHeading() + " had incorrect doi");
        assertEquals(actualArticle.getTitle(), expectedArticle.getTitle(),
            "Article " + (j + 1) + " in group " + actualGroup.getHeading() + " had incorrect title");
      }
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testUpdateIssue(Issue issue, List<TOCArticleGroup> articleGroupList) throws Exception {
    //execute the action to get the current CSV
    action.setCommand("foo");
    action.setIssueURI(issue.getIssueUri());
    action.execute();
    clearMessages();

    List<String> existingArticles = dummyDataStore.get(Issue.class, issue.getID()).getArticleDois();

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
    action.setIssueURI(issue.getIssueUri());
    action.setArticleOrderCSV(reorderedArticleCsv);
    action.setDisplayName(displayName);
    action.setImageURI(imageUri);
    action.setRespectOrder(true);


    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertTrue(action.getActionMessages().size() > 0, "Action didn't return messages indicating success");

    //check properties on action
    assertEquals(action.getIssue().getIssueUri(), issue.getIssueUri(), "action changed issues after update");
    assertEquals(action.getDisplayName(), displayName, "action had incorrect display name");
    assertEquals(action.getImageURI(), imageUri, "Action had incorrect image uri");
    assertEquals(action.getArticleOrderCSV(), StringUtils.join(orderedArticlesForCSV,","), "action didn't have correct article csv");


    //check what got stored to the database
    Issue storedIssue = dummyDataStore.get(Issue.class, issue.getID());

    assertEquals(storedIssue.getDisplayName(), displayName, "Issue got saved to the database with incorrect display name");
    assertEquals(storedIssue.getImageUri(), imageUri, "Issue got saved to the database with incorrect image uri");
    assertEquals(storedIssue.getArticleDois().size(), existingArticles.size(),
        "Articles got removed or added from the issue on reordering");

    //The db, however, stores articles in the order we told it
    for (int i = 0; i < orderedArticlesForDb.length; i++) {
      assertEquals(storedIssue.getArticleDois().get(i), orderedArticlesForDb[i],
          "Articles didn't get ordered correctly in the database; article " + (i + 1) + " was incorrect");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowAddingArticleToCsv(Issue issue, List<TOCArticleGroup> articleGroupList) throws Exception {
    //execute the action to get the original csv
    action.setIssueURI(issue.getIssueUri());
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String incorrectCsv = originalCsv + ",id:some-fake-new-article";

    action.setCommand("UPDATE_ISSUE");
    action.setArticleOrderCSV(incorrectCsv);
    action.setRespectOrder(true);
    action.setDisplayName(issue.getDisplayName());
    action.setImageURI(issue.getImageUri());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 0, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowRemovingArticleFromCsv(Issue issue, List<TOCArticleGroup> articleGroupList) throws Exception {
    //execute the action to get the original csv
    action.setIssueURI(issue.getIssueUri());
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String articleToRemove = originalCsv.substring(0, originalCsv.indexOf(","));
    String incorrectCsv = originalCsv.replace(articleToRemove + ",", "");

    action.setCommand("UPDATE_ISSUE");
    action.setArticleOrderCSV(incorrectCsv);
    action.setRespectOrder(true);
    action.setDisplayName(issue.getDisplayName());
    action.setImageURI(issue.getImageUri());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 0, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testActionDoesNotAllowChangingArticlesInCsv(Issue issue, List<TOCArticleGroup> articleGroupList) throws Exception {
    //execute the action to get the original csv
    action.setIssueURI(issue.getIssueUri());
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String changedCsv = originalCsv.substring(originalCsv.indexOf(",") + 1);
    changedCsv = "id:this-article-was-not-in-original-csv," + changedCsv;

    assertEquals(changedCsv.split(",").length, originalCsv.split(",").length,
        "test added or removed articles instead of just changing one");


    action.setCommand("UPDATE_ISSUE");
    action.setArticleOrderCSV(changedCsv);
    action.setRespectOrder(true);
    action.setDisplayName(issue.getDisplayName());
    action.setImageURI(issue.getImageUri());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 0, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute", "testUpdateIssue"}, alwaysRun = true)
  public void testAddArticle(Issue issue, List<TOCArticleGroup> articleGroupList) throws Exception {
    String articlesToAddCsv = "id:new-article-for-adding-to-issue1,id:new-article-for-adding-to-issue2";
    Article article = new Article();
    article.setDoi(articlesToAddCsv.split(",")[0]);
    article.setTypes(new HashSet<String>(1));
    article.getTypes().add(ArticleType.getOrderedListForDisplay().get(0).getUri().toString());
    article.setDate(Calendar.getInstance().getTime());
    dummyDataStore.store(article);


    action.setCommand("ADD_ARTICLE");
    action.setIssueURI(issue.getIssueUri());
    action.setArticlesToAddCsv(articlesToAddCsv);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return messages indicating success");

    for (String doi : articlesToAddCsv.split(",")) {
      assertTrue(action.getArticleOrderCSV().contains(doi), "Article " + doi + " didn't get added to action's csv");
      assertTrue(action.getIssue().getArticleDois().contains(doi),
          "Article " + doi + " didn't get added to action's issue");
    }
    //the first article should be in the first TOC group
    boolean foundMatch = false;
    for (ArticleInfo articleInfo : action.getArticleGroups().get(0).getArticles()) {
      if (articleInfo.getDoi().equals(article.getDoi())) {
        foundMatch = true;
        break;
      }
    }
    assertTrue(foundMatch, "New article didn't get added to correct group");
    String orphanDoi = articlesToAddCsv.split(",")[1];
    //the second article should be an orphan
    ArrayList<ArticleInfo> orphans = action.getArticleGroups().get(action.getArticleGroups().size() - 1).getArticles();
    boolean foundOrphan = false;
    for (ArticleInfo articleInfo : orphans) {
      if (orphanDoi.equals(articleInfo.getDoi())) {
        foundOrphan = true;
        break;
      }
    }
    assertTrue(foundOrphan, "Non existent article didn't get added to orphan list");

    //check the values that got stored to the database
    List<String> storedArticles = dummyDataStore.get(Issue.class, issue.getID()).getArticleDois();
    for (String doi : articlesToAddCsv.split(",")) {
      assertTrue(storedArticles.contains(doi), "Article " + doi + " didn't get added to the issue in the database");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testRemoveArticles(Issue issue, List<TOCArticleGroup> articleGroupList) throws Exception {
    List<String> articlesToDelete = dummyDataStore.get(Issue.class, issue.getID()).getArticleDois().subList(0, 3);
    String[] articlesToDeleteArray = new String[3];
    for (int i = 0; i < 3; i++) {
      articlesToDeleteArray[i] = articlesToDelete.get(i);
    }


    action.setCommand("REMOVE_ARTICLES");
    action.setIssueURI(issue.getIssueUri());
    action.setArticlesToRemove(articlesToDeleteArray);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return messages indicating success");

    for (String doi : articlesToDelete) {
      assertFalse(action.getIssue().getArticleDois().contains(doi),
          "Article " + doi + " didn't get removed from action's issue list");
      assertFalse(action.getArticleOrderCSV().contains(doi),
          "Article " + doi + " didn't get removed from action's csv");
    }

    //check the values in the db
    List<String> storedArticleList = dummyDataStore.get(Issue.class, issue.getID()).getArticleDois();
    for (String doi : articlesToDelete) {
      assertFalse(storedArticleList.contains(doi), "Article " + doi + " didn't get removed from issue in the database");
      try {
        articleService.getArticle(doi, DEFAULT_ADMIN_AUTHID);
      } catch (NoSuchArticleIdException e) {
        fail("Article " + doi + " got deleted from the database instead of just being removed from the issue");
      }
    }
  }
}
