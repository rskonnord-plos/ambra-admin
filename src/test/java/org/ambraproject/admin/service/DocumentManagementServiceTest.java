/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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

package org.ambraproject.admin.service;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.ArticleRelationship;
import org.ambraproject.models.UserProfile;
import org.custommonkey.xmlunit.XMLUnit;
import org.ambraproject.filestore.FSIDMapper;
import org.ambraproject.filestore.FileStoreException;
import org.ambraproject.filestore.FileStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.UnexpectedRollbackException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.ambraproject.action.BaseTest;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.models.Article;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Joe Osowski
 */
public class DocumentManagementServiceTest extends AdminBaseTest {
  @Autowired
  protected DocumentManagementService documentManagementService;
  @Autowired
  protected ArticleService articleService;
  @Autowired
  protected FileStoreService fileStoreService;

  final String articleArchive = "pone.0000202.zip";
  final String articleXmlFile = "info_doi_10_1371_journal_pone_0000202.xml";
  final String articleUri = "info:doi/10.1371/journal.pone.0000202";
  final String articleUri2 = "info:doi/10.1371/journal.pone.0000203";
  final String articleUri3 = "info:doi/10.1371/journal.pone.0000204";
  final String crossRefArticleXML = "sample-article.xml";
  final String crossRefArticleDOI = "info:doi/10.1371/journal.pone.0000008";
  final String crossRefArticleResultXML = "info_doi_10_1371_journal_pone_0000008.xml";
  final String crossRefArticleResultTestXML = "sample-crossref.xml";

  @DataProvider(name = "storedUnpublishedArticles")
  public Object[][] getStoredUnpublishedArticles()
  {
    Article article = new Article();
    article.setState(Article.STATE_UNPUBLISHED);
    article.setDoi(articleUri);

    dummyDataStore.store(article);

    return new Object[][] {
      { articleUri }
    };
  }

  @DataProvider(name = "storedPublishedArticles")
  public Object[][] getStoredpublishedArticles()
  {
    Article article = new Article();
    article.setState(Article.STATE_ACTIVE);
    article.setDoi(articleUri2);
    article.setRelatedArticles(new ArrayList<ArticleRelationship>());

    Long id = Long.valueOf(dummyDataStore.store(article));
    article.setID(id);

    UserProfile annotationCreator = new UserProfile(
        "authIdForDeleteArticle",
        "email@deleteArticle.org",
        "displayName@DelteArticle"
    );
    dummyDataStore.store(annotationCreator);
    //create some annotatons on the article
    Annotation comment = new Annotation(annotationCreator, AnnotationType.COMMENT, id);
    dummyDataStore.store(comment);
    Annotation reply = new Annotation(annotationCreator, AnnotationType.REPLY, id);
    reply.setParentID(comment.getID());
    dummyDataStore.store(reply);
    Annotation replyToReply = new Annotation(annotationCreator, AnnotationType.REPLY, id);
    replyToReply.setParentID(reply.getID());
    dummyDataStore.store(replyToReply);

    //foreign article relationship
    ArticleRelationship foreignRelationship = new ArticleRelationship();
    foreignRelationship.setOtherArticleDoi(article.getDoi());
    foreignRelationship.setOtherArticleID(id);

    Article relatedArticle = new Article("id:doi-for-related-article");
    relatedArticle.setRelatedArticles(Arrays.asList(foreignRelationship));
    dummyDataStore.store(relatedArticle);

    article = dummyDataStore.get(Article.class, id);
    //add a reciprocal relationship on original article
    ArticleRelationship relationship = new ArticleRelationship();
    relationship.setOtherArticleDoi(relatedArticle.getDoi());
    relationship.setOtherArticleID(relatedArticle.getID());
    article.getRelatedArticles().add(relationship);

    dummyDataStore.update(article);

    return new Object[][] {
      { articleUri2, id }
    };
  }

  @DataProvider(name = "storedPublishedArticles2")
  public Object[][] getStoredpublishedArticles2()
  {
    Article article = new Article();
    article.setState(Article.STATE_ACTIVE);
    article.setDoi(articleUri3);

    dummyDataStore.store(article);

    return new Object[][] {
      { articleUri3 }
    };
  }
  
  @AfterMethod(alwaysRun = true)
  public void cleanupFiles() {
    new File(documentManagementService.getDocumentDirectory(), articleArchive).delete();
    new File(documentManagementService.getIngestedDocumentDirectory(), articleArchive).delete();
  }

  @BeforeMethod(alwaysRun = true)
  public void cleanupFilesBefore() {
    new File(documentManagementService.getDocumentDirectory(), articleArchive).delete();
    new File(documentManagementService.getIngestedDocumentDirectory(), articleArchive).delete();
  }

  @Test(dataProvider = "storedUnpublishedArticles")
  public void testGenerateIngestedData(String article) throws IOException, NoSuchArticleIdException
  {
    //I don't use the article ID passed in as it should just be the constant "articleUri".
    //But the dataprovider needed to be called to be sure the dummyDataStore is populated.

    //Set up temp files.
    String ingestDir = documentManagementService.getDocumentDirectory();
    String ingestedDir = documentManagementService.getIngestedDocumentDirectory();

    String sourceFile = ingestDir + "/" + articleArchive;
    String destFile = ingestedDir + "/" + articleArchive;

    assertTrue((new File(sourceFile)).createNewFile(), "Couldn't create temp file.");
    assertTrue((new File(destFile)).createNewFile(), "Couldn't create temp file.");

    //Test that the dest file is overwritten and the source is moved
    Article a = articleService.getArticle(articleUri, DEFAULT_ADMIN_AUTHID);
    documentManagementService.generateIngestedData(new File(sourceFile), a.getDoi());

    //Test that the file exists
    assertTrue((new File(destFile)).exists(), "File doesn't exist: " + destFile);

    //Reset state
    assertTrue((new File(destFile)).delete(), "Can't delete file: " + destFile);

    //Now test the source is moved
    assertTrue((new File(sourceFile)).createNewFile(), "Couldn't create temp file.");
    documentManagementService.generateIngestedData(new File(sourceFile), a.getDoi());

    assertFalse((new File(sourceFile)).exists(), "Old file not moved: " + sourceFile);
    assertTrue((new File(destFile)).exists(), "New file not moved to correct place: " + sourceFile);

    //Reset state
    assertTrue((new File(destFile)).delete(), "Can't delete file: " + destFile);  }

  @Test
  public void testGenerateCrossrefInfoDoc() throws FileNotFoundException, IOException, SAXException,
    TransformerException
  {
    String ingestedDir = documentManagementService.getIngestedDocumentDirectory();
    InputStream xml = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(crossRefArticleXML);
    Document doc = XMLUnit.buildTestDocument(new InputSource(xml));

    documentManagementService.generateCrossrefInfoDoc(doc, URI.create(crossRefArticleDOI));

    //Assert that file was created.
    File f = new File(ingestedDir + "/" + crossRefArticleResultXML);

    assertTrue(f.exists(), "File does not exist:" + ingestedDir + "/" + crossRefArticleResultXML);

    //Compare the resulting file with a base line
    InputStream xml1 = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(crossRefArticleResultTestXML);

    XMLUnit.compareXML(new InputSource(xml1),new InputSource(new FileInputStream(f)));

    //cleanup
    assertTrue(f.delete(), "File was not removed: " + ingestedDir + "/" + crossRefArticleResultXML);
  }

  @Test(dataProvider = "storedPublishedArticles")
  void testDisable(String article, Long articleId) throws Exception
  {
    final String file1 = FSIDMapper.doiTofsid(article + ".fileone", "txt");
    final String file2 = FSIDMapper.doiTofsid(article + ".filetwo", "txt");

    //Create some files to remove from the filestore
    OutputStream fs = fileStoreService.getFileOutStream(file1, 50);
    fs.write("File left blank".getBytes());
    fs.close();

    fs = fileStoreService.getFileOutStream(file2, 50);
    fs.write("File left blank".getBytes());
    fs.close();

    //Check article initial state
    Article a = articleService.getArticle(article, DEFAULT_ADMIN_AUTHID);
    assertEquals(a.getState(),Article.STATE_ACTIVE, "Article state not Active");

    documentManagementService.disable(article, DEFAULT_ADMIN_AUTHID);

    try {
      articleService.getArticle(article, DEFAULT_ADMIN_AUTHID);
      fail("Article not disabled");
    } catch (NoSuchArticleIdException ex) {}

    //assert removed from file system
    try {
      fileStoreService.getFileByteArray(file1);
      fail("File not deleted: " + file1);
    } catch (FileStoreException ex) {}

    try {
      fileStoreService.getFileByteArray(file2);
      fail("File not deleted: " + file2);
    } catch (FileStoreException ex) {}
  }

  @Test(expectedExceptions = { SecurityException.class })
  void testDisableSecurity() throws Exception
  {
    documentManagementService.disable(articleUri, DEFAULT_USER_AUTHID);
  }

  @Test(dataProvider = "storedPublishedArticles")
  void testDelete(String article, Long articleId) throws Exception {
    documentManagementService.delete(article, DEFAULT_ADMIN_AUTHID);
    assertNull(dummyDataStore.get(Article.class, articleId),"didn't delete article");
    for (ArticleRelationship relationship : dummyDataStore.getAll(ArticleRelationship.class)) {
      if (articleId.equals(relationship.getOtherArticleID())) {
        fail("failed to delete unlink article relationship: " + relationship);
      }
    }
  }

  @Test(dataProvider = "storedPublishedArticles", expectedExceptions = { SecurityException.class })
  void testDeleteSecurity(String article, Long articleId) throws Exception
  {
    documentManagementService.delete(article, DEFAULT_USER_AUTHID);
  }

  @Test(dataProvider = "storedPublishedArticles2")
  void testUnPublish(String article) throws Exception
  {
    Article a = articleService.getArticle(article, DEFAULT_ADMIN_AUTHID);
    assertEquals(a.getState(), Article.STATE_ACTIVE, "Article not set as published: " + article);

    documentManagementService.unPublish(article, DEFAULT_ADMIN_AUTHID);

    a = articleService.getArticle(article, DEFAULT_ADMIN_AUTHID);
    assertEquals(a.getState(), Article.STATE_UNPUBLISHED, "Article not set as unpublished: " + article);
  }

  @Test(dataProvider = "storedPublishedArticles2", expectedExceptions = { SecurityException.class })
  void testUnPublishSecurity(String article) throws Exception
  {
    documentManagementService.unPublish(article, DEFAULT_USER_AUTHID);
  }

  @Test(dataProvider = "storedUnpublishedArticles")
  void testPublish(String article) throws NoSuchArticleIdException
  {
    Article a = articleService.getArticle(article, DEFAULT_ADMIN_AUTHID);
    assertEquals(a.getState(), Article.STATE_UNPUBLISHED, "Article not set as unpublished: " + article);

    documentManagementService.publish(new String[] { article }, DEFAULT_ADMIN_AUTHID);

    a = articleService.getArticle(article, DEFAULT_ADMIN_AUTHID);
    assertEquals(a.getState(), Article.STATE_ACTIVE, "Article not set as published: " + article);
  }

  @Test(dataProvider = "storedUnpublishedArticles", expectedExceptions = { UnexpectedRollbackException.class })
  void testPublishSecurity(String article)
  {
    documentManagementService.publish(new String[] { article }, DEFAULT_USER_AUTHID);
  }

  @Test
  void testGetUploadableFiles()
  {
    List<String> files = documentManagementService.getUploadableFiles();
    String ingestDir = documentManagementService.getDocumentDirectory();

    Object[] files2 = (new File(ingestDir)).list(new FilenameFilter() {
      @Override
      public boolean accept(File file, String s) {
        return s.endsWith("zip");
      }
    });

    assertEqualsNoOrder(files.toArray(), files2, "Uploadable files list differs.");
  }

  @Test
  void testRevertIngestedQueue() throws IOException
  {
    //Set up a file to be moved back from the ingest queue
    //Method shouldn't actually operate on file contents.  So I create temp files for this test
    String ingestDir = documentManagementService.getDocumentDirectory();
    String ingestedDir = documentManagementService.getIngestedDocumentDirectory();

    assertTrue((new File(ingestedDir + "/" + articleArchive)).createNewFile(), "Couldn't create temp file.");
    assertTrue((new File(ingestedDir + "/" + articleXmlFile)).createNewFile(), "Couldn't create temp file.");

    documentManagementService.revertIngestedQueue(articleUri);

    assertFalse((new File(ingestedDir, articleArchive)).exists(), "Archive file not reverted: " + articleArchive);
    assertFalse((new File(ingestedDir, articleXmlFile)).exists(), "Archive XML file not reverted: " + articleXmlFile);

    File ingestFile = new File(ingestDir, articleArchive);

    //Confirm file existance and clean up
    assertTrue(ingestFile.delete(), "File " + articleArchive + " has not been moved back to the ingest folder.");
  }
}
