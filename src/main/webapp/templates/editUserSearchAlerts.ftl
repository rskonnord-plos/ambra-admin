<#include "includes/globals.ftl">
<#if displayName?exists && displayName?length gt 0>
  <#assign addressingUser = displayName +"'s" >
<#else>
  <#assign addressingUser = "User" >
</#if>
<#function isFound collection value>
  <#list collection as element>
    <#if element = value>
      <#return "true">
    </#if>
  </#list>
  <#return "false">
</#function>

<html>
<head>
  <title>Ambra: Administration: Manage Users</title>
  <#include "includes/header.ftl">
  <style type="text/css" media="all"> @import "${request.contextPath}/css/edit_profile.css";</style>
  <script type="text/javascript" src="${request.contextPath}/javascript/edit_profile.js"></script>
</head>
<body>
<h1 style="text-align: center">Ambra: Administration: Edit User Search Alerts</h1>
<#include "includes/navigation.ftl">
<@messages />

<fieldset>
  <legend><strong>Edit User Profile</strong></legend>
  <div id="container" class="profile">
  <@s.url id="editProfileByAdminURL" action="editProfileByAdmin" namespace="/"
    userAuthId="${userAuthId}" includeParams="none"/>
  <@s.url id="editPreferencesByAdminURL" action="retrieveUserAlertsByAdmin" namespace="/"
    userAuthId="${userAuthId}" includeParams="none"/>
  <@s.url id="editSearchAlertsByAdminURL" action="retrieveUserSearchAlertsByAdmin" namespace="/"
    userAuthId="${userAuthId}" includeParams="none"/>
  <@s.url id="editRolesURL" action="editRoles" namespace="/"
    userAuthId="${userAuthId}" includeParams="none"/>

  <@s.a href="%{editProfileByAdminURL}">Profile</@s.a>,
  <@s.a href="%{editPreferencesByAdminURL}">Alerts/Preferences</@s.a>,
  <@s.a href="%{editSearchAlertsByAdminURL}">Search Alerts</@s.a>,
  <@s.a href="%{editRolesURL}">Roles</@s.a>

  <br/>

  <@s.form action="saveSearchAlertsByAdmin" namespace="/" method="post" cssClass="ambra-form" method="post" title="Search Alert Form" name="userSearchAlerts">
    <fieldset id="alert-form">
      <legend><strong>Manage your search alert emails</strong></legend>
        <!--show the select all check box row only if user has any search result saved -->
        <ol>
          <li>
            <#if userSearchAlerts?has_content>
              <span class="search-alerts-title">&nbsp;</span>
              <ol>
                <li class="search-alerts-weekly">
                  <label for="checkAllWeekly">
                  <input type="checkbox" value="checkAllWeekly" name="checkAllWeekly"
                    onclick="selectAllCheckboxes(this, document.userSearchAlerts.weeklyAlerts);"/> Select All
                  </label>
                </li>
                <li class="search-alerts-monthly">
                  <label for="checkAllMonthly">
                  <input type="checkbox" value="checkAllMonthly" name="checkAllMonthly"
                    onclick="selectAllCheckboxes(this, document.userSearchAlerts.monthlyAlerts);"/> Select All
                  </label>
                </li>
                <li class="search-alerts-delete">
                  <label for="checkAllDelete">
                  <input type="checkbox" value="checkAllDelete" name="checkAllDelete"
                    onclick="selectAllCheckboxes(this, document.userSearchAlerts.deleteAlerts);"/> Select All
                  </label>
                </li>
              </ol>
            </#if>
          </li>
          <#list userSearchAlerts as ua>
            <li>
              <span class="search-alerts-title">${ua.searchName}</span>
              <ol>
                <li class="search-alerts-weekly">
                  <label for="${ua.savedSearchId}">
                    <@s.checkbox name="weeklyAlerts" onclick="selectCheckboxPerCollection(this.form.checkAllWeekly, this.form.weeklyAlerts);" fieldValue="${ua.savedSearchId}" value="${ua.weekly?string}"/>
                    Weekly </label>
                </li>
                <li class="search-alerts-monthly">
                  <label for="${ua.savedSearchId}">
                    <@s.checkbox name="monthlyAlerts" onclick="selectCheckboxPerCollection(this.form.checkAllMonthly, this.form.monthlyAlerts);" fieldValue="${ua.savedSearchId}" value="${ua.monthly?string}"/>
                    Monthly </label>
                </li>
                <li class="search-alerts-delete">
                  <label for="${ua.savedSearchId}">
                    <@s.checkbox name="deleteAlerts" onclick="selectCheckboxPerCollection(this.form.checkAllDelete,
                    this.form.deleteAlerts);" fieldValue="${ua.savedSearchId}" value="false"/>
                    Delete  </label>
                </li>
              </ol>
            </li>
          </#list>
        </ol>
      <br clear="all" />
      <@s.hidden name="userAuthId"/>
      <@s.submit value="Submit" tabindex="99"/>
    </fieldset>
  </@s.form>
  </div>
</fieldset>
</body>
</html>