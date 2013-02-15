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

package org.ambraproject.admin.service.impl;

import org.ambraproject.admin.service.AdminRolesService;
import org.ambraproject.admin.views.RolePermissionView;
import org.ambraproject.admin.views.UserRoleView;
import org.ambraproject.models.UserRole;
import org.ambraproject.models.UserProfile;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.permission.PermissionsService;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * Methods to Administer user roles
 */
public class AdminRolesServiceImpl extends HibernateServiceImpl implements AdminRolesService {
  private PermissionsService permissionsService;
  /**
   * Get all the roles associated with a user
   *
   * @param userProfileID
   *
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

    this.permissionsService.clearCache();
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

    this.permissionsService.clearCache();
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  public Long createRole(final String roleName)
  {
    return (Long)hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        UserRole userRole = new UserRole(roleName, null);

        //Add the role to the collection
        session.save(userRole);

        return userRole.getID();
      }
    });
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  public void deleteRole(final Long roleId) {
    hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
      UserRole ur = (UserRole)session.load(UserRole.class, roleId);

      /*
       If there is a way to do this with hibernate, I am all ears, I played with it a bit
       but didn't want it to be a timesink
      */
      session.createSQLQuery("delete from userProfileRoleJoinTable where " +
        "userRoleID = " + roleId).executeUpdate();

      session.delete(ur);

      return null;
      }
    });

    this.permissionsService.clearCache();
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  public List<RolePermissionView> getRolePermissions(final Long roleId)
  {
    Set<UserRole.Permission> permissions = (Set<UserRole.Permission>)hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        UserRole ur = (UserRole)session.load(UserRole.class, roleId);

        return ur.getPermissions();
      }
    });

    List<RolePermissionView> results = new ArrayList<RolePermissionView>();

    for(UserRole.Permission p : UserRole.Permission.values()) {
      if(permissions.contains(p)) {
        results.add(new RolePermissionView(p.toString(), true));
      } else {
        results.add(new RolePermissionView(p.toString(), false));
      }
    }

    return results;
  }

  /**
   * @inheritDoc
   */
  @SuppressWarnings("unchecked")
  public void setRolePermissions(final Long roleId, final String[] permissions)
  {
    hibernateTemplate.execute(new HibernateCallback()
    {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Set<UserRole.Permission> newPerms = new HashSet<UserRole.Permission>(permissions.length);

        for(String p : permissions) {
          newPerms.add(UserRole.Permission.valueOf(p));
        }

        UserRole ur = (UserRole)session.load(UserRole.class, roleId);
        ur.setPermissions(newPerms);
        session.save(ur);

        return null;
      }
    });

    this.permissionsService.clearCache();
  }

  /**
   * Sets the PermissionsService.
   *
   * @param permService The PermissionsService to set.
   */
  @Required
  public void setPermissionsService(PermissionsService permService) {
    this.permissionsService = permService;
  }
}
