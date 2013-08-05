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

/**
 * Action class for managing news articles
 */

@SuppressWarnings("serial")
public class ManageNewsAction extends BaseAdminActionSupport {
  private final static String SAVE_ARTICLES = "SAVE_ARTICLES";

  private String articles;
  private String command;

  @Override
  public String execute() throws Exception {
    initJournal();

    if (command != null && SAVE_ARTICLES.equals(command)) {
      // write to database
      try {
        adminService.setNewsArticles(articles);
        addActionMessage("saved the articles in the news.");
      } catch (Exception ex) {
        addActionError(ex.getMessage());
      }
    }
    try {
      // read from database
      articles = adminService.getNewsArticles();
    } catch (Exception ex) {
      addActionError(ex.getMessage());
      articles = "";
    }
    return SUCCESS;
  }

  public String getArticles() {
    return articles;
  }

  public void setArticles(String articles) {
    this.articles = articles;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }
}
