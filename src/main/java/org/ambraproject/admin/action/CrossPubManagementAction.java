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

package org.ambraproject.admin.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;

public class CrossPubManagementAction extends BaseAdminActionSupport {
  // Fields set by templates
  private String command;
  private String articlesToAdd;
  private String[] articlesToRemove;
  private List<String> dois = new ArrayList<String>();

  private static final Logger log = LoggerFactory.getLogger(CrossPubManagementAction.class);

  private enum XP_COMMANDS {
    ADD_ARTICLES,
    REMOVE_ARTICLES,
    INVALID;

    /**
     * Convert a string specifying an action to its enumerated equivalent.
     *
     * @param action string value to convert.
     * @return enumerated equivalent
     */
    public static XP_COMMANDS toCommand(String action) {
      XP_COMMANDS a;
      try {
        a = valueOf(action);
      } catch (Exception e) {
        // It's ok just return invalid.
        a = INVALID;
      }
      return a;
    }
  }

  /**
   * Main entry point for Cross Publication management action.
   */
  @Override
  public String execute() throws Exception {

    switch (XP_COMMANDS.toCommand(command)) {
      case ADD_ARTICLES: {
        addArticles();
        break;
      }
      case REMOVE_ARTICLES: {
        removeArticles();
        break;
      }
    }
    repopulate();
    return SUCCESS;
  }

  private void repopulate() {
    initJournal();
    dois = adminService.getCrossPubbedArticles(getJournal());
  }

  private void removeArticles() {
    for (String articleDoi : articlesToRemove) {
      try {
        adminService.removeArticleFromJournal(articleDoi, getCurrentJournal());
        addActionMessage("Removed article: " + articleDoi);
      } catch (Exception e) {
        log.error("Error uncrosspublishing article: " + articleDoi + " from journal: " + getCurrentJournal(), e);
        addActionError("Failed to remove " + articleDoi + ": " + e.getMessage());
      }
    }
  }

  private void addArticles() {
    for (String articleDoi : articlesToAdd.split(",")) {
      try {
        adminService.crossPubArticle(articleDoi, getCurrentJournal());
        addActionMessage("Article: " + articleDoi + " cross published in journal.");
      } catch (Exception e) {
        log.error("Error crosspublishing article: " + articleDoi + " to journal: " + getCurrentJournal(), e);
        addActionError("Failed to cross publish " + articleDoi + ": " + e.getMessage());
      }
    }
  }

  public List<String> getDois() {
    return dois;
  }

  /**
   * Set Articles to add.
   *
   * @param articlesToAdd a comma separated list of articles to add.
   */
  public void setArticlesToAdd(String articlesToAdd) {
    this.articlesToAdd = articlesToAdd;
  }

  /**
   * Set Articles to delete.
   *
   * @param articlesToRemove Array of articles to delete.
   */
  public void setArticlesToRemove(String[] articlesToRemove) {
    this.articlesToRemove = articlesToRemove;
  }

  /**
   * Sets the Action to execute.
   *
   * @param command the command to execute.
   */
  @Required
  public void setCommand(String command) {
    this.command = command;
  }

}
