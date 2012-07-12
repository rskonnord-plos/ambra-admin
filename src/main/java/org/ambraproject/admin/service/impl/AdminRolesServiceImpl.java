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

package org.ambraproject.admin.service.impl;

import org.ambraproject.admin.service.AdminRolesService;
import org.ambraproject.admin.views.UserRoleView;
import org.ambraproject.models.UserRole;
import org.ambraproject.models.UserProfile;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ambraproject.service.HibernateServiceImpl;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Methods to Administer user roles
 */
public class AdminRolesServiceImpl extends HibernateServiceImpl implements AdminRolesService {
  /**
   * Get all the roles associated with a user
   *
   * @param userProfileID
   * @return
   */
  public Set<UserRoleView> getUserRoles(final Long userProfileID)
  {
    UserProfile up = (UserProfile)hibernateTemplate.load(UserProfile.class, userProfileID);

    if(up == null) {
      throw new HibernateException("Can not find user with ID: " + userProfileID);
    }

    Set<UserRoleView> results = new HashSet<UserRoleView>();

    for(UserRole ur : up.getRoles())
    {
      results.add(new UserRoleView(ur.getID(), ur.getRoleName(), true));
    }

    return results;
  }

  /**
   * Get all the available user roles.  If the passed in user has the mentioned role, it will be
   * noted as such in the view
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<UserRoleView> getAllRoles(final Long userProfileID)
  {
    return (List<UserRoleView>)hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        UserProfile profile = (UserProfile)session.load(UserProfile.class, userProfileID);
        List<Object[]> results = (List<Object[]>)session.createCriteria(UserRole.class)
          .setProjection(Projections.projectionList()
            .add(Projections.property("ID"))
            .add(Projections.property("roleName"))).list();

        List<UserRoleView> roleViews = new ArrayList<UserRoleView>();

        for(Object[] row : results) {
          boolean assigned = false;

          for(UserRole role : profile.getRoles())
          {
            if(role.getID().equals((Long)row[0])) {
              assigned = true;
              break;
            }
          }

          roleViews.add(new UserRoleView((Long)row[0], (String)row[1], assigned));
        }

        return roleViews;
      }
    });
  }

  /**
   * Revoke all the roles from the passed in userProfileID
   *
   * @param userProfileID
   */
  @SuppressWarnings("unchecked")
  public void revokeAllRoles(final Long userProfileID)
  {
    List<UserProfile> userProfiles = (List<UserProfile>)hibernateTemplate.findByCriteria(
      DetachedCriteria.forClass(UserProfile.class)
        .add(Restrictions.eq("ID", userProfileID))
        .setFetchMode("userRole", FetchMode.JOIN)
        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));

    if(userProfiles.size() == 0) {
      throw new HibernateException("Can not find user with ID: " + userProfileID);
    }

    UserProfile up = userProfiles.get(0);

    //Set roles to an empty collection
    up.setRoles(new HashSet<UserRole>());

    hibernateTemplate.update(up);
  }

  /**
   * Grant the passed in role to the passed in user
   *
   * @param userProfileID
   * @param roleId
   */
  @SuppressWarnings("unchecked")
  public void grantRole(final Long userProfileID, final Long roleId)
  {
    hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        UserProfile up = (UserProfile)session.load(UserProfile.class, userProfileID);
        UserRole ur = (UserRole)session.load(UserRole.class, roleId);

        //Add the role to the collection
        up.getRoles().add(ur);

        //Save the changes
        session.save(up);

        return null;
      }
    });
  }
}
