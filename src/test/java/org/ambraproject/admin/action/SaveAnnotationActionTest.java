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
import org.ambraproject.models.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA. User: alex Date: 3/29/12 Time: 12:11 PM To change this template use File | Settings |
 * File Templates.
 */
public class SaveAnnotationActionTest extends AdminWebTest {
  @Autowired
  protected SaveAnnotationAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @BeforeMethod
  public void resetAction() {
    action.setModel(new Annotation());
    action.getModel().setAnnotationCitation(new AnnotationCitation());
    action.setAuthorGivenNames(new String[0]);
    action.setAuthorSurnames(new String[0]);
    action.setAuthorSuffixes(new String[0]);
    action.setCollabAuthors(new String[0]);
  }

  @Test
  public void testExecute() throws Exception {
    UserProfile userProfile = new UserProfile(
        "authIdSaveAnnotationActionTest",
        "email@saveAnnotationActionTest.org",
        "displayNameSaveAnnotationActionTest"
    );
    dummyDataStore.store(userProfile);
    Article article = new Article("id:doi-SaveAnnotationActionTest");
    dummyDataStore.store(article);

    Annotation original = new Annotation(userProfile, AnnotationType.FORMAL_CORRECTION, article.getID());
    original.setBody("After surviving the horrific car crash, Elena, still shaken by her resemblance to " +
        "Katherine Pierce, is rescued by Damon who takes on her on a road trip to Georgia where he meets " +
        "with an old friend of his, a witch/barmaid named Bree, to ask for her help on a spell that could " +
        "free Katherine from her tomb. Back in Mystic Falls, Stefan tries to help Bonnie understand her " +
        "wican powers and gets to meet her grandmother Tituba. Meanwhile, Jeremy meets a new local girl, " +
        "named Anna, who give him insight on the vampire legends about the town as he continues to research his ...");
    original.setXpath("old xpath");
    original.setAnnotationUri("id:annotationUriToChange");

    original.setAnnotationCitation(new AnnotationCitation());
    original.getAnnotationCitation().setTitle("Unpleasantville");
    original.getAnnotationCitation().setYear("2100");
    original.getAnnotationCitation().setIssue("Original Issue");
    original.getAnnotationCitation().setJournal("Original Journal");
    original.getAnnotationCitation().setNote("Damon, Elena and Stefan attend a school dance with a 1950s' theme, " +
        "where Alaric introduces himself to Damon whom he suspects is the vampire that killed his wife.");
    original.getAnnotationCitation().setSummary("Stefan and Damon try to figure out the identity of the new " +
        "vampire in town who is now stalking Elena in which Stefan gives Elena jewelry filled with Vervain " +
        "to protect her family and friends while she continues to investigate her true origins.");
    original.getAnnotationCitation().setUrl("original url");
    original.getAnnotationCitation().setPublisher("original publisher");
    dummyDataStore.store(original);

    action.setAnnotationId(original.getID());
    action.getModel().setBody("New body");
    action.getModel().setXpath("new xpath");
    action.getModel().setAnnotationUri("new annotation uri");
    action.getModel().getAnnotationCitation().setTitle("New Title");
    action.getModel().getAnnotationCitation().setYear("2010");
    action.getModel().getAnnotationCitation().setJournal("new Journal");
    action.getModel().getAnnotationCitation().setIssue("new Issue");
    action.getModel().getAnnotationCitation().setNote("new note");
    action.getModel().getAnnotationCitation().setSummary("new summary");
    action.getModel().getAnnotationCitation().setUrl("new url");
    action.getModel().getAnnotationCitation().setPublisher("new publisher");
    action.getModel().getAnnotationCitation().setELocationId("new eLocationID");

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));
    assertEquals(action.getAnnotationId(), original.getID(), "Action changed annotation id");

    Annotation storedAnnotation = dummyDataStore.get(Annotation.class, action.getAnnotationId());
    assertNotNull(storedAnnotation, "Annotation got deleted");
    assertEquals(storedAnnotation.getArticleID(), original.getArticleID(), "Action changed article id");
    assertEquals(storedAnnotation.getType(), original.getType(), "Action changed type");
    assertEquals(storedAnnotation.getParentID(), original.getParentID(), "Action changed parent id");

    assertEquals(storedAnnotation.getBody(), action.getModel().getBody(), "Action didn't update annotation body");
    assertEquals(storedAnnotation.getAnnotationUri(), action.getModel().getAnnotationUri(), "Action didn't update annotation uri");
    assertEquals(storedAnnotation.getXpath(), action.getModel().getXpath(), "Action didn't update annotation xpath");

    assertNotNull(storedAnnotation.getAnnotationCitation(), "Annotation citation got deleted");
    assertEquals(storedAnnotation.getAnnotationCitation().getTitle(), action.getModel().getAnnotationCitation().getTitle(),
        "Action didn't update citation title");
    assertEquals(storedAnnotation.getAnnotationCitation().getYear(), action.getModel().getAnnotationCitation().getYear(),
        "Action didn't update citation year");
    assertEquals(storedAnnotation.getAnnotationCitation().getJournal(), action.getModel().getAnnotationCitation().getJournal(),
        "Action didn't update citation journal");
    assertEquals(storedAnnotation.getAnnotationCitation().getIssue(), action.getModel().getAnnotationCitation().getIssue(),
        "Action didn't update citation issue");
    assertEquals(storedAnnotation.getAnnotationCitation().getNote(), action.getModel().getAnnotationCitation().getNote(),
        "Action didn't update citation note");
    assertEquals(storedAnnotation.getAnnotationCitation().getSummary(), action.getModel().getAnnotationCitation().getSummary(),
        "Action didn't update citation summary");
    assertEquals(storedAnnotation.getAnnotationCitation().getUrl(), action.getModel().getAnnotationCitation().getUrl(),
        "Action didn't update citation url");
    assertEquals(storedAnnotation.getAnnotationCitation().getPublisher(), action.getModel().getAnnotationCitation().getPublisher(),
        "Action didn't update citation publisher");
    assertEquals(storedAnnotation.getAnnotationCitation().getELocationId(), action.getModel().getAnnotationCitation().getELocationId(),
        "Action didn't update citation eLocationId");
  }

  @Test
  public void testAddAuthor() throws Exception {
    UserProfile userProfile = new UserProfile(
        "authIdSaveAnnotationActionTest",
        "email@saveAnnotationActionTest.org",
        "displayNameSaveAnnotationActionTest"
    );
    dummyDataStore.store(userProfile);
    Article article = new Article("id:doi-SaveAnnotationActionTest");
    dummyDataStore.store(article);

    Annotation original = new Annotation(userProfile, AnnotationType.FORMAL_CORRECTION, article.getID());
    original.setAnnotationCitation(new AnnotationCitation());
    dummyDataStore.store(original);

    CorrectedAuthor author = new CorrectedAuthor("Foo", "McFoo", "PhD");
    String collabAuthor = "The Skoll Foundation";
    action.setAnnotationId(original.getID());
    action.setAuthorGivenNames(new String[]{author.getGivenNames()});
    action.setAuthorSurnames(new String[]{author.getSurName()});
    action.setAuthorSuffixes(new String[]{author.getSuffix()});
    action.setCollabAuthors(new String[]{collabAuthor});

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));
    assertEquals(action.getAnnotationId(), original.getID(), "Action changed annotation id");

    List<CorrectedAuthor> storedAuthors = dummyDataStore.get(
        AnnotationCitation.class,
        original.getAnnotationCitation().getID()
    ).getAuthors();
    List<String> storedCollabAuthors = dummyDataStore.get(
        AnnotationCitation.class,
        original.getAnnotationCitation().getID()
    ).getCollaborativeAuthors();

    assertEquals(storedAuthors.size(), 1, "Action didn't store author");
    assertEquals(storedAuthors.get(0), author, "action didn't store correct author");

    assertEquals(storedCollabAuthors.size(), 1, "Action didn't store collab author");
    assertEquals(storedCollabAuthors.get(0), collabAuthor, "Action didn't store correct collab author");
  }

  @Test
  public void testDeleteAuthor() throws Exception {
    UserProfile userProfile = new UserProfile(
        "authIdSaveAnnotationActionTest",
        "email@saveAnnotationActionTest.org",
        "displayNameSaveAnnotationActionTest"
    );
    dummyDataStore.store(userProfile);
    Article article = new Article("id:doi-SaveAnnotationActionTest");
    dummyDataStore.store(article);

    Annotation original = new Annotation(userProfile, AnnotationType.FORMAL_CORRECTION, article.getID());
    original.setAnnotationCitation(new AnnotationCitation());
    original.getAnnotationCitation().setAuthors(Arrays.asList(
        new CorrectedAuthor("stored","author","1"),
        new CorrectedAuthor("stored","author","2"),
        new CorrectedAuthor("stored","author","3")
    ));
    original.getAnnotationCitation().setCollaborativeAuthors(Arrays.asList(
        "collab author 1",
        "collab author 2",
        "collab author 3"
    ));
    dummyDataStore.store(original);

    action.setAnnotationId(original.getID());
    //delete the middle author and collab author
    action.setAuthorGivenNames(new String[]{
        original.getAnnotationCitation().getAuthors().get(0).getGivenNames(),
        original.getAnnotationCitation().getAuthors().get(2).getGivenNames()
    });
    action.setAuthorSurnames(new String[]{
        original.getAnnotationCitation().getAuthors().get(0).getSurName(),
        original.getAnnotationCitation().getAuthors().get(2).getSurName()
    });
    action.setAuthorSuffixes(new String[]{
        original.getAnnotationCitation().getAuthors().get(0).getSuffix(),
        original.getAnnotationCitation().getAuthors().get(2).getSuffix()
    });
    action.setCollabAuthors(new String[]{
        original.getAnnotationCitation().getCollaborativeAuthors().get(0),
        original.getAnnotationCitation().getCollaborativeAuthors().get(2)
    });



    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));
    assertEquals(action.getAnnotationId(), original.getID(), "Action changed annotation id");

    List<CorrectedAuthor> storedAuthors = dummyDataStore.get(
        AnnotationCitation.class,
        original.getAnnotationCitation().getID()
    ).getAuthors();

    List<String> storedCollabAuthors = dummyDataStore.get(
        AnnotationCitation.class,
        original.getAnnotationCitation().getID()
    ).getCollaborativeAuthors();

    assertEquals(storedAuthors.size(), 2, "Action didn't delete author");
    assertEquals(storedAuthors.get(0), original.getAnnotationCitation().getAuthors().get(0), "Action changed first author");
    assertEquals(storedAuthors.get(1), original.getAnnotationCitation().getAuthors().get(2), "Action changed second author");

    assertEquals(storedCollabAuthors.size(), 2, "Action didn't delete collab author");
    assertEquals(storedCollabAuthors.get(0), original.getAnnotationCitation().getCollaborativeAuthors().get(0),
        "Action changed first collab author");
    assertEquals(storedCollabAuthors.get(1), original.getAnnotationCitation().getCollaborativeAuthors().get(2),
        "Action changed second collab author");
  }

  @Test
  public void testDoesNotAllowEmptyAuthor() throws Exception {
    UserProfile userProfile = new UserProfile(
        "authIdSaveAnnotationActionTest",
        "email@saveAnnotationActionTest.org",
        "displayNameSaveAnnotationActionTest"
    );
    dummyDataStore.store(userProfile);
    Article article = new Article("id:doi-SaveAnnotationActionTest");
    dummyDataStore.store(article);

    String originalCollabAuthor = "The Fu Foundation";
    CorrectedAuthor originalAuthor = new CorrectedAuthor("Alaric", "Saltzman", "Tch.");

    Annotation original = new Annotation(userProfile, AnnotationType.FORMAL_CORRECTION, article.getID());
    original.setAnnotationCitation(new AnnotationCitation());
    original.getAnnotationCitation().setAuthors(Arrays.asList(originalAuthor));
    original.getAnnotationCitation().setCollaborativeAuthors(Arrays.asList(originalCollabAuthor));
    dummyDataStore.store(original);

    action.setAnnotationId(original.getID());
    action.setAuthorGivenNames(new String[]{originalAuthor.getGivenNames(), ""});
    action.setAuthorSurnames(new String[]{originalAuthor.getSurName(), ""});
    action.setAuthorSuffixes(new String[]{originalAuthor.getSuffix(), ""});
    action.setCollabAuthors(new String[]{originalCollabAuthor, ""});

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");
    assertEquals(action.getActionMessages().size(), 1, "Action didn't return message indicating success");
    assertEquals(action.getActionErrors().size(), 0,
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0,
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));
    assertEquals(action.getAnnotationId(), original.getID(), "Action changed annotation id");

    List<CorrectedAuthor> storedAuthors = dummyDataStore.get(
        AnnotationCitation.class,
        original.getAnnotationCitation().getID()
    ).getAuthors();

    List<String> storedCollabAuthors = dummyDataStore.get(
        AnnotationCitation.class,
        original.getAnnotationCitation().getID()
    ).getCollaborativeAuthors();

    assertEquals(storedAuthors.size(), 1, "Action changed number of authors");
    assertEquals(storedAuthors.get(0), originalAuthor, "action changed original author");

    assertEquals(storedCollabAuthors.size(), 1, "Action stored empty collab author");
    assertEquals(storedCollabAuthors.get(0), originalCollabAuthor, "Action didn't changed original collab author");
  }
}
