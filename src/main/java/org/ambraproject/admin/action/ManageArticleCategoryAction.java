/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.action;

import org.ambraproject.models.ArticleCategory;
import org.ambraproject.models.Issue;
import org.ambraproject.models.Volume;
import org.ambraproject.views.TOCArticleGroup;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Arrays;
import java.util.List;

/**
 * Volumes are associated with some journals and hubs. A volume is an aggregation of of issues. Issue are aggregations
 * of articles.
 */
@SuppressWarnings("serial")
public class ManageArticleCategoryAction extends BaseAdminActionSupport {

  // Past in as parameters
  private String command;
  private String[] categoryToDelete;
  private String displayName;

  private String articlesToAddCsv;
  private boolean respectOrder = false;

  private String[] articlesToRemove;

  //Used by template
  // Fields Used by template
  private ArticleCategory articleCategory;
  private String articleOrderCSV;
  private List<ArticleCategory> articleCategories;
  private List<TOCArticleGroup> articleGroups;

  private static final Logger log = LoggerFactory.getLogger(ManageArticleCategoryAction.class);

  /**
   * Enumeration used to dispatch commands within the action.
   */
  public enum MVJ_COMMANDS {
    ADD_ARTICLE,
    REMOVE_ARTICLES,
    CREATE_CATEGORY,
    REMOVE_CATEGORY,
    UPDATE_CATEGORY,
    INVALID;

    /**
     * Convert a string specifying a command to its enumerated equivalent.
     *
     * @param command string value to convert.
     * @return enumerated equivalent
     */
    public static MVJ_COMMANDS toCommand(String command) {
      MVJ_COMMANDS a;
      try {
        a = valueOf(command);
      } catch (Exception e) {
        // It's ok just return invalid.
        a = INVALID;
      }
      return a;
    }
  }

  /**
   * Manage Journals.  Display Journals and processes all add/deletes.
   */
  @Override
  public String execute() throws Exception {

    switch (MVJ_COMMANDS.toCommand(command)) {
      case CREATE_CATEGORY:
        createCategory();
        break;

      case REMOVE_CATEGORY:
        //removeCategory();
        break;

      case UPDATE_CATEGORY:
        //updateCategory();
        break;

      case ADD_ARTICLE:
        //addArticles();
        break;

      case REMOVE_ARTICLES:
        //removeArticles();
        break;

      case INVALID:
        repopulate();
        break;
    }
    return SUCCESS;
  }

  private void createCategory() {
    try {
      ArticleCategory category = adminService.createArticleCategory(getCurrentJournal(), displayName);
      if (category != null) {
        addActionMessage("Created New Category: " + displayName);
      } else {
        addActionError("Duplicate Category: " + displayName);
      }
    } catch (Exception e) {
      log.error("Error creating category " + displayName + " for " + getCurrentJournal(), e);
      addActionError("Category not created due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void repopulate() {
    //articleCategory = adminService.getArticleCategory(getCurrentJournal());
    initJournal();
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String[] getCategoryToDelete() {
    return categoryToDelete;
  }

  public void setCategoryToDelete(String[] categoryToDelete) {
    this.categoryToDelete = categoryToDelete;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getArticlesToAddCsv() {
    return articlesToAddCsv;
  }

  public void setArticlesToAddCsv(String articlesToAddCsv) {
    this.articlesToAddCsv = articlesToAddCsv;
  }

  public boolean isRespectOrder() {
    return respectOrder;
  }

  public void setRespectOrder(boolean respectOrder) {
    this.respectOrder = respectOrder;
  }

  public String[] getArticlesToRemove() {
    return articlesToRemove;
  }

  public void setArticlesToRemove(String[] articlesToRemove) {
    this.articlesToRemove = articlesToRemove;
  }

  public ArticleCategory getArticleCategory() {
    return articleCategory;
  }

  public void setArticleCategory(ArticleCategory articleCategory) {
    this.articleCategory = articleCategory;
  }

  public String getArticleOrderCSV() {
    return articleOrderCSV;
  }

  public void setArticleOrderCSV(String articleOrderCSV) {
    this.articleOrderCSV = articleOrderCSV;
  }

  public List<ArticleCategory> getArticleCategories() {
    return articleCategories;
  }

  public void setArticleCategories(List<ArticleCategory> articleCategories) {
    this.articleCategories = articleCategories;
  }

  public List<TOCArticleGroup> getArticleGroups() {
    return articleGroups;
  }

  public void setArticleGroups(List<TOCArticleGroup> articleGroups) {
    this.articleGroups = articleGroups;
  }
}