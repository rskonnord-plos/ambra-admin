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

package org.ambraproject.admin.flags.service;

import org.ambraproject.admin.views.FlagView;
import org.ambraproject.models.AnnotationType;

import java.util.List;

/**
 * @author Alex Kudlick 3/23/12
 */
public interface FlagService {

  /**
   * Get a list of all flagged annotations, ordered by when they were flagged
   *
   * @return
   */
  public List<FlagView> getFlaggedComments();

  /**
   * Delete flags by id
   *
   * @param flagIds the ids of the flags to delete
   */
  public void deleteFlags(Long... flagIds);

  /**
   * Delete comment by id
   *
   * @param commentIds the ids of the comments to be deleted
   */
  public void deleteFlagAndComment(Long... commentIds);

  /**
   * Convert the flagged comments to corrections. Also create and
   * attach {@link org.ambraproject.models.AnnotationCitation} objects to the annotations
   *
   * @param newType the type to convert the flagged annotations to
   * @param flagIds the ids of the flags on the annotations to convert
   */
  public void convertToType(AnnotationType newType, Long... flagIds);

}
