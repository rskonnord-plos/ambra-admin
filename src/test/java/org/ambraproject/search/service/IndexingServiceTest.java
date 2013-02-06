/*
 * $HeadURL: http://svn.ambraproject.org/svn/ambra/ambra-admin/branches/january_fixes/src/test/java/org/ambraproject/search/service/ArticleIndexingServiceTest.java $
 * $Id: ArticleIndexingServiceTest.java 11490 2012-08-30 22:46:10Z akudlick $
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.search.service;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.models.Article;
import org.ambraproject.service.search.SolrServerFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.ambraproject.ApplicationException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test for {@link IndexingService}.  The test methods belong to two groups:
 * <ol><li>originalConfig</li><li>badConfig</li></ol>
 * <p/>
 * The methods in the &quot;originalConfig&quot; group must run before those in the &quot;badConfig&quot; group since
 * the latter change the configuration on the indexing bean.  This is enforced by setting
 * dependsOnGroups={&quot;originalConfig&quot;} on the &quot;badConfig&quot; methods
 *
 * @author Dragisa Krsmanovic
 * @author Joe Osowski
 */
public class IndexingServiceTest extends AdminBaseTest {

  private static String oneArticleId = "info:doi/10.1371/journal.pgen.1000096";
  private static String badConfigFile = "org/ambraproject/search/searchConfig-badValues.xml";

  @Autowired
  protected IndexingService indexingService;

  @Autowired
  protected SolrServerFactory solrServerFactory;

  @BeforeGroups(groups = {"badConfig"})
  private void setBadConfig() throws ConfigurationException {
    String fileName = getClass().getClassLoader().getResource(badConfigFile).getFile();
    Configuration tempConfiguration = new XMLConfiguration(fileName);
    indexingService.setAmbraConfiguration(tempConfiguration);
  }

  @DataProvider(name = "articleData")
  public Object[][] getArticleData() {
    Article article1 = new Article();
    article1.setDoi("info:doi/10.1371/journal.pgen.1000096");
    article1.setState(Article.STATE_ACTIVE);
    dummyDataStore.store(article1);
    Article article2 = new Article();
    article2.setDoi("info:doi/10.1371/journal.pgen.1000100");
    article2.setState(Article.STATE_ACTIVE);
    dummyDataStore.store(article2);

    return new Object[][]{{article1}, {article2}};
  }

  @Test(dataProvider = "articleData", groups = {"originalConfig"})
  public void testArticlePublished(Article article) throws Exception {
    String articleId = article.getDoi();

    indexingService.articlePublished(articleId);

    String solrID = articleId.replaceAll("info:doi/", "");
    SolrQuery query = new SolrQuery("id:" + solrID);
    QueryResponse solrRes = solrServerFactory.getServer().query(query);

    SolrDocumentList sdl = solrRes.getResults();
    assertEquals(1l, sdl.getNumFound(), "didn't send article to solr server");
  }

  @Test(groups = {"badConfig"}, dependsOnGroups = {"originalConfig"})
  public void testNoIndexingQueueConfigured() throws Exception {
    indexingService.articlePublished(oneArticleId);
  }

  @Test(dataProvider = "articleData", groups = {"originalConfig"}, dependsOnMethods = {"testIndexArticle"})
  public void testArticleDeleted(Article article) throws Exception {
    String articleId = article.getDoi();
    indexingService.indexArticle(articleId);
    String solrID = articleId.replaceAll("info:doi/", "");

    //delete it.
    indexingService.articleDeleted(articleId);

    //confirm it was removed.
    SolrQuery query = new SolrQuery("id:" + solrID);
    QueryResponse solrRes = solrServerFactory.getServer().query(query);
    SolrDocumentList sdl = solrRes.getResults();

    assertEquals(0, sdl.getNumFound(), "failed to remove article from solr server");
  }

  @Test(groups = {"badConfig"}, dependsOnGroups = {"originalConfig"})
  public void testNoDeleteQueueConfigured() throws Exception {
    indexingService.articleDeleted(oneArticleId);
  }

  @Test(groups = {"originalConfig"})
  public void testArticleCrossPublished() throws Exception {
    dummyDataStore.store(new Article(oneArticleId));
    indexingService.articleCrossPublished(oneArticleId);
  }

  @Test(groups = {"badConfig"}, dependsOnGroups = {"originalConfig"})
  public void testNoCrossPublishIndexingQueueConfigured() throws Exception {
    indexingService.articleCrossPublished(oneArticleId);
  }

  @Test(dataProvider = "articleData", groups = {"originalConfig"})
  public void testIndexArticle(Article article) throws Exception {
    indexingService.indexArticle(article.getDoi());
  }


  @Test(expectedExceptions = {ApplicationException.class}, groups = {"badConfig"}, dependsOnGroups = {"originalConfig"})
  public void testIndexArticleNoQueueSet() throws Exception {
    indexingService.indexArticle(oneArticleId);
  }
}

