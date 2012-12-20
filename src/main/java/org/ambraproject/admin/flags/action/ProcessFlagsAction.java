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

package org.ambraproject.admin.flags.action;

import org.ambraproject.admin.action.BaseAdminActionSupport;
import org.ambraproject.admin.flags.service.FlagService;
import org.ambraproject.models.AnnotationType;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Alex Kudlick 3/26/12
 */
public class ProcessFlagsAction extends BaseAdminActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ProcessFlagsAction.class);
  private FlagService flagService;

  private Long[] commentsToUnflag;
  private Long[] commentsToDelete;
  private Long[] convertToFormalCorrection;
  private Long[] convertToMinorCorrection;
  private Long[] convertToRetraction;

  @Override
  public String execute() throws Exception {
    try {
      if (!ArrayUtils.isEmpty(commentsToUnflag)) {
        flagService.deleteFlags(commentsToUnflag);
        addActionMessage("Successfully deleted " + commentsToUnflag.length + "  flags");
      }
      if (!ArrayUtils.isEmpty(commentsToDelete)) {
        flagService.deleteFlagAndComment(commentsToDelete);
        addActionMessage("Successfully deleted " + commentsToDelete.length + "  comments");
      }
      if (!ArrayUtils.isEmpty(convertToFormalCorrection)) {
        flagService.convertToType(AnnotationType.FORMAL_CORRECTION, convertToFormalCorrection);
        addActionMessage("Successfully converted " + convertToFormalCorrection.length + " annotations to formal correction");
      }
      if (!ArrayUtils.isEmpty(convertToMinorCorrection)) {
        flagService.convertToType(AnnotationType.MINOR_CORRECTION, convertToMinorCorrection);
        addActionMessage("Successfully converted " + convertToMinorCorrection.length + " annotations to minor correction");
      }
      if (!ArrayUtils.isEmpty(convertToRetraction)) {
        flagService.convertToType(AnnotationType.RETRACTION, convertToRetraction);
        addActionMessage("Successfully converted " + convertToRetraction.length + " annotations to retraction");
      }
    } catch (Exception e) {
      log.error("error processing flags", e);
      addActionError("Error processing flags: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  @Required
  public void setFlagService(FlagService flagService) {
    this.flagService = flagService;
  }

  public void setCommentsToUnflag(Long[] commentsToUnflag) {
    this.commentsToUnflag = commentsToUnflag;
  }

  public void setCommentsToDelete(Long[] commentsToDelete) {
    this.commentsToDelete = commentsToDelete;
  }

  public void setConvertToFormalCorrection(Long[] convertToFormalCorrection) {
    this.convertToFormalCorrection = convertToFormalCorrection;
  }

  public void setConvertToMinorCorrection(Long[] convertToMinorCorrection) {
    this.convertToMinorCorrection = convertToMinorCorrection;
  }

  public void setConvertToRetraction(Long[] convertToRetraction) {
    this.convertToRetraction = convertToRetraction;
  }

}
