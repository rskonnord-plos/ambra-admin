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

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.admin.views.FlagView;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Flag;
import org.ambraproject.models.FlagReasonCode;
import org.ambraproject.models.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alex Kudlick 3/23/12
 */
public class ListFlagsActionTest extends AdminWebTest {

  @Autowired
  protected ListFlagsAction action;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @Test
  public void testExecute() throws Exception {
    //make sure there aren't any other flags in the db stored by other tests
    dummyDataStore.deleteAll(Flag.class);

    //set up data
    UserProfile creator = new UserProfile(
        "id:creatorForListFlagsActionTest",
        "email@ListFlagsActionTest.org",
        "displaynameForListFlagsActionTest");
    dummyDataStore.store(creator);

    Annotation comment = new Annotation(creator, AnnotationType.COMMENT, 123l);
    comment.setTitle("test title for ListFlagsActionTest");
    dummyDataStore.store(comment);

    Flag flag1 = new Flag(creator, FlagReasonCode.SPAM, comment);
    flag1.setComment("This is totally spam");
    dummyDataStore.store(flag1);

    Flag flag2 = new Flag(creator, FlagReasonCode.INAPPROPRIATE, comment);
    flag2.setComment("This is totally inappropriate");
    dummyDataStore.store(flag2);

    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getActionErrors().size(), 0, 
        "Action had error messages: " + StringUtils.join(action.getActionErrors(), ";"));
    assertEquals(action.getFieldErrors().size(), 0, 
        "Action had field error messages: " + StringUtils.join(action.getFieldErrors().values(), ";"));

    assertNotNull(action.getFlaggedComments(), "Action had null list of flagged comments");
    assertEqualsNoOrder(action.getFlaggedComments().toArray(),
        new FlagView[]{new FlagView(flag1), new FlagView(flag2)}, "Incorrect flags");

  }

}
