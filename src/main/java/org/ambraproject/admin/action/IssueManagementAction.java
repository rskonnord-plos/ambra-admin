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

import org.ambraproject.models.Issue;
import org.ambraproject.views.TOCArticleGroup;
import org.apache.commons.lang.xwork.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class IssueManagementAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(IssueManagementAction.class);
  // Fields set by templates
  private String command;
  private String issueURI;
  private String imageURI;
  private String displayName;
  private String articlesToAddCsv;
  private boolean respectOrder = false;

  private String[] articlesToRemove;
  // Fields Used by template
  private Issue issue;
  private String articleOrderCSV;

  private List<TOCArticleGroup> articleGroups;

  public enum IM_COMMANDS {
    ADD_ARTICLE,
    REMOVE_ARTICLES,
    UPDATE_ISSUE,
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

      case UPDATE_ISSUE:
        updateIssue();
        break;

      case INVALID:
        repopulate();
        break;
    }
    return SUCCESS;
  }

  private void addArticles() {
    if (articlesToAddCsv != null) {
      try {
        adminService.addArticlesToIssue(issueURI, articlesToAddCsv.split(","));
        addActionMessage("Successfully added articles to issue");
      } catch (Exception e) {
        log.error("Failed to add article(s) '" + articlesToAddCsv + "' to issue " + issueURI, e);
        addActionMessage("Article(s) not added due to the following error: " + e.getMessage());
      }
    }
    repopulate();
  }

  private void removeArticles() {
    try {
      adminService.removeArticlesFromIssue(issueURI, articlesToRemove);
      addActionMessage("Removed the following article(s) from issue: " + Arrays.toString(articlesToRemove));
    } catch (Exception e) {
      log.error("Failed to remove articles " + Arrays.toString(articlesToRemove) + " from issue " + issueURI, e);
      addActionMessage("Article(s) not removed due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void updateIssue() {
    try {
      adminService.updateIssue(issueURI, imageURI, displayName, respectOrder, Arrays.asList(articleOrderCSV.split(",")));
      addActionMessage("Successfully updated issue " + issueURI);
    } catch (Exception e) {
      log.error("Failed to update issue '" + issueURI + "'", e);
      addActionError("Issue not updated due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void repopulate() {
    issue = adminService.getIssue(issueURI);
    articleGroups = adminService.getArticleGroupList(issue);
    articleOrderCSV = adminService.formatArticleCsv(articleGroups);
    initJournal();
  }

  public List<TOCArticleGroup> getArticleGroups() {
    return articleGroups;
  }

  public Issue getIssue() {
    return issue;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String name) {
    displayName = name;
  }

  public String getImageURI() {
    return imageURI;
  }

  public void setImageURI(String uri) {
    this.imageURI = uri;
  }

  public String getArticleOrderCSV() {
    return articleOrderCSV;
  }

  public void setArticleOrderCSV(String articleOrderCSV) {
    this.articleOrderCSV = articleOrderCSV;
  }

  public void setRespectOrder(Boolean respectOrder) {
    this.respectOrder = respectOrder;
  }

  public void setIssueURI(String issueURI) {
    this.issueURI = issueURI;
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
