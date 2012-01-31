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

import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.Article;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Journal;

import java.net.URI;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 1/30/12
 */
public class CrossPubManagementActionTest extends AdminWebTest {

  @Autowired
  protected CrossPubManagementAction action;

  @Test
  public void testAddArticles() throws Exception {
    URI[] dois = new URI[3];
    for (int i = 0; i < 3; i++) {
      Article article = new Article();
      article.setDoi("id:testArticleToAdd" + i);
      article.seteIssn("notTheDefaulteIssn");
      dummyDataStore.store(article);
      dois[i] = URI.create(article.getDoi());
    }

    setupAdminContext();
    action.setCommand("ADD_ARTICLES");
    action.setArticlesToAdd(StringUtils.join(dois, ","));
    action.execute();

    Journal storedJournal = dummyDataStore.get(defaultJournal.getId(), Journal.class);
    for (URI doi : dois) {
      assertTrue(storedJournal.getSimpleCollection().contains(doi), "Article " + doi + " didn't get added to journal");
    }
  }
  
  @Test
  public void testRemoveArticles() throws Exception {
    Journal storedJournal = dummyDataStore.get(defaultJournal.getId(), Journal.class);
    
    String[] dois = new String[3];
    for (int i = 0; i < 3; i++) {
      Article article = new Article();
      article.setDoi("id:testArticleToRemove" + i);
      article.seteIssn("notTheDefaulteIssn");
      dummyDataStore.store(article);
      storedJournal.getSimpleCollection().add(URI.create(article.getDoi()));
      dois[i] = article.getDoi();
    }
    dummyDataStore.update(storedJournal);

    setupAdminContext();
    action.setCommand("REMOVE_ARTICLES");
    action.setArticlesToRemove(dois);
    action.execute();

    storedJournal = dummyDataStore.get(defaultJournal.getId(), Journal.class);
    for (String doi : dois) {
      assertFalse(storedJournal.getSimpleCollection().contains(URI.create(doi)), "Article " + doi + " didn't get removed from journal");
    }
  }
  
}
