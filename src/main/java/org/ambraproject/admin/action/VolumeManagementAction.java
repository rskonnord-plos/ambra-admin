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
import org.ambraproject.models.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 */
public class VolumeManagementAction extends BaseAdminActionSupport {

  // Fields set by templates
  private String command;
  private String volumeURI;
  private String issueURI;
  private String displayName;
  private String imageURI;
  private String[] issuesToDelete;

  // Fields used by template
  private String issuesCSV;
  private Volume volume;
  private List<Issue> issues;

  private static final Logger log = LoggerFactory.getLogger(VolumeManagementAction.class);

  /**
   *
   */
  private enum VM_COMMANDS {
    UPDATE_VOLUME,
    CREATE_ISSUE,
    REMOVE_ISSUES,
    INVALID;

    /**
     * Convert a string specifying an action to its enumerated equivalent.
     *
     * @param action string value to convert.
     * @return enumerated equivalent
     */
    public static VM_COMMANDS toCommand(String action) {
      VM_COMMANDS a;
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
   * Main entry porint for Volume management action.
   */
  @Override
  @Transactional(rollbackFor = {Throwable.class})
  public String execute() throws Exception {
    // Dispatch on hidden field command
    switch (VM_COMMANDS.toCommand(command)) {
      case CREATE_ISSUE:
        createIssue();
        break;

      case UPDATE_VOLUME:
        updateVolume();
        break;

      case REMOVE_ISSUES:
        removeIssues();
        break;

      case INVALID:
        repopulate();
        break;
    }
    return SUCCESS;
  }

  private void createIssue() {
    try {
      Issue issue = new Issue(issueURI);
      issue.setImageUri(imageURI);
      issue.setDisplayName(displayName);
      adminService.addIssueToVolume(volumeURI, issue);
      addActionMessage("Created Issue: " + issueURI);
    } catch (Exception e) {
      log.error("Error creating issue " + issueURI + " for volume " + volumeURI, e);
      addActionError("Issue not created due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void updateVolume() {
    try {
      adminService.updateVolume(volumeURI, displayName, issuesCSV);
      addActionMessage("Successfully updated volume " + volumeURI);
    } catch (Exception e) {
      log.error("Failed to update volume " + volumeURI, e);
      addActionError("Volume was not updated due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void removeIssues() {
    for (String issueUri : issuesToDelete) {
      try {
        adminService.deleteIssue(issueUri);
        addActionMessage("Deleted issue " + issueUri);
      } catch (Exception e) {
        log.error("Failed to delete issue " + issueUri, e);
        addActionError("Issue '" + issueUri + "' not removed due to the following error: " + e.getMessage());
      }
    }
    repopulate();
  }

  private void repopulate() {
    // Re-populate fields for template
    volume = adminService.getVolume(volumeURI);
    issues = adminService.getIssues(volumeURI);
    issuesCSV = adminService.formatIssueCsv(issues);
    initJournal();
  }

  public List<Issue> getIssues() {
    return issues;
  }

  public String getIssuesCSV() {
    return issuesCSV;
  }

  public Volume getVolume() {
    return volume;
  }

  public void setVolumeURI(String volumeURI) {
    this.volumeURI = volumeURI.trim();
  }

  public void setIssueURI(String issueURI) {
    this.issueURI = issueURI.trim();
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName.trim();
  }

  public void setImageURI(String imageUri) {
    this.imageURI = imageUri;
  }

  public void setIssuesToDelete(String[] issues) {
    this.issuesToDelete = issues;
  }

  public void setIssuesCSV(String issuesCSV) {
    this.issuesCSV = issuesCSV;
  }

  public void setCommand(String command) {
    this.command = command;
  }

}
