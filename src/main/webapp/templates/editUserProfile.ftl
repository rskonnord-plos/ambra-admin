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
  <#assign addressingUser = displayName >
<#else>
  <#assign addressingUser = "User" >
</#if>
<#if orgVisibility == "public" >
  <#assign checkedValue = true>
<#else>
  <#assign checkedValue = false>
</#if>
<html>
  <head>
    <title>Ambra: Administration: Manage Users</title>
    <#include "includes/header.ftl">
    <style type="text/css" media="all"> @import "${request.contextPath}/css/edit_profile.css";</style>
    <script type="text/javascript" src="${request.contextPath}/javascript/edit_profile.js"></script>
  </head>
  <body>
    <h1 style="text-align: center">Ambra: Administration: Edit User Profile</h1>
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
        <#if permissions?seq_contains("MANAGE_ROLES")>
          <@s.a href="%{editRolesURL}">Roles</@s.a>
        </#if>

        <br/><br/>

        Display Name: <b>${displayName!"None"}</b>
        <@s.form name="userForm" id="userForm" action="saveProfileByAdmin" namespace="/"
        method="post" title="User Information Form" cssClass="ambra-form" enctype="multipart/form-data">
          <@s.action name="selectList" namespace="/" id="selectList"/>
          <fieldset>
            <legend><b>Public Information</b> <span class="note">( <span class="required">*</span> are required)</span></legend>
            <ol>
              <#--Store email and alerts journals so they can be passed back to the action when we do a save-->
              <@s.hidden name="email"/>
              <@s.hidden name="alertsJournals"/>

              <#if displayName?has_content && !showDisplayName>
              <#--store the display name on the page so that it gets set on the action when we go back to save-->
                <@s.hidden name="displayName"/>
              <#else>
                <!--after="(Usernames are <strong>permanent</strong> and must be between 4 and 18 characters)"-->
                  <@s.textfield name="displayName" label="Username" requiredLabel="true" tabindex="1" maxlength="18" after="(Usernames are <strong>permanent</strong> and must be between 4 and 18 characters)" />
              </#if>
              <@s.select label="Title" name="title" value="title" col=true list="%{#selectList.allTitles}" tabindex="10" />
              <@s.textfield name="givenNames" label="First Name" col=true requiredLabel="true" tabindex="2" />
              <@s.textfield name="surnames" label="Last Name" col=true requiredLabel="true" tabindex="3"/>
              <@s.textfield name="city" label="City" col=true row=true tabindex="4"/>
              <@s.select label="Country" name="country" value="country" list="%{#selectList.get('countries')}" tabindex="5" col=true />
              <@s.textarea name="biographyText" label="Short Biography" cssClass="long-input" rows="5" cols="50" tabindex="14"/>
              <@s.textfield name="researchAreasText" label="Research Areas" col=true row=true tabindex="15" />
              <@s.textfield name="interestsText" label="Other Interests" col=true tabindex="16" />
              <@s.textfield name="homePage" label="Home page" col=true row=true tabindex="17" />
              <@s.textfield name="weblog" label="Blog URL" col=true tabindex="18" />
            </ol>
          </fieldset>
          <fieldset>
            <legend><b>Additional Information</b></legend>
            <ol>
              <@s.select label="Organization Type" name="organizationType" value="organizationType" list="%{#selectList.allOrganizationTypes}" tabindex="9" col=true />
              <@s.textfield name="organizationName" label="Organization Name" cols="50" col=true tabindex="11" />
              <@s.textarea name="postalAddress" col=true row=true label="Organization Address" rows="5" cols="50" tabindex="6" />
              <@s.select label="Your Role" name="positionType" value="positionType" list="%{#selectList.allPositionTypes}" col=true tabindex="11" />
            </ol>
            <fieldset class="public-private">
              <#if checkedValue>
                <input type="checkbox" name="orgVisibility" id="orgVisibility" value="public" onclick="changeHidden();"
                       checked/>
              <#else>
                <input type="checkbox" name="orgVisibility" id="orgVisibility" value="private" onclick="changeHidden();"/>
              </#if>
              <label for="orgVisibility">Display ${addressingUser}'s Additional Information publicly.</label>
            </fieldset>
            <@s.hidden name="userAuthId"/>
            <@s.submit value="Submit" tabindex="99"/>
          </fieldset>
        </@s.form>
      </div>
    </fieldset>
  </body>
</html>
