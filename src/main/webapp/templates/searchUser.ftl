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
<html>
  <head>
    <title>Ambra: Administration: Manage Users</title>
    <#include "includes/header.ftl">
  </head>
  <body>
    <h1 style="text-align: center">Ambra: Administration: Manage Users</h1>
    <#include "includes/navigation.ftl">
    
    <@messages />
    <#if users?exists && users?has_content>
      <p>
        <fieldset>
          <legend><b>Edit User Profiles</b></legend>
          <ul>
            <#list users as user>
              <@s.url id="editProfileByAdminURL" action="editProfileByAdmin" namespace="/" userAuthId="${user.authId}" includeParams="none"/>
              <@s.url id="editRolesURL" action="editRoles" namespace="/" userAuthId="${user.authId}" includeParams="none"/>
              <li>
                User: {Id: <b>${user.ID}</b>; User name: <b>${user.displayName!}</b>; Email: <b>${user.email!}</b>}
                <@s.a href="%{editProfileByAdminURL}">Edit Profile</@s.a>&nbsp;
                <#if permissions?seq_contains("MANAGE_ROLES")>
                  <@s.a href="%{editRolesURL}">Edit Roles</@s.a>
                </#if>
              </li>
            </#list>
          </ul>
        </fieldset>
      </p>
    </#if>
    <p>
      <fieldset>
          <legend><b>Find User by Authorization Id</b></legend>
          <@s.form name="findUserByUserIdForm" action="findUserByAuthId" namespace="/" method="post">
            <@s.textfield name="userAuthId" label="Auth Id" requiredLabel="true"/>
            <@s.submit value="Find Auth Id" />
          </@s.form>
      </fieldset>
    </p>

    <p>
      <fieldset>
          <legend><b>Find User by Email</b></legend>
          <@s.form name="findUserByEmailAddressForm" action="findUserByEmailAddress" namespace="/" method="post">
            <@s.textfield name="emailAddress" label="Email Address" requiredLabel="true"/>
            <@s.submit value="Find User Email" />
          </@s.form>
      </fieldset>
    </p>

    <p>
      <fieldset>
          <legend><b>Find User by Name</b></legend>
          <@s.form name="findUserByNameForm" action="findUserByName" namespace="/" method="post">
            <@s.textfield name="name" label="User Name" requiredLabel="true"/>
            <@s.submit value="Find User Name" />
          </@s.form>
      </fieldset>
    </p>
  </body>
</html>
