/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2011 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.service;

import org.ambraproject.ApplicationException;
import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.annotation.service.AnnotationConverter;
import org.ambraproject.annotation.service.Flag;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.models.UserProfile;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class FlagManagementServiceTest extends AdminBaseTest {

  @Autowired
  protected FlagManagementService flagManagementService;

  @Autowired
  protected AnnotationConverter converter;

  @DataProvider(name = "listFlag")
  public Object[][] listFlag() throws MalformedURLException {
    UserProfile creator = new UserProfile();
    creator.setId(URI.create("id:test-id-for-creator"));
    creator.setRealName("realname");
    creator.setDisplayName("displayname");
    dummyDataStore.store(creator);

    UserAccount userAccount = new UserAccount();
    userAccount.setId(URI.create("testid2"));
    userAccount.setProfile(creator);
    dummyDataStore.store(userAccount);

    Comment originalComment = new Comment();
    originalComment.setId(URI.create("id://test-id"));
    originalComment.setAnnotates(URI.create("id://test-annotates"));
    originalComment.setContext("context");
    originalComment.setCreated(new Date());

    //Set the body on the comment
    AnnotationBlob originalCommentBody = new AnnotationBlob();
    originalCommentBody.setId("testbodyid");
    originalCommentBody.setBody("testbody".getBytes());
    originalCommentBody.setCIStatement("ci statement");
    originalComment.setBody(originalCommentBody);

    dummyDataStore.store(originalCommentBody);
    dummyDataStore.store(originalComment);

    Comment flag = new Comment();
    flag.setId(URI.create("id://test-id-2"));
    flag.setAnnotates(originalComment.getId());
    flag.setCreator(creator.getId().toString());
    flag.setContext("context2");
    flag.setCreator("testid2");
    flag.setCreated(new Date());

    //Set the body on the comment2
    AnnotationBlob flagBody = new AnnotationBlob();
    flagBody.setId("testbodyid2");
    flagBody.setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?><flag reasonCode=\"resoncode\"><comment>Another reasoncode.</comment></flag>".getBytes());
    flagBody.setCIStatement("ci statement-2");
    flag.setBody(flagBody);

    dummyDataStore.store(flagBody);
    dummyDataStore.store(flag);

    Map<Comment, Flag> results = new HashMap<Comment, Flag>(1);
    results.put(originalComment, new Flag(converter.convert(flag, true, true)));

    return new Object[][]{
        {results}
    };
  }

  /**
   * Test for FlagManagementService.getFlaggedComments()
   *
   * @param flaggedComments
   * @throws ApplicationException
   */

  @Test(dataProvider = "listFlag")
  public void testGetFlaggedComments(Map<Comment, Flag> flaggedComments) throws ApplicationException {
    Collection<FlaggedCommentRecord> list = flagManagementService.getFlaggedComments();
    assertNotNull(list, "The list is null");
    assertEquals(list.size(), flaggedComments.size());

    for (Map.Entry<Comment, Flag> entry : flaggedComments.entrySet()) {
      Comment originalComment = entry.getKey();
      Flag flag = entry.getValue();
      FlaggedCommentRecord record = null;
      for (FlaggedCommentRecord flaggedCommentRecord : list) {
        if (flaggedCommentRecord.getTarget().equals(originalComment.getId().toString())) {
          record = flaggedCommentRecord;
          break;
        }
      }
      assertNotNull(record, "didn't find a matching FlaggedCommentRecord for " + originalComment);
      assertEquals(record.getCreatorid(), flag.getCreator(), "Flagged Comment Record had incorrect creator");
      assertEquals(record.getFlagId(), flag.getId(), "FlaggedCommentRecord had incorrect flag id");
      assertEquals(record.getTarget(), originalComment.getId().toString(), "FlaggedCommentRecord had incorrect target annotation id");
      assertEquals(record.getReasonCode(), flag.getReasonCode(), "FlaggedCommentRecord had incorrect reason code");
      assertEquals(record.getFlagComment(), flag.getComment(), "FlaggedCommentRecord had incorrect flag comment");
    }
  }
}
