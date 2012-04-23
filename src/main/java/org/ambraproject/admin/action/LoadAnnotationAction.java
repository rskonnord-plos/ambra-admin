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

import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.annotation.service.AnnotationService;
import org.ambraproject.views.AnnotationView;
import org.springframework.beans.factory.annotation.Required;

/**
 * Load up an annotation by ID or URI
 * @author Alex Kudlick 3/27/12
 */
public class LoadAnnotationAction extends BaseAdminActionSupport {
  
  private AnnotationService annotationService;
  private Long annotationId;
  private String annotationUri;
  
  private AnnotationView annotation;
  
  @Override
  public String execute() throws Exception {
    if (annotationUri != null) {
      this.annotation = annotationService.getBasicAnnotationViewByUri(annotationUri);
    } else if (annotationId != null) {
      this.annotation = annotationService.getBasicAnnotationView(annotationId);
    } else {
      addActionError("No Annotation Specified");
      return INPUT;
    }
    return SUCCESS;
  }

  @Required
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  public void setAnnotationId(Long annotationId) {
    this.annotationId = annotationId;
  }

  public void setAnnotationUri(String annotationUri) {
    this.annotationUri = annotationUri;
  }

  public AnnotationView getAnnotation() {
    return annotation;
  }
}
