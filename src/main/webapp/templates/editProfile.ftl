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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <style type="text/css" media="all"> @import "${request.contextPath}/css/edit_profile.css";</style>
</head>
<body>
<div id="container" class="profile    `">

<#if displayName?exists && displayName?length gt 0>
  <#assign addressingUser = displayName +"'s" >
<#else>
  <#assign addressingUser = "User's" >
</#if>
<br/>
<@s.url id="adminTopURL" action="adminTop" namespace="/admin" includeParams="none"/>
<@s.a href="%{adminTopURL}">back to admin console</@s.a>
<br/>
<br/>
<@s.url id="editProfileByAdminURL" action="editProfileByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
<@s.url id="editPreferencesByAdminURL" action="retrieveUserAlertsByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
Edit <@s.a href="%{editProfileByAdminURL}">profile</@s.a>
or <@s.a href="%{editPreferencesByAdminURL}">alerts/preferences</@s.a> for <strong>${topazId}</strong>
<br/>

<#if Parameters.tabId?exists>
   <#assign tabId = Parameters.tabId>
<#else>
   <#assign tabId = "">
</#if>

<#assign actionValue="saveProfileByAdmin"/>
<#assign namespaceValue="/admin"/>

<@s.form name="userForm" id="userForm" action="${actionValue}" namespace="${namespaceValue}" method="post" title="User Information Form" cssClass="ambra-form" enctype="multipart/form-data">

  <fieldset>
  <legend>${addressingUser} Private Information</legend>
  <ol>
    <li>
      <strong>${email}</strong><br />
      <a href="${freemarker_config.changeEmailURL}" title="Click here to change your e-mail address">Change ${addressingUser} e-mail address</a><br/>
      <a href="${freemarker_config.changePasswordURL}" title="Click here to change your password">Change ${addressingUser} password</a>
    </li>
  </ol>
  </fieldset>
  <fieldset>
  <legend>${addressingUser} Public Profile</legend>
  <ol>
	<li>Fields marked with <span class="required">*</span> are required.</li>
  <li><em>The following required fields will always appear publicly.</em></li>

   	<#if !isDisplayNameSet>
      <!--after="(Usernames are <strong>permanent</strong> and must be between 4 and 18 characters)"-->
        <@s.textfield name="displayName" label="Username" required="true" tabindex="1" maxlength="18" after="(Usernames are <strong>permanent</strong> and must be between 4 and 18 characters)" />
	  </#if>
   	<#if tabId?has_content>
          <@s.textfield name="givenNames" label="First/Given Name" required="true" tabindex="2" />
	  <#else>
          <@s.textfield name="givenNames" label="First/Given Name" required="true" tabindex="2" />
	  </#if>
   	  <#if tabId?has_content>	
          <@s.textfield name="surnames" label="Last/Family Name" required="true" tabindex="3"/>
	  <#else>
          <@s.textfield name="surnames" label="Last/Family Name" required="true" tabindex="3"/>
	  </#if>
		<br />
   	  <#if tabId?has_content>	
          <@s.textfield name="city" label="City" required="true" tabindex="4"/>
	  <#else>
          <@s.textfield name="city" label="City" required="true" tabindex="4"/>
	  </#if>
    <@s.action name="selectList" namespace="/" id="selectList"/>
    <#if tabId?has_content>	
          <@s.select label="Country" name="country" value="country"
          list="%{#selectList.get('countries')}" tabindex="5" required="true" />
	  <#else>
          <@s.select label="Country" name="country" value="country"
          list="%{#selectList.get('countries')}" tabindex="5" required="true" />
    </#if>

			</li>
		</ol>
	</fieldset>
	<fieldset>
	<legend>${addressingUser} Extended Profile</legend>
		<ol>
   	  <#if tabId?has_content>	
        <@s.textarea name="postalAddress" label="Address" cssClass="long-input"  rows="5" cols="50" tabindex="6" />
	  <#else>
        <@s.textarea name="postalAddress" label="Address" cssClass="long-input"  rows="5" cols="50" tabindex="6" />
	  </#if></li>
		<li>
				<fieldset class="public-private">
				<legend>Should ${addressingUser} address to appear publicly or privately?</legend>
   	  <#if tabId?has_content>	
          <@s.radio name="extendedVisibility"  label="Public" list="{'public'}" checked="true" tabindex="7" cssClass="radio" class="radio"/>
	  <#else>
          <@s.radio name="extendedVisibility" label="Public" list="{'public'}" checked="true" tabindex="7" cssClass="radio" class="radio"/>
	  </#if>
   	  <#if tabId?has_content>	
          <@s.radio name="extendedVisibility"  label="Private" list="{'private'}" tabindex="8" cssClass="radio" class="radio"/>
	  <#else>
          <@s.radio name="extendedVisibility" label="Private" list="{'private'}" tabindex="8" cssClass="radio" class="radio"/>
	  </#if>
				</fieldset>
			</li>
			<li class="form-last-item">
				<ol>
   	  <#if tabId?has_content>
          <@s.select label="Organization Type" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="9" />
	  <#else>
          <@s.select label="Organization Type" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="9" />
	  </#if>
          <@s.textfield name="organizationName" label="Organization Name" tabindex="10" />
				</ol>
				<ol>
   	  <#if tabId?has_content>	
            <@s.select label="Title"  name="title" value="title"
            list="%{#selectList.allTitles}" tabindex="10" />
	  <#else>
            <@s.select label="Title" name="title" value="title" list="%{#selectList.allTitles}" tabindex="10" />
	  </#if>

   	  <#if tabId?has_content>	
            <@s.select label="Position Type" name="positionType" value="positionType"
            list="%{#selectList.allPositionTypes}" tabindex="11" />
	  <#else>
            <@s.select label="Position Type" name="positionType" value="positionType" list="%{#selectList.allPositionTypes}" tabindex="11" />
	  </#if>
				</ol>
				<fieldset class="public-private">
				<legend>Would you like your organization information and title to appear publicly or privately?</legend>
   	  <#if tabId?has_content>	
          <@s.radio name="orgVisibility"  label="Public" list="{'public'}" tabindex="12" cssClass="radio" class="radio"/>
	  <#else>
          <@s.radio name="orgVisibility" label="Public" list="{'public'}" tabindex="12" cssClass="radio" class="radio"/>
	  </#if>
   	  <#if tabId?has_content>	
          <@s.radio name="orgVisibility"  label="Private" list="{'private'}" tabindex="13" cssClass="radio" class="radio"/>
	  <#else>
          <@s.radio name="orgVisibility" label="Private" list="{'private'}" tabindex="13" cssClass="radio" class="radio"/>
	  </#if>
				</fieldset>
		  </li>
		</ol>
	</fieldset>
	<fieldset>
		<legend>Optional Public Information</legend>
		<ol>
   	  <#if tabId?has_content>	
	      <@s.textarea name="biographyText" label="About Me" rows="5" cols="50" tabindex="14"/>
	  <#else>
	      <@s.textarea name="biographyText" label="About Me" rows="5" cols="50" tabindex="14"/>
	  </#if>
   	  <#if tabId?has_content>	
	      <@s.textfield name="researchAreasText" label="Research Areas" cssClass="long-input" tabindex="15" />
	  <#else>
	      <@s.textfield name="researchAreasText" label="Research Areas" cssClass="long-input" tabindex="15" />
	  </#if>
   	  <#if tabId?has_content>	
	      <@s.textfield name="interestsText" label="Interests"  cssClass="long-input" tabindex="16" />
	  <#else>
	      <@s.textfield name="interestsText" label="Interests"  cssClass="long-input" tabindex="16" />
	  </#if>
			<li>
   	  <#if tabId?has_content>	
        <@s.textfield name="homePage" label="Home page"  cssClass="long-input" tabindex="17" />
	  <#else>
        <@s.textfield name="homePage" label="Home page"  cssClass="long-input" tabindex="17" />
	  </#if>
   	  <#if tabId?has_content>	
        <@s.textfield name="weblog" label="Weblog"  cssClass="long-input" tabindex="18" />
	  <#else>
        <@s.textfield name="weblog" label="Weblog"  cssClass="long-input" tabindex="18" />
	  </#if>
			</li>
		</ol>

    <@s.hidden name="topazId"/>
    <@s.submit value="Submit" tabindex="99"/>

	</fieldset>

</@s.form>
</div>
</body>
