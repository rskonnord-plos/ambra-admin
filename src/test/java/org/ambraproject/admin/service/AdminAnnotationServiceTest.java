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

package org.ambraproject.admin.service;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationCitation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.CorrectedAuthor;
import org.ambraproject.models.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 3/28/12
 */
public class AdminAnnotationServiceTest extends AdminBaseTest {

  @Autowired
  protected AdminAnnotationService annotationService;

  @DataProvider(name = "annotationsToEdit")
  public Object[][] getAnnotationsToEdit() {
    UserProfile userProfile = new UserProfile(
        "authIdForEditAnnotation",
        "email@editAnnotation.org",
        "displayNameForEditAnnotation"
    );
    dummyDataStore.store(userProfile);
    Article article = new Article("id:doi-for-editAnnotation");
    article.setTitle("Article title");
    article.seteIssn("1241-213875");
    article.setDate(Calendar.getInstance().getTime());
    article.setJournal("Test journal");
    article.setCollaborativeAuthors(Arrays.asList("The Skoll Foundation", "The Fu Foundation"));
    article.setVolume("volume");
    article.setUrl("http://dx.doi.org/foo");
    article.setAuthors(Arrays.asList(
        new ArticleAuthor("Foo","McFoo","F.o.o."), 
        new ArticleAuthor("James","McCoy","Mr."))
    );
    dummyDataStore.store(article);

    Annotation originalCorrection = new Annotation(userProfile, AnnotationType.MINOR_CORRECTION, article.getID());
    originalCorrection.setTitle("Old Correction Title");
    originalCorrection.setAnnotationUri("old correction annotation uri");
    originalCorrection.setBody("Old correction annotation body");
    originalCorrection.setCompetingInterestBody("old correction competing interest");
    originalCorrection.setAnnotationCitation(new AnnotationCitation(article));
    originalCorrection.getAnnotationCitation().setNote("Old note");
    originalCorrection.getAnnotationCitation().setSummary("Old summary");
    dummyDataStore.store(originalCorrection);

    //Just change the basic annotation properties
    Annotation changeBasicProperties = new Annotation(new UserProfile(), AnnotationType.FORMAL_CORRECTION, article.getID());
    changeBasicProperties.setTitle("Change Basic Properties Title");
    changeBasicProperties.setAnnotationUri("Change Basic Properties annotation uri");
    changeBasicProperties.setBody("Change Basic Properties body");
    changeBasicProperties.setCompetingInterestBody("Change Basic Properties competing interest");
    //keep the same annotation citation
    changeBasicProperties.setAnnotationCitation(new AnnotationCitation(article));


    //change the basic citation info
    Annotation changeCitationInfo = new Annotation(new UserProfile(), AnnotationType.FORMAL_CORRECTION, article.getID());
    changeCitationInfo.setAnnotationCitation(new AnnotationCitation(article));
    changeCitationInfo.getAnnotationCitation().setTitle("New Citation title");
    changeCitationInfo.getAnnotationCitation().setJournal("New Citation journal");
    changeCitationInfo.getAnnotationCitation().setELocationId("New Citation eLocationId");
    changeCitationInfo.getAnnotationCitation().setVolume("New Citation volume");
    changeCitationInfo.getAnnotationCitation().setNote("New Citation note");
    changeCitationInfo.getAnnotationCitation().setSummary("New Citation summary");
    changeCitationInfo.getAnnotationCitation().setUrl("New Citation url");

    //remove an author
    Annotation removeAuthor = new Annotation(new UserProfile(), AnnotationType.FORMAL_CORRECTION, article.getID());
    removeAuthor.setAnnotationCitation(new AnnotationCitation(article));
    removeAuthor.getAnnotationCitation().getAuthors().remove(0);

    //add an author
    Annotation addAuthor = new Annotation(new UserProfile(), AnnotationType.FORMAL_CORRECTION, article.getID());
    addAuthor.setAnnotationCitation(new AnnotationCitation(article));
    addAuthor.getAnnotationCitation().getAuthors().add(new CorrectedAuthor(new ArticleAuthor("New", "Article", "Author")));

    //add a collab author
    Annotation addCollabAuthor = new Annotation(new UserProfile(), AnnotationType.FORMAL_CORRECTION, article.getID());
    addCollabAuthor.setAnnotationCitation(new AnnotationCitation(article));
    addCollabAuthor.getAnnotationCitation().getCollaborativeAuthors().add("New Collab authors");

    //add a collab author
    Annotation removeCollabAuthor = new Annotation(new UserProfile(), AnnotationType.FORMAL_CORRECTION, article.getID());
    removeCollabAuthor.setAnnotationCitation(new AnnotationCitation(article));
    removeCollabAuthor.getAnnotationCitation().getCollaborativeAuthors().remove(0);

    //just want to check that parent ids don't get overwritten
    Annotation originalReply = new Annotation(userProfile, AnnotationType.REPLY, article.getID());
    originalReply.setParentID(originalCorrection.getID());
    dummyDataStore.store(originalReply);

    Annotation newReply = new Annotation(new UserProfile(), AnnotationType.REPLY, article.getID());
    newReply.setParentID(originalReply.getID());

    return new Object[][]{
        {originalCorrection, changeBasicProperties},
        {originalCorrection, changeCitationInfo},

        {originalCorrection, addAuthor},
        {originalCorrection, removeAuthor},

        {originalCorrection, addCollabAuthor},
        {originalCorrection, removeCollabAuthor},
        {originalReply, newReply}
    };
  }

  @Test(dataProvider = "annotationsToEdit")
  public void testEditAnnotation(Annotation originalAnnotation, Annotation newAnnotation) {
    annotationService.editAnnotation(originalAnnotation.getID(), newAnnotation);
    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, originalAnnotation.getID());
    assertNotNull(storedAnnotation, "deleted stored annotation");
    //properties that shouldn't have changed
    assertEquals(storedAnnotation.getArticleID(), originalAnnotation.getArticleID(), "Edit changed article Id");
    assertEquals(storedAnnotation.getType(), originalAnnotation.getType(), "Edit changed type");
    assertEquals(storedAnnotation.getParentID(), originalAnnotation.getParentID(), "Edit changed parent id");
    assertNotNull(storedAnnotation.getCreator(), "Edit deleted creator");
    assertEquals(storedAnnotation.getCreator().getID(), originalAnnotation.getCreator().getID(), "Edit changed creator");

    //properties that should have changed
    assertEquals(storedAnnotation.getTitle(), newAnnotation.getTitle(), "Edit didn't update title");
    assertEquals(storedAnnotation.getBody(), newAnnotation.getBody(), "Edit didn't update body");
    assertEquals(storedAnnotation.getCompetingInterestBody(), newAnnotation.getCompetingInterestBody(),
        "Edit didn't update competing interest statement");

    if (originalAnnotation.getAnnotationCitation() == null) {
      assertNull(storedAnnotation.getAnnotationCitation(), "Edit added a new citation when one didn't exist before");
    } else {
      assertNotNull(storedAnnotation.getAnnotationCitation(), "Edit deleted citation");
      AnnotationCitation actualCitation = storedAnnotation.getAnnotationCitation();
      AnnotationCitation expectedCitation = newAnnotation.getAnnotationCitation();
      assertEquals(actualCitation.getTitle(), expectedCitation.getTitle(), "Edit didn't update citation title");
      assertEquals(actualCitation.getYear(), expectedCitation.getYear(), "Edit didn't update citation year");
      assertEquals(actualCitation.getVolume(), expectedCitation.getVolume(), "Edit didn't update citation volume");
      assertEquals(actualCitation.getIssue(), expectedCitation.getIssue(), "Edit didn't update citation issue");
      assertEquals(actualCitation.getJournal(), expectedCitation.getJournal(), "Edit didn't update citation journal");
      assertEquals(actualCitation.getELocationId(), expectedCitation.getELocationId(), "Edit didn't update citation eLocationId");

      if (expectedCitation.getCollaborativeAuthors() != null) {
        assertEquals(actualCitation.getCollaborativeAuthors().toArray(), expectedCitation.getCollaborativeAuthors().toArray(),
            "Edit didn't update citation collab authors");
      } else {
        assertTrue(actualCitation.getCollaborativeAuthors() == null || actualCitation.getCollaborativeAuthors().isEmpty(),
            "Stored citation had collab authors when none were expected");
      }
      if (expectedCitation.getAuthors() != null) {
        assertNotNull(actualCitation.getAuthors(), "Authors got deleted");
        assertEquals(actualCitation.getAuthors().size(), expectedCitation.getAuthors().size(),
            "Edited citation had incorrect number of authors");
        for (int i = 0; i < actualCitation.getAuthors().size(); i++) {
          CorrectedAuthor actualAuthor = actualCitation.getAuthors().get(i);
          CorrectedAuthor expectedAuthor = expectedCitation.getAuthors().get(i);
          assertEquals(actualAuthor.getGivenNames(), expectedAuthor.getGivenNames(), 
              "Citation author " + (i + 1) + " had incorrect given names");
          assertEquals(actualAuthor.getSurName(), expectedAuthor.getSurName(), 
              "Citation author " + (i + 1) + " had incorrect surnames");
          assertEquals(actualAuthor.getSuffix(), expectedAuthor.getSuffix(), 
              "Citation author " + (i + 1) + " had incorrect suffix");
        }
      }
    }

  }
}
