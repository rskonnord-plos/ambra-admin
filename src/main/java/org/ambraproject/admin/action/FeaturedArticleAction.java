/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 *   http://plos.org
 *   http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.admin.action;

import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.service.taxonomy.TaxonomyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Map;

/**
 * Action class for administering the featured articles by category
 */
public class FeaturedArticleAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(FeaturedArticleAction.class);

  private TaxonomyService taxonomyService;
  private ArticleService articleService;
  private Map<String, String> featuredArticles;
  private List<String> deleteList;
  private String doi;
  private String subjectArea;

  @Override
  public String execute() throws Exception {
    log.debug("Execute Called");

    featuredArticles = taxonomyService.getFeaturedArticles(super.getCurrentJournal());

    return SUCCESS;
  }

  public String create() throws Exception {
    log.debug("Create Called");

    featuredArticles = taxonomyService.getFeaturedArticles(super.getCurrentJournal());

    if(this.doi == null || this.doi.length() == 0) {
      addActionError("DOI must be specified");
    } else {
      try {
        articleService.getArticle(this.doi, super.getAuthId());
      } catch (NoSuchArticleIdException ex) {
        addActionError("No article found for DOI: " + this.doi);
      }
    }

    if(this.subjectArea == null || this.subjectArea.length() == 0) {
      addActionError("Subject area must be specified");
    }

    for(String key : featuredArticles.keySet()) {
      if(key.toLowerCase().equals(this.subjectArea.toLowerCase())) {
        addActionError(this.subjectArea + " already has a featured article.");
      }
    }

    if(this.hasActionErrors()) {
      return ERROR;
    }

    taxonomyService.createFeaturedArticle(super.getCurrentJournal(), this.subjectArea, this.doi, super.getAuthId());
    featuredArticles = taxonomyService.getFeaturedArticles(super.getCurrentJournal());

    addActionMessage("Featured article created for subject area '" + this.subjectArea+ "' and '" + this.doi + "'");

    return SUCCESS;
  }

  public String delete() throws Exception {
    log.debug("Delete Called");

    if(deleteList == null || deleteList.size() == 0) {
      addActionError("No subject areas specified to delete");

      featuredArticles = taxonomyService.getFeaturedArticles(super.getCurrentJournal());

      return ERROR;
    }

    for(String subjectArea : deleteList) {
      taxonomyService.deleteFeaturedArticle(super.getCurrentJournal(), subjectArea, super.getAuthId());
      addActionMessage("Featured articles for '" + subjectArea + "' removed.");
    }

    featuredArticles = taxonomyService.getFeaturedArticles(super.getCurrentJournal());

    return SUCCESS;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public void setSubjectArea(String subjectArea) {
    this.subjectArea = subjectArea;
  }

  public void setDeleteList(List<String> deleteList) {
    this.deleteList = deleteList;
  }

  public Map<String, String> getFeaturedArticles() {
    return featuredArticles;
  }

  @Required
  public void setTaxonomyService(TaxonomyService taxonomyService)
  {
    this.taxonomyService = taxonomyService;
  }

  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }
}
