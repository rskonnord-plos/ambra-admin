/*
 * Copyright (c) 2006-2013 by Public Library of Science
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

import org.ambraproject.search.SavedSearchRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Action class for resending email alerts
 *
 * @author Joe Osowski
 */
@SuppressWarnings("serial")
public class ManageEmailAlertsAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ManageEmailAlertsAction.class);

  private String startTimeStr;
  private String endTimeStr;

  private Date startDateTime;
  private Date endDateTime;

  @Override
  public String execute() throws Exception {
    // create a faux journal object for templates
    initJournal();

    return SUCCESS;
  }

  public String sendMonthlyAlerts() {
    try {
      if(parseDates()) {
        this.adminService.sendJournalAlerts(SavedSearchRetriever.AlertType.MONTHLY, this.startDateTime, this.endDateTime);

        addActionMessage("Queued Monthly Alerts to be sent.");

        return SUCCESS;
      } else {
        return INPUT;
      }
    } catch(Exception ex) {
      addActionError("Error queueing message: " + ex.getMessage());
      return ERROR;
    }
  }

  public String sendWeeklyAlerts() {
    try {
      if(parseDates()) {
        this.adminService.sendJournalAlerts(SavedSearchRetriever.AlertType.WEEKLY, this.startDateTime, this.endDateTime);

        addActionMessage("Queued Weekly Alerts to be sent.");

        return SUCCESS;
      } else {
        return INPUT;
      }
    } catch(Exception ex) {
      addActionError("Error queueing message: " + ex.getMessage());
      return ERROR;
    }
  }

  private boolean parseDates() {
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

    if(this.startTimeStr == null || this.startTimeStr.trim().length() == 0) {
      this.startDateTime = null;
    } else {
      try {
        this.startDateTime = formatter.parse(this.startTimeStr);

        addActionMessage("Start Date set to:" + this.startDateTime.toString());

      } catch(ParseException ex) {
        addActionError("Start date can not be parsed: " + ex.getMessage());
      }
    }

    if(this.endTimeStr == null || this.endTimeStr.trim().length() == 0) {
      this.endDateTime = null;
    } else {
      try {
        this.endDateTime = formatter.parse(this.endTimeStr);

        addActionMessage("End Date set to:" + this.endDateTime.toString());

      } catch(ParseException ex) {
        addActionError("End date can not be parsed: " + ex.getMessage());
      }
    }

    return (getActionErrors().size() == 0);
  }


  public void setStartTime(String startTime) {
    this.startTimeStr = startTime;
  }

  public void setEndTime(String endTime) {
    this.endTimeStr = endTime;
  }
}
