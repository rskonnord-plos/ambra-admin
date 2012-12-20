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
package org.ambraproject.user.action;

import org.ambraproject.action.BaseSessionAwareActionSupport;
import org.ambraproject.models.UserProfile;
import org.ambraproject.service.user.UserAlert;
import org.ambraproject.service.user.UserService;
import org.ambraproject.views.SavedSearchView;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.ServletException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User Alerts Action that is called by the admin to update a user's alerts preferences
 * (distinct from the one that might be called by a member user to edit preferences)
 */
public class AdminUserAlertsAction extends BaseSessionAwareActionSupport {
  protected UserService userService;

  private String userAuthId;
  private String displayName;
  private String[] monthlyAlerts = new String[]{};
  private String[] weeklyAlerts = new String[]{};
  private String[] deleteAlerts = new String[]{};
  private List<SavedSearchView> savedSearches;

  public String getUserAuthId() {
    return userAuthId;
  }

  public void setUserAuthId(String userAuthId) {
    this.userAuthId = userAuthId;
  }

  public Collection<UserAlert> getUserAlerts() {
    return userService.getAvailableAlerts();
  }

  /**
   * Save the alerts.
   *
   * @return webwork status
   * @throws Exception Exception
   */
  public String saveAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    userService.setAlerts(authId, Arrays.asList(monthlyAlerts), Arrays.asList(weeklyAlerts));
    return SUCCESS;
  }

  /**
   * Retrieve the alerts for the logged in user
   *
   * @return webwork status
   * @throws Exception Exception
   */
  public String retrieveAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }

    final UserProfile user = userService.getUserByAuthId(authId);
    final List<String> monthlyAlertsList = user.getMonthlyAlerts();
    final List<String> weeklyAlertsList = user.getWeeklyAlerts();

    monthlyAlerts = monthlyAlertsList.toArray(new String[monthlyAlertsList.size()]);
    weeklyAlerts = weeklyAlertsList.toArray(new String[weeklyAlertsList.size()]);
    displayName = user.getDisplayName();

    return SUCCESS;
  }

  public Collection<SavedSearchView> getUserSearchAlerts() {
    return savedSearches;
  }

  /**
   * save the user search alerts
   * @return webwork status
   * @throws Exception
   */
  public String saveSearchAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    userService.setSavedSearchAlerts(authId, Arrays.asList(monthlyAlerts), Arrays.asList(weeklyAlerts), Arrays.asList(deleteAlerts));
    return SUCCESS;
  }

  public String retrieveSearchAlerts() throws Exception {
    final String authId = getUserAuthId();
    if (authId == null) {
      throw new ServletException("Unable to resolve ambra user");
    }
    final UserProfile user = userService.getUserByAuthId(authId);
    savedSearches = userService.getSavedSearches(user.getID());

    return SUCCESS;
  }

  /**
   * @return categories that have monthly alerts
   */
  public String[] getMonthlyAlerts() {
    return monthlyAlerts;
  }

  /**
   * Set the categories that have monthly alerts
   *
   * @param monthlyAlerts monthlyAlerts
   */
  public void setMonthlyAlerts(final String[] monthlyAlerts) {
    this.monthlyAlerts = monthlyAlerts;
  }

  /**
   * @return weekly alert categories
   */
  public String[] getWeeklyAlerts() {
    return weeklyAlerts;
  }

  /**
   * Set weekly alert categories
   *
   * @param weeklyAlerts weeklyAlerts
   */
  public void setWeeklyAlerts(String[] weeklyAlerts) {
    this.weeklyAlerts = weeklyAlerts;
  }

  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName The displayName to set.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * @param userService the userService to use
   */
  @Required
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public String[] getDeleteAlerts() {
    return deleteAlerts;
  }

  public void setDeleteAlerts(String[] deleteAlerts) {
    this.deleteAlerts = deleteAlerts;
  }
}
