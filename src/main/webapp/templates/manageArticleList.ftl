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
  <title>Manage Article List</title>
  <#include "includes/header.ftl">
</head>
  <body>
    <h1 style="text-align: center">Manage Article List</h1>
    <#include "includes/navigation.ftl">
    <@messages />

    <!-- create a Article List -->
    <p>
      <fieldset>
        <legend>Create Article List</legend>
        <@s.form method="post" namespace="/"  action="manageArticleList" name="createList" id="create_list" >
          <@s.hidden name="command" value="CREATE_LIST"/>

          <table border="0" cellpadding="10" cellspacing="0">
            <tr>
              <th align="center">List Code</th>
              <td><@s.textfield name="listCode" size="50" required="true"/></td>
            </tr>
            <tr>
              <th align="center">Display Name</th>
              <td><@s.textfield name="displayName" size="50" required="true"/></td>
            </tr>
          </table>
          <@s.submit align="right" value="Create"/>
        </@s.form>
      </fieldset>
    </p>

    <!-- list Existing Article List -->
    <fieldset>
      <legend>Existing Article List</legend>

      <#-- Check to see if there are article list Associated with this Journal -->
      <#if (articleList?size > 0)>
        <@s.form method="post" namespace="/" action="manageArticleList" name="removeList" id="removeList" >
          <@s.hidden name="command" value="REMOVE_LIST"/>

          <table border="1" cellpadding="10" cellspacing="0">
            <tr>
              <th>Delete</th>
              <th>Display Name</th>
              <th>listCode</th>
              <th>Update</th>
            </tr>
            <#list articleList as al>
              <tr>
                <td align="center">
                  <@s.checkbox name="listToDelete" fieldValue="${al.listCode}"/>
                  </td>
                  <td>
                  ${al.displayName!''}
                  </td>
                  <td>
                  ${al.listCode}
                  </td>
                  <td>
                    <@s.url namespace="/" action="articleManagement" listCode="${al.listCode}" id="articleManagement"/>
                    <@s.a href="${articleManagement}">Update</@s.a>
                  </td>
              </tr>
            </#list>
          </table>
          <@s.submit value="Delete Selected Article List"/>
        </@s.form>
      <#else>
        <strong>There are no article list currently associated with ${journal.journalKey}.</strong>
      </#if>
    </fieldset>
  </body>
</html>