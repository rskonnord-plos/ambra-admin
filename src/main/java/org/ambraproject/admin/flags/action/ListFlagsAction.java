/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.flags.action;

import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.flags.service.FlagService;
import org.ambraproject.admin.views.FlagView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;


@SuppressWarnings("serial")
public class ListFlagsAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ListFlagsAction.class);

  private Collection<FlagView> flaggedComments;
  private FlagService flagService;

  @Override
  public String execute() throws Exception {
    // create a faux journal object for template
    initJournal();
    // catch all Exceptions to keep Admin console active (vs. Site Error)
    try {
      flaggedComments = flagService.getFlaggedComments();
      return SUCCESS;
    } catch (Exception e) {
      log.error("Admin console Exception", e);
      addActionError("Exception: " + e);
      return ERROR;
    }
  }

  public Collection<FlagView> getFlaggedComments() {
    return flaggedComments;
  }

  @Required
  public void setFlagService(FlagService flagService) {
    this.flagService = flagService;
  }
}
