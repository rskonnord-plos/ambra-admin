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
import org.ambraproject.admin.service.DocumentManagementService;
import org.ambraproject.admin.service.SyndicationService;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.article.service.SampleArticleData;
import org.ambraproject.filestore.FSIDMapper;
import org.ambraproject.models.Article;
import org.ambraproject.models.Syndication;
import org.ambraproject.views.article.ArticleInfo;
import org.apache.commons.io.FileUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipFile;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Alex Kudlick 1/24/12
 */
public class AdminTopActionTest extends AdminWebTest {

  @Autowired
  protected AdminTopAction action;

  @Autowired
  protected ArticleService articleService; //just using this to check on articles that got ingested

  @Autowired
  protected DocumentManagementService documentManagementService;

  @Autowired
  protected SyndicationService syndicationService; //just using this to check that things get syndicated

  @Autowired
  @Qualifier("ingestDir")
  protected String ingestDir;
  @Autowired
  @Qualifier("ingestedDir")
  protected String ingestedDir;
  @Autowired
  @Qualifier("filestoreDir")
  protected String filestoreDir;

  @BeforeClass
  public void resetResources() {
    // TODO if there is a way to reset the resources, that logic should replace this hack
    // (files in the test-classes/ingest folder, which are files in the test/resources/ingest)
    // having this extra file here is causing the testBasicRequest test to fail
    File zip = new File(ingestDir, "pone.00000.zip");
    if (zip.exists()) {
      zip.delete();
    }
  }

  @Test
  public void testBasicRequest() throws Exception {
    //Add a publishable article
    Article publishableArticle = new Article();
    publishableArticle.setDoi("id:publishable-article-for-adminTopAction");
    publishableArticle.setState(Article.STATE_UNPUBLISHED);
    publishableArticle.setDate(Calendar.getInstance().getTime());
    publishableArticle.seteIssn(defaultJournal.geteIssn());
    dummyDataStore.store(publishableArticle);
    //Add a syndication for the article
    Syndication syndication = new Syndication();
    syndication.setDoi(publishableArticle.getDoi());
    syndication.setTarget("PMC");
    syndication.setStatus(Syndication.STATUS_PENDING);
    dummyDataStore.store(syndication);

    Article failedSyndicationArticle = new Article();
    failedSyndicationArticle.setDoi("id:article-with-failed-syndication-for-adminTop");
    failedSyndicationArticle.seteIssn(defaultJournal.geteIssn());
    dummyDataStore.store(failedSyndicationArticle);

    Syndication failedSyndication = new Syndication();
    failedSyndication.setDoi(failedSyndicationArticle.getDoi());
    failedSyndication.setTarget("FOO");
    failedSyndication.setStatus(Syndication.STATUS_FAILURE);
    dummyDataStore.store(failedSyndication);

    //run execute()
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getUploadableFiles().size(), 7, "Action returned incorrect number of uploadable files");
    boolean foundCorrectArticle = false;
    for (ArticleInfo article : action.getPublishableArticles()) {
      if (article.getDoi().equals(publishableArticle.getDoi())) {
        foundCorrectArticle = true;
        break;
      }
    }
    assertTrue(foundCorrectArticle, "Action didn't correctly return a publishable article");
    assertEquals(action.getPublishableSyndications().get(publishableArticle.getDoi()).size(), 1,
        "Action didn't return publishable syndication");
    assertTrue(action.getIsFailedSyndications(), "Action didn't find failed syndications");
  }

  @DataProvider(name = "articlesToSort")
  public Object[][] getArticlesToSort() {

    List<Article> articleList = dummyDataStore.findByCriteria(
        DetachedCriteria.forClass(Article.class)
            .add(Restrictions.eq("state",Article.STATE_UNPUBLISHED)
            )
    );

    for (Article article : articleList) {
      dummyDataStore.delete(article);
    }

    //some fake pub dates
    Calendar oneYearAgo = Calendar.getInstance();
    oneYearAgo.add(Calendar.YEAR, -1);
    Calendar oneMonthAgo = Calendar.getInstance();
    oneMonthAgo.add(Calendar.MONTH, -1);
    Calendar today = Calendar.getInstance();

    //Populate db w/ some articles
    Article article1 = new Article();
    article1.setDoi("id:article-for-sorting1");
    article1.setDate(oneYearAgo.getTime());
    article1.setState(Article.STATE_UNPUBLISHED);
    article1.seteIssn(defaultJournal.geteIssn());
    dummyDataStore.store(article1);

    Article article2 = new Article();
    article2.setDoi("id:article-for-sorting2");
    article2.setDate(today.getTime());
    article2.setState(Article.STATE_UNPUBLISHED);
    article2.seteIssn(defaultJournal.geteIssn());
    dummyDataStore.store(article2);

    Article article3 = new Article();
    article3.setDoi("id:article-for-sorting3");
    article3.setDate(oneMonthAgo.getTime());
    article3.setState(Article.STATE_UNPUBLISHED);
    article3.seteIssn(defaultJournal.geteIssn());
    dummyDataStore.store(article3);

    final Comparator<ArticleInfo> dateAscending = new Comparator<ArticleInfo>() {
      @Override
      public int compare(ArticleInfo article, ArticleInfo article1) {
        if (article.getDate() == null) {
          return article1.getDate() == null ? 0 : -1;
        } else if (article1.getDate() == null) {
          return 1;
        }
        return article.getDate().compareTo(article1.getDate());
      }
    };
    final Comparator<ArticleInfo> dateDescending = new Comparator<ArticleInfo>() {
      @Override
      public int compare(ArticleInfo article, ArticleInfo article1) {
        return -1 * dateAscending.compare(article, article1);
      }
    };
    final Comparator<ArticleInfo> doiAscending = new Comparator<ArticleInfo>() {
      @Override
      public int compare(ArticleInfo article, ArticleInfo article1) {
        return article.getDoi().compareTo(article1.getDoi());
      }
    };
    final Comparator<ArticleInfo> doiDescending = new Comparator<ArticleInfo>() {
      @Override
      public int compare(ArticleInfo article, ArticleInfo article1) {
        return -1 * doiAscending.compare(article, article1);
      }
    };
    return new Object[][]{
        {"Sort by Pub Date Asc", dateAscending},
        {"Sort by Pub Date Desc", dateDescending},
        {"Sort by DOI Asc", doiAscending},
        {"Sort by DOI Desc", doiDescending}
    };
  }

  @Test(dependsOnMethods = "testBasicRequest", dataProvider = "articlesToSort", alwaysRun = true)
  public void testSort(String directive, Comparator<ArticleInfo> comparator) {
    action.setAction(directive);
    action.processArticles();

    assertTrue(action.getPublishableArticles().size() > 1, "action didn't have any publishable articles");

    for (int i = 0; i < action.getPublishableArticles().size() - 1; i++) {
      ArticleInfo article = action.getPublishableArticles().get(i);
      ArticleInfo nextArticle = action.getPublishableArticles().get(i + 1);
      assertTrue(comparator.compare(article, nextArticle) <= 0,
          "Articles weren't in order when sorting by: '" + directive + "'");
    }
  }

  private String crossrefFileName(String doi) {
    return doi.replaceAll("[.:/]", "_") + ".xml";
  }

  private String zipFileName(String doi) {
    return doi.replaceFirst("info:doi/10.1371/journal.", "") + ".zip";
  }

  //Ingester test tests all the different scenarios of ingest, so we won't do anything comprehensive here.  Just test the overall ingest.
  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleArticle")
  public void testIngest(ZipFile archive, Article article) throws Exception {
    String zipFileName = new File(archive.getName()).getName();
    File crossref_file = new File(ingestedDir, crossrefFileName(article.getDoi()));
    File articleDir = new File(filestoreDir, article.getDoi().replaceAll("info:doi/10.1371/journal.", "10.1371/"));

    //delete the article in case it's still in the database from the ingester test
    try {
      documentManagementService.delete(article.getDoi(), DEFAULT_ADMIN_AUTHID);
    } catch (NoSuchArticleIdException e) {
      //ignore
    }

    try {
      action.setFilesToIngest(new String[]{zipFileName});
      String result = action.ingest();
      assertEquals(result, Action.SUCCESS, "Action didn't return success");
      assertTrue(new File(ingestedDir, zipFileName).exists(), "Zip file didn't get moved to ingested directory");
      assertFalse(new File(ingestDir, zipFileName).exists(), "Zip file didn't get moved to ingested directory");
      assertTrue(crossref_file.exists(), "crossref file didn't get created");
      assertTrue(articleDir.exists(), "Article didn't get written to the file store");

      try {
        articleService.getArticle(article.getDoi(), DEFAULT_ADMIN_AUTHID);
      } catch (NoSuchArticleIdException e) {
        fail("Article didn't get written to the database");
      }

      //cleanup
    } finally {
      FileUtils.deleteQuietly(crossref_file);
      try {
        //move the archive back
        FileUtils.moveFile(new File(ingestedDir, zipFileName), new File(ingestDir, zipFileName));
      } catch (IOException e) {
        //suppress
      }
      try {
        //delete the files that got written to the filestore
        FileUtils.deleteDirectory(articleDir);
      } catch (IOException e) {
        //suppress
      }
      //delete the article
      documentManagementService.delete(article.getDoi(), DEFAULT_ADMIN_AUTHID);
    }
  }

  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleArticle")
  public void testIngestDuplicateArticle(ZipFile archive, Article article) throws Exception {
    String zipFileName = new File(archive.getName()).getName();

    Article articleToStore = new Article();
    articleToStore.setDoi(article.getDoi());
    dummyDataStore.store(articleToStore);

    File destinationFile = new File(ingestedDir, zipFileName);
    File crossrefFile = new File(ingestedDir, crossrefFileName(article.getDoi()));
    try {
      dummyDataStore.store(article);
      action.setFilesToIngest(new String[]{zipFileName});
      action.ingest();
      assertNotNull(action.getActionErrors(), "nul list of errors");
      assertTrue(action.getActionErrors().size() > 0, "didn't add an error for ingest");
      assertFalse(destinationFile.exists(), "Zip file got moved to ingested dir");
      assertFalse(crossrefFile.exists(), "Crossref file got created in ingest dir");

    } finally {
      if (destinationFile.exists()) {
        try {
          FileUtils.moveFile(destinationFile, new File(ingestDir, zipFileName));
        } catch (IOException e) {
          //ignore
        }
      }
      documentManagementService.delete(article.getDoi(), DEFAULT_ADMIN_AUTHID);
    }
  }


  @DataProvider(name = "articleToDisable")
  public Object[][] articleToDisable() throws IOException {
    Article article = new Article();
    article.setDoi("info:doi/10.1371/journal.pone.000102");
    article.setState(Article.STATE_ACTIVE);

    //create files in the ingested and file store directories to check that they get moved out
    File articleDir = new File(filestoreDir, "/10.1371/pone.000102");
    File zip = new File(ingestedDir, "pone.000102.zip");
    File crossref = new File(ingestedDir, crossrefFileName(article.getDoi()));

    if (!articleDir.exists()) {
      articleDir.mkdirs();
      File xml = new File(articleDir, "pone.000102.xml");
      if (!xml.exists()) {
        xml.createNewFile();
      }
    }
    if (!zip.exists()) {
      zip.createNewFile();
    }
    if (!crossref.exists()) {
      crossref.createNewFile();
    }

    Long id = Long.valueOf(dummyDataStore.store(article));
    return new Object[][]{
        {article.getDoi(), id}
    };
  }

  @Test(dataProvider = "articleToDisable")
  public void testDisableOneArticle(String doi, Long articleId) throws Exception {
    try {
      action.setArticle(doi);
      String result = action.disableArticle();
      assertEquals(result, Action.SUCCESS);
      assertEquals(dummyDataStore.get(Article.class, articleId).getState(),
          Article.STATE_DISABLED, "Article didn't get correct state set");

      assertFalse(new File(filestoreDir, FSIDMapper.doiTofsid(doi, "xml")).exists(), "article didn't get deleted from the filestore");
      assertFalse(new File(ingestedDir, crossrefFileName(doi)).exists(), "crossref file didn't get deleted in the ingested folder");
      assertFalse(new File(ingestedDir, zipFileName(doi)).exists(), "zip file didn't get deleted from the ingested folder");
      assertTrue(new File(ingestDir, zipFileName(doi)).exists(), "zip file didn't get moved to the ingest folder");
    } finally {
      //Clean up any files that may still be around
      FileUtils.deleteQuietly(new File(ingestedDir, crossrefFileName(doi)));
      FileUtils.deleteQuietly(new File(ingestedDir, zipFileName(doi)));
      FileUtils.deleteQuietly(new File(ingestDir, zipFileName(doi)));
      try {
        FileUtils.deleteDirectory(new File(filestoreDir, FSIDMapper.zipToFSID(doi, "")));
      } catch (IOException e) {
        //suppress
      }
    }
  }

  @Test(dataProvider = "articleToDisable")
  public void testUnpublishArticle(String doi, Long articleId) throws Exception {
    try {
      action.setArticle(doi);
      String result = action.unpublish();
      assertEquals(result, Action.SUCCESS);
      assertEquals(dummyDataStore.get(Article.class, articleId).getState(),
          Article.STATE_UNPUBLISHED, "Article didn't get correct state set");

      assertTrue(new File(filestoreDir, FSIDMapper.doiTofsid(doi, "xml")).exists(), "article was deleted from the filestore");
      assertTrue(new File(ingestedDir, crossrefFileName(doi)).exists(), "crossref file was deleted from the ingested folder");
      assertTrue(new File(ingestedDir, zipFileName(doi)).exists(), "zip file was deleted from the ingested folder");
      assertFalse(new File(ingestDir, zipFileName(doi)).exists(), "zip file was added to the ingest folder");
    } finally {
      //Clean up any files that may still be around
      FileUtils.deleteQuietly(new File(ingestedDir, crossrefFileName(doi)));
      FileUtils.deleteQuietly(new File(ingestedDir, zipFileName(doi)));
      FileUtils.deleteQuietly(new File(ingestDir, zipFileName(doi)));
      try {
        FileUtils.deleteDirectory(new File(filestoreDir, FSIDMapper.zipToFSID(doi, "")));
      } catch (IOException e) {
        //suppress
      }
    }
  }

  @DataProvider(name = "articles")
  public Object[][] getArticles() {
    Article article1 = new Article();
    article1.setDoi("info:doi/10.1371/journal.ppat.0030025");
    article1.setState(Article.STATE_UNPUBLISHED);
    article1.setArchiveName("ppat.0030025.zip");
    Long id1 = Long.valueOf(dummyDataStore.store(article1));

    Syndication syndication1 = new Syndication();
    syndication1.setDoi(article1.getDoi());
    syndication1.setTarget("FOO");
    syndication1.setStatus(Syndication.STATUS_PENDING);
    dummyDataStore.store(syndication1);

    Article article2 = new Article();
    article2.setDoi("info:doi/10.1371/journal.pone.0015784");
    article2.setArchiveName("pone.0015784");
    article2.setState(Article.STATE_UNPUBLISHED);
    Long id2 = Long.valueOf(dummyDataStore.store(article2));

    return new Object[][]{
        {new String[]{article1.getDoi(), article2.getDoi()}, new Long[]{id1, id2}}
    };
  }

  @Test(dataProvider = "articles")
  public void testPublishAndSyndicate(String[] dois, Long[] ids) {
    String[] syndicates = new String[dois.length];
    for (int i = 0; i < dois.length; i++) {
      syndicates[i] = dois[i] + "::FOO";
    }
    action.setArticles(dois);
    action.setSyndicates(syndicates);
    action.setAction("Publish and Syndicate");
    assertEquals(action.processArticles(), Action.SUCCESS, "Action didn't return success");
    for (Long id : ids) {
      assertEquals(dummyDataStore.get(Article.class, id).getState(), Article.STATE_ACTIVE,
          "Article didn't get state set to published");
    }
    for (String doi : dois) {
      assertEquals(syndicationService.getSyndication(doi, "FOO").getStatus(), Syndication.STATUS_IN_PROGRESS,
          "Syndication didn't get status updated");
    }
  }

  @Test(dataProvider = "articles", dependsOnMethods = {"testPublishAndSyndicate"})
  public void testDisableArticles(String[] dois, Long[] ids) {
    action.setArticles(dois);
    action.setAction("Disable and Revert Ingest");
    assertEquals(action.processArticles(), Action.SUCCESS, "Action didn't return success");

    //We already check moving files around in testDisableOneArticle(), so here we'll just check the db
    for (Long id : ids) {
      assertEquals(dummyDataStore.get(Article.class, id).getState(), Article.STATE_DISABLED, "Article didn't get disabled");
    }
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
