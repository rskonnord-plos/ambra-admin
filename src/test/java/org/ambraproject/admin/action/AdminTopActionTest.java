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
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.article.service.ArticleService;
import org.ambraproject.article.service.NoSuchArticleIdException;
import org.ambraproject.article.service.SampleArticleData;
import org.ambraproject.models.Article;
import org.ambraproject.models.Syndication;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import static org.testng.Assert.*;

/**
 * @author Alex Kudlick 1/24/12
 */
public class AdminTopActionTest extends AdminWebTest {

  @Autowired
  protected AdminTopAction action;

  @Autowired
  protected ArticleService articleService; //just using this to check on articles that got ingested

  @Autowired
  @Qualifier("ingestDir")
  protected String ingestDir;
  @Autowired
  @Qualifier("ingestedDir")
  protected String ingestedDir;
  @Autowired
  @Qualifier("filestoreDir")
  protected String filestoreDir;

  @BeforeMethod
  public void setup() {
    setupAdminContext();
  }

  @Test
  public void testBasicRequest() throws Exception {
    //Add a publishable article
    Article publishableArticle = new Article();
    publishableArticle.setDoi("id:publishable-article-for-adminTopAction");
    publishableArticle.setState(Article.STATE_UNPUBLISHED);
    dummyDataStore.store(publishableArticle);
    //Add a syndication for the article
    Syndication syndication = new Syndication();
    syndication.setDoi(publishableArticle.getDoi());
    syndication.setTarget("PMC");
    syndication.setStatus(Syndication.STATUS_PENDING);
    dummyDataStore.store(syndication);

    //run execute()
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getUploadableFiles().size(), 7, "Action returned incorrect number of uploadable files");
    boolean foundCorrectArticle = false;
    for (Article article : action.getPublishableArticles()) {
      if (article.getDoi().equals(publishableArticle.getDoi())) {
        foundCorrectArticle = true;
        break;
      }
    }
    assertTrue(foundCorrectArticle, "Action didn't correctly return a publishable article");
    assertEquals(action.getPublishableSyndications().get(publishableArticle.getDoi()).size(), 1,
        "Action didn't return publishable syndication");
  }

  //Ingester test tests all the different scenarios of ingest, so we won't do anything comprehensive here.  Just test the overall ingest.
  @Test(dataProviderClass = SampleArticleData.class, dataProvider = "sampleArticle")
  public void testIngest(ZipFile archive, Article article) throws Exception {
    String zipFileName = new File(archive.getName()).getName();
    File crossref_file = new File(ingestedDir, article.getDoi().replaceAll("[.:/]", "_") + ".xml");
    File articleDir = new File(filestoreDir, article.getDoi().replaceAll("info:doi/10.1371/journal.", "10.1371/"));

    //delete the article in case it's still in the database from the ingester test
    try {
      articleService.delete(article.getDoi(), DEFAULT_ADMIN_AUTHID);
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
      articleService.delete(article.getDoi(), DEFAULT_ADMIN_AUTHID);
    }
  }


}
