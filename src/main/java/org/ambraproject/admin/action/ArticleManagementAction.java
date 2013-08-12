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
import org.ambraproject.views.article.ArticleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Manage list of articles in a article list
 */
public class ArticleManagementAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ArticleManagementAction.class);
  // Fields set by templates
  private String command;
  private String listCode;
  private String displayName;
  private String articlesToAddCsv;
  private String[] articlesToRemove;

  // Fields Used by template
  private ArticleList articleList;
  private String articleOrderCSV;
  private List<ArticleInfo> articleInfoList;

  public enum IM_COMMANDS {
    ADD_ARTICLE,
    REMOVE_ARTICLES,
    UPDATE_LIST,
    INVALID;

    public static IM_COMMANDS toCommand(String command) {
      IM_COMMANDS a;
      try {
        a = valueOf(command);
      } catch (Exception e) {
        // It's ok just return invalid.
        a = INVALID;
      }
      return a;
    }
  }

  @Override
  public String execute() throws Exception {

    switch (IM_COMMANDS.toCommand(command)) {
      case ADD_ARTICLE:
        addArticles();
        break;

      case REMOVE_ARTICLES:
        removeArticles();
        break;

      case UPDATE_LIST:
        updateList();
        break;

      case INVALID:
        repopulate();
        break;
    }
    return SUCCESS;
  }

  /**
   * add articles to article list
   */
  private void addArticles() {
    if (articlesToAddCsv != null) {
      try {
        adminService.addArticlesToList(listCode, articlesToAddCsv.split(","));
        addActionMessage("Successfully added articles to list");
      } catch (Exception e) {
        log.error("Failed to add article(s) '" + articlesToAddCsv + "' to list " + listCode, e);
        addActionMessage("Article(s) not added due to the following error: " + e.getMessage());
      }
    }
    repopulate();
  }

  /**
   * remove articles from article list
   */
  private void removeArticles() {
    try {
      adminService.removeArticlesFromList(listCode, articlesToRemove);
      addActionMessage("Removed the following article(s) from list: " + Arrays.toString(articlesToRemove));
    } catch (Exception e) {
      log.error("Failed to remove articles " + Arrays.toString(articlesToRemove) + " from list " + listCode, e);
      addActionMessage("Article(s) not removed due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  /**
   * update order of articles in article list
   */
  private void updateList() {
    try {
      adminService.updateList(listCode, displayName, Arrays.asList(articleOrderCSV.split(",")));
      addActionMessage("Successfully updated list " + listCode);
    } catch (Exception e) {
      log.error("Failed to update list '" + listCode + "'", e);
      addActionError("Article List not updated due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void repopulate() {
    articleList = adminService.getList(listCode);
    articleInfoList = adminService.getArticleList(articleList);
    articleOrderCSV = adminService.formatArticleInfoCsv(articleInfoList);
    initJournal();
  }

  public String getListCode() {
    return listCode;
  }

  public void setListCode(String listCode) {
    this.listCode = listCode;
  }

  public ArticleList getArticleList() {
    return articleList;
  }

  public void setArticleList(ArticleList articleList) {
    this.articleList = articleList;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String name) {
    displayName = name;
  }

  public List<ArticleInfo> getArticleInfoList() {
    return articleInfoList;
  }

  public void setArticleInfoList(List<ArticleInfo> articleInfoList) {
    this.articleInfoList = articleInfoList;
  }

  public String getArticleOrderCSV() {
    return articleOrderCSV;
  }

  public void setArticleOrderCSV(String articleOrderCSV) {
    this.articleOrderCSV = articleOrderCSV;
  }

  public void setArticlesToAddCsv(String articlesToAddCsv) {
    this.articlesToAddCsv = articlesToAddCsv;
  }

  public void setArticlesToRemove(String[] articlesToRemove) {
    if (articlesToRemove != null) {
      this.articlesToRemove = articlesToRemove.clone();
    } else {
      this.articlesToRemove = new String[0];
    }
  }

  public void setCommand(String command) {
    this.command = command;
  }
}