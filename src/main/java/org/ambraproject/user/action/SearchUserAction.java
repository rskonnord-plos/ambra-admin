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

import org.ambraproject.admin.service.AdminService;
import org.ambraproject.models.Journal;
import org.ambraproject.models.UserProfile;
import org.ambraproject.search.service.SearchUserService;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Search a user based on a criteria
 */
public class SearchUserAction extends UserActionSupport {

  private String userAuthId;
  private String accountId;
  private String emailAddress;
  private String name;
  private UserProfile[] users;

  private AdminService adminService;
  private SearchUserService searchUserService;
  // Fields Used by template
  private Journal journal;

  /**
   * Just display search page.
   *
   * @return webwork status
   */
  @Override
  public String execute() {
    journal = adminService.getJournal(getCurrentJournal());
    return SUCCESS;
  }

  /**
   * Find user with a given auth id
   *
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String executeFindUserByAuthId() throws Exception {
    // create a faux journal object for template
    journal = adminService.getJournal(getCurrentJournal());

    final UserProfile user = userService.getUserByAuthId(userAuthId);
    if (null == user) {
      addActionError("No user for the given auth id");
      return INPUT;
    }
    users = new UserProfile[]{user};

    return SUCCESS;
  }

  /**
   * Find user with a given account id
   *
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String executeFindUserByAccountId() throws Exception {
    // create a faux journal object for template
    journal = adminService.getJournal(getCurrentJournal());

    final UserProfile userId = userService.getUserByAccountUri(accountId);
    if (null == userId) {
      addActionError("No user found with the accounid:" + accountId);
      return INPUT;
    }
    users = new UserProfile[]{userId};

    return SUCCESS;
  }

  /**
   * Find user with a given display name
   *
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String executeFindUserByName() throws Exception {
    // create a faux journal object for template
    journal = adminService.getJournal(getCurrentJournal());

    final List<UserProfile> userList = searchUserService.findUsersByDisplayName(name);
    if (userList.isEmpty()) {
      addActionError("No user(s) found with the username:" + name);
      return INPUT;
    }
    users = userList.toArray(new UserProfile[userList.size()]);

    return SUCCESS;
  }

  /**
   * Find user with a given email address
   *
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String executeFindUserByEmailAddress() throws Exception {
    // create a faux journal object for template
    journal = adminService.getJournal(getCurrentJournal());

    final List<UserProfile> userList = searchUserService.findUsersByEmail(emailAddress);
    if (userList.isEmpty()) {
      addActionError("No user(s) found with the email:" + emailAddress);
      return INPUT;
    }
    users = userList.toArray(new UserProfile[userList.size()]);

    return SUCCESS;
  }

  /**
   * Setter for userAuthId.
   *
   * @param userAuthId Value to set for userAuthId.
   */
  public void setUserAuthId(final String userAuthId) {
    this.userAuthId = userAuthId;
  }

  /**
   * Setter for accountId.
   *
   * @param accountId Value to set for accountId.
   */
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  /**
   * Setter for emailAddress.
   *
   * @param emailAddress Value to set for emailAddress.
   */
  public void setEmailAddress(final String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * Setter for displayName
   *
   * @param name Value to set for displayName.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Getter for users.
   *
   * @return Value of users.
   */
  public UserProfile[] getUsers() {
    return users;
  }

  public Journal getJournal() {
    return journal;
  }

  /**
   * @param adminService Admin service
   */
  @Required
  public void setAdminService(AdminService adminService) {
    this.adminService = adminService;
  }

  @Required
  public void setSearchUserService(SearchUserService searchUserService) {
    this.searchUserService = searchUserService;
  }
}
