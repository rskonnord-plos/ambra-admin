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

package org.ambraproject.admin.service.impl;

import org.ambraproject.admin.service.AdminAnnotationService;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationCitation;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Kudlick 3/28/12
 */
public class AdminAnnotationServiceImpl extends HibernateServiceImpl implements AdminAnnotationService {
  private static final Logger log = LoggerFactory.getLogger(AdminAnnotationServiceImpl.class);

  @Override
  public void editAnnotation(Long annotationId, Annotation properties) {
    if (annotationId == null) {
      throw new IllegalArgumentException("Didn't specify an annotationId");
    }

    Annotation storedAnnotation = (Annotation) hibernateTemplate.get(Annotation.class, annotationId);

    if (storedAnnotation == null) {
      throw new IllegalArgumentException("Provided an id that didn't correspond to an annotation: " + annotationId);
    }

    log.debug("Updating properties on annotation {}", annotationId);

    storedAnnotation.setAnnotationUri(properties.getAnnotationUri());
    storedAnnotation.setTitle(properties.getTitle());
    storedAnnotation.setBody(properties.getBody());
    storedAnnotation.setXpath(properties.getXpath());
    storedAnnotation.setCompetingInterestBody(properties.getCompetingInterestBody());

    if (storedAnnotation.getAnnotationCitation() != null && properties.getAnnotationCitation() != null) {
      AnnotationCitation storedCitation = storedAnnotation.getAnnotationCitation();
      AnnotationCitation newCitation = properties.getAnnotationCitation();
      storedCitation.setTitle(newCitation.getTitle());
      storedCitation.setVolume(newCitation.getVolume());
      storedCitation.setIssue(newCitation.getIssue());
      storedCitation.setJournal(newCitation.getJournal());

      storedCitation.setPublisher(newCitation.getPublisher());
      storedCitation.setYear(newCitation.getYear());
      storedCitation.setELocationId(newCitation.getELocationId());
      storedCitation.setUrl(newCitation.getUrl());
      storedCitation.setNote(newCitation.getNote());
      storedCitation.setSummary(newCitation.getSummary());

      storedCitation.getCollaborativeAuthors().clear();
      if (newCitation.getCollaborativeAuthors() != null) {
        storedCitation.getCollaborativeAuthors().addAll(newCitation.getCollaborativeAuthors());
      }
      storedCitation.getAuthors().clear();
      if (newCitation.getAuthors() != null) {
        storedCitation.getAuthors().addAll(newCitation.getAuthors());
      }
    }

    hibernateTemplate.update(storedAnnotation); //cascade updates to citation

  }
}
