package org.ambraproject.admin.views;

/**
 * A view classs for role/permissions associations
 */
public class RolePermissionView {
  final String name;
  private boolean assigned;

  public RolePermissionView(final String name, final boolean assigned) {
    this.name = name;
    this.assigned = assigned;
  }

  /**
   * Get the permission name
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Is this role assigned to the current user?
   *
   * @return
   */
  public boolean getAssigned() {
    return assigned;
  }

}
