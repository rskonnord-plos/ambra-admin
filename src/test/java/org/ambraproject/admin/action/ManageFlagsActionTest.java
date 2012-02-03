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

import com.opensymphony.xwork2.Action;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.admin.service.FlaggedCommentRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.ReplyThread;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.models.UserProfile;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.testng.Assert.assertEquals;

/**
 * @author Alex Kudlick 2/2/12
 */
public class ManageFlagsActionTest extends AdminWebTest {

  @Autowired
  protected ManageFlagsAction action;

  private DateFormat dateFormatter;

  public ManageFlagsActionTest() {
    super();
    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
  
  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @DataProvider(name = "flags")
  public Object[][] getFlags() {
    //make sure there are no flags left over from other tests
    dummyDataStore.deleteAll(Annotation.class);

    Comment comment = new Comment();
    comment.setId(URI.create("id:test-comment-for-manage-flags"));
    comment.setTitle("The Marriage Plot");
    comment.setCreated(Calendar.getInstance().getTime());
    dummyDataStore.store(comment);

    ReplyThread reply = new ReplyThread();
    reply.setInReplyTo(comment.getId().toString());
    reply.setRoot(comment.getId().toString());
    dummyDataStore.store(reply);

    UserProfile profile1 = new UserProfile();
    profile1.setDisplayName("Voldemort");
    dummyDataStore.store(profile1);
    UserAccount account1 = new UserAccount();
    account1.setProfile(profile1);
    dummyDataStore.store(account1);
    Calendar created = Calendar.getInstance();
    created.add(Calendar.YEAR, -1);


    Comment flag1 = new Comment();
    flag1.setAnnotates(comment.getId());
    flag1.setBody(new AnnotationBlob());
    flag1.getBody().setBody((
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<flag reasonCode=\"Create Correction\">" +
            "<comment>Note created and flagged as a correction</comment>" +
            "</flag>").getBytes());
    flag1.setCreated(created.getTime());
    flag1.setCreator(account1.getId().toString());
    dummyDataStore.store(flag1);

    UserProfile profile2 = new UserProfile();
    profile2.setDisplayName("MyCoolUserN4m3");
    dummyDataStore.store(profile2);
    UserAccount account2 = new UserAccount();
    account2.setProfile(profile2);
    dummyDataStore.store(account2);

    Comment flag2 = new Comment();
    flag2.setAnnotates(reply.getId());
    flag2.setBody(new AnnotationBlob());
    flag2.getBody().setBody(
        ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<flag reasonCode=\"other\">" +
            "<comment>flagging for elevation</comment>" +
            "</flag>").getBytes());
    flag2.setCreated(Calendar.getInstance().getTime());
    flag2.setCreator(account2.getId().toString());
    dummyDataStore.store(flag2);

    UserProfile profile3 = new UserProfile();
    profile3.setDisplayName("Harry Potter");
    dummyDataStore.store(profile3);
    UserAccount account3 = new UserAccount();
    account3.setProfile(profile3);
    dummyDataStore.store(account3);

    Comment flag3 = new Comment();
    flag3.setAnnotates(reply.getId());
    flag3.setBody(new AnnotationBlob());
    flag3.getBody().setBody(
        ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<flag kajsd< > some broken xml"
        ).getBytes());
    Calendar created3 = Calendar.getInstance();
    created3.add(Calendar.HOUR, 1);
    flag3.setCreated(created3.getTime());
    flag3.setCreator(account3.getId().toString());
    dummyDataStore.store(flag3);

    List<FlaggedCommentRecord> flags = new ArrayList<FlaggedCommentRecord>(2);
    flags.add(
        new FlaggedCommentRecord(
            flag1.getId().toString(),
            flag1.getAnnotates().toString(),
            comment.getTitle(),
            "Note created and flagged as a correction",
            dateFormatter.format(flag1.getCreated()),
            profile1.getDisplayName(),
            account1.getId().toString(),
            null,
            "Create Correction",
            comment.getWebType(),
            true,
            false)
    );

    flags.add(
        new FlaggedCommentRecord(
            flag2.getId().toString(),
            flag2.getAnnotates().toString(),
            flag2.getTitle(),
            "flagging for elevation",
            dateFormatter.format(flag2.getCreated()),
            profile2.getDisplayName(),
            account2.getId().toString(),
            comment.getId().toString(),
            "other",
            reply.getWebType(),
            true,
            false)
    );

    flags.add(
        new FlaggedCommentRecord(
            flag3.getId().toString(),
            flag3.getAnnotates().toString(),
            flag3.getTitle(),
            "-missing-",
            dateFormatter.format(flag3.getCreated()),
            profile3.getDisplayName(),
            account3.getId().toString(),
            comment.getId().toString(),
            "-missing-",
            reply.getWebType(),
            true,
            true)
    );

    return new Object[][]{
        {flags}
    };
  }

  @Test(dataProvider = "flags")
  public void testExecute(List<FlaggedCommentRecord> flags) throws Exception {
    String result = action.execute();
    assertEquals(result, Action.SUCCESS, "action didn't return success");

    assertEquals(action.getFlaggedComments().size(), flags.size(), "Action returned incorrect number of flags");

    for (int i = 0; i < flags.size(); i++) {
      FlaggedCommentRecord actual = (FlaggedCommentRecord) ((List) action.getFlaggedComments()).get(i);
      FlaggedCommentRecord expected = flags.get(i);
      assertEquals(actual.getReasonCode(), expected.getReasonCode(), "Flag " + (i + 1) + " had incorrect reason code");
      assertEquals(actual.getFlagComment(), expected.getFlagComment(), "Flag " + (i + 1) + " had incorrect flag comment");
      assertEquals(actual.getTarget(), expected.getTarget(), "Flag " + (i + 1) + " had incorrect target");
      assertEquals(actual.getTargetTitle(), expected.getTargetTitle(), "Flag " + (i + 1) + " had incorrect target title");

      assertMatchingDates(dateFormatter.parse(actual.getCreated()), dateFormatter.parse(expected.getCreated()));

      assertEquals(actual.getCreator(), expected.getCreator(), "Flag " + (i + 1) + " had incorrect creator");
      assertEquals(actual.getCreatorid(), expected.getCreatorid(), "Flag " + (i + 1) + " had incorrect creatorId");
      assertEquals(actual.getRoot(), expected.getRoot(), "Flag " + (i + 1) + " had incorrect root");
      assertEquals(actual.isBroken(), expected.isBroken(),
          "Flag " + (i + 1) + " was expected to be broken and wasn't, or vice versa");
    }

  }
}
