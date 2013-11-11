package org.ambraproject.admin.action;


import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.admin.service.AdminService;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleList;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.views.article.ArticleInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ArticleManagementActionTest extends AdminWebTest {

  @Autowired
  protected ArticleManagementAction action;

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
    Map<String, Integer> indices = new HashMap<String, Integer>();
    Set<String> validDois = new HashSet<String>();
    Set<String> orphanDois = new HashSet<String>();

    ArticleList articleList = new ArticleList();
    articleList.setListCode("id:testArticleListForArticleManagement");
    articleList.setDisplayName("News");
    int articlesCount = 8;
    articleList.setArticleDois(new ArrayList<String>());

    for (int i = 0; i < articlesCount; i++) {
      String doi;

      // two articles are invalid at index 3 and 4
      if (i != 3 && i != 4) {
        doi = "id:valid-article-" + i;
        Article article = new Article();
        article.setDoi(doi);
        article.setTitle("Title for Valid Article " + i);
        dummyDataStore.store(article);
        validDois.add(doi);
      }
      else {
        doi = "id:orphan-article-" + i;
        orphanDois.add(doi);
      }

      indices.put(doi, i);
      articleList.getArticleDois().add(doi);
    }

    dummyDataStore.store(articleList);

    return new Object[][]{
        {articleList, indices, validDois, orphanDois}
    };
  }

  private boolean  isSorted(String[] dois, Map<String, Integer> indices) {
    for (int i=0; i<dois.length-1; ++i) {
      int index1 = indices.get(dois[i]);
      int index2 = indices.get(dois[i+1]);
      if (index1 >= index2) {
        return false; // not sorted
      }
    }
    return true;
  }

  @Test(dataProvider = "basicInfo")
  public void testExecuteNoAction(ArticleList articleList, Map<String, Integer> indices,
                                  Set<String> validDois, Set<String> orphanDois) throws Exception {
    action.setListCode(articleList.getListCode());
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");

    assertEquals(action.getArticleList().getListCode(), articleList.getListCode(), "Action returned incorrect issue");
    assertEquals(action.getArticleList().getDisplayName(), articleList.getDisplayName(),
        "Action returned issue with incorrect display name");
    assertEquals(action.getArticleList().getArticleDois(), articleList.getArticleDois(),
        "Action returned wrong Dois in articleList");

    assertEquals(action.getArticleOrderCSV().split(","), articleList.getArticleDois().toArray(),
        "Action returned different articleOrderCSV");

    //the action should show the article csv in order of the valid articles
    assertEquals(isSorted(action.getArticleOrderCSV().split(","), indices), true,
        "Action returned unsorted articles");

    for (ArticleInfo articleInfo: action.getArticleInfoList()) {
      String doi = articleInfo.getDoi();
      assertEquals(validDois.contains(doi), true,
          "Action returned orphan articles in articleInfoList");
    }
    for (String doi: action.getOrphanDois()) {
      assertEquals(orphanDois.contains(doi), true,
          "Action returned valid article in orphanDois");
    }
  }


  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecuteNoAction"}, alwaysRun = true)
  public void testUpdateArticleList(ArticleList articleList, Map<String, Integer> indices,
                                    Set<String> validDois, Set<String> orphanDois) throws Exception {
    //execute the action to get the current CSV
    action.setCommand("foo");
    action.setListCode(articleList.getListCode());
    action.execute();
    clearMessages();

    List<String> existingArticles = dummyDataStore.get(ArticleList.class, articleList.getID()).getArticleDois();

    // move first doi to last in reorder
    String reorderedArticleCsv = action.getArticleOrderCSV();
    String articleToReorder = reorderedArticleCsv.substring(0, reorderedArticleCsv.indexOf(","));
    reorderedArticleCsv = reorderedArticleCsv.replaceFirst(articleToReorder + ",", "");
    reorderedArticleCsv += ("," + articleToReorder);
    String[] orderedArticlesForDb = reorderedArticleCsv.split(",");
    Map<String, Integer> newIndices = new HashMap<String, Integer>();
    for (int i=0; i<orderedArticlesForDb.length; ++i) {
      newIndices.put(orderedArticlesForDb[i], i);
    }


    String displayName = "Spam";

    action.setCommand("UPDATE_LIST");
    action.setListCode(articleList.getListCode());
    action.setArticleOrderCSV(reorderedArticleCsv);
    action.setDisplayName(displayName);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertTrue(action.getActionMessages().size() > 0, "Action didn't return messages indicating success");

    //check properties on action
    assertEquals(action.getArticleList().getListCode(), articleList.getListCode(),
        "action changed article list after update");
    assertEquals(action.getDisplayName(), displayName,
        "action had incorrect display name");

    assertEquals(action.getArticleList().getArticleDois().toArray(), orderedArticlesForDb,
        "Action returned wrong Dois in articleList");

    assertEquals(action.getArticleOrderCSV(), reorderedArticleCsv,
        "Action returned different articleOrderCSV");

    //the action should show the article csv in order of the valid articles
    assertEquals(isSorted(action.getArticleOrderCSV().split(","), newIndices), true,
        "Action returned wrong order after update articles");

    for (ArticleInfo articleInfo: action.getArticleInfoList()) {
      String doi = articleInfo.getDoi();
      assertEquals(validDois.contains(doi), true,
          "Action returned orphan articles in articleInfoList");
    }
    for (String doi: action.getOrphanDois()) {
      assertEquals(orphanDois.contains(doi), true,
          "Action returned valid article in orphanDois");
    }

    //check what got stored to the database
    ArticleList storedArticleList = dummyDataStore.get(ArticleList.class, articleList.getID());

    assertEquals(storedArticleList.getDisplayName(), displayName,
        "Article List got saved to the database with incorrect display name");

    assertEquals(storedArticleList.getArticleDois().size(), existingArticles.size(),
        "Articles got removed or added from the list on reordering");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecuteNoAction"}, alwaysRun = true)
  public void testActionDoesNotAllowAddingArticleToCsv(ArticleList articleList, Map<String, Integer> indices,
                                                       Set<String> validDois, Set<String> orphanDois) throws Exception {
    //execute the action to get the original csv
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String incorrectCsv = originalCsv + ",id:some-fake-new-article";

    action.setCommand("UPDATE_LIST");
    action.setArticleOrderCSV(incorrectCsv);
    action.setDisplayName(articleList.getDisplayName());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 0, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecuteNoAction"}, alwaysRun = true)
  public void testActionDoesNotAllowRemovingArticleFromCsv(ArticleList articleList, Map<String, Integer> indices,
                                                           Set<String> validDois, Set<String> orphanDois) throws Exception {
    //execute the action to get the original csv
    action.setListCode(articleList.getListCode());
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String articleToRemove = originalCsv.substring(0, originalCsv.indexOf(","));
    String incorrectCsv = originalCsv.replace(articleToRemove + ",", "");

    action.setCommand("UPDATE_LIST");
    action.setArticleOrderCSV(incorrectCsv);
    action.setDisplayName(articleList.getDisplayName());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 0, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecuteNoAction"}, alwaysRun = true)
  public void testActionDoesNotAllowChangingArticlesInCsv(ArticleList articleList, Map<String, Integer> indices,
                                                          Set<String> validDois, Set<String> orphanDois) throws Exception {
    //execute the action to get the original csv
    action.setListCode(articleList.getListCode());
    action.execute();
    String originalCsv = action.getArticleOrderCSV();

    String changedCsv = originalCsv.substring(originalCsv.indexOf(",") + 1);
    changedCsv = "id:this-article-was-not-in-original-csv," + changedCsv;

    assertEquals(changedCsv.split(",").length, originalCsv.split(",").length,
        "test added or removed articles instead of just changing one");


    action.setCommand("UPDATE_LIST");
    action.setArticleOrderCSV(changedCsv);
    action.setDisplayName(articleList.getDisplayName());

    //should fail
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionErrors().size() > 0, "Action didn't return error messages");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecuteNoAction", "testUpdateArticleList"}, alwaysRun = true)
  public void testAddArticle(ArticleList articleList, Map<String, Integer> indices,
                             Set<String> validDois, Set<String> orphanDois) throws Exception {
    String articlesToAddCsv = "id:new-article-for-adding-1,id:new-article-for-adding-2";
    for (String doi: articlesToAddCsv.split(",")) {
      Article article = new Article();
      article.setDoi(doi);
      article.setTitle("New article for adding");
      dummyDataStore.store(article);
    }

    action.setCommand("ADD_ARTICLE");
    action.setListCode(articleList.getListCode());
    action.setArticlesToAddCsv(articlesToAddCsv);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return messages indicating success");

    for (String doi : articlesToAddCsv.split(",")) {
      assertTrue(action.getArticleOrderCSV().contains(doi), "Article " + doi + " didn't get added to action's csv");
      assertTrue(action.getArticleList().getArticleDois().contains(doi),
          "Article " + doi + " didn't get added to action's articleList");
    }

    //check the values that got stored to the database
    List<String> storedArticles = dummyDataStore.get(ArticleList.class, articleList.getID()).getArticleDois();
    for (String doi : articlesToAddCsv.split(",")) {
      assertTrue(storedArticles.contains(doi), "Article " + doi + " didn't get added to the list in the database");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecuteNoAction"}, alwaysRun = true)
  public void testRemoveArticles(ArticleList articleList, Map<String, Integer> indices,
                                 Set<String> validDois, Set<String> orphanDois) throws Exception {
    // delete first three dois
    List<String> articlesToDelete = dummyDataStore.get(ArticleList.class, articleList.getID()).getArticleDois().subList(0, 3);
    String[] articlesToDeleteArray = new String[3];
    for (int i = 0; i < 3; i++) {
      articlesToDeleteArray[i] = articlesToDelete.get(i);
    }


    action.setCommand("REMOVE_ARTICLES");
    action.setListCode(articleList.getListCode());
    action.setArticlesToRemove(articlesToDeleteArray);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");

    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return messages indicating success");

    for (String doi : articlesToDelete) {
      assertFalse(action.getArticleList().getArticleDois().contains(doi),
          "Article " + doi + " didn't get removed from action's article list");
      assertFalse(action.getArticleOrderCSV().contains(doi),
          "Article " + doi + " didn't get removed from action's csv");
    }

    //check the values in the db
    List<String> storedArticleList = dummyDataStore.get(ArticleList.class, articleList.getID()).getArticleDois();
    for (String doi : articlesToDelete) {
      assertFalse(storedArticleList.contains(doi), "Article " + doi + " didn't get removed from list in the database");
      try {
        articleService.getArticle(doi, DEFAULT_ADMIN_AUTHID);
      } catch (NoSuchArticleIdException e) {
        fail("Article " + doi + " got deleted from the database instead of just being removed from the list");
      }
    }
  }
}