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
import org.ambraproject.models.Article;
import org.ambraproject.models.Journal;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 1/30/12
 */
public class CrossPubManagementActionTest extends AdminWebTest {

  @Autowired
  protected CrossPubManagementAction action;

  @DataProvider
  public Object[][] expectedDois() {
    Journal journal = new Journal("crosspubManagementAction journal");
    journal.seteIssn("crossPubEissn");
    dummyDataStore.store(journal);

    List<String> expectedDois = new ArrayList<String>(3);
    for (int i = 1; i <= 3; i++) {
      Article article = new Article("id:crossPubManagement-" + i);
      article.seteIssn(defaultJournal.geteIssn());
      article.setJournals(new HashSet<Journal>(2));
      article.getJournals().add(defaultJournal);
      article.getJournals().add(journal);
      dummyDataStore.store(article);
      expectedDois.add(article.getDoi());
    }
    Article pubbedInJournal = new Article("id:crossPubManagementPubbedInJournal");
    pubbedInJournal.seteIssn(journal.geteIssn());
    pubbedInJournal.setJournals(new HashSet<Journal>(1));
    pubbedInJournal.getJournals().add(journal);
    dummyDataStore.store(pubbedInJournal);

    Article notCrosspubbed = new Article("id:crossPubManagementNotCrossPubbed");
    notCrosspubbed.seteIssn(defaultJournal.geteIssn());
    notCrosspubbed.setJournals(new HashSet<Journal>(1));
    notCrosspubbed.getJournals().add(defaultJournal);
    dummyDataStore.store(notCrosspubbed);

    Map<String, Object> requestAttributes = getDefaultRequestAttributes();
    requestAttributes.put(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT,
        new VirtualJournalContext(journal.getJournalKey(), defaultJournal.getJournalKey(),
            "http", 80, "localhost", "", new ArrayList<String>(0)));

    return new Object[][]{
        {requestAttributes, journal, expectedDois}
    };
  }

  @Test(dataProvider = "expectedDois")
  public void testExecute(Map<String, Object> requestAttributes, Journal journal, List<String> expectedDois) throws Exception {
    action.setRequest(requestAttributes);
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    assertNotNull(action.getDois(), "action had null list of dois");
    assertEqualsNoOrder(action.getDois().toArray(), expectedDois.toArray(), "Action had incorrect dois");
  }

  @Test(dataProvider = "expectedDois", dependsOnMethods = {"testExecute"})
  public void testRemoveArticles(Map<String, Object> requestAttributes, Journal journal, List<String> expectedDois) throws Exception {
    //cross pub a bunch of articles and then remove them
    String[] articlesToRemove = new String[3];
    List<Article> removedArticles = new ArrayList<Article>(3);
    for (int i = 0; i < 3; i++) {
      Article article = new Article("id:crossPubManagementArticleToRemove-" + i);
      article.seteIssn(defaultJournal.geteIssn());
      article.setJournals(new HashSet<Journal>(2));
      article.getJournals().add(defaultJournal);
      article.getJournals().add(journal);
      dummyDataStore.store(article);
      articlesToRemove[i] = article.getDoi();
      removedArticles.add(article);
    }

    action.setCommand("REMOVE_ARTICLES");
    action.setArticlesToRemove(articlesToRemove);
    action.setRequest(requestAttributes);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 3, "Action didn't return messages indicating succes");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");
    assertEqualsNoOrder(action.getDois().toArray(), expectedDois.toArray(), "action had incorrect dois");

    for (Article article : removedArticles) {
      assertFalse(dummyDataStore.get(Article.class, article.getID()).getJournals().contains(journal),
          "journal didn't get removed from article");
    }
  }

  @Test(dataProvider = "expectedDois", dependsOnMethods = {"testExecute","testRemoveArticles"})
  public void testAddArticles(Map<String, Object> requestAttributes, Journal journal, List<String> expectedDois) throws Exception {
    List<Article> articles = new ArrayList<Article>(3);
    List<String> doisToAdd = new ArrayList<String>(3);
    for (int i = 0; i < 3; i++) {
      Article article = new Article();
      article.setDoi("id:testArticleToAdd" + i);
      article.seteIssn("notTheDefaulteIssn");
      dummyDataStore.store(article);
      articles.add(article);
      doisToAdd.add(article.getDoi());
    }

    action.setCommand("ADD_ARTICLES");
    action.setArticlesToAdd(StringUtils.join(doisToAdd, ","));
    action.setRequest(requestAttributes);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 3, "Action didn't return messages indicating succes");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages");

    expectedDois.addAll(doisToAdd);
    assertEqualsNoOrder(action.getDois().toArray(), expectedDois.toArray(), "Action had incorrect doi list");


    for (Article article : articles) {
      assertTrue(dummyDataStore.get(Article.class, article.getID()).getJournals().contains(journal),
          "journal didn't get added to article");
    }
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
