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

package org.ambraproject.admin.service;

import org.ambraproject.models.Annotation;
import org.ambraproject.service.HibernateService;

/**
 * @author Alex Kudlick 3/28/12
 */
public interface AdminAnnotationService extends HibernateService {

  /**
   * Edit an existing annotation with the properties set on the provided object. This does NOT change:
   * <ul>
   * <li>the {@link Annotation#ID}</li>
   * <li>the {@link Annotation#type}. Use {@link org.ambraproject.admin.flags.service.FlagService#convertToType(org.ambraproject.models.AnnotationType, Long...)} to do that</li>
   * <li>the {@link Annotation#articleID}</li>
   * <li>the {@link Annotation#creator}</li>
   * <li>the {@link Annotation#parentID}</li>
   * </ul>
   * 
   * This will update {@link Annotation#annotationCitation} properties if a citation already exists on the existing annotation.
   *
   * @param annotationId the id of the annotation to edit
   * @param properties   an {@link Annotation} object with properties to store
   */
  public void editAnnotation(Long annotationId, Annotation properties);
}
