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
import org.ambraproject.models.UserProfile;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.ArticleContributor;
import org.topazproject.ambra.models.Citation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Dragisa Krsmanovic
 * @author Joe Osowski
 */
public class CitationServiceTest extends AdminBaseTest {

  @Autowired
  protected CitationService citationService;

  @DataProvider(name = "storedCitation")
  public Object[][] getStoredCitation() {
    Citation citation = new Citation();
    citation.setTitle("Citation title");
    citation.setDisplayYear("1999");
    citation.setJournal("PLoS ONE");
    citation.setVolume("01");
    citation.setIssue("01");
    citation.setELocationId("132");
    citation.setDoi("info:doi/10.1371/journal.fake.doi1234");

    List<String> collabAuthors = new ArrayList<String>(2);
    collabAuthors.add("John P. Smith");
    collabAuthors.add("The Heritage Foundation");
    citation.setCollaborativeAuthors(collabAuthors);

    UserProfile author = new UserProfile();
    author.setSurname("Borges");
    author.setGivenNames("Jorge Luis");
    author.setSuffix("Esquire");
    dummyDataStore.store(author);
    ArrayList<UserProfile> authors = new ArrayList<UserProfile>();
    authors.add(author);

    ArticleContributor ac = new ArticleContributor();
    ac.setSurnames("Doe");
    ac.setGivenNames("John");
    ac.setSuffix("Mr");
    dummyDataStore.store(ac);
    ArrayList<ArticleContributor> annotationAuthors = new ArrayList<ArticleContributor>();
    annotationAuthors.add(ac);
    citation.setAnnotationArticleAuthors(annotationAuthors);
    
    citation.setId(URI.create(dummyDataStore.store(citation)));

    return new Object[][]{
        {dummyDataStore.get(Citation.class,citation.getId())} //Reload because tests change the citation stored in the db
    };
  }

  @Test(dataProvider = "storedCitation")
  public void testUpdateCitation(Citation original) {
    String title = "title2";
    String year = "year2";
    String journal = "journal2";
    String volume = "volume2";
    String issue = "issue2";
    String eLocationId = "eLocationID2";
    String doi = "doi2";

    citationService.updateCitation(
        original.getId().toString(),
        title,
        year,
        journal,
        volume,
        issue,
        "  " + eLocationId + "  ",
        doi);
    Citation citation = dummyDataStore.get(Citation.class, original.getId());
    assertEquals(citation.getTitle(), title, "Citation title didn't updated");
    assertEquals(citation.getDisplayYear(), year, "Citation display year didn't get updated");
    assertEquals(citation.getVolume(), volume, "Citation volume didn't get updated");
    assertEquals(citation.getIssue(), issue, "Citation issue didn't get updated");
    assertEquals(citation.getJournal(), journal, "Citation journal didn't get updated");
    assertEquals(citation.getELocationId(), eLocationId, "citation eLocationId didn't get updated");
    assertEquals(citation.getDoi(), doi, "Citation doi didn't get updated");
  }


  @Test(expectedExceptions = {HibernateException.class})
  public void testUpdateCitationThatDoesNotExist() {
    String badCitationId = "citation-bad-id";
    String title = "title2";
    String year = "year2";
    String journal = "journal2";
    String volume = "volume2";
    String issue = "issue2";
    String eLocationId = "eLocationID2";
    String doi = "doi2";

    citationService.updateCitation(badCitationId, title, year, journal, volume, issue, "  " + eLocationId + "  ", doi);
  }

  @DataProvider(name = "dummyAnnotationAuthor")
  public Object[][] getDummyAnnotationAuthor() {
    Citation citation = (Citation) getStoredCitation()[0][0];
    ArticleContributor author = new ArticleContributor();
    author.setGivenNames("oldGivenName");
    author.setSurnames("oldSurName");
    author.setSuffix("oldSuffix");
    String userId = dummyDataStore.store(author);
    return new Object[][]{
        {citation.getId().toString(), userId}
    };
  }

  @Test(dataProvider = "storedCitation")
  public void testAddCollaborativeAuthor(Citation citation) {
    int sizeBefore = citation.getCollaborativeAuthors().size();

    String newCollabAuthor = "newCollabAuthor";
    citationService.addCollaborativeAuthor(citation.getId().toString(), newCollabAuthor);

    citation = dummyDataStore.get(Citation.class,citation.getId());

    assertEquals(citation.getCollaborativeAuthors().size(), sizeBefore + 1,
        "Collaborative author didn't get added to citation");
    assertTrue(citation.getCollaborativeAuthors().contains(newCollabAuthor),
        "Collaborative author didn't get added to citation");
  }


  @Test(dataProvider = "storedCitation")
  public void testDeleteCollaborativeAuthor(Citation citation) {
    int sizeBefore = citation.getCollaborativeAuthors().size();
    String author = citation.getCollaborativeAuthors().get(0);

    citationService.deleteCollaborativeAuthor(citation.getId().toString(), 0);

    citation = dummyDataStore.get(Citation.class,citation.getId());

    assertEquals(citation.getCollaborativeAuthors().size(), sizeBefore - 1, "Collaborative author didn't get deleted");
    assertFalse(citation.getCollaborativeAuthors().contains(author),"Collaborative author didn't get deleted");
  }

  @Test(dataProvider = "storedCitation")
  public void testUpdateCollaborativeAuthor(Citation citation) {
    int updatingIndex = 0;
    int sizeBefore = citation.getCollaborativeAuthors().size();

    String oldAuthorValue = citation.getCollaborativeAuthors().get(updatingIndex);
    String newAuthorValue = "UpdatedAuthor";

    citationService.updateCollaborativeAuthor(citation.getId().toString(), updatingIndex, " " + newAuthorValue + " ");

    citation = dummyDataStore.get(Citation.class,citation.getId());

    String dbAuthorValue = citation.getCollaborativeAuthors().get(updatingIndex);

    assertEquals(citation.getCollaborativeAuthors().size(), sizeBefore,
        "Updated citation lost or gained a collaborative author");

    assertNotSame(oldAuthorValue, newAuthorValue,"Updated author changed place in citation's list");
    assertEquals(dbAuthorValue, newAuthorValue, "Collab Author didn't get updated");
  }

  @Test(dataProvider = "storedCitation")
  public void testAddAnnotationAuthor(Citation citation) {
    int sizeBefore = citation.getAnnotationArticleAuthors().size();

    String surnames = "New Surname";
    String givenNames = "NewGivenName";
    String suffix = "NewSuffix";

    String authorId = citationService.addAnnotationAuthor(citation.getId().toString(), surnames, givenNames, suffix);

    ArticleContributor newAuthor = dummyDataStore.get(ArticleContributor.class, URI.create(authorId));

    assertNotNull(newAuthor, "new author didn't get stored to the database");
    assertEquals(newAuthor.getSurnames(), surnames,"stored author didn't have correct surnames");
    assertEquals(newAuthor.getGivenNames(), givenNames,"stored author didn't have correct given names");
    assertEquals(newAuthor.getSuffix(), suffix,"stored author didn't have correct suffix");

    citation = dummyDataStore.get(Citation.class,citation.getId());

    assertEquals(citation.getAnnotationArticleAuthors().size(), sizeBefore + 1,"Citation didn't get updated with new author");

    //Check that the author is attached to the citation
    for (ArticleContributor p : citation.getAnnotationArticleAuthors()) {
      if (p.getId().equals(newAuthor.getId())) {
        assertEquals(newAuthor.getSurnames(), p.getSurnames(),
            "Author attached to citation didn't have correct surnames");
        assertEquals(newAuthor.getGivenNames(), p.getGivenNames(),
            "Author attached to citation didn't have correct given names");
        assertEquals(newAuthor.getSuffix(), p.getSuffix(),
            "Author attached to citation didn't have correct suffix");

        return;
      }
    }

    fail("New author not found as part of authors collection associated with citation.");
  }

  @Test(dataProvider = "dummyAnnotationAuthor")
  public void testUpdateAnnotationAuthor(String notUsed, String userId) {
    assertNotNull(dummyDataStore.get(ArticleContributor.class, URI.create(userId)),"DataProvider didn't store user to the database");
    String surnames = "New Surname";
    String givenNames = "NewGivenName";
    String suffix = "         ";

    citationService.updateAnnotationAuthor(userId, " " + surnames + " ", givenNames, suffix);

    ArticleContributor author = dummyDataStore.get(ArticleContributor.class, URI.create(userId));

    assertNotNull(author,"Author wasn't stored to the database");
    assertEquals(author.getSurnames(), surnames,"Author didn't get surnames updated");
    assertEquals(author.getGivenNames(), givenNames,"Author didn't get given names updated");
    assertNull(author.getSuffix(),"Author didn't get suffix updated");
  }

  @Test(dataProvider = "storedCitation")
  public void testDeleteAnnotationAuthor(Citation citation) {
    ArticleContributor author1 = citation.getAnnotationArticleAuthors().get(0);
    String authorId = author1.getId().toString();
    int sizeBefore = citation.getAnnotationArticleAuthors().size();

    citationService.deleteAnnotationAuthor(citation.getId().toString(), authorId);

    citation = dummyDataStore.get(Citation.class,citation.getId());

    assertEquals(citation.getAnnotationArticleAuthors().size(), sizeBefore - 1);
    assertFalse(citation.getAnnotationArticleAuthors().contains(author1));
  }
}
