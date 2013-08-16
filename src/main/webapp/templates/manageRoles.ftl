<#--
  $HeadURL::                                                                            $
  $Id$

  Copyright (c) 2007-2013 by Public Library of Science
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
  <title>Ambra: Administration: Manage Roles / Permissions</title>
<#include "includes/header.ftl">
</head>
<body>
<h1 style="text-align: center">Ambra: Administration: Manage Roles / Permissions</h1>
<#include "includes/navigation.ftl">

<@messages />

<fieldset>
  <legend>User Roles</legend>
  <p>
    <#list userRoles as role>
      <@s.url id="editRoleURL" namespace="/admin" action="getRolePermissions"
      roleID="${role.ID}" />
      <@s.a href="${editRoleURL}">${role.roleName}</@s.a><br/>
    </#list>
  </p>
</fieldset>

<@s.form action="createRole" namespace="/" method="post" cssClass="ambra-form"
method="post" title="Create Role Form" name="createRole">
<fieldset>
  <legend>Create new Role</legend>
  <p>
    <@s.textfield name="roleName" label="Role Name" requiredLabel="true" /><br/>
    <@s.submit value="Create Role" />
  </p>
</fieldset>
</@s.form>

</body>
</html>
