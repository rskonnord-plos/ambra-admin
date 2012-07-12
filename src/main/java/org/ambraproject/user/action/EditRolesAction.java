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

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.service.AdminRolesService;
import org.ambraproject.admin.views.UserRoleView;
import org.ambraproject.models.UserProfile;
import org.ambraproject.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Edits a user's role. User must be logged in already.
 */
public class EditRolesAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(EditRolesAction.class);
  private AdminRolesService adminRolesService;
  private UserService userService;
  private List<UserRoleView> userRoles;
  private String userAuthId;
  private String displayName;
  private String email;
  private Long[] roleIDs;

  /**
   * Do input verification and setup the role list
   *
   * @return status code from webwork
   *
   * @throws Exception Exception
   */
  public String execute() throws Exception {

    if (userAuthId == null) {
      addFieldError("authId", "authId is required");
      return INPUT;
    }

    loadCommonValues(this.userAuthId);

    return SUCCESS;
  }

  /**
   * Assign the roles passed in through the form submit
   *
   * @return status code from webwork
   *
   * @throws Exception
   */
  public String assignRoles() throws Exception {

    if (userAuthId == null) {
      addFieldError("authId", "authId is required");
      return INPUT;
    }

    UserProfile userProfile = userService.getUserByAuthId(this.userAuthId);

    //Revoke all roles and then reassign them
    this.adminRolesService.revokeAllRoles(userProfile.getID());

    if(this.roleIDs != null) {
      for(Long roleID : roleIDs) {
        this.adminRolesService.grantRole(userProfile.getID(), roleID);
      }
    }

    loadCommonValues(this.userAuthId);

    addActionMessage("Roles Updated Successfully");

    return SUCCESS;
  }

  private void loadCommonValues(String authid)
  {
    UserProfile userProfile = userService.getUserByAuthId(authid);
    this.userRoles = adminRolesService.getAllRoles(userProfile.getID());

    this.userAuthId = userProfile.getAuthId();
    this.email = userProfile.getEmail();
    this.displayName = userProfile.getDisplayName();
  }

  /**
   * Get the list of roles attached to the passed in user
   *
   * @return
   */
  public List<UserRoleView> getUserRoles()
  {
    return this.userRoles;
  }

  /**
   * Struts setter for the user Roles
   * @param roleIDs the roles to assign to the current user
   */
  public void setRoleIDs(final Long[] roleIDs) {
    this.roleIDs = roleIDs;
  }

  /**
   * Struts setter for the user's authorization ID
   * @return
   */
  public void setUserAuthId(String userAuthId)
  {
    this.userAuthId = userAuthId;
  }

  /**
   * Get the user's authorization ID
    * @return
   */
  public String getUserAuthId()
  {
    return this.userAuthId;
  }

  /**
   * Get the user's email address
   * @return
   */
  public String getEmail()
  {
    return this.email;
  }

  /**
   * Get the user's display name
   *
   * @return
   */
  public String getDisplayName()
  {
    return this.displayName;
  }


  @Required
  public void setAdminRolesService(AdminRolesService adminRolesService) {
    this.adminRolesService = adminRolesService;
  }

  @Required
  public void setUserService(UserService userService)
  {
    this.userService = userService;
  }
}
