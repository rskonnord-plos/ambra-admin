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
  <title>Ambra: Administration: Manage Annotations</title>
<#include "includes/header.ftl">
  <script type="text/javascript" src="${request.contextPath}/javascript/edit_annotation.js"></script>
</head>
<body>
<h1 style="text-align: center">Ambra: Administration: Manage Annotations</h1>
<#include "includes/navigation.ftl">

<@messages />

<fieldset>
  <legend><b>Load Annotation</b></legend>
<#if annotation??>
  <#assign annotationIdValue = annotation.ID?c />
  <#assign annotationUriValue = "info:doi/${annotation.annotationUri}" />
<#else>
  <#assign annotationIdValue = "" />
  <#assign annotationUriValue = "" />
</#if>
<@s.form name="manageAnnotationLoadById" action="manageAnnotationLoad" namespace="/" method="get">
  <table>
    <tr>
      <td><b>Annotation ID (numeric)</b></td>
      <td><@s.textfield name="annotationId" size="60" value="${annotationIdValue}"/></td>
    </tr>
    <tr>
      <td colspan="2"><@s.submit value="Load Annotation" /></td>
    </tr>
  </table>
</@s.form>
  <br/>
<@s.form name="manageAnnotationLoadByUri" action="manageAnnotationLoad" namespace="/" method="get">
  <table>
    <tr>
      <td><b>Annotation URI</b></td>
      <td><@s.textfield name="annotationUri" size="60" value="${annotationUriValue}"/></td>
    </tr>
    <tr>
      <td colspan="2"><@s.submit value="Load Annotation" /></td>
    </tr>
  </table>
</@s.form>
</fieldset>

<#if annotation??>
  <@s.form name="manageAnnotationSave" action="manageAnnotationSave" namespace="/" method="post">

  <#--If there's no citation for this annotation, we need a hidden field for annotation uri. If there is a citation, it's an  editable textfield-->
  <#if !annotation.citation??>
    <@s.hidden name="annotationUri" value="${annotationUriValue}"/>
  </#if>

  <fieldset>
    <legend><b>Annotation Details</b></legend>
    <@s.hidden name="annotationId" label="hiddenAnnotationId" required="true" value="${annotation.ID?c}"/>
    <table>
      <tr>
        <td><b>Title</b></td>
        <@s.hidden name="title" label="title" required="true" value="${annotation.originalTitle!}"/>
        <td>${annotation.originalTitle!"No Title for this Annotation"}</td>
      </tr>
      <tr>
        <td valign="top"><b>Body</b></td>
        <td><@s.textarea name="body" value="${annotation.originalBody!}" rows="9" cols="100"/></td>
      </tr>
      <tr>
        <td><b>Context</b></td>
        <td><@s.textarea name="xpath" value="${annotation.xpath!}" rows="3" cols="100"/></td>
      </tr>
      <tr>
        <td><b>Id</b></td>
        <#if annotation.type == "Rating">
          <#assign annotationURL = "rate/getArticleRatings.action?articleURI=${annotation.articleDoi!}#${annotation.ID?c!}" />
        <#else>
          <#assign annotationURL = "annotation/listThread.action?root=${annotation.ID?c!}" />
        </#if>
        <td>
          <a href="${annotationURL}">${annotation.ID?c!}</a>
        </td>
      </tr>
      <tr>
        <td><b>Type</b></td>
        <td>${annotation.type!"No Type"}</td>
      </tr>
      <tr>
        <td><b>Created</b></td>
        <td>${annotation.created?string("EEEE, MMMM dd, yyyy, hh:mm:ss a '('zzz')'")!"No Creation Date"}</td>
      </tr>
      <tr>
        <td><b>Creator</b></td>
        <@s.url id="showUser" namespace="/user" action="showUser" userId="${annotation.creatorID?c!}"/>
        <td><@s.a href="${showUser}">${annotation.creatorDisplayName!"No Creator"}</@s.a></td>
      </tr>
      <tr>
        <td><b>Annotates</b></td>
        <td>
          <a href="article/${annotation.articleDoi}">${annotation.articleDoi}</a>
        </td>
      </tr>
      <tr>
        <#if annotation.competingInterestStatement?has_content>
          <#assign ciStatement = annotation.competingInterestStatement/>
        <#else>
          <#assign ciStatement = "No Conflict of Interest Statement"/>
        </#if>
        <td><b>Conflict of Interest</b></td>
        <td>${ciStatement}</td>
      </tr>
    </table>
  </fieldset>

    <@s.submit value="Save Annotation" />
    <#if annotation.correction>
      <#if ! annotation.citation??>
      <b>No Citation for this Annotation<b>
      <#else>
        <#assign citation = annotation.citation/>
        <fieldset>
          <legend><b>Annotation Citation</b></legend>
          <table>
            <tr>
              <td><b>Citation Title</b></td>
              <td><@s.textfield name="annotationCitation.title" value="${citation.title!}" size="40"/></td>
            </tr>
            <tr>
              <td><b>Year</b></td>
              <td><@s.textfield name="annotationCitation.year" value="${citation.year!}" size="10"/></td>
            </tr>
            <tr>
              <td><b>Volume</b></td>
              <td><@s.textfield name="annotationCitation.volume" value="${citation.volume!}" size="10"/></td>
            </tr>
            <tr>
              <td><b>Issue</b></td>
              <td><@s.textfield name="annotationCitation.issue" value="${citation.issue!}" size="10"/></td>
            </tr>
            <tr>
              <td><b>Journal</b></td>
              <td><@s.textfield name="annotationCitation.journal" value="${citation.journal!}" size="20"/></td>
            </tr>
            <tr>
              <td><b>eLocationId</b></td>
              <td><@s.textfield name="annotationCitation.eLocationId" value="${citation.eLocationId!}" size="20"/></td>
            </tr>
            <tr>
              <td><b>DOI</b></td>
              <td><@s.textfield name="annotationUri" value="info:doi/${annotation.annotationUri!}" size="40"/></td>
            </tr>
            <tr>
              <td><b>URL</b></td>
              <@s.hidden name="annotationCitation.url" label="annotationCitation.url" required="true" value="${citation.url!}"/>
              <td>${citation.url!"No URL"}</td>
            </tr>
            <tr>
              <td><b>Note</b></td>
              <@s.hidden name="annotationCitation.note" label="annotationCitation.note" required="true" value="${citation.note!}"/>
              <td>${citation.note!"No Note"}</td>
            </tr>
            <tr>
              <td><b>Summary</b></td>
              <@s.hidden name="annotationCitation.summary" label="annotationCitation.summary" required="true" value="${citation.summary!}"/>
              <td>${citation.summary!"No Summary"}</td>
            </tr>
            <tr>
              <td colspan="2">
                <fieldset>
                  <legend><b>Citation Authors</b></legend>
                  <table id="authors">
                    <#if citation.authors?? && citation.authors?size gt 0>
                      <tr>
                        <td><b>Given Names</b></td>
                        <td><b>Surnames</b></td>
                        <td><b>Suffixes</b></td>
                      </tr>
                      <#list citation.authors as author>
                        <tr id="author_${author_index}">
                          <td><@s.textfield name="authorGivenNames" value="${author.givenNames!}" size="20"/></td>
                          <td><@s.textfield name="authorSurnames" value="${author.surnames!}" size="20"/></td>
                          <td><@s.textfield name="authorSuffixes" value="${author.suffix!}" size="20"/></td>
                          <td><a href="#"
                                 onClick="deleteAuthor('author_${author_index}','${author.givenNames!} ${author.surnames!}', false);return false;">
                            Delete Author</a></td>
                        </tr>
                      </#list>
                    <#else>
                      There are currently no Authors associated with this Citation.
                    </#if>
                    <tr id="addAuthor">
                      <td><@s.textfield name="authorGivenNames" value="" size="20"/></td>
                      <td><@s.textfield name="authorSurnames" value="" size="20"/></td>
                      <td><@s.textfield name="authorSuffixes" value="" size="20"/></td>
                      <td><a href="#" onClick="document.getElementById('manageAnnotationSave').submit()">Add Author</a></td>
                    </tr>
                  </table>
                </fieldset>
              </td>
            </tr>

            <tr>
              <td colspan="2">
                <fieldset>
                  <legend><b>Citation Collaborative Authors</b></legend>
                  <table id="collabAuthors">
                    <#if citation.collabAuthors?? && (citation.collabAuthors?size > 0)>
                      <#list citation.collabAuthors as collabAuthor>
                        <tr id="collabAuthor_${collabAuthor_index}">
                          <td><@s.textfield name="collabAuthors" value="${collabAuthor!}" size="75"/></td>
                          <td><a href="#"
                                 onClick="deleteAuthor('collabAuthor_${collabAuthor_index}', '${collabAuthor}', true);return false;">Delete
                            Collaborative Author</a></td>
                        </tr>
                      </#list>
                    <#else>
                      There are currently no Collaborative Authors associated with this Citation.
                    </#if>
                    <tr>
                      <td><@s.textfield name="collabAuthors" value="" size="75"/></td>
                      <td><a href="#" onClick="document.getElementById('manageAnnotationSave').submit()">Add Collaborative Author</a></td>
                    </tr>
                  </table>
                </fieldset>
              </td>
            </tr>

          </table>
        </fieldset>
        <@s.submit value="Save Annotation" />
      </#if>
    </#if>
  </@s.form>
</#if>
</body>
</html>