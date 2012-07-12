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


package org.ambraproject.admin.service;

import org.ambraproject.admin.views.UserRoleView;
import java.util.List;
import java.util.Set;

/**
 * Methods to Administer user roles
 */
public interface AdminRolesService {
  /**
   * Get all the roles associated with a user
   * @param userProfileID
   * @return
   */
  public Set<UserRoleView> getUserRoles(final Long userProfileID);

  /**
   * Get all the available user roles.  If the passed in user has the mentioned role, it will be
   * noted as such in the view
   * @return
   */
  public List<UserRoleView> getAllRoles(final Long userProfileID);

  /**
   * Revoke all the roles from the passed in userID
   * @param userProfileID
   */
  public void revokeAllRoles(final Long userProfileID);

  /**
   * Grant the passed in role to the passed in user
   * @param userProfileID
   * @param roleId
   */
  public void grantRole(final Long userProfileID, final Long roleId);
}
