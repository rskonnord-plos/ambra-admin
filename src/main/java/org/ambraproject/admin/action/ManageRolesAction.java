package org.ambraproject.admin.action;

import org.ambraproject.admin.service.AdminRolesService;
import org.ambraproject.admin.views.RolePermissionView;
import org.ambraproject.admin.views.UserRoleView;
import org.ambraproject.models.UserProfile;
import org.ambraproject.models.UserRole;
import org.ambraproject.service.permission.PermissionsService;
import org.ambraproject.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import java.util.List;

/**
 * Manage permissions and roles
 */
public class ManageRolesAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ManageRolesAction.class);
  private AdminRolesService adminRolesService;
  private UserService userService;
  private List<UserRoleView> userRoles;

  private Long roleID;
  private String roleName;
  private List<RolePermissionView> permissions;
  private String[] newPermissions;

  /**
   * Gets a list of roles
   *
   * @return SUCCESS
   */
  public String execute()
  {
    // create a faux journal object for templates
    initJournal();

    UserProfile userProfile = userService.getUserByAuthId(getAuthId());
    this.userRoles = adminRolesService.getAllRoles(userProfile.getID());

    return SUCCESS;
  }

  /**
   * Gets a list of permissions associated with a role
   *
   * @return
   */
  public String getRolePermissions()
  {
    execute();

    if(this.roleID != null && this.roleID > 0) {

      for(UserRoleView r : userRoles) {
        if(r.getID() == this.roleID) {
          this.roleName = r.getRoleName();
        }
      }

      this.permissions = adminRolesService.getRolePermissions(this.roleID);

      return SUCCESS;
    } else {
      return INPUT;
    }
  }

  /**
   * Associates a set of permissions with a role
   *
   * @return
   */
  public String setRolePermissions()
  {
    permissionsService.checkPermission(UserRole.Permission.MANAGE_ROLES, this.getAuthId());

    adminRolesService.setRolePermissions(this.roleID, this.newPermissions);

    this.permissions = adminRolesService.getRolePermissions(this.roleID);

    this.addActionMessage("New Permissions Saved");

    return getRolePermissions();
  }

  public String createRole()
  {
    permissionsService.checkPermission(UserRole.Permission.MANAGE_ROLES, this.getAuthId());

    if(this.roleName == null || this.roleName.length() == 0) {
      execute();
      this.addFieldError("roleName","Role Name must not be empty");

      return INPUT;
    } else {
      if(this.roleName.length() > 14) {
        execute();

        this.addFieldError("roleName","Role Name must not not be longer then 15 characters");

        return INPUT;
      }

      adminRolesService.createRole(this.roleName);

      //Load data after new role created
      execute();

      this.addActionMessage("New Role Created");

      return SUCCESS;
    }
  }

  public String deleteRole() {
    permissionsService.checkPermission(UserRole.Permission.MANAGE_ROLES, this.getAuthId());

    this.adminRolesService.deleteRole(this.roleID);

    addActionMessage("Deleted Role");

    //Load data after new role deleted
    execute();

    return SUCCESS;
  }

  public List<UserRoleView> getUserRoles()
  {
    return this.userRoles;
  }

  public List<RolePermissionView> getPermissionsForRole()
  {
    return this.permissions;
  }

  @Required
  public void setAdminRolesService(AdminRolesService adminRolesService)
  {
    this.adminRolesService = adminRolesService;
  }

  @Required
  public void setUserService(UserService userService)
  {
    this.userService = userService;
  }

  public Long getRoleID()
  {
    return this.roleID;
  }

  public void setRoleID(Long roleID)
  {
    this.roleID = roleID;
  }

  public void setPermissions(String[] permissions)
  {
    this.newPermissions = permissions;
  }

  public void setRoleName(String roleName)
  {
    this.roleName = roleName;
  }

  public String getRoleName()
  {
    return this.roleName;
  }
}
