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

package org.ambraproject.admin.flags.service;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.admin.views.FlagView;
import org.ambraproject.cache.Cache;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationCitation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.CorrectedAuthor;
import org.ambraproject.models.Flag;
import org.ambraproject.models.FlagReasonCode;
import org.ambraproject.models.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * @author Alex Kudlick 3/23/12
 */
public class FlagServiceTest extends AdminBaseTest {

  @Autowired
  protected FlagService flagService;
  
  @Autowired
  protected Cache articleHtmlCache; //just used to check that articles get kicked out of the cache when they should

  @Test
  public void testGetFlaggedComments() {
    //make sure there aren't any other flags in the db stored by other tests
    dummyDataStore.deleteAll(Flag.class);

    //set up data
    UserProfile commentCreator = new UserProfile(
        "id:creatorForFlagManagementServiceTest",
        "email@FlagManagementServiceTest.org",
        "displaynameForFlagManagementServiceTest");
    dummyDataStore.store(commentCreator);

    UserProfile flagCreator = new UserProfile(
        "flagCreatorForFlagManagementServiceTest",
        "flagCreator@FlagManagementServiceTest.org",
        "flagCreatorForFlagManagementServiceTest");
    dummyDataStore.store(flagCreator);

    Annotation comment = new Annotation(commentCreator, AnnotationType.COMMENT, 123l);
    comment.setTitle("test title for flagManagementServiceTest");
    dummyDataStore.store(comment);

    Annotation reply = new Annotation(commentCreator, AnnotationType.REPLY, 123l);
    reply.setTitle("test title for reply on flagManagementServiceTest");
    dummyDataStore.store(reply);

    Annotation correction = new Annotation(commentCreator, AnnotationType.MINOR_CORRECTION, 123l);
    correction.setTitle("test title for minorCorrection on flagManagementServiceTest");
    dummyDataStore.store(correction);

    Calendar lastYear = Calendar.getInstance();
    lastYear.add(Calendar.YEAR, -1);

    Calendar lastMonth = Calendar.getInstance();
    lastMonth.add(Calendar.MONTH, -1);

    Flag firstFlag = new Flag(flagCreator, FlagReasonCode.SPAM, comment);
    firstFlag.setComment("Spamalot");
    firstFlag.setCreated(lastYear.getTime());
    dummyDataStore.store(firstFlag);

    Flag secondFlag = new Flag(flagCreator, FlagReasonCode.INAPPROPRIATE, reply);
    secondFlag.setComment("inappropriate");
    secondFlag.setCreated(lastMonth.getTime());
    dummyDataStore.store(secondFlag);

    Flag thirdFlag = new Flag(flagCreator, FlagReasonCode.SPAM, correction);
    thirdFlag.setComment("More spam");
    dummyDataStore.store(thirdFlag);

    //call the service method
    Collection<FlagView> list = flagService.getFlaggedComments();

    assertNotNull(list, "returned null list of flagged comments");
    assertEquals(list.toArray(), new Object[]{
        new FlagView(firstFlag),
        new FlagView(secondFlag),
        new FlagView(thirdFlag)}, "Incorrect flags");

  }

  @Test
  public void testDeleteFlags() {
    UserProfile creator = new UserProfile(
        "id:creatorForDeleteFlagsServiceTest",
        "email@DeleteFlagsServiceTest.org",
        "displaynameForDeleteFlagsServiceTest");
    dummyDataStore.store(creator);

    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, 12l);
    dummyDataStore.store(annotation);

    Flag flag1 = new Flag(creator, FlagReasonCode.SPAM, annotation);
    Long id1 = Long.valueOf(dummyDataStore.store(flag1));

    Flag flag2 = new Flag(creator, FlagReasonCode.OFFENSIVE, annotation);
    Long id2 = Long.valueOf(dummyDataStore.store(flag2));

    flagService.deleteFlags(id1, id2);

    assertNull(dummyDataStore.get(Flag.class, id1), "didn't delete first flag");
    assertNull(dummyDataStore.get(Flag.class, id2), "didn't delete second flag");

  }

  @Test
  public void testConvertToFormalCorrection() throws ParseException {
    UserProfile creator = new UserProfile(
        "id:creatorForConvertToFormalCorrectionServiceTest",
        "email@ConvertToFormalCorrectionServiceTest.org",
        "displaynameForConvertToFormalCorrectionServiceTest");
    dummyDataStore.store(creator);

    Article article = new Article("id:doi-for-convert-to-formal-correction-by-service");
    article.setTitle("Title for Convert to Formal Correction by Service");
    article.seteLocationId("eLocationId for Convert to Formal Correction by Service");
    article.setJournal("journal for Convert to Formal Correction by Service");
    article.setDescription("description for Convert to Formal Correction by Service");
    article.setDate(new SimpleDateFormat("yyyy-mm-dd").parse("2100-03-03"));
    article.setAuthors(new ArrayList<ArticleAuthor>(2));
    article.getAuthors().add(new ArticleAuthor("John", "Smith", "MD"));
    article.getAuthors().add(new ArticleAuthor("Harry", "Potter", "PhD"));
    dummyDataStore.store(article);
    
    //put the article in cache
    articleHtmlCache.put(article.getDoi(), new Cache.Item(article));

    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, article.getID());
    dummyDataStore.store(annotation);

    Flag flag1 = new Flag(creator, FlagReasonCode.CORRECTION, annotation);
    Long id1 = Long.valueOf(dummyDataStore.store(flag1));
    Flag flag2 = new Flag(creator, FlagReasonCode.CORRECTION, annotation);
    Long id2 = Long.valueOf(dummyDataStore.store(flag2));

    flagService.convertToType(AnnotationType.FORMAL_CORRECTION, id1, id2);

    assertNull(dummyDataStore.get(Flag.class, id1), "didn't delete first flag");
    assertNull(dummyDataStore.get(Flag.class, id2), "didn't delete second flag");

    checkStoredCorrection(annotation.getID(), article, AnnotationType.FORMAL_CORRECTION, "2100");
    
    assertNull(articleHtmlCache.get(article.getDoi()),"article didn't get kicked out of cache");
  }

  @Test
  public void testConvertToMinorCorrection() throws ParseException {
    UserProfile creator = new UserProfile(
        "id:creatorForConvertToMinorCorrectionServiceTest",
        "email@ConvertToMinorCorrectionServiceTest.org",
        "displaynameForConvertToMinorCorrectionServiceTest");
    dummyDataStore.store(creator);

    Article article = new Article("id:doi-for-convert-to-minor-correction-by-service");
    dummyDataStore.store(article);

    //put the article in cache
    articleHtmlCache.put(article.getDoi(), new Cache.Item(article));

    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, article.getID());
    dummyDataStore.store(annotation);

    Flag flag1 = new Flag(creator, FlagReasonCode.CORRECTION, annotation);
    Long id1 = Long.valueOf(dummyDataStore.store(flag1));
    Flag flag2 = new Flag(creator, FlagReasonCode.CORRECTION, annotation);
    Long id2 = Long.valueOf(dummyDataStore.store(flag2));

    flagService.convertToType(AnnotationType.MINOR_CORRECTION, id1, id2);

    assertNull(dummyDataStore.get(Flag.class, id1), "didn't delete first flag");
    assertNull(dummyDataStore.get(Flag.class, id2), "didn't delete second flag");

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotation.getID());
    assertNotNull(storedAnnotation, "Annotation got deleted");
    assertEquals(storedAnnotation.getType(), AnnotationType.MINOR_CORRECTION, "Annotation didn't get converted to minor correction");
    assertNull(storedAnnotation.getAnnotationCitation(), "Minor correction shouldn't get a citation created");

    assertNull(articleHtmlCache.get(article.getDoi()),"article didn't get kicked out of cache");
  }

  @Test
  public void testConvertToRetraction() throws ParseException {
    UserProfile creator = new UserProfile(
        "id:creatorForConvertToRetractionServiceTest",
        "email@ConvertToRetractionServiceTest.org",
        "displaynameForConvertToRetractionServiceTest");
    dummyDataStore.store(creator);

    Article article = new Article("id:doi-for-convert-to-retraction-by-service");
    article.setTitle("Title for Convert to Retraction by Service");
    article.seteLocationId("eLocationId for Convert to Retraction by Service");
    article.setJournal("journal for Convert to Retraction by Service");
    article.setDescription("description for Convert to Retraction by Service");
    article.setDate(new SimpleDateFormat("yyyy-mm-dd").parse("2100-03-03"));
    article.setAuthors(new ArrayList<ArticleAuthor>(2));
    article.getAuthors().add(new ArticleAuthor("John", "Smith", "MD"));
    article.getAuthors().add(new ArticleAuthor("Harry", "Potter", "PhD"));
    dummyDataStore.store(article);

    //put the article in cache
    articleHtmlCache.put(article.getDoi(), new Cache.Item(article));

    Annotation annotation = new Annotation(creator, AnnotationType.COMMENT, article.getID());
    dummyDataStore.store(annotation);

    Flag flag1 = new Flag(creator, FlagReasonCode.CORRECTION, annotation);
    Long id1 = Long.valueOf(dummyDataStore.store(flag1));
    Flag flag2 = new Flag(creator, FlagReasonCode.CORRECTION, annotation);
    Long id2 = Long.valueOf(dummyDataStore.store(flag2));

    flagService.convertToType(AnnotationType.RETRACTION, id1, id2);

    assertNull(dummyDataStore.get(Flag.class, id1), "didn't delete first flag");
    assertNull(dummyDataStore.get(Flag.class, id2), "didn't delete second flag");

    checkStoredCorrection(annotation.getID(), article, AnnotationType.RETRACTION, "2100");

    assertNull(articleHtmlCache.get(article.getDoi()), "article didn't get kicked out of cache");
  }

  @Test
  public void testConvertToNote() throws ParseException {
    UserProfile creator = new UserProfile(
        "id:creatorForConvertToNoteServiceTest",
        "email@ConvertToNoteServiceTest.org",
        "displaynameForConvertToNoteServiceTest");
    dummyDataStore.store(creator);

    Article article = new Article("id:doi-for-convert-to-note-by-service");
    article.setTitle("Title for Convert to Note by Service");
    article.seteLocationId("eLocationId for Convert to Note by Service");
    article.setJournal("journal for Convert to Note by Service");
    article.setDate(new SimpleDateFormat("yyyy-mm-dd").parse("2100-03-03"));
    article.setAuthors(new ArrayList<ArticleAuthor>(2));
    article.getAuthors().add(new ArticleAuthor("John", "Smith", "MD"));
    article.getAuthors().add(new ArticleAuthor("Harry", "Potter", "PhD"));
    dummyDataStore.store(article);

    //put the article in cache
    articleHtmlCache.put(article.getDoi(), new Cache.Item(article));

    Annotation annotation = new Annotation(creator, AnnotationType.MINOR_CORRECTION, article.getID());
    annotation.setAnnotationCitation(new AnnotationCitation(article));
    dummyDataStore.store(annotation);

    Flag flag1 = new Flag(creator, FlagReasonCode.SPAM, annotation);
    Long id1 = Long.valueOf(dummyDataStore.store(flag1));

    flagService.convertToType(AnnotationType.NOTE, id1);

    assertNull(dummyDataStore.get(Flag.class, id1), "didn't delete first flag");

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotation.getID());
    assertNotNull(storedAnnotation, "deleted annotation");
    assertEquals(storedAnnotation.getType(), AnnotationType.NOTE, "Didn't set correct type");
    assertNull(storedAnnotation.getAnnotationCitation(), "Didn't disassociate citation from annotation");
    assertNull(dummyDataStore.get(AnnotationCitation.class, annotation.getAnnotationCitation().getID()),
        "Didn't delete citation");

    assertNull(articleHtmlCache.get(article.getDoi()),"article didn't get kicked out of cache");
  }

  private void checkStoredCorrection(Long annotationId, Article article, AnnotationType expectedType, String expectedYear) {
    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, annotationId);
    assertNotNull(storedAnnotation, "deleted annotation");
    assertEquals(storedAnnotation.getType(), expectedType, "Didn't convert to correct type");
    assertNotNull(storedAnnotation.getAnnotationCitation(), "Didn't create a citation for the annotation");
    assertEquals(storedAnnotation.getAnnotationCitation().getTitle(), article.getTitle(), "Citation had incorrect title");
    assertEquals(storedAnnotation.getAnnotationCitation().getSummary(), article.getDescription(), "Citation had incorrect summary");
    assertEquals(storedAnnotation.getAnnotationCitation().getELocationId(), article.geteLocationId(),
        "Citation had incorrect eLocationId");
    assertEquals(storedAnnotation.getAnnotationCitation().getJournal(), article.getJournal(),
        "Citation had incorrect journal");
    assertEquals(storedAnnotation.getAnnotationCitation().getYear(), expectedYear,
        "Citation had incorrect year");
    assertNotNull(storedAnnotation.getAnnotationCitation().getAuthors(), "Citation didn't have author list");
    assertEquals(storedAnnotation.getAnnotationCitation().getAuthors().size(), article.getAuthors().size(),
        "Citation had incorrect number of authors");
    for (int i = 0; i < storedAnnotation.getAnnotationCitation().getAuthors().size(); i++) {
      CorrectedAuthor actual = storedAnnotation.getAnnotationCitation().getAuthors().get(i);
      ArticleAuthor expected = article.getAuthors().get(i);
      assertEquals(actual.getGivenNames(), expected.getGivenNames(), "Author " + (i + 1) + " had incorrect given names");
      assertEquals(actual.getSurName(), expected.getSurnames(), "Author " + (i + 1) + " had incorrect surnames");
      assertEquals(actual.getSuffix(), expected.getSuffix(), "Author " + (i + 1) + " had incorrect suffix");
    }
  }

  @Test
  public void testDeleteComment(){

    dummyDataStore.deleteAll(Flag.class);
    dummyDataStore.deleteAll(Annotation.class);

    UserProfile creator = new UserProfile(
        "id:creatorForDeleteFlagsServiceTest",
        "email@DeleteFlagsServiceTest.org",
        "displaynameForDeleteFlagsServiceTest");
    dummyDataStore.store(creator);

    Article article = new Article("id:doi-for-delete-comment-by-service");
    dummyDataStore.store(article);

    Article noteArticle = new Article("id:article-with-note-to-delete");
    dummyDataStore.store(noteArticle);

    //put the note article in cache to see that it gets kicked out
    articleHtmlCache.put(noteArticle.getDoi(), new Cache.Item(article));

    Annotation comment1 = new Annotation(creator, AnnotationType.COMMENT, article.getID());
    dummyDataStore.store(comment1);
    
    Annotation reply = new Annotation(creator, AnnotationType.REPLY, article.getID());
    reply.setParentID(comment1.getID());
    dummyDataStore.store(reply);

    Annotation note = new Annotation(creator, AnnotationType.NOTE, noteArticle.getID());
    note.setXpath("test xpath");
    dummyDataStore.store(note);

    Flag flag1 = new Flag(creator, FlagReasonCode.CORRECTION, comment1);
    flag1.setFlaggedAnnotation(comment1);
    Long id1 = Long.valueOf(dummyDataStore.store(flag1));

    Flag flag2 = new Flag(creator, FlagReasonCode.CORRECTION, note);
    flag2.setFlaggedAnnotation(note);
    Long id2 = Long.valueOf(dummyDataStore.store(flag2));

    Flag flag3 = new Flag(creator, FlagReasonCode.SPAM, note);
    flag3.setFlaggedAnnotation(note);
    Long id3 = Long.valueOf(dummyDataStore.store(flag3));

    flagService.deleteFlagAndComment(id1, id2);

    assertNull(dummyDataStore.get(Flag.class, id1), "didn't delete first flag");
    assertNull(dummyDataStore.get(Flag.class, id2), "didn't delete second flag");
    assertNull(dummyDataStore.get(Flag.class, id3), "didn't delete third flag");

    assertNull(dummyDataStore.get(Annotation.class, comment1.getID()), "didn't delete first annotation");
    assertNull(dummyDataStore.get(Annotation.class, reply.getID()), "didn't delete second annotation");
    assertNull(dummyDataStore.get(Annotation.class, note.getID()), "didn't delete third annotation");

    assertNull(articleHtmlCache.get(noteArticle.getDoi()),"article with note didn't get kicked out of cache");
  }

}
