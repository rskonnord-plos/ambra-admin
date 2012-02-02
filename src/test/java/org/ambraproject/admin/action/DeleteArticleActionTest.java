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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.Reply;

import java.io.File;
import java.net.URI;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 2/2/12
 */
public class DeleteArticleActionTest extends AdminWebTest {

  @Autowired
  protected DeleteArticleAction action;

  //intellij doesn't recognize the qualifier b/c it's in an included resource
  @Qualifier("filestoreDir")
  @Autowired
  protected String filestoreDir;

  //intellij doesn't recognize the qualifier b/c it's in an included resource
  @Qualifier("ingestedDir")
  @Autowired
  protected String ingestedDir;

  //intellij doesn't recognize the qualifier b/c it's in an included resource
  @Qualifier("ingestDir")
  @Autowired
  protected String ingestDir;

  @Test
  public void testExecute() throws Exception {
    action.setArticle(null);
    String result = action.execute();

    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ","));
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");
  }

  @Test
  public void testDelete() throws Exception {
    Article article = new Article();
    article.setDoi("info:doi/10.1371/journal.pone.00000");
    dummyDataStore.store(article);

    //make some files in the filestore to see if they get deleted
    File homeDir = new File(filestoreDir + File.separatorChar + "10.1371", "pone.00000");
    homeDir.mkdirs();
    homeDir.deleteOnExit();
    File xml = new File(homeDir, "pone.00000.xml");
    if (!xml.exists()) {
      xml.createNewFile();
      xml.deleteOnExit();
    }

    //make a file in the ingested folder to see that it gets moved back to ingest
    File zip = new File(ingestedDir, "pone.00000.zip");
    if (!zip.exists()) {
      zip.createNewFile();
      zip.deleteOnExit();
    }
    File crossrefFile = new File(ingestedDir, "info_doi_10_1371_journal_pone_00000.xml");
    if (!crossrefFile.exists()) {
      crossrefFile.createNewFile();
      crossrefFile.deleteOnExit();
    }
    File ingestZip = new File(ingestDir, zip.getName());
    ingestZip.deleteOnExit();

    //add some annotations to the article to see that they get deleted
    Comment comment = new Comment();
    comment.setAnnotates(URI.create(article.getDoi()));
    dummyDataStore.store(comment);
    Reply reply = new Reply();
    reply.setInReplyTo(comment.getId().toString());
    dummyDataStore.store(reply);


    action.setArticle(article.getDoi());
    String result = action.deleteArticle();

    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ","));
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");

    //check that the article got deleted from the database
//    assertNull(dummyDataStore.get(Reply.class, reply.getId()), "Reply to a commment on the article didn't get deleted from the database");
//    assertNull(dummyDataStore.get(Comment.class, comment.getId()), "Comment on the article didn't get deleted from the database");
    assertNull(dummyDataStore.get(Article.class, article.getID()), "Article didn't get deleted from the database");

    //check that the file got deleted from the filestore
    assertEquals(homeDir.list(), new String[0], "files didn't get deleted from the filestore");
    assertFalse(zip.exists(), "zip in the ingested directory didn't get deleted");
    assertFalse(crossrefFile.exists(), "crossref file didn't get deleted");
    assertTrue(ingestZip.exists(),
        "ingested file didn't get added to the ingest directory");
  }

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
