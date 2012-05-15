package org.ambraproject.admin;

import org.ambraproject.admin.service.OnCrossPubListener;

/**
 * @author Alex Kudlick 5/2/12
 */
public class DummyOnCrossPublishListener implements OnCrossPubListener {

  private int invocationCount = 0;

  @Override
  public void articleCrossPublished(String articleId) throws Exception {
    invocationCount++;
  }

  public int getInvocationCount() {
    return invocationCount;
  }

  public void reset() {
    invocationCount = 0;
  }
}
