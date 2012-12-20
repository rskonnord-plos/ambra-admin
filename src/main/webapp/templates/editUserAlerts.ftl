<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2010 by Public Library of Science
  http://plos.org
  http://ambraproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
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
    <h1 style="text-align: center">Ambra: Administration: Edit User Alerts</h1>
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

        <@s.form action="saveAlertsByAdmin" namespace="/" method="post" cssClass="ambra-form"
        method="post" title="Alert Form" name="userAlerts">
          <fieldset id="alert-form">
            <legend><strong>Email Alerts</strong></legend>
            <ol>
              <li>
                <span class="alerts-title">&nbsp;</span>
                <ol>
                  <li class="alerts-weekly">
                    <label for="checkAllWeekly">
                      <input type="checkbox" value="checkAllWeekly" name="checkAllWeekly"
                             onclick="selectAllCheckboxes(this, document.userAlerts.weeklyAlerts);"/> Select All
                    </label>
                  </li>
                  <li>
                    <label for="checkAllMonthly">
                      <input type="checkbox" value="checkAllMonthly" name="checkAllMonthly"
                             onclick="selectAllCheckboxes(this, document.userAlerts.monthlyAlerts);"/> Select All
                    </label>
                  </li>
                </ol>
              </li>
              <#list userAlerts as ua>
              <li>
                <span class="alerts-title">${ua.name}</span>
                <ol>
                  <li class="alerts-weekly">
                    <#if ua.weeklyAvailable>
                      <label for="${ua.key}">
                        <@s.checkbox name="weeklyAlerts" onclick="selectCheckboxPerCollection(this.form.checkAllWeekly, this.form.weeklyAlerts);" fieldValue="${ua.key}" value="${isFound(weeklyAlerts, ua.key)}"/>
                        Weekly </label>
                    </#if>
                  </li>

                  <li>
                    <#if ua.monthlyAvailable>
                      <label for="${ua.key}">
                        <@s.checkbox name="monthlyAlerts" onclick="selectCheckboxPerCollection(this.form.checkAllMonthly, this.form.monthlyAlerts);"  fieldValue="${ua.key}" value="${isFound(monthlyAlerts, ua.key)}"/>
                        Monthly </label>
                    <#else>
                    </#if>
                  </li>
                </ol>
              </#list>
            </li>

            </ol>
            <br clear="all"/>

            <@s.hidden name="userAuthId"/>
            <@s.submit value="Submit" tabindex="99"/>

          </fieldset>
        </@s.form>
      </div>
    </fieldset>
  </body>
</html>