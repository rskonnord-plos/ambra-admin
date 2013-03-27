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

/**
 * Action class for resending email alerts
 *
 * @author Joe Osowski
 */
@SuppressWarnings("serial")
public class ManageEmailAlertsAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ManageEmailAlertsAction.class);

  @SuppressWarnings("unchecked")
  @Override
  public String execute() throws Exception {
    // create a faux journal object for template
    initJournal();

    return SUCCESS;
  }

  public String sendMonthlyAlerts() {
    this.adminService.sendJournalAlerts(SavedSearchRetriever.AlertType.MONTHLY);

    addActionMessage("Queued Monthly Alerts to be sent.");

    return SUCCESS;
  }

  public String sendWeeklyAlerts() {
    this.adminService.sendJournalAlerts(SavedSearchRetriever.AlertType.WEEKLY);

    addActionMessage("Queued Weekly Alerts to be sent.");

    return SUCCESS;
  }
}
