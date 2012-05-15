/* $HeadURL::                                                                            $
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

import org.ambraproject.models.Volume;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Volumes are associated with some journals and hubs. A volume is an aggregation of of issues. Issue are aggregations
 * of articles.
 */
@SuppressWarnings("serial")
public class ManageVirtualJournalsAction extends BaseAdminActionSupport {

  // Past in as parameters
  private String command;
  private String currentIssueUri;
  private String volumeURI;
  private String[] volsToDelete;
  private String volumeDisplayName;

  //Used by template
  private List<Volume> volumes;

  private static final Logger log = LoggerFactory.getLogger(ManageVirtualJournalsAction.class);

  /**
   * Enumeration used to dispatch commands within the action.
   */
  public enum MVJ_COMMANDS {
    UPDATE_ISSUE,
    CREATE_VOLUME,
    REMOVE_VOLUMES,
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
  @Transactional(rollbackFor = {Throwable.class})
  public String execute() throws Exception {

    switch (MVJ_COMMANDS.toCommand(command)) {
      case UPDATE_ISSUE:
        updateIssue();
        break;

      case CREATE_VOLUME:
        createVolume();
        break;

      case REMOVE_VOLUMES:
        removeVolumes();
        break;

      case INVALID:
        repopulate();
        break;
    }
    return SUCCESS;
  }

  private void updateIssue() {
    if (currentIssueUri != null) {
      try {
        adminService.setCurrentIssue(getCurrentJournal(), currentIssueUri);
        addActionMessage("Current Issue (URI) set to: " + currentIssueUri);
      } catch (IllegalArgumentException e) {
        addActionError("Issue '" + currentIssueUri + "' doesn't exist; try creating it first");
      } catch (Exception e) {
        log.error("Error setting current issue for " + getCurrentJournal() + " to " + currentIssueUri, e);
        addActionError("Current Issue not updated due to the following error: " + e.getMessage());
      }
    } else {
      addActionError("Invalid Current Issue (URI)");
    }
    repopulate();
  }

  private void createVolume() {
    try {
      Volume volume = adminService.createVolume(getCurrentJournal(), volumeURI, volumeDisplayName);
      if (volume != null) {
        addActionMessage("Created Volume: " + volumeURI);
      } else {
        addActionError("Duplicate Volume URI: " + volumeURI);
      }
    } catch (Exception e) {
      log.error("Error creating volume " + volumeURI + " for " + getCurrentJournal(), e);
      addActionError("Volume not created due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void removeVolumes() {
    try {
      if (!ArrayUtils.isEmpty(volsToDelete)) {
        String[] deletedVolumes = adminService.deleteVolumes(getCurrentJournal(), volsToDelete);
        addActionMessage("Successfully removed the following volumes: " + Arrays.toString(deletedVolumes));
      }
    } catch (Exception e) {
      log.error("Error deleting volumes: " + Arrays.toString(volsToDelete), e);
      addActionError("Volume remove failed due to the following error: " + e.getMessage());
    }
    repopulate();
  }

  private void repopulate() {
    volumes = adminService.getVolumes(getCurrentJournal());
    initJournal();
  }

  /**
   * Gets a list of Volume objects associated with the journal.
   *
   * @return list of Volume objects associated with the journals.
   */
  public List<Volume> getVolumes() {
    return volumes;
  }


  /**
   * Set volume URI.
   *
   * @param vol the volume URI.
   */
  public void setVolumeURI(String vol) {
    this.volumeURI = vol;
  }

  /**
   * Get current issue.
   *
   * @return current issue.
   */
  public String getCurIssue() {
    return currentIssueUri;
  }

  /**
   * Set current issue.
   *
   * @param currentIssueURI the current issue for this journal.
   */
  public void setCurrentIssueURI(String currentIssueURI) {
    this.currentIssueUri = currentIssueURI;
  }

  /**
   * Set volumes to delete.
   *
   * @param vols .
   */
  public void setVolsToDelete(String[] vols) {
    this.volsToDelete = vols;
  }

  /**
   * Sets the command to execute.
   *
   * @param command the command to execute for this action.
   */
  @Required
  public void setCommand(String command) {
    this.command = command;
  }

  public void setDisplayName(String volumeDisplayName) {
    this.volumeDisplayName = volumeDisplayName;
  }
}
