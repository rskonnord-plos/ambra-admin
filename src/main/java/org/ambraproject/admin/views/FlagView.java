/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.views;

import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Flag;
import org.ambraproject.models.FlagReasonCode;

import java.util.Calendar;
import java.util.Date;

/**
 * Immutable View wrapper around a flagged comment
 */
public class FlagView {

  private final Long ID;
  private final String comment;
  private final Date date;
  private final FlagReasonCode reasonCode;
  private final String creatorName;
  private final Long creatorID;
  private final Long annotationID;
  private final AnnotationType type;
  private final String annotationTitle;

  public FlagView(Flag flag) {
    this.ID = flag.getID();
    this.comment = flag.getComment();
    this.reasonCode = flag.getReason();

    this.creatorName = flag.getCreator().getDisplayName();
    this.creatorID = flag.getCreator().getID();

    this.annotationID = flag.getFlaggedAnnotation().getID();
    this.annotationTitle = flag.getFlaggedAnnotation().getTitle();
    this.type = flag.getFlaggedAnnotation().getType();

    //Defensive copy
    Calendar date = Calendar.getInstance();
    date.setTime(flag.getCreated());
    this.date = date.getTime();
  }

  public boolean getIsCorrection() {
    return type.isCorrection();
  }

  public boolean getIsFormalCorrection() {
    return type == AnnotationType.FORMAL_CORRECTION;
  }

  public boolean getIsMinorCorrection() {
    return type == AnnotationType.MINOR_CORRECTION;
  }

  public boolean getIsRetraction() {
    return type == AnnotationType.RETRACTION;
  }

  public boolean getIsComment() {
    return type == AnnotationType.COMMENT;
  }

  public boolean getIsReply() {
    return type == AnnotationType.REPLY;
  }

  public String getComment() {
    return comment;
  }

  public Date getDate() {
    //defensive copy
    Calendar date = Calendar.getInstance();
    date.setTime(this.date);
    return date.getTime();
  }

  public Long getID() {
    return ID;
  }

  public Long getCreatorID() {
    return creatorID;
  }

  public FlagReasonCode getReasonCode() {
    return reasonCode;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public Long getAnnotationID() {
    return annotationID;
  }

  public String getAnnotationTitle() {
    return annotationTitle;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FlagView that = (FlagView) o;

    if (annotationID != null ? !annotationID.equals(that.annotationID) : that.annotationID != null) return false;
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    if (creatorID != null ? !creatorID.equals(that.creatorID) : that.creatorID != null) return false;
    if (creatorName != null ? !creatorName.equals(that.creatorName) : that.creatorName != null) return false;
    if (reasonCode != that.reasonCode) return false;
    if (type != that.type) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = comment != null ? comment.hashCode() : 0;
    result = 31 * result + (reasonCode != null ? reasonCode.hashCode() : 0);
    result = 31 * result + (creatorName != null ? creatorName.hashCode() : 0);
    result = 31 * result + (creatorID != null ? creatorID.hashCode() : 0);
    result = 31 * result + (annotationID != null ? annotationID.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "FlagView{" +
        "comment='" + comment + '\'' +
        ", date=" + date +
        ", reasonCode=" + reasonCode +
        ", creatorName='" + creatorName + '\'' +
        ", creatorID=" + creatorID +
        ", annotationID=" + annotationID +
        ", type=" + type +
        '}';
  }
}
