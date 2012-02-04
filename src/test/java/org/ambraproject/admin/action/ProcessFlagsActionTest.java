/* $HeadURL$
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

package org.ambraproject.admin.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.Article;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.ReplyThread;
import org.topazproject.ambra.models.Retraction;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Dragisa Krsmanovic
 */

public class ProcessFlagsActionTest extends AdminWebTest {
  @Autowired
  protected ManageFlagsAction action;

  @DataProvider(name = "commentWithFlags")
  public Object[][] getCommentWithFlags(Method testMethod) throws URISyntaxException {
    Article article = new Article();
    article.setDoi("id:articleToAnnotate");
    dummyDataStore.store(article);

    Comment comment = new Comment();
    comment.setId(URI.create("id:comment-for-" + testMethod.getName()));
    comment.setAnnotates(URI.create(article.getDoi()));
    comment.setTitle("Original Comment");
    comment.setType(Comment.RDF_TYPE);
    comment.setBody(new AnnotationBlob());
    comment.getBody().setBody("Some comment text".getBytes());
    dummyDataStore.store(comment);

    Comment flag = new Comment();
    flag.setId(URI.create("id:flag-for-" + testMethod.getName()));
    flag.setTitle("The flag");
    flag.setAnnotates(comment.getId());
    flag.setBody(new AnnotationBlob());
    flag.getBody().setBody(
        ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<flag reasonCode=\"Create Correction\">" +
            "<comment>Note created and flagged as a correction</comment>" +
            "</flag>").getBytes());
    dummyDataStore.store(flag);

    Reply reply = new ReplyThread();
    reply.setId(URI.create("id:reply-for-" + testMethod.getName()));
    reply.setInReplyTo(comment.getId().toString());
    reply.setRoot(comment.getId().toString());
    reply.setType(Reply.RDF_TYPE);
    dummyDataStore.store(reply);

    return new Object[][]{
        {comment.getId(), Annotea.WEB_TYPE_COMMENT, new String[]{flag.getId() + "_" + comment.getId()}}
    };
  }

  @Test(dataProvider = "commentWithFlags")
  public void testConvertToFormalCorrection(URI annotationId, String type, String[] flags) throws Exception {

    action.setCommentsToUnflag(null);
    action.setCommentsToDelete(null);
    action.setConvertToFormalCorrection(flags);
    action.setConvertToMinorCorrection(null);
    action.setConvertToRetraction(null);

    assertFalse(dummyDataStore.get(Annotation.class, annotationId) instanceof FormalCorrection,
        "Annotation to convert was already a formal correction");
    String result = action.processFlags();
    assertEquals(result, Action.SUCCESS, "action to convert formal correction with " + flags.length + " flags didn't succeed");
    assertEquals(action.getActionErrors().size(), 0, "Action returned errors: " + StringUtils.join(action.getActionErrors(), ","));

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotationId);
    assertTrue(storedAnnotation instanceof FormalCorrection, "Annotation wasn't converted to formal correction");
    assertEquals(storedAnnotation.getType(), FormalCorrection.RDF_TYPE, "Annotation didn't get correct type");

    for (String paramStr : flags) {
      String flagId = paramStr.split("_")[0];
      assertNull(dummyDataStore.get(Annotation.class, URI.create(flagId)), "flag didn't get deleted");
    }
  }

  @Test(dataProvider = "commentWithFlags")
  public void testConvertToMinorCorrection(URI annotationId, String type, String[] flags) throws Exception {
    action.setCommentsToUnflag(null);
    action.setCommentsToDelete(null);
    action.setConvertToFormalCorrection(null);
    action.setConvertToMinorCorrection(flags);
    action.setConvertToRetraction(null);

    assertFalse(dummyDataStore.get(Annotation.class, annotationId) instanceof MinorCorrection,
        "Annotation to convert was already a minor correction");

    String result = action.processFlags();
    assertEquals(result, Action.SUCCESS, "action to convert formal correction with " + flags.length + " flags didn't succeed");
    assertEquals(action.getActionErrors().size(), 0, "Action returned errors: " + StringUtils.join(action.getActionErrors(), ","));

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotationId);
    assertTrue(storedAnnotation instanceof MinorCorrection, "Annotation wasn't converted to minor correction");
    assertEquals(storedAnnotation.getType(), MinorCorrection.RDF_TYPE, "Annotation didn't get correct type");

    for (String paramStr : flags) {
      String flagId = paramStr.split("_")[0];
      assertNull(dummyDataStore.get(Annotation.class, URI.create(flagId)), "flag didn't get deleted");
    }
  }

  @Test(dataProvider = "commentWithFlags")
  public void testConvertToRetraction(URI annotationId, String type, String[] flags) throws Exception {
    action.setCommentsToUnflag(null);
    action.setCommentsToDelete(null);
    action.setConvertToFormalCorrection(null);
    action.setConvertToMinorCorrection(null);
    action.setConvertToRetraction(flags);

    assertFalse(dummyDataStore.get(Annotation.class, annotationId) instanceof Retraction,
        "Annotation to convert was already a retraction");

    String result = action.processFlags();
    assertEquals(result, Action.SUCCESS, "action to convert formal correction with " + flags.length + " flags didn't succeed");
    assertEquals(action.getActionErrors().size(), 0, "Action returned errors: " + StringUtils.join(action.getActionErrors(), ","));

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotationId);
    assertTrue(storedAnnotation instanceof Retraction, "Annotation wasn't converted to retraction");
    assertEquals(storedAnnotation.getType(), Retraction.RDF_TYPE, "Annotation didn't get correct type");

    for (String paramStr : flags) {
      String flagId = paramStr.split("_")[0];
      assertNull(dummyDataStore.get(Annotation.class, URI.create(flagId)), "flag didn't get deleted");
    }
  }

  @Test(dataProvider = "commentWithFlags")
  public void testUnflag(URI annotationId, String type, String[] flags) throws Exception {
    //for Unflag and delete comments, the params in the flag string are reversed
    String[] commentsToUnflag = new String[flags.length];
    for (int i = 0; i < flags.length; i++) {
      String[] tokens = flags[i].split("_");
      commentsToUnflag[i] = tokens[1] + "_" + tokens[0];
    }

    action.setCommentsToUnflag(commentsToUnflag);
    action.setCommentsToDelete(null);
    action.setConvertToFormalCorrection(null);
    action.setConvertToMinorCorrection(null);
    action.setConvertToRetraction(null);

    String result = action.processFlags();

    assertEquals(result, Action.SUCCESS, "action to unflag with " + flags.length + " flags didn't succeed");
    assertEquals(action.getActionErrors().size(), 0, "Action returned errors: " + StringUtils.join(action.getActionErrors(), ","));

    assertNotNull(dummyDataStore.get(Annotation.class, annotationId), "Annotation got deleted");

    for (String paramStr : flags) {
      String flagId = paramStr.split("_")[0];
      assertNull(dummyDataStore.get(Annotation.class, URI.create(flagId)), "Flag didn't get deleted");
    }
  }

  @Test(dataProvider = "commentWithFlags")
  public void testDeleteComment(URI annotationId, String type, String[] flags) throws Exception {
    //for delete comments, the params in the flag string are weird
    String[] commentsToDelete = new String[flags.length];
    for (int i = 0; i < flags.length; i++) {
      String[] tokens = flags[i].split("_");
      commentsToDelete[i] = "_" + tokens[1] + "_" + type;
    }

    action.setCommentsToUnflag(null);
    action.setCommentsToDelete(commentsToDelete);
    action.setConvertToFormalCorrection(null);
    action.setConvertToMinorCorrection(null);
    action.setConvertToRetraction(null);

    String result = action.processFlags();

    assertEquals(result, Action.SUCCESS, "action to delete comment didn't succeed");
    assertEquals(action.getActionErrors().size(), 0, "Action returned errors: " + StringUtils.join(action.getActionErrors(), ","));

    assertNull(dummyDataStore.get(Annotation.class, annotationId), "Annotation didn't get deleted");
  }

  @DataProvider(name = "flaggedReply")
  public Object[][] getFlaggedReply() {
    Comment comment = new Comment();
    dummyDataStore.store(comment);

    ReplyThread root = new ReplyThread();
    root.setRoot(comment.getId().toString());
    root.setInReplyTo(comment.getId().toString());
    root.setReplies(new ArrayList<ReplyThread>(1));
    dummyDataStore.store(root);

    ReplyThread reply = new ReplyThread();
    dummyDataStore.store(reply);
    root.addReply(reply);
    dummyDataStore.update(root);


    Comment flag = new Comment();
    flag.setAnnotates(reply.getId());
    dummyDataStore.store(flag);
    
    return new Object[][]{
        {reply.getId(), new String[] {reply.getId() + "_" + flag.getId() + "_" + Annotea.WEB_TYPE_REPLY}}
    };
  }
  
  
  @Test(dataProvider = "flaggedReply")
  public void testUnflagReply(URI replyId, String[] paramStrings) {
    action.setCommentsToUnflag(paramStrings);
    action.setCommentsToDelete(null);
    action.setConvertToFormalCorrection(null);
    action.setConvertToMinorCorrection(null);
    action.setConvertToRetraction(null);

    String result = action.processFlags();

    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, "Action returned errors: " + StringUtils.join(action.getActionErrors(), ","));

    assertNotNull(dummyDataStore.get(Reply.class, replyId), "Reply got deleted");

    for (String paramStr : paramStrings) {
      String flagId = paramStr.split("_")[0];
      assertNull(dummyDataStore.get(Annotation.class, URI.create(flagId)), "Flag didn't get deleted");
    }
  }


  @Override
  protected BaseActionSupport getAction() {
    return action;
  }
}
