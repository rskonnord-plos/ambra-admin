package org.ambraproject.admin.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.ArticleList;
import org.ambraproject.models.Journal;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ManageArticleListActionTest extends AdminWebTest {

  @Autowired
  protected ManageArticleListAction action;

  @DataProvider(name = "basicInfo")
  public Object[][] getCurrentArticleList() {
    Journal journal = new Journal();
    journal.setJournalKey("journalForTestManageArticleLists");
    journal.seteIssn("eIssnjournalForTestManageArticleLists");
    journal.setArticleList(new ArrayList<ArticleList>(3));

    for (int i = 1; i <= 3; i++) {
      ArticleList articleList = new ArticleList();
      articleList.setDisplayName("news" + i);
      articleList.setListCode("id:fake-list-for-manage-journals" + i);
      dummyDataStore.store(articleList);
      journal.getArticleList().add(dummyDataStore.get(ArticleList.class,articleList.getID()));
    }

    dummyDataStore.store(journal);

    return new Object[][]{
        {journal}
    };
  }

  @Test(dataProvider = "basicInfo")
  public void testExecute(Journal journal) throws Exception {
    //make sure to use a journal for this test
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");

    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default execute");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    assertEquals(action.getArticleList().size(), journal.getArticleList().size(),"Action returned incorrect number " +"of " +"article list");
    for (int i = 0; i < journal.getArticleList().size(); i++) {
      ArticleList actual = action.getArticleList().get(i);
      ArticleList expected = journal.getArticleList().get(i);
      assertEquals(actual.getListCode(), expected.getListCode(), "Article List " + (i + 1) + " didn't have correct " +
          "listCode");
      assertEquals(actual.getDisplayName(), expected.getDisplayName(),
          "Article List " + (i + 1) + " didn't have correct display name");
    }
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testCreateArticleList(Journal journal) throws Exception {
    int initialNumberOfArticleList = dummyDataStore.get(Journal.class, journal.getID()).getArticleList().size();
    String listCode = "id:new-list-for-create-articlelist";
    String articleListDisplayName = "News";
    //set properties on the action
    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("CREATE_LIST");
    action.setListCode(listCode);
    action.setDisplayName(articleListDisplayName);

    //run the action
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check action's return values
    assertEquals(action.getArticleList().size(), initialNumberOfArticleList + 1, "action didn't add new articleList to list");
    ArticleList actualList = action.getArticleList().get(action.getArticleList().size() - 1);
    assertEquals(actualList.getListCode(), listCode, "Article List didn't have correct listCode");
    assertEquals(actualList.getDisplayName(), articleListDisplayName, "Article List didn't have correct name");

    assertTrue(action.getActionMessages().size() > 0, "Action didn't return a message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check values stored to the database
    Journal storedJournal = dummyDataStore.get(Journal.class, journal.getID());
    assertEquals(storedJournal.getArticleList().size(), initialNumberOfArticleList + 1,
        "journal didn't get article list added in the database");

    assertEquals(storedJournal.getArticleList().get(storedJournal.getArticleList().size() - 1).getListCode(), listCode,
        "Journal didn't have article list added in the db");

    //try creating a duplicate article list and see if we get an error message
    action.execute();
    assertEquals(action.getActionErrors().size(), 1, "action didn't add error when trying to save duplicate article " +
        "list");
  }

  @Test(dataProvider = "basicInfo", dependsOnMethods = {"testExecute"}, alwaysRun = true)
  public void testRemoveArticleList(Journal journal) throws Exception {
    List<ArticleList> initialArticleList = dummyDataStore.get(Journal.class, journal.getID()).getArticleList();

    String[] listCodeToDelete = new String[]{initialArticleList.get(0).getListCode(), initialArticleList.get(2).getListCode()};
    List<ArticleList> listToDelete = new ArrayList<ArticleList>(listCodeToDelete.length);
    for (ArticleList articleList : initialArticleList) {
      if (ArrayUtils.indexOf(listCodeToDelete, articleList.getListCode()) != -1) {
        listToDelete.add(articleList);
      }
    }

    Map<String, Object> request = getDefaultRequestAttributes();
    request.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, makeVirtualJournalContext(journal));
    action.setRequest(request);
    action.setCommand("REMOVE_LIST");
    action.setListToDelete(listCodeToDelete);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    //check the return values on the action
    assertEquals(action.getArticleList().size(), initialArticleList.size() - 2, "action didn't remove article list");
    assertTrue(action.getActionMessages().size() > 0, "Action didn't add message for deleting article list");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");


    List<ArticleList> storedArticleList = dummyDataStore.get(Journal.class, journal.getID()).getArticleList();
    for (ArticleList deletedArticleList : listToDelete) {
      assertFalse(storedArticleList.contains(deletedArticleList), "Article List " + deletedArticleList + " didn't get " +
          "removed " + "from " + "journal");
      assertNull(dummyDataStore.get(ArticleList.class, deletedArticleList.getID()), "Article List didn't get removed from " +
          "the database");
    }
  }

  private VirtualJournalContext makeVirtualJournalContext(Journal journal) {
    return new VirtualJournalContext(
        journal.getJournalKey(),
        "dfltJournal",
        "http",
        80,
        "localhost",
        "ambra-webapp",
        new ArrayList<String>());
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }


}
