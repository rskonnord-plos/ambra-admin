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
import org.ambraproject.annotation.service.WebAnnotation;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.ArticleContributor;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.FormalCorrection;
import org.topazproject.ambra.models.MinorCorrection;
import org.topazproject.ambra.models.Retraction;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick 2/6/12
 */
public class ManageAnnotationActionTest extends AdminWebTest {

  @Autowired
  protected ManageAnnotationAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @Test
  public void testExecute() throws Exception {

    action.setAnnotationId(null);
    final String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ","));

    assertNull(action.getAnnotation(), "Action returned annotation on default request");
    assertNull(action.getAnnotationId(), "Action returned annotation id on default request");
  }

  @DataProvider(name = "annotation")
  public Object[][] getAnnotation() {
    UserProfile user = new UserProfile();
    user.setDisplayName("EmmaSwan");
    dummyDataStore.store(user);

    Comment comment = new Comment();
    comment.setBody(new AnnotationBlob());
    comment.getBody().setBody(("Emma Swan gets the surprise of her life when Henry, " +
        "the son she gave up 10 years ago, arrives on her doorstep.").getBytes());
    comment.setType(Comment.RDF_TYPE);
    comment.setCreated(Calendar.getInstance().getTime());
    comment.setCreator(user.getAccountUri());
    comment.setAnnotates(URI.create("id:fake-article-to-annotate"));
    comment.setContext("fakecontext");
    dummyDataStore.store(comment);
    WebAnnotation commentWebAnnotation = new WebAnnotation(comment,
        user.getDisplayName(), new String(comment.getBody().getBody()));

    Article articleToRetract = new Article();
    articleToRetract.setDoi("id:test-article-to-retract");
    articleToRetract.setAuthors(new ArrayList<ArticleAuthor>(3));
    articleToRetract.getAuthors().add(new ArticleAuthor());
    articleToRetract.getAuthors().get(0).setGivenNames("Harry");
    articleToRetract.getAuthors().get(0).setSurnames("Potter");
    articleToRetract.getAuthors().get(0).setSuffix("Dr.");

    articleToRetract.getAuthors().add(new ArticleAuthor());
    articleToRetract.getAuthors().get(1).setGivenNames("Michael");
    articleToRetract.getAuthors().get(1).setSurnames("Eisen");
    articleToRetract.getAuthors().get(1).setSuffix("PhD");

    articleToRetract.getAuthors().add(new ArticleAuthor());
    articleToRetract.getAuthors().get(2).setGivenNames("Gregory");
    articleToRetract.getAuthors().get(2).setSurnames("House");
    articleToRetract.getAuthors().get(2).setSuffix("MD");

    articleToRetract.setCollaborativeAuthors(new ArrayList<String>(2));
    articleToRetract.getCollaborativeAuthors().add("The Skoll Foundation");
    articleToRetract.getCollaborativeAuthors().add("Free Willy Foundation");

    dummyDataStore.store(articleToRetract);

    Retraction retraction = new Retraction();
    retraction.setBody(new AnnotationBlob());
    retraction.getBody().setBody(("Michael's quest to find out who killed Diego is interrupted as he helps a " +
        "widow take on a network of insurance scammers. However, the job could be jeopardized when Madeline " +
        "befriends an \"asset\" during the mission.").getBytes());
    retraction.setType(MinorCorrection.RDF_TYPE);
    retraction.setCreated(Calendar.getInstance().getTime());
    retraction.setCreator(user.getAccountUri());
    retraction.setAnnotates(URI.create(articleToRetract.getDoi()));
    retraction.setContext("someFakeContext");

    //Annotations still use 'Citation' objects
    retraction.setBibliographicCitation(new Citation());
    retraction.getBibliographicCitation().setTitle(articleToRetract.getTitle());
    retraction.getBibliographicCitation().setDoi(articleToRetract.getDoi());

    retraction.getBibliographicCitation().setAnnotationArticleAuthors(new ArrayList<ArticleContributor>(3));
    for (ArticleAuthor author : articleToRetract.getAuthors()) {
      ArticleContributor citationAuthor = new ArticleContributor();
      citationAuthor.setGivenNames(author.getGivenNames());
      citationAuthor.setSurnames(author.getSurnames());
      citationAuthor.setSuffix(author.getSuffix());
      dummyDataStore.store(citationAuthor);
      retraction.getBibliographicCitation().getAnnotationArticleAuthors().add(citationAuthor);
    }
    retraction.getBibliographicCitation().setCollaborativeAuthors(new ArrayList<String>(2));
    retraction.getBibliographicCitation().getCollaborativeAuthors().addAll(articleToRetract.getCollaborativeAuthors());

    dummyDataStore.store(retraction.getBibliographicCitation());
    dummyDataStore.store(retraction);

    WebAnnotation retractionWebAnnotation = new WebAnnotation(retraction,
        user.getDisplayName(), new String(retraction.getBody().getBody()));


    return new Object[][]{
        {comment.getId().toString(), commentWebAnnotation, null,
            new String(comment.getBody().getBody()), comment.getContext()},

        {retraction.getId().toString(), retractionWebAnnotation,
            articleToRetract, new String(retraction.getBody().getBody()), retraction.getContext()}
    };
  }

  @Test(dataProvider = "annotation", dependsOnMethods = {"testExecute"})
  public void testLoadAnnotation(String annotationId, WebAnnotation expectedAnnotation, Article annotatedArticle,
                                 String annotationBody, String context) throws Exception {
    action.setAnnotationBody(null);
    action.setAnnotationContext(null);
    action.setAnnotationId(annotationId);
    final String result = action.loadAnnotation();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionMessages().size(), 0, "Action returned messages on default request");
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));

    assertEquals(action.getAnnotation().getTitle(), expectedAnnotation.getTitle(), "Action didn't return correct title");
    assertEquals(action.getAnnotation().getType(), expectedAnnotation.getType(), "Action didn't return correct type");
    assertMatchingDates(action.getAnnotation().getCreatedAsDate(), expectedAnnotation.getCreatedAsDate());
    assertEquals(action.getAnnotation().getCreator(), expectedAnnotation.getCreator(), "Action didn't return correct creator");
    assertEquals(action.getAnnotation().getAnnotates(), expectedAnnotation.getAnnotates(), "Action didn't return correct annotation target");

    if (annotatedArticle != null) {
      assertEquals(action.getCitationTitle(), annotatedArticle.getTitle(),
          "Action didn't return correct annotation citation title");
      assertEquals(action.getCitationDisplayYear(), annotatedArticle.getDate(),
          "Action didn't return correct annotation citation display year");
      assertEquals(action.getCitationVolumeNumber(), annotatedArticle.getVolume(),
          "Action didn't return correct annotation citation volume number");
      assertEquals(action.getCitationJournal(), annotatedArticle.getJournal(),
          "Action didn't return correct annotation citation journal");
      assertEquals(action.getCitationDoi(), annotatedArticle.getDoi(),
          "Action didn't return correct annotation citation doi");

      assertEquals(action.getCitationAuthorIds().length, action.getCitationAuthorGivenNames().length,
          "number of author ids and author given names didn't match");
      assertEquals(action.getCitationAuthorIds().length, action.getCitationAuthorSurnames().length,
          "number of author ids and author surnames didn't match");
      assertEquals(action.getCitationAuthorIds().length, action.getCitationAuthorSuffixes().length,
          "number of author ids and author suffixes didn't match");

      assertEquals(action.getCitationAuthorIds().length, annotatedArticle.getAuthors().size(),
          "Action returned incorrect number of authors");
      for (int i = 0; i < annotatedArticle.getAuthors().size(); i++) {
        assertEquals(action.getCitationAuthorGivenNames()[i], annotatedArticle.getAuthors().get(i).getGivenNames(),
            "Author " + (i + 1) + " had incorrect given name");
        assertEquals(action.getCitationAuthorSurnames()[i], annotatedArticle.getAuthors().get(i).getSurnames(),
            "Author " + (i + 1) + " had incorrect surname");
        assertEquals(action.getCitationAuthorSuffixes()[i], annotatedArticle.getAuthors().get(i).getSuffix(),
            "Author " + (i + 1) + " had incorrect suffix");
      }
      assertEquals(action.getCitationCollaborativeAuthorNames(), annotatedArticle.getCollaborativeAuthors().toArray(),
          "action returned incorrect collaborative authors");
    } else {
      assertNull(action.getCitationTitle(), "Action returned citation title when citation should be null");
      assertNull(action.getCitationDisplayYear(), "Action returned citation display year when citation should be null");
      assertNull(action.getCitationVolumeNumber(), "Action returned citation volume when citation should be null");
      assertNull(action.getCitationJournal(), "Action returned citation journal when citation should be null");
      assertNull(action.getCitationDoi(), "Action returned citation doi when citation should be null");

      assertNull(action.getCitationAuthorIds(), "action returned author ids when citation should be null");
      assertNull(action.getCitationAuthorGivenNames(), "action returned author given names when citation should be null");
      assertNull(action.getCitationAuthorSurnames(), "action returned author surnames when citation should be null");
      assertNull(action.getCitationAuthorSuffixes(), "action returned author suffixes when citation should be null");
      assertNull(action.getCitationCollaborativeAuthorNames(), "action returned collaborative authors when citation should be null");
    }

    assertEquals(action.getAnnotationBody(), annotationBody, "Action didn't return correct annotation body");
    assertEquals(action.getAnnotationContext(), context, "Action didn't return correct context");
  }

  @Test(dataProvider = "annotation", dependsOnMethods = {"testLoadAnnotation"})
  public void testEditAnnotation(String annotationId, WebAnnotation expectedAnnotation, Article annotatedArticle,
                                 String annotationBody, String context) throws Exception {
    //load up the annotation to set the stored values in the action
    reloadAnnotation(annotationId);

    final String newAnnotationBody = "New annotation body for " + annotationId;
    final String newAnnotationContext = "some brand new context for " + annotationId;

    action.setAnnotationBody(newAnnotationBody);
    action.setAnnotationContext(newAnnotationContext);

    final String result = action.saveAnnotation();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertTrue(action.getActionMessages().size() >= 2, "Action didn't return messages indicating success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));

    assertEquals(action.getAnnotationId(), annotationId, "Action changed annotations after saving changes");
    assertEquals(action.getAnnotationBody(), newAnnotationBody, "Action didn't have correct annotation body");
    assertEquals(action.getAnnotationContext(), newAnnotationContext, "Action didn't have correct context");

    //check the values that got stored to the db
    final Annotation storedAnnotation = dummyDataStore.get(Annotation.class, URI.create(annotationId));
    assertEquals(storedAnnotation.getContext(), newAnnotationContext, "Annotation didn't get correct context stored");
    assertEquals(new String(((AnnotationBlob) storedAnnotation.getBody()).getBody()), newAnnotationBody,
        "Annotation didn't get correct body stored");
  }

  @Test(dataProvider = "annotation", dependsOnMethods = {"testLoadAnnotation"})
  public void testAddAuthor(String annotationId, WebAnnotation expectedAnnotation, Article annotatedArticle,
                            String annotationBody, String context) throws Exception {
    //only annotations that have a copy of the article's citation can have authors added, removed, etc.
    if (annotatedArticle != null) {
      //load up the annotation to set the stored values in the action
      reloadAnnotation(annotationId);

      String[] newAuthorGivenNames = new String[action.getCitationAuthorGivenNames().length + 1];
      String[] newAuthorSurnames = new String[action.getCitationAuthorGivenNames().length + 1];
      String[] newAuthorSuffixes = new String[action.getCitationAuthorGivenNames().length + 1];

      for (int i = 0; i < action.getCitationAuthorGivenNames().length; i++) {
        newAuthorGivenNames[i] = action.getCitationAuthorGivenNames()[i];
        newAuthorSurnames[i] = action.getCitationAuthorSurnames()[i];
        newAuthorSuffixes[i] = action.getCitationAuthorSuffixes()[i];
      }
      newAuthorGivenNames[newAuthorGivenNames.length - 1] = "John";
      newAuthorSurnames[newAuthorSurnames.length - 1] = "Smith";
      newAuthorSuffixes[newAuthorSuffixes.length - 1] = "M.D.";

      action.setCitationAuthorGivenNames(newAuthorGivenNames);
      action.setCitationAuthorSurnames(newAuthorSurnames);
      action.setCitationAuthorSuffixes(newAuthorSuffixes);
      final String result = action.saveAnnotation();

      assertEquals(result, Action.SUCCESS, "Action didn't return success");
      assertTrue(action.getActionMessages().size() >= 2, "Action didn't return messages indicating success");
      assertEquals(action.getActionErrors().size(), 0,
          "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));

      assertEquals(action.getAnnotationId(), annotationId, "Action changed annotations after saving changes");
      assertEquals(action.getCitationAuthorGivenNames(), newAuthorGivenNames, "Action didn't have correct author given names");
      assertEquals(action.getCitationAuthorSurnames(), newAuthorSurnames, "Action didn't have correct author surnames");
      assertEquals(action.getCitationAuthorSuffixes(), newAuthorSuffixes, "Action didn't have correct author suffixes");

      //check the values that got stored to the database
      List<ArticleContributor> storedAuthors = getStoredAuthors(annotationId);
      assertEquals(storedAuthors.size(), newAuthorGivenNames.length, "Incorrect number of authors were stored to the database");
      ArticleContributor newAuthor = storedAuthors.get(storedAuthors.size() - 1);
      assertEquals(newAuthor.getGivenNames(), "John", "Author got stored with incorrect given name");
      assertEquals(newAuthor.getSurnames(), "Smith", "Author got stored with incorrect surname");
      assertEquals(newAuthor.getSuffix(), "M.D.", "Author got stored with incorrect suffix");
    }
  }

  @Test(dataProvider = "annotation", dependsOnMethods = {"testLoadAnnotation"})
  public void testDeleteAuthor(String annotationId, WebAnnotation expectedAnnotation, Article annotatedArticle,
                               String annotationBody, String context) throws Exception {
    //only annotations that have a copy of the article's citation can have authors added, removed, etc.
    if (annotatedArticle != null) {
      //load up the annotation to set the stored values in the action
      reloadAnnotation(annotationId);
      String[] expectedAuthorGivenNames = Arrays.copyOfRange(action.getCitationAuthorGivenNames(), 1,
          action.getCitationAuthorGivenNames().length);
      String[] expectedAuthorSurnames = Arrays.copyOfRange(action.getCitationAuthorSurnames(), 1,
          action.getCitationAuthorSurnames().length);
      String[] expectedAuthorSuffixes = Arrays.copyOfRange(action.getCitationAuthorSuffixes(), 1,
          action.getCitationAuthorSuffixes().length);
      String deletedAuthorGivenName = action.getCitationAuthorGivenNames()[0];
      String deletedAuthorSurname = action.getCitationAuthorSurnames()[0];
      String deletedAuthorSuffix = action.getCitationAuthorSuffixes()[0];

      //The action class assumes that the last entry in the author arrays is a new author to add, unless it's all empty strings
      final String[] citationAuthorGivenNames = Arrays.copyOf(action.getCitationAuthorGivenNames(),
          action.getCitationAuthorGivenNames().length + 1);
      citationAuthorGivenNames[citationAuthorGivenNames.length - 1] = "";

      final String[] citationAuthorSurnames = Arrays.copyOf(action.getCitationAuthorSurnames(),
          action.getCitationAuthorSurnames().length + 1);
      citationAuthorSurnames[citationAuthorSurnames.length - 1] = "";

      final String[] citationAuthorSuffixes = Arrays.copyOf(action.getCitationAuthorSuffixes(),
          action.getCitationAuthorSuffixes().length + 1);
      citationAuthorSuffixes[citationAuthorSuffixes.length - 1] = "";

      action.setCitationAuthorGivenNames(citationAuthorGivenNames);
      action.setCitationAuthorSurnames(citationAuthorSurnames);
      action.setCitationAuthorSuffixes(citationAuthorSuffixes);
      action.setCitationAuthorDeleteIndex(0);
      final String result = action.saveAnnotation();

      assertEquals(result, Action.SUCCESS, "Action didn't return success");
      assertTrue(action.getActionMessages().size() >= 2, "Action didn't return messages indicating success");
      assertEquals(action.getActionErrors().size(), 0,
          "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));

      assertEquals(action.getAnnotationId(), annotationId, "Action changed annotations after saving changes");
      assertEquals(action.getCitationAuthorGivenNames(), expectedAuthorGivenNames,
          "Action didn't have correct author given names");
      assertEquals(action.getCitationAuthorSurnames(), expectedAuthorSurnames,
          "Action didn't have correct author surnames");
      assertEquals(action.getCitationAuthorSuffixes(), expectedAuthorSuffixes,
          "Action didn't have correct author suffixes");

      //check the db
      final List<ArticleContributor> storedAuthors = getStoredAuthors(annotationId);
      assertEquals(storedAuthors.size(), expectedAuthorGivenNames.length, "Author didn't get deleted from the database");
      for (ArticleContributor author : storedAuthors) {
        boolean isDeletedAuthor = deletedAuthorGivenName.equals(author.getGivenNames())
            && deletedAuthorSurname.equals(author.getSurnames()) && deletedAuthorSuffix.equals(author.getSuffix());
        assertFalse(isDeletedAuthor, "Wrong author got deleted");
      }
    }
  }

  @Test(dataProvider = "annotation", dependsOnMethods = {"testLoadAnnotation"})
  public void testAddCollabAuthor(String annotationId, WebAnnotation expectedAnnotation, Article annotatedArticle,
                                  String annotationBody, String context) throws Exception {
    //only annotations that have a copy of the article's citation can have authors added, removed, etc.
    if (annotatedArticle != null) {
      //load up the annotation to set the stored values in the action
      reloadAnnotation(annotationId);
      String[] collabAuthors = new String[action.getCitationCollaborativeAuthorNames().length + 1];
      for (int i = 0; i < action.getCitationCollaborativeAuthorNames().length; i++) {
        collabAuthors[i] = action.getCitationCollaborativeAuthorNames()[i];
      }
      collabAuthors[collabAuthors.length - 1] = "The Bill and Melinda Gates Foundation";

      action.setCitationCollaborativeAuthorNames(collabAuthors);
      final String result = action.saveAnnotation();

      assertEquals(result, Action.SUCCESS, "Action didn't return success");
      assertTrue(action.getActionMessages().size() >= 2, "Action didn't return messages indicating success");
      assertEquals(action.getActionErrors().size(), 0,
          "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));

      assertEquals(action.getAnnotationId(), annotationId, "Action changed annotations after saving changes");
      assertEquals(action.getCitationCollaborativeAuthorNames(), collabAuthors,
          "Action didn't have correct collaborative authors");

      //check the db
      assertEquals(getStoredCollabAuthors(annotationId).toArray(), collabAuthors,
          "Incorrect collab authors were stored to the db");
    }
  }

  @Test(dataProvider = "annotation", dependsOnMethods = {"testLoadAnnotation","testAddCollabAuthor"})
  public void testDeleteCollabAuthor(String annotationId, WebAnnotation expectedAnnotation, Article annotatedArticle,
                                  String annotationBody, String context) throws Exception {
    //only annotations that have a copy of the article's citation can have authors added, removed, etc.
    if (annotatedArticle != null) {
      //load up the annotation to set the stored values in the action
      reloadAnnotation(annotationId);
      
      //the action assumes that the last collab author entry is a new one to add, unless it's an empty string 
      String[] collabAuthors = new String[action.getCitationCollaborativeAuthorNames().length + 1];
      for (int i = 0; i < action.getCitationCollaborativeAuthorNames().length; i++) {
        collabAuthors[i] = action.getCitationCollaborativeAuthorNames()[i];
      }
      collabAuthors[collabAuthors.length - 1] = "";
      String[] expectedCollabAuthors = Arrays.copyOfRange(collabAuthors, 1, collabAuthors.length - 1);

      action.setCitationCollaborativeAuthorNames(collabAuthors);
      action.setCitationCollaborativeAuthorDeleteIndex(0);
      final String result = action.saveAnnotation();

      assertEquals(result, Action.SUCCESS, "Action didn't return success");
      assertTrue(action.getActionMessages().size() >= 2, "Action didn't return messages indicating success");
      assertEquals(action.getActionErrors().size(), 0,
          "Action returned error messages: " + StringUtils.join(action.getActionErrors(), ";"));

      assertEquals(action.getAnnotationId(), annotationId, "Action changed annotations after saving changes");
      assertEquals(action.getCitationCollaborativeAuthorNames(), expectedCollabAuthors,
          "Action didn't have correct collaborative authors");

      //check the db
      assertEquals(getStoredCollabAuthors(annotationId).toArray(), expectedCollabAuthors,
          "Incorrect collab authors were stored to the db");
    }
  }

  private List<ArticleContributor> getStoredAuthors(String annotationId) {
    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, URI.create(annotationId));
    List<ArticleContributor> storedAuthors;
    if (storedAnnotation instanceof Retraction) {
      storedAuthors = ((Retraction) storedAnnotation).getBibliographicCitation().getAnnotationArticleAuthors();
    } else {
      storedAuthors = ((FormalCorrection) storedAnnotation).getBibliographicCitation().getAnnotationArticleAuthors();
    }
    return storedAuthors;
  }

  private List<String> getStoredCollabAuthors(String annotationId) {
    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, URI.create(annotationId));
    List<String> storedAuthors;
    if (storedAnnotation instanceof Retraction) {
      storedAuthors = ((Retraction) storedAnnotation).getBibliographicCitation().getCollaborativeAuthors();
    } else {
      storedAuthors = ((FormalCorrection) storedAnnotation).getBibliographicCitation().getCollaborativeAuthors();
    }
    return storedAuthors;
  }

  private void reloadAnnotation(String annotationId) throws Exception {
    action.setAnnotationBody(null);
    action.setAnnotationContext(null);
    action.setCitationId(null);
    action.setCitationAuthorDeleteIndex(-1);
    action.setCitationCollaborativeAuthorDeleteIndex(-1);
    action.setAnnotationId(annotationId);
    action.loadAnnotation();
  }

}
