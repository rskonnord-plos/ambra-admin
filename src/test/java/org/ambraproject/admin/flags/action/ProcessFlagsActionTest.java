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

package org.ambraproject.admin.flags.action;

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationCitation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.Flag;
import org.ambraproject.models.FlagReasonCode;
import org.ambraproject.models.Rating;
import org.ambraproject.models.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Alex Kudlick 3/26/12
 */
public class ProcessFlagsActionTest extends AdminWebTest {

  private static final Long[] EMPTY_ARRAY = new Long[0];
  @Autowired
  protected ProcessFlagsAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @BeforeMethod
  public void resetAction() {
    action.setCommentsToDelete(EMPTY_ARRAY);
    action.setCommentsToUnflag(EMPTY_ARRAY);
    action.setConvertToFormalCorrection(EMPTY_ARRAY);
    action.setConvertToMinorCorrection(EMPTY_ARRAY);
    action.setConvertToNote(EMPTY_ARRAY);
    action.setConvertToRetraction(EMPTY_ARRAY);
  }

  @DataProvider(name = "flags")
  public Object[][] getFlags() {
    UserProfile creator = new UserProfile(
        "id:creatorForProcessFlagsActionTest",
        "email@ProcessFlagsActionTest.org",
        "displaynameForProcessFlagsActionTest");
    dummyDataStore.store(creator);

    Article article = new Article("id:doi-for-ProcessFlagsActionTest");
    article.setAuthors(Arrays.asList(
        new ArticleAuthor("Stefan", "Salvatore", "Vmp."),
        new ArticleAuthor("Damon", "Salvatore", "Vmp.")
    ));
    dummyDataStore.store(article);

    List<Long> annotationIds = new ArrayList<Long>(4);
    List<Long> flagIds = new ArrayList<Long>(4);

    Annotation comment = new Annotation(creator, AnnotationType.COMMENT, article.getID());
    dummyDataStore.store(comment);
    annotationIds.add(comment.getID());

    Flag flagComment = new Flag(creator, FlagReasonCode.CORRECTION, comment);
    dummyDataStore.store(flagComment);
    flagIds.add(flagComment.getID());

    Annotation correction = new Annotation(creator, AnnotationType.MINOR_CORRECTION, article.getID());
    correction.setAnnotationCitation(new AnnotationCitation());
    dummyDataStore.store(correction);
    annotationIds.add(correction.getID());

    Flag flagCorrection = new Flag(creator, FlagReasonCode.INAPPROPRIATE, correction);
    dummyDataStore.store(flagCorrection);
    flagIds.add(flagCorrection.getID());

    Annotation reply = new Annotation(creator, AnnotationType.REPLY, article.getID());
    dummyDataStore.store(reply);
    annotationIds.add(reply.getID());

    Flag flagReply = new Flag(creator, FlagReasonCode.SPAM, reply);
    dummyDataStore.store(flagReply);
    flagIds.add(flagReply.getID());

    Rating rating = new Rating(creator, article.getID());
    dummyDataStore.store(rating);
    annotationIds.add(rating.getID());

    Flag flagRating = new Flag(creator, FlagReasonCode.OTHER, rating);
    dummyDataStore.store(flagRating);
    flagIds.add(flagRating.getID());

    return new Object[][]{
        {flagIds, annotationIds}
    };
  }

  @Test(dataProvider = "flags")
  public void testDeleteFlags(List<Long> flagIds, List<Long> annotationIds) throws Exception {
    action.setCommentsToUnflag(flagIds.toArray(new Long[flagIds.size()]));

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    for (Long flagId : flagIds) {
      assertNull(dummyDataStore.get(Flag.class, flagId), "Didn't delete flag: " + flagId);
    }
    for (Long annotationId : annotationIds) {
      assertNotNull(dummyDataStore.get(Annotation.class, annotationId), "Deleted annotation: " + annotationId);
    }
  }

  @Test(dataProvider = "flags")
  public void testConvertToFormalCorrection(List<Long> flagIds, List<Long> annotationIds) throws Exception {
    action.setConvertToFormalCorrection(flagIds.toArray(new Long[flagIds.size()]));

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    for (Long flagId : flagIds) {
      assertNull(dummyDataStore.get(Flag.class, flagId), "Didn't delete flag: " + flagId);
    }
    for (Long annotationId : annotationIds) {
      Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotationId);
      assertNotNull(storedAnnotation, "deleted annotation");
      assertEquals(storedAnnotation.getType(), AnnotationType.FORMAL_CORRECTION, "Didn't convert to correct type");
      assertNotNull(storedAnnotation.getAnnotationCitation(), "Didn't create a citation");
    }
  }

  @Test(dataProvider = "flags")
  public void testConvertToMinorCorrection(List<Long> flagIds, List<Long> annotationIds) throws Exception {
    action.setConvertToMinorCorrection(flagIds.toArray(new Long[flagIds.size()]));

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    for (Long flagId : flagIds) {
      assertNull(dummyDataStore.get(Flag.class, flagId), "Didn't delete flag: " + flagId);
    }
    for (Long annotationId : annotationIds) {
      Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotationId);
      assertNotNull(storedAnnotation, "deleted annotation");
      assertEquals(storedAnnotation.getType(), AnnotationType.MINOR_CORRECTION, "Didn't convert to correct type");
      assertNull(storedAnnotation.getAnnotationCitation(), "Minor corrections shouldn't get citation created");
    }
  }

  @Test(dataProvider = "flags")
  public void testConvertToRetraction(List<Long> flagIds, List<Long> annotationIds) throws Exception {
    action.setConvertToRetraction(flagIds.toArray(new Long[flagIds.size()]));

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    for (Long flagId : flagIds) {
      assertNull(dummyDataStore.get(Flag.class, flagId), "Didn't delete flag: " + flagId);
    }
    for (Long annotationId : annotationIds) {
      Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotationId);
      assertNotNull(storedAnnotation, "deleted annotation");
      assertEquals(storedAnnotation.getType(), AnnotationType.RETRACTION, "Didn't convert to correct type");
      assertNotNull(storedAnnotation.getAnnotationCitation(), "Didn't create a citation");
    }
  }

  @Test(dataProvider = "flags")
  public void testConvertToNote(List<Long> flagIds, List<Long> annotationIds) throws Exception {
    action.setConvertToNote(flagIds.toArray(new Long[flagIds.size()]));

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    for (Long flagId : flagIds) {
      assertNull(dummyDataStore.get(Flag.class, flagId), "Didn't delete flag: " + flagId);
    }
    for (Long annotationId : annotationIds) {
      Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotationId);
      assertNotNull(storedAnnotation, "deleted annotation");
      assertEquals(storedAnnotation.getType(), AnnotationType.NOTE, "Didn't convert to correct type");
      assertNull(storedAnnotation.getAnnotationCitation(), "Didn't remove citation");
    }
  }
}
