<#--
 $HeadURL$
 $Id$

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
    <title>Manage Articles for the news</title>
    <#include "includes/header.ftl">
  </head>
  <body>
    <h1 style="text-align: center">Manage Articles for the news</h1>
    <#include "includes/navigation.ftl">

    <#--<@messages/>-->

    <fieldset>
      <legend>News Articles DOIs</legend>
      <p>Please enter ten DOIs, one per line, of the articles to appear in the news.</p>
      <@s.form method="post" namespace="/" action="manageNews" name="news" id="news" >
        <@s.hidden name="command" value="SAVE_ARTICLES"/>
        <textarea name="articles" cols="40" rows="7"></textarea>
        <br/>
        <@s.submit value="Save"/>
      </@s.form>
    </fieldset>
  </body>
</html>
