package org.ambraproject.queue;

import org.apache.camel.Body;
import org.apache.camel.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alex Kudlick 5/10/12
 */
public class DummyResponseConsumer {

  private List<String> responses = new ArrayList<String>();

  @Handler
  public synchronized void handleResponse(@Body String body) throws Exception {
    responses.add(body);
  }

  public synchronized List<String> getResponses() {
    return Collections.unmodifiableList(responses);
  }

}
