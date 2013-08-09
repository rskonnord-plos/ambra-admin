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

import org.ambraproject.models.ArticleList;
import org.ambraproject.views.TOCArticleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Article List are associated with some journals. A article list is an aggregation of articles.
 */
@SuppressWarnings("serial")
public class ManageArticleListAction extends BaseAdminActionSupport {

  // Past in as parameters
  private String command;
  private String[] listToDelete;
  private String displayName;
  private String listCode;

  private String articlesToAddCsv;
  private boolean respectOrder = false;

  private String[] articlesToRemove;

  // Fields Used by template
  private List<ArticleList> articleList;
  private String articleOrderCSV;
  private List<TOCArticleGroup> articleGroups;

  private static final Logger log = LoggerFactory.getLogger(ManageArticleListAction.class);

  /**
   * Enumeration used to dispatch commands within the action.
   */
  public enum MVJ_COMMANDS {
    ADD_ARTICLE,
    REMOVE_ARTICLES,
    CREATE_LIST,
    REMOVE_LIST,
    UPDATE_LIST,
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
      case CREATE_LIST:
        createArticleList();
        break;

      case REMOVE_LIST:
        //removeArticleList();
        break;

      case UPDATE_LIST:
        //updateArticleList();
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

  private void createArticleList() {
    try {
      ArticleList article = adminService.createArticleList(getCurrentJournal(), listCode,  displayName);
      if (article != null) {
        addActionMessage("Created New Article List: " + displayName);
      } else {
        addActionError("Duplicate Article List: " + displayName);
      }
    } catch (Exception e) {
      log.error("Error creating article list " + displayName + " for " + getCurrentJournal(), e);
      addActionError("Article list not created due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void repopulate() {
    articleList = adminService.getArticleList(getCurrentJournal());
    initJournal();
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String[] getListToDelete() {
    return listToDelete;
  }

  public void setListToDelete(String[] listToDelete) {
    this.listToDelete = listToDelete;
  }

  public String getListCode() {
    return listCode;
  }

  public void setListCode(String listCode) {
    this.listCode = listCode;
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

  public List<ArticleList> getArticleList() {
    return articleList;
  }

  public void setArticleList(List<ArticleList> articleList) {
    this.articleList = articleList;
  }

  public String getArticleOrderCSV() {
    return articleOrderCSV;
  }

  public void setArticleOrderCSV(String articleOrderCSV) {
    this.articleOrderCSV = articleOrderCSV;
  }

  public List<TOCArticleGroup> getArticleGroups() {
    return articleGroups;
  }

  public void setArticleGroups(List<TOCArticleGroup> articleGroups) {
    this.articleGroups = articleGroups;
  }
}