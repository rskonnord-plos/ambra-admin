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

package org.ambraproject.search.service;

import org.ambraproject.models.UserProfile;

import java.util.List;

/**
 * Service for executing fuzzy searches against users - e.g. if you don't know the complete display name
 *
 * @author Alex Kudlick 2/17/12
 */
public interface SearchUserService {

  /**
   * Find users with a display name matching the one given. If there is an exact match, only one user should be returned.
   *
   * @param displayName the display name to use in searching
   * @return users with a similar display name
   */
  public List<UserProfile> findUsersByDisplayName(String displayName);

  /**
   * find users matching the given email string.  If there is an exact match, only one user should be returned
   *
   * @param email the email to use in searching. e.g. <var>"test@plos.org"</var>, <var>"test"</var>, <var>"@plos.org"</var>, etc.
   * @return a list of users with a matching email
   */
  public List<UserProfile> findUsersByEmail(String email);

}
