<#--
 $$HeadURL:: $
 $$Id$

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
  <title>Article Management</title>
<#include "includes/header.ftl">
</head>
  <body>
    <h1 style="text-align: center">Article Management</h1>
    <#include "includes/navigation.ftl">

    <@messages />

    <!-- Update a Article List -->
    <fieldset>
      <legend>Update Article List</legend>
      <@s.form method="post" namespace="/" action="articleManagement" id="update_list" name="updateList">
        <@s.hidden name="command" value="UPDATE_LIST"/>
          <table border="0" cellpadding="10" cellspacing="0">
            <tr>
              <th align="center">Article List (ListCode)</th>
              <@s.hidden name="listCode" value="${articleList.listCode}"/>
              <td>${articleList.listCode}</td>
            </tr>
            <tr>
              <th align="center">Display Name</th>
              <td>
                <@s.textfield name="displayName" value="${articleList.displayName}" size="50"/>
              </td>
            </tr>
            <tr>
              <th align="center">Article URI List
                <br><kbd>(Reorder only)</kbd></br>
              </th>
              <td>
                <@s.textfield name="articleOrderCSV" value="${articleOrderCSV!}" size="100"/>
              </td>
            </tr>
        </table>
        <@s.submit align="right" value="Update"/>
      </@s.form>
    </fieldset>

    <!-- Add articles -->
    <fieldset>
      <legend>Add Articles</legend>
      <@s.form method="post" namespace="/" action="articleManagement" name="addArticle" id="add_article">
        <@s.hidden name="command" value="ADD_ARTICLE"/>
        <@s.hidden name="listCode" value="${articleList.listCode}"/>
        <table border="0" cellpadding="10" cellspacing="0">
          <tr>
            <th align="center">Article (URIs)</th>
            <td><@s.textfield name="articlesToAddCsv" size="50" /></td>
          </tr>
          <tr>
            <td colspan="2">
              <kbd>(A comma separated list of article URIs can be used for multiple entries.)</kbd>
            </td>
          </tr>
        </table>
        <@s.submit align="right" value="Add"/>
      </@s.form>
    </fieldset>

    <!-- list Existing Article For this list-->
    <fieldset>
      <legend>Articles in List</legend>
      <@s.form  method="post" namespace="/" action="articleManagement" name="removeArticles" id="removeArticles">
        <@s.hidden name="command" value="REMOVE_ARTICLES"/>
        <@s.hidden name="listCode" value="${articleList.listCode}"/>
        <table border="1" cellpadding="10" cellspacing="0">
          <#if (articleInfoList?size > 0)>
            <tr>
              <th>Delete</th>
              <th>Article URI</th>
              <th>Title</th>
            </tr>
            <#list articleInfoList as article>
              <@s.url id="articleURL" includeParams="none" namespace="/article" action="fetchArticle" articleURI="${article.doi}"/>
              <tr>
                <td align="center">
                  <@s.checkbox name="articlesToRemove" fieldValue="${article.doi}"/>
                </td>
                <td>
                ${article.doi}
                </td>
                <td>
                  <a target="_article" href="${articleURL}">${article.title}</a>
                </td>
              </tr>
            </#list>
          <#else>
            <strong>There are no articles associated with this list.</strong>
          </#if>
        </table>
        <#if (orphanDois?size > 0) >
          <b>Orphaned Articles</b>
          <table border="1" cellpadding="10" cellspacing="0">
            <tr>
              <td colspan="2">
                <ul>
                  <li><kbd>Incorrect URIs not associated with an article.</kbd></li>
                </ul>
              </td>
            </tr>
            <tr>
              <th>Delete</th>
              <th>Article URI</th>
            </tr>
            <#list orphanDois as orphan>
              <tr>
                <td align="center">
                  <@s.checkbox name="articlesToRemove" fieldValue="${orphan}"/>
                </td>
                <td>
                  ${orphan}
                </td>
              </tr>
            </#list>
          </table>
        </#if>
        <#if (articleInfoList?size > 0 || orphanDois?size > 0)>
          <@s.submit value="Remove Selected Articles"/>
        </#if>
      </@s.form>
    </fieldset>

  </body>
</html>