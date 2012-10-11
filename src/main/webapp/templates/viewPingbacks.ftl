<#--
 $HeadURL: http://svn.ambraproject.org/svn/ambra/ambra-admin/head/src/main/webapp/templates/crossPubManagement.ftl $
 $Id: crossPubManagement.ftl 11009 2012-05-15 21:23:36Z josowski $

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
    <title>View Pingbacks</title>
<#include "includes/header.ftl">
</head>
<body>
<h1 style="text-align: center">View Pingbacks</h1>
<#include "includes/navigation.ftl">

<@messages />

<h3>Articles with Pingbacks</h3>
<table border="1" cellpadding="10" cellspacing="0">
    <tr>
        <th>Article</th>
        <th>Pingback Count</th>
    </tr>
<#list articlesWithPingbackCounts as obj>
    <tr>
        <td><a href="${obj.articleUrl}">${obj.articleTitle}</a></td>
        <td>${obj.pingbackCount}</td>
    </tr>
</#list>
</table>

<h3>Individual Pingbacks</h3>
<table border="1" cellpadding="10" cellspacing="0">
    <tr>
        <th>Link Source</th>
        <th>Linked Article</th>
        <th>Timestamp</th>
    </tr>
<#list pingbacksByDate as obj>
    <tr>
        <td><a href="${obj.sourceUrl}">${obj.sourceTitle}</a></td>
        <td><a href="${obj.articleUrl}">${obj.articleTitle}</a></td>
        <td>${obj.timestamp}</td>
    </tr>
</#list>
</table>

</body>
</html>