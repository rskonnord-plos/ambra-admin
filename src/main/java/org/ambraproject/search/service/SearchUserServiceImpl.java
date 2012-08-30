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
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Simple implementation of {@link SearchUserService} that uses SQL like restrictions to find users
 *
 * @author Alex Kudlick 2/17/12
 */
public class SearchUserServiceImpl extends HibernateServiceImpl implements SearchUserService {

  private static final Logger log = LoggerFactory.getLogger(SearchUserServiceImpl.class);

  @Override
  @SuppressWarnings("unchecked")
  public List<UserProfile> findUsersByDisplayName(String displayName) {
    log.debug("Searching for users with display name like: {}", displayName);
    //if there's an exact match, return that
    List<UserProfile> matchingUsers = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(UserProfile.class)
            .add(Restrictions.eq("displayName", displayName)),
        0, 1);
    if (!matchingUsers.isEmpty()) {
      log.debug("Found user exact match for display name: {}", displayName);
      return matchingUsers;
    } else {
      return hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(UserProfile.class)
              .add(Restrictions.ilike("displayName", displayName, MatchMode.ANYWHERE))
      );
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserProfile> findUsersByEmail(String email) {
    log.debug("Searching for users with email like {}", email);

    List<UserProfile> matchingUsers = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(UserProfile.class)
            .add(Restrictions.eq("email", email)),
        0, 1);
    if (!matchingUsers.isEmpty()) {
      log.debug("found exact match for email: {}", email);
      return matchingUsers;
    } else {
      return hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(UserProfile.class)
              .add(Restrictions.ilike("email", email, MatchMode.ANYWHERE)));
    }
  }
}
