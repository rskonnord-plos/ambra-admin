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

package org.ambraproject.admin.service;

import org.ambraproject.ApplicationException;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.views.TOCArticleGroup;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Journal;
import org.ambraproject.models.Volume;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * AdminService encapsulates the basic services needed by all administrative actions.
 */
public interface AdminService {
  public static final String ORDER_BY_FIELD_DATE_CREATED = "created";
  public static final String ORDER_BY_FIELD_ARTICLE_DOI = "doi";
  public static final String ORDER_BY_FIELD_DATE_PUBLISHED = "date";

  /**
   * Returns a Map of publishable articles in the order indicated.  The key of each element is the DOI (URI) of the
   * Article.  The value of each element is the Article itself.
   *
   * @param eIssn            eIssn of the Journal that this Article belongs to (no filter if null or empty)
   * @param orderField       field on which to sort the results (no sort if null or empty) Should be one of the
   *                         ORDER_BY_FIELD_ constants from this class
   * @param isOrderAscending controls the sort order (default is "false" so default order is descending)
   * @return Map of publishable articles sorted as indicated
   * @throws org.ambraproject.ApplicationException
   *
   */
  public List<ArticleInfo> getPublishableArticles(String eIssn, String orderField,
                                                  boolean isOrderAscending) throws ApplicationException;

  /**
   * Get the journal specified by the given key. Result will be lazy, so volumes won't be accessible outside of a
   * session
   *
   * @param journalKey the key of the journal to get
   * @return the journal with the given key
   */
  public Journal getJournal(String journalKey);

  /**
   * Cross publish an article in a journal, and invoke all cross-publish listeners
   *
   * @param articleDoi the doi of the article to cross publish
   * @param journalKey the key of the journal to publish in
   */
  public void crossPubArticle(String articleDoi, String journalKey) throws Exception;

  /**
   * remove an article from a journal it's cross-published in and invoke all cross-publish listeners
   *
   * @param articleDoi the doi of the article to remove
   * @param journalKey the key of the journal to remove the article from
   */
  public void removeArticleFromJournal(String articleDoi, String journalKey) throws Exception;

  /**
   * Get all the articles which are cross published in a journal
   * @param journal the journal to use in getting cross published articles
   * @return a list of dois of articles cross published in the journal
   */
  public List<String> getCrossPubbedArticles(Journal journal);

  /**
   * Set current Journal issue URI.
   *
   * @param journalKey the {@link Journal#journalKey} of the journal on which to set the current issue
   * @param issueUri   the {@link Issue#issueUri} of the issue to set as current
   */
  public void setCurrentIssue(String journalKey, String issueUri);

  /**
   * Return a Volume object specified by URI. The issues will be lazy-loaded and so not available outside of a session.
   *
   * @param volumeUri the URI of the volume.
   * @return the volume object requested.
   * @throws RuntimeException throws RuntimeException if any one of the Volume URIs supplied by the journal does not
   *                          exist.
   */
  public Volume getVolume(String volumeUri);

  /**
   * Uses the list of volume URIs maintained by the journal to create a list of Volume objects.
   *
   * @param journalName Keyname of the current journal
   * @return the list of volumes for the current journal (never null)
   * @throws RuntimeException throws RuntimeException if any one of the Volume URIs supplied by the journal does not
   *                          exist.
   */
  public List<Volume> getVolumes(String journalName);

  /**
   * Create a new Volume and add it to the current Journal's list of volumes it contains.
   *
   * @param journalName Keyname of the current journal
   * @param volumeUri   the uri of the new volume.
   * @param displayName the display name of the volume.
   * @return the volume object that was created. ( returns null if there is no journal or volumeUri already exists ).
   */
  public Volume createVolume(String journalName, String volumeUri, String displayName)
  ;

  /**
   * Remove volumes from the journal and delete them.
   *
   * @param journalKey Keyname of the current journal
   * @param volumeUris the uris of the volumes to delete
   * @return the volumeUris of the volumes that were actually deleted (any uris that didn't correspond to volumes in the
   *         journal will be omitted)
   */
  public String[] deleteVolumes(String journalKey, String... volumeUris);

  /**
   * Update a volume by uri. This only allows reordering of the issues via csv, not additions or deletions.
   *
   * @param volumeUri   the uri of the volume to update
   * @param displayName the new display name to set
   * @param issueCsv    a csv of the issue uris to set on the volume
   * @throws IllegalArgumentException if an issue already assigned to the volume does not appear in the csv, or if a new
   *                                  one has been added to it
   */
  public void updateVolume(String volumeUri, String displayName, String issueCsv)
      throws IllegalArgumentException;

  /**
   * Delete an Issue specified by URI. Remove it from each volume that references it.
   *
   * @param issueUri the uri of the issue to delete
   */
  public void deleteIssue(String issueUri);

  /**
   * Get an Issue specified by URI.
   *
   * @param issueUri the {@link Issue#issueUri} of the issue to retrieve
   * @return the Issue object specified by URI.
   */
  public Issue getIssue(String issueUri);

  /**
   * Get a list of issues from the specified volume.
   *
   * @param volumeUri the {@link Volume#volumeUri} of the volume to use to get issues
   */
  public List<Issue> getIssues(String volumeUri);

  /**
   * Format a csv string from each of the {@link Issue#issueUri}s of the issues provided
   *
   * @param issues the issues to format
   */
  public String formatIssueCsv(List<Issue> issues);

  /**
   * Add the given issue to the volume, and save it to the database. If the {@link Issue#description} and {@link
   * Issue#title} properties are not set on the issue and there is an article with doi equal to {@link Issue#imageUri},
   * the {@link Issue#description} and {@link Issue#title} will be copied from the article
   *
   *
   * @param volumeUri the uri of the volume to which the issue will be added
   * @param issue     transient issue object to add to the given volume
   */
  public void addIssueToVolume(String volumeUri, Issue issue);

  /**
   * Update an Issue
   *
   * @param issueUri     the {@link org.ambraproject.models.Issue#issueUri} of the issue to update
   * @param imageUri     the {@link org.ambraproject.models.Issue#imageUri} to set on the issue
   * @param displayName  the {@link org.ambraproject.models.Issue#displayName} to set on the issue
   * @param respectOrder respect the order manual ordering of articles within articleTypes.
   * @param articleDois  a list of article dois to set on the issue
   */
  public void updateIssue(String issueUri, String imageUri, String displayName,
                          boolean respectOrder, List<String> articleDois);

  /**
   * Remove articles from an issue
   *
   * @param issueUri    the {@link org.ambraproject.models.Issue#issueUri} of the issue to update
   * @param articleDois the dois of articles to remove from the issue
   */
  public void removeArticlesFromIssue(String issueUri, String... articleDois);

  /**
   * Add article dois to an issue. If any dois are already in the issue, they will not be added again.
   *
   * @param issueUri    the {@link Issue#issueUri} of the issue to update
   * @param articleDois the dois to add to the issue.
   */
  public void addArticlesToIssue(String issueUri, String... articleDois);

  /**
   * Get a list of the articles in an issue, broken in to groups based on {@link org.ambraproject.models.Article#types}.
   * There will be a group for each type in {@link org.ambraproject.model.article.ArticleType#getOrderedListForDisplay()}
   * and one group containing orphaned articles.
   * <p/>
   * if the issue has {@link Issue#respectOrder} set to true, the articles in each group will be ordered as they appear
   * in the issue's {@link Issue#articleDois} list. Otherwise they will be ordered with most recent articles first.
   *
   * @param issue the issue to use to retrieve articles for
   * @return all the articles in the issue, broken in to groups
   */
  public List<TOCArticleGroup> getArticleGroupList(Issue issue);

  /**
   * Create a comma-delimited string of article dois in the articles groups
   *
   * @param issueArticleGroups groups of articles to format
   * @return a comma-delimited list of the articles in groups
   */
  public String formatArticleCsv(List<TOCArticleGroup> issueArticleGroups);
}
