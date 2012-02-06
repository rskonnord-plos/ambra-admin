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
import org.ambraproject.Constants;
import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.AdminWebTest;
import org.ambraproject.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.RatingContent;

import java.net.URI;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;

/**
 * @author Alex Kudlick 2/6/12
 */
public class ViewRatingActionTest extends AdminWebTest {

  @Autowired
  protected ViewRatingAction action;
  @Autowired
  protected UserService userService;

  @Override
  protected BaseActionSupport getAction() {
    return action;
  }

  @Test
  public void testExecute() throws Exception {
    Rating rating = new Rating();
    rating.setAnnotates(URI.create("id:test-article-to-rate"));
    rating.setCreator("id:creator-of-rating");
    rating.setBody(new RatingContent());
    rating.getBody().setCommentTitle("Desperate Souls");
    rating.getBody().setCommentValue("Regina and Mr. Gold play dirty politics and take opposite sides when Emma " +
        "runs for a coveted Storybrooke public office against Sidney. Meanwhile, back in the fairytale world " +
        "that was, Rumplestiltskin tries to track down the ultimate power source in order to help his son " +
        "avert the horrors of a meaningless war.");
    dummyDataStore.store(rating);

    action.setRatingId(rating.getId().toString());
    final HashMap<String, Object> session = new HashMap<String, Object>();
    session.put(Constants.AMBRA_USER_KEY, userService.getUserByAuthId(DEFAULT_ADMIN_AUTHID));
    action.setSession(session);
    
    final String result = action.execute();
    
    
    assertEquals(result, Action.SUCCESS, "Action didn't return success");
    assertEquals(action.getRating().getId(), rating.getId(), "Action didn't return correct rating");
    assertEquals(action.getRating().getAnnotates(), rating.getAnnotates(), "Action didn't return correct rating creator");
    assertEquals(action.getRating().getAnnotates(), rating.getAnnotates(), "Action didn't return correct rating target");
    assertEquals(action.getRating().getBody().getCommentTitle(), rating.getBody().getCommentTitle(),
        "Action didn't return correct rating title");
    assertEquals(action.getRating().getBody().getCommentValue(), rating.getBody().getCommentValue(),
        "Action didn't return correct rating comment");
    assertEquals(action.getRating().getBody().getCIStatement(), rating.getBody().getCIStatement(),
        "Action didn't return correct rating CIStatement");
  }
}
