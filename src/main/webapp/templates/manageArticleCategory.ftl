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
  <title>Manage Article Categories</title>
  <#include "includes/header.ftl">
</head>
  <body>
    <h1 style="text-align: center">Manage Article Categories</h1>
    <#include "includes/navigation.ftl">
    <@messages />

    <!-- create a Category -->
    <p>
      <fieldset>
        <legend>Create Article Category</legend>
        <@s.form method="post" namespace="/"  action="manageArticleCategory" name="createCategory" id="create_category" >
          <@s.hidden name="command" value="CREATE_CATEGORY"/>
          <@s.hidden name="journalToModify" value="${journal.journalKey}"/>

          <table border="0" cellpadding="10" cellspacing="0">
            <tr>
              <th align="center">Display Name</th>
              <td><@s.textfield name="displayName" size="50" required="true"/></td>
            </tr>
          </table>
          <@s.submit align="right" value="Create"/>
        </@s.form>
      </fieldset>
    </p>
  </body>
</html>