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

package org.ambraproject.admin.filter;

import org.ambraproject.admin.AdminBaseTest;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author Alex Kudlick 1/30/12
 */
public class RedirectToAmbraFilterTest extends AdminBaseTest {

  //Make sure we load up the ambra test config
  @Autowired
  protected Configuration configuration;

  private RedirectToAmbraFilter filter;

  @BeforeMethod
  public void initFilter() throws ServletException {
    filter = new RedirectToAmbraFilter();
    filter.init(new MockFilterConfig());
  }
  
  @DataProvider(name = "journalContext")
  @SuppressWarnings("unchecked")
  public Object[][] getJournalContexts() {
    String defaultJournal = configuration.getString("ambra.virtualJournals.default");
    String defaultUrl = configuration.getString("ambra.virtualJournals." + defaultJournal + ".url");
    Collection<String> virtualJournals = configuration.getList("ambra.virtualJournals.journals");

    Object[][] results = new Object[virtualJournals.size() + 1][2];
    VirtualJournalContext unknownContext = new VirtualJournalContext(
        "SomeCrazyJournalThatShouldGoToDefault",
        defaultJournal,
        "http://",
        8080,
        "localhost",
        "/",
        virtualJournals);
    results[0] = new Object[]{unknownContext, defaultUrl};

    int i = 1;
    for (Object journal : configuration.getList("ambra.virtualJournals.journals")) {
      VirtualJournalContext journalContext = new VirtualJournalContext(
          journal.toString(),
          defaultJournal,
          "http://",
          8080,
          "localhost",
          "/",
          virtualJournals);
      String url = configuration.getString("ambra.virtualJournals." + journal + ".url");
      results[i] = new Object[]{journalContext, url};
      i++;
    }
    return results;
  }

  @Test(dataProvider = "journalContext")
  public void testFilter(VirtualJournalContext journalContext, String expectedRedirectUrl) throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();
    request.setAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT, journalContext);
    request.setRequestURI("/article");
    request.setQueryString("id=foo:bar");

    filter.doFilter(request, response, filterChain);
    assertEquals(response.getRedirectedUrl(), expectedRedirectUrl + "/article?id=foo:bar", "response didn't get redirected correctly");
  }
}
