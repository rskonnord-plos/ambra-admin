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
package org.ambraproject.user.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ambraproject.ApplicationException;
import org.springframework.beans.factory.annotation.Required;

/**
 * Creates a new admin user in Topaz. User must be logged in already.
 */
public class AssignAdminRoleAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(AssignAdminRoleAction.class);
  private Long userId;
  private AdminService adminService;

  /**
   * Assign the given userId an admin role.
   * @return status code from webwork
   * @throws Exception Exception
   */
  public String execute() throws Exception {
    if (userId == null) {
      addFieldError("userId", "Id is required");
      return INPUT;
    }
    try {
      adminService.assignAdminRole(userId);
      addActionMessage("Assigned Admin Role to user with id: " + userId);
      //clear the user id so it doesn't show up in the form
      userId = null;
    } catch (ApplicationException e) {
      log.error("Error assigning admin role", e);
      addActionError("Error assigning admin role; " + getMessage(e));
    }
    return SUCCESS;
  }

  private String getMessage(Exception e) {
    String message = e.getMessage();
    Throwable cause = e.getCause();
    while (cause != null) {
      message += ("; " + cause.getMessage());
      cause = cause.getCause();
    }
    return message;
  }

  /**
   * Struts setter for userId.
   * 
   * @param userId Value to set for userId.
   */
  public void setUserId(final Long userId) {
    this.userId = userId;
  }

  /**
   * Struts getter for userId.
   *
   * @return Topaz Id.
   */
  public Long getUserId() {
    return userId;
  }

  @Required
  public void setAdminService(AdminService adminService) {
    this.adminService = adminService;
  }
}
