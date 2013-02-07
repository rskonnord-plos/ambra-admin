<#--
 $HeadURL: http://svn.ambraproject.org/svn/ambra/ambra-admin/branches/january_fixes/src/main/webapp/templates/indexArticle.ftl $
 $Id: indexArticle.ftl 10080 2012-01-19 18:40:33Z akudlick $

 Copyright (c) 2006-2010 by Public Library of Science
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
  <title>Manage Article Search Index</title>
  <#include "includes/header.ftl">
</head>
<body>
  <h1 style="text-align: center">Manage Search Indexes</h1>
  <#include "includes/navigation.ftl">

  <@messages />

  <!-- XPub and article URI -->
  <fieldset>
    <legend>Article ID</legend>
    <@s.form method="post" namespace="/" action="indexArticle" name="indexArticle" id="indexArticle" >
      <label for="articleId">Article Uri:</label>
      <input type="text" id="articleId" name="articleId" label="Article Uri" size="50" maxlength="50"/>
      &nbsp;<input type="submit" name="action" value="Re-Index" />
    </@s.form>
  </fieldset>

  <fieldset>
    <legend>Re-Index Academic Editors</legend>
    <@s.form method="post" namespace="/" action="indexAE" name="indexAE" id="indexAE" >
      <input type="submit" name="action" value="Re-Index" />
    </@s.form>
  </fieldset>

</body>
</html>
