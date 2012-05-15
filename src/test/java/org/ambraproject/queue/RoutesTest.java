package org.ambraproject.queue;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alex Kudlick 5/9/12
 */
@ContextConfiguration
public class RoutesTest extends AbstractTestNGSpringContextTests {

  @EndpointInject(uri = "mock:mail")
  protected MockEndpoint mailEndpoint;

  @Autowired
  protected DummyArticleIndexingService articleIndexingService;

  @Autowired
  @Qualifier("pmcResponseConsumer")
  protected DummyResponseConsumer responseConsumer;

  @Test
  @DirtiesContext
  public void testIndexCron() throws InterruptedException {
    mailEndpoint.setExpectedMessageCount(2);
    Thread.sleep(3000);

    assertEquals(articleIndexingService.getIndexAllCount(), 2,
        "Cron didn't index all correct number of times");
    mailEndpoint.assertIsSatisfied();
  }
}
