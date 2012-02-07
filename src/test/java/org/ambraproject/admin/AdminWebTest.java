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

package org.ambraproject.admin;

import org.ambraproject.BaseWebTest;
import org.ambraproject.action.BaseActionSupport;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.HashSet;

/**
 * Common Base class for tests fo admin action classes. This allows us to specify a new config location for the context.xml.
 * Subclasses must override {@link #getAction()} so that this class can provide some default support for before/after tests.
 * <p/>
 * By default, it sets up an admin context, and sets the default request on the actions.  Tests that wish to use a user context (e.g. for checking
 * that permissions get denied) must explicitly call {@link org.ambraproject.BaseWebTest#setupUserContext()}
 * <p/>
 * <b>Note:</b> Since we clear out messages after methods, the action class tests are NOT threadsafe. In order to be threadsafe, a test
 * would have to create an instance of the action within each test method.
 *
 * @author Alex Kudick  1/11/12
 */
@ContextConfiguration(locations = "adminWebContext.xml")
public abstract class AdminWebTest extends BaseWebTest {

  protected abstract BaseActionSupport getAction();

  @Override
  public void setDefaultRequest() {
    setupAdminContext();
    getAction().setRequest(getDefaultRequestAttributes());
  }

}
