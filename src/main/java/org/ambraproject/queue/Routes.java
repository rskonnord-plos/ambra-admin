/*
 * $HeadURL$
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

package org.ambraproject.queue;

import org.apache.camel.LoggingLevel;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Dragisa Krsmanovic
 */
public class Routes extends SpringRouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(Routes.class);

  private Configuration configuration;
  private long redeliveryInterval = 600l; // 10 minutes
  public static final String SEARCH_INDEXALL = "seda:search.indexall";
  public static final String SEARCH_INDEX = "seda:search.index";
  public static final String SEARCH_DELETE = "seda:search.delete";
  public static final String AE_REINDEX = "seda:ae.index";
  public static final String REFRESH_REFERENCES = "seda:refresh.references";

  private String mailEndpoint;

  /**
   * <p>
   * Setter method for configuration. Injected through Spring.
   * </p>
   * <p>
   * Response queues are obtained from configuration file.
   * Beans that consume response queue are named <target_lowercase>ResponseConsumer and should already
   * be defined in Spring context. For example for PMC, consumer bean is named "pmcResponseConsumer".
   * </p>
   * <p>
   * In addition to normal route, two routes for testing are configuret for each target:
   * <ol>
   * <li>direct:test<target>Ok - loopback route that always returs success.</li>
   * <li>direct:test<target>Fail - loopback route that always returns failuer.</li>
   * </ol>
   * </p>
   *
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Required
  public void setMailEndpoint(String mailEndpoint) {
    this.mailEndpoint = mailEndpoint;
  }

  /**
   * Redelivery interval. In case an error is thrown during processing of incoming message, how long do we wait
   * to retry processing.
   *
   * @param redeliveryInterval Interval in seconds. Default is 600 (10 minutes).
   */
  public void setRedeliveryInterval(long redeliveryInterval) {
    this.redeliveryInterval = redeliveryInterval;
  }

  public void configure() throws Exception {

    @SuppressWarnings("unchecked")
    List<HierarchicalConfiguration> syndications = ((HierarchicalConfiguration) configuration)
        .configurationsAt("ambra.services.syndications.syndication");

    if (syndications != null) {

      onException(Exception.class)
          .handled(true)
          .retryAttemptedLogLevel(LoggingLevel.ERROR)
          .retriesExhaustedLogLevel(LoggingLevel.ERROR)
          .maximumRedeliveries(-1) // redeliver forever
          .maximumRedeliveryDelay(redeliveryInterval * 1000l)
          .redeliveryDelay(redeliveryInterval * 1000l);

      for (HierarchicalConfiguration syndication : syndications) {
        String target = syndication.getString("[@target]");
        String name = syndication.getString("name");
        String responseQueue = syndication.getString("responseQueue", null);
        log.info("Creating routes for " + name);

        String beanName = target.toLowerCase() + "ResponseConsumer";

        if (responseQueue != null) {
          log.info("Setting consumer for response queue " + responseQueue + " to " + beanName);
          from(responseQueue)
              .transacted()
              .to("bean:" + beanName);
        }
      }
    }

    String searchIndexingQueue = configuration.getString("ambra.services.search.articleIndexingQueue", null);
    if (searchIndexingQueue != null) {
      log.info("Creating article search indexing route");
      // As set in default exception handler: redeliver forever
      from(SEARCH_INDEX)
          .to("log:org.ambraproject.queue.search.delete.MessageReceived?level=INFO" +
              "&showBodyType=false" +
              "&showBody=false" +
              "&showExchangeId=true")
          .to(searchIndexingQueue);
    } else {
      log.warn("Search index queue not defined. No route created.");
    }

    String searchDeleteQueue = configuration.getString("ambra.services.search.articleDeleteQueue", null);
    if (searchDeleteQueue != null) {
      log.info("Creating article search deleting route");
      // As set in default exception handler: redeliver forever
      from(SEARCH_DELETE)
          .to("log:org.ambraproject.queue.search.index.MessageReceived?level=INFO" +
              "&showBodyType=false" +
              "&showBody=false" +
              "&showExchangeId=true")
          .to(searchDeleteQueue);
    } else {
      log.warn("Search delete queue not defined. No route created.");
    }

    String searchMailReceiver = configuration.getString("ambra.services.search.indexingMailReceiver", null);
    if (searchMailReceiver != null) {
      String ambraHost = configuration.getString("ambra.network.hosts.default");

      log.info("Creating index all articles route");
      from(SEARCH_INDEXALL)
          .onException(Exception.class)
            .handled(true)
            .maximumRedeliveries(0) // do not retry
            .setHeader("to", constant(searchMailReceiver))
            .setHeader("from", constant("do-not-reply@plos.org"))
            .setHeader("subject", constant("Failed to queue any articles for indexing (" + ambraHost + ")"))
            .setBody(exceptionMessage())
            .to(mailEndpoint)
          .end()
          .to("log:org.ambraproject.queue.search.indexall.MessageReceived?level=INFO" +
              "&showBodyType=false" +
              "&showBody=false" +
              "&showExchangeId=true")
          .to("bean:indexingService?method=indexAllArticles")
          .setHeader("to", constant(searchMailReceiver))
          .setHeader("from", constant("do-not-reply@plos.org"))
          .setHeader("subject", constant("Finished queueing articles for indexing (" + ambraHost + ")"))
          .to(mailEndpoint);

      String solrIndexCron = configuration.getString("ambra.services.search.solrIndexCron", null);
      if (solrIndexCron != null) {
        log.info("Configuring cron for indexing all articles with value: {}", solrIndexCron);
        from("quartz://ambra/indexAll?cron=" + solrIndexCron)
            .to(SEARCH_INDEXALL);
      } else {
        log.warn("ambra.services.search.solrIndexCron not defined. Not creating automatic indexing route.");
      }
    } else {
      log.warn("ambra.services.search.indexingMailReceiver not set. Index all queue not created.");
    }

    String aeIndexMailReceiver =
      configuration.getString("ambra.services.academic-editor-reindex.indexingMailReceiver", null);

    if (aeIndexMailReceiver != null) {
      String ambraHost = configuration.getString("ambra.network.hosts.default");

      log.info("Creating AE reindex route");
      from(AE_REINDEX)
        .onException(Exception.class)
          .handled(true)
          .maximumRedeliveries(0) // do not retry
          .setHeader("to", constant(aeIndexMailReceiver))
          .setHeader("from", constant("do-not-reply@plos.org"))
          .setHeader("subject", constant("Failed to run AE Reindex (" + ambraHost + ")"))
          .setBody(exceptionMessage())
          .to(mailEndpoint)
        .end()
        .to("log:org.ambraproject.queue.ae.reindex.MessageReceived?level=INFO" +
          "&showBodyType=false" +
          "&showBody=false" +
          "&showExchangeId=true")
        .to("bean:indexingService?method=reindexAcademicEditors")
        .setHeader("to", constant(aeIndexMailReceiver))
        .setHeader("from", constant("do-not-reply@plos.org"))
        .setHeader("subject", constant("Finished AE Reindex (" + ambraHost + ")"))
        .to(mailEndpoint);

      String aeIndexCron = configuration.getString("ambra.services.academic-editor-reindex.reindex-cron", null);
      if (aeIndexCron != null) {
        log.info("Configuring cron for indexing all articles with value: {}", aeIndexCron);
        from("quartz://ambra/aeReindex?cron=" + aeIndexCron)
          .to(AE_REINDEX);
      } else {
        log.warn("ambra.services.academic-editor-reindex.reindex-cron not defined. Not creating automatic indexing route.");
      }
    } else {
      log.warn("ambra.services.academic-editor-reindex.indexingMailReceiver not set. Reindex queue not created.");
    }

    String refreshCitedArticlesQueue = configuration.getString("ambra.services.queue.refreshCitedArticles", null);

    if (refreshCitedArticlesQueue != null) {
      log.info("Creating article search indexing route");

      from(REFRESH_REFERENCES)
        .to("log:org.ambraproject.queue.refresh.references.MessageReceived?level=INFO" +
          "&showBodyType=false" +
          "&showBody=false" +
          "&showExchangeId=true")
        .to(refreshCitedArticlesQueue);
    } else {
      log.warn("ambra.services.queue.refreshCitedArticles not set, refresh cited articles queue not defined. No route created.");
    }
  }
}
