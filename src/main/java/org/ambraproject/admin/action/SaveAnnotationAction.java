/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.action;

import com.opensymphony.xwork2.ModelDriven;
import org.ambraproject.admin.service.AdminAnnotationService;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationCitation;
import org.ambraproject.models.ArticleAuthor;
import org.ambraproject.models.CorrectedAuthor;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author Alex Kudlick 3/28/12
 */
public class SaveAnnotationAction extends BaseAdminActionSupport implements ModelDriven<Annotation> {
  private static final Logger log = LoggerFactory.getLogger(SaveAnnotationAction.class);

  private AdminAnnotationService adminAnnotationService;

  private Annotation annotation;
  private Long annotationId;

  //Can't figure out how to get struts to populate a list of complex objects (e.g. authors), so we'll use arrays instead
  private String[] authorGivenNames;
  private String[] authorSurnames;
  private String[] authorSuffixes;
  private String[] collabAuthors;


  public SaveAnnotationAction() {
    super();
    this.annotation = new Annotation();
    //This allows us to pass citation parameters from the freemarker directly to the object
    annotation.setAnnotationCitation(new AnnotationCitation());
  }

  @Override
  public String execute() throws Exception {
    //create author objects from the the names
    if (authorGivenNames != null) {
      annotation.getAnnotationCitation().setAuthors(new ArrayList<CorrectedAuthor>(authorGivenNames.length));
      for (int i = 0; i < authorGivenNames.length; i++) {
        //only add an author if at least one name was entered
        if (!StringUtils.isEmpty(authorGivenNames[i]) ||
            !StringUtils.isEmpty(authorSurnames[i]) ||
            !StringUtils.isEmpty(authorSuffixes[i])) {
          annotation.getAnnotationCitation().getAuthors().add(
              new CorrectedAuthor(authorGivenNames[i], authorSurnames[i], authorSuffixes[i])
          );
        }

      }
    } else {
      annotation.getAnnotationCitation().setAuthors(new LinkedList<CorrectedAuthor>());
    }

    //set collab authors
    if (collabAuthors != null) {
      annotation.getAnnotationCitation().setCollaborativeAuthors(new ArrayList<String>(collabAuthors.length));
      for (String collabAuthor : collabAuthors) {
        //only add a collab author if it's non empty
        if (!StringUtils.isEmpty(collabAuthor)) {
          annotation.getAnnotationCitation().getCollaborativeAuthors().add(collabAuthor);
        }
      }
    } else {
      annotation.getAnnotationCitation().setCollaborativeAuthors(new LinkedList<String>());
    }

    //perform the update
    try {
      adminAnnotationService.editAnnotation(annotationId, annotation);
      addActionMessage("Successfully updated annotation");
      return SUCCESS;
    } catch (Exception e) {
      log.error("Error updating annotation", e);
      addActionError("Error updating annotation: " + e.getMessage());
      return ERROR;
    }
  }

  @Required
  public void setAdminAnnotationService(AdminAnnotationService adminAnnotationService) {
    this.adminAnnotationService = adminAnnotationService;
  }

  @Override
  public Annotation getModel() {
    return annotation;
  }

  //setter for tests to reset properties
  public void setModel(Annotation annotation) {
    this.annotation = annotation;
  }

  public void setAnnotationId(Long annotationId) {
    this.annotationId = annotationId;
  }

  //Getter so we can pass the id along to the next action
  public Long getAnnotationId() {
    return annotationId;
  }

  public void setAuthorGivenNames(String[] authorGivenNames) {
    this.authorGivenNames = authorGivenNames;
  }

  public void setAuthorSurnames(String[] authorSurnames) {
    this.authorSurnames = authorSurnames;
  }

  public void setAuthorSuffixes(String[] authorSuffixes) {
    this.authorSuffixes = authorSuffixes;
  }

  public void setCollabAuthors(String[] collabAuthors) {
    this.collabAuthors = collabAuthors;
  }
}
