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
  <title>Ambra: Administration: Resend email alerts</title>
  <#include "includes/header.ftl">
</head>
<body>
  <h1 style="text-align: center">Ambra: Administration: Resend email alerts</h1>
  <#include "includes/navigation.ftl">

  <@messages />

  <fieldset>
    <legend><strong>Send Weekly Alerts</strong></legend>
    Enter start and end times as needed.  If fields are left blank, appropriate values will be assigned.<br/>
    <br/>
    <@s.form name="sendWeeklyAlerts" action="sendWeeklyAlerts" method="post" namespace="/">
      Start Time: <input type="text" name="startTime" label="Start Date" size="10" value=""/>&nbsp;(MM/DD/YYYY)<br/>
      End Time: <input type="text" name="endTime" label="End Date" size="10" value=""/>&nbsp;(MM/DD/YYYY)<br/>
      <input type="submit" name="action" value="Send" />
    </@s.form>
  </fieldset>

  <fieldset>
    <legend><strong>Send Monthly Alerts</strong></legend>
    Enter start and end times as needed.  If fields are left blank, appropriate values will be assigned.<br/>
    <br/>
    <@s.form name="sendMonthlyAlerts" action="sendMonthlyAlerts" method="post">
      Start Time: <input type="text" name="startTime" label="Start Date" size="10" value=""/>&nbsp;(MM/DD/YYYY)<br/>
      End Time: <input type="text" name="endTime" label="End Date" size="10" value=""/>&nbsp;(MM/DD/YYYY)<br/>
      <input type="submit" name="action" value="Send" />
    </@s.form>
  </fieldset>

  <br/>
  <br/>
  <br/>
</body>
</html>