<#--
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
  <title>Manage Featured Articles</title>
<#include "includes/header.ftl">
</head>
<body>
<h1 style="text-align: center">Manage Featured Articles</h1>
<#include "includes/navigation.ftl">
<@messages />

<#-- create a featured Article -->
<p>
  <fieldset>
    <legend>Create Featured Article</legend>
    <@s.form method="post" namespace="/"  action="featuredArticleCreate" name="featuredArticleCreate" id="featuredArticleCreate" >
      <table border="0" cellpadding="10" cellspacing="0">
        <tr>
          <th align="center">Subject Area:</th>
          <td><@s.textfield name="subjectArea" size="50" required="true"/></td>
        </tr>
        <tr>
          <th align="center">DOI:</th>
          <td><@s.textfield name="doi" size="50" required="true"/></td>
        </tr>
      </table>
      <@s.submit align="right" value="Create"/>
    </@s.form>
  </fieldset>
</p>

<#-- list existing featured articles -->
<fieldset>
  <legend>Existing Featured Article List</legend>
  <#if (featuredArticles?size > 0)>
    <@s.form method="post" namespace="/" action="featuredArticleDelete" name="deleteList" id="deleteList" >
      <table border="1" cellpadding="10" cellspacing="0">
        <tr>
          <th>Delete</th>
          <th>Subject Area</th>
          <th>DOI</th>
        </tr>
        <#list featuredArticles?keys as sa>
          <tr>
            <td align="center"><@s.checkbox name="deleteList" fieldValue="${sa}"/></td>
            <td>${sa}</td>
            <td>${featuredArticles[sa]}</td>
          </tr>
        </#list>
      </table>
      <@s.submit value="Delete Selected Featured Articles"/>
    </@s.form>
  <#else>
    <strong>There are no featured articles associated with ${currentJournal}.</strong>
  </#if>
</fieldset>
</body>
</html>