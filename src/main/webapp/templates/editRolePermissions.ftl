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
  <title>Ambra: Administration: Edit Role Permissions</title>
<#include "includes/header.ftl">
</head>

<body>
<h1 style="text-align: center">Ambra: Administration: Edit Role Permissions</h1>
<#include "includes/navigation.ftl">

<@messages />

<fieldset>
  <legend><strong>Edit Role "${roleName}" Permissions</strong></legend>

  <@s.form action="editPermissionsAssign" namespace="/" method="post" cssClass="ambra-form"
  method="post" title="Permissions Form" name="rolePermissions">
    <@s.hidden name="roleID" />
    <#list permissionsForRole as p>
      <@s.checkbox name="permissions" label="${p.name}" fieldValue="${p.name}"
      value="${p.assigned?string}"/><br/>
    </#list>
    <br/>
    <@s.submit value="Save" />
  </@s.form>
</fieldset>

<fieldset>
  <legend><strong>Delete Role "${roleName}"</strong></legend>

  <@s.form action="deleteRole" namespace="/" method="post" cssClass="ambra-form"
      method="post" title="Delete Role Form" name="deleteRole"
      onsubmit="return confirm('Are you sure, all users will be removed from this role?');">
    <@s.hidden name="roleID" />
    <@s.submit value="Delete Role"/>
  </@s.form>
</fieldset>

<@s.a href="${manageRoles}">Role List</@s.a>

</body>
</html>