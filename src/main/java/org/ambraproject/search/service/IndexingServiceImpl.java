/*
 * $HeadURL: http://svn.ambraproject.org/svn/ambra/ambra-admin/branches/january_fixes/src/main/java/org/ambraproject/search/service/ArticleIndexingServiceImpl.java $
 * $Id: ArticleIndexingServiceImpl.java 12312 2012-11-19 21:37:01Z josowski $
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

package org.ambraproject.search.service;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVEntryConverter;
import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;
import org.ambraproject.service.search.SolrHttpService;
import org.ambraproject.views.AcademicEditorView;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.ambraproject.ApplicationException;
import org.ambraproject.admin.service.OnCrossPubListener;
import org.ambraproject.admin.service.OnDeleteListener;
import org.ambraproject.admin.service.OnPublishListener;
import org.ambraproject.article.service.ArticleDocumentService;
import org.ambraproject.queue.MessageSender;
import org.ambraproject.queue.Routes;
import org.ambraproject.service.raptor.RaptorService;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.models.Article;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.util.*;

/**
 * Service class for indexing SOLR dat. It is plugged in as OnPublishListener into
 * DocumentManagementService.
 *
 * @author Bill OConnor
 * @author Dragisa Krsmanovic
 * @author Joe Osowski
 */
public class IndexingServiceImpl extends HibernateServiceImpl
  implements OnPublishListener, OnDeleteListener, OnCrossPubListener, IndexingService {

  private static final Logger log = LoggerFactory.getLogger(IndexingServiceImpl.class);

  protected static final int DEFAULT_INCREMENT_LIMIT_SIZE = 200;

  private ArticleDocumentService articleDocumentService;
  private SolrHttpService solrHttpService;
  private RaptorService raptorService;
  private MessageSender messageSender;
  private String indexingQueue;
  private String deleteQueue;
  private int incrementLimitSize;

  @Required
  public void setArticleDocumentService(ArticleDocumentService articleDocumentService) {
    this.articleDocumentService = articleDocumentService;
  }

  @Required
  public void setMessageSender(MessageSender messageSender) {
    this.messageSender = messageSender;
  }

  @Required
  public void setRaptorService(RaptorService raptorService) {
    this.raptorService = raptorService;
  }

  @Required
  public void setSolrHttpService(SolrHttpService solrHttpService) {
    this.solrHttpService = solrHttpService;
  }

  @Required
  public void setAmbraConfiguration(Configuration ambraConfiguration) {
    indexingQueue = ambraConfiguration.getString("ambra.services.search.articleIndexingQueue", null);
    if (indexingQueue != null && indexingQueue.trim().length() == 0) {
      indexingQueue = null;
    }
    log.info("Article indexing queue set to " + indexingQueue);

    deleteQueue = ambraConfiguration.getString("ambra.services.search.articleDeleteQueue", null);
    if (deleteQueue != null && deleteQueue.trim().length() == 0) {
      deleteQueue = null;
    }
    log.info("Article delete queue set to " + deleteQueue);

    incrementLimitSize = ambraConfiguration.getInt("ambra.services.search.incrementLimitSize",
      DEFAULT_INCREMENT_LIMIT_SIZE);
  }

  /**
   * Method that is fired on article publish operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that publish operation will succeed even if ActiveMQ is down.
   *
   * @see Routes
   * @param articleId ID of the published article
   * @throws Exception if message send fails
   */
  @Transactional(readOnly = true)
  public void articlePublished(String articleId) throws Exception {
    if (indexingQueue != null) {
      log.info("Indexing published article " + articleId);
      indexOneArticle(articleId);
    } else {
      log.warn("Article indexing queue not set. Article " + articleId + " will not be indexed.");
    }
  }

  /**
   * Method that is fired on article delete operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that delete operation will succeed even if ActiveMQ is down.
   *
   * @see Routes
   * @param articleId ID of the deleted article
   * @throws Exception if message send fails
   */
  public void articleDeleted(String articleId) throws Exception {
    if (deleteQueue != null) {
      log.info("Deleting article " + articleId + " from search index.");
      messageSender.sendMessage(Routes.SEARCH_DELETE, articleId);
    } else {
      log.warn("Article index delete queue not set. Article " + articleId + " will not be deleted from search index.");
    }
  }

  /**
   * Method that is fired on article cross publish operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that cross publish operation will succeed even if ActiveMQ is down.
   *
   * @see Routes
   * @param articleId ID of the cross published article
   * @throws Exception if message send fails
   */
  @Transactional(readOnly = true)
  public void articleCrossPublished(String articleId) throws Exception {
    if (indexingQueue != null) {
      log.info("Indexing cross published article " + articleId);
      indexArticle(articleId);
    } else {
      log.warn("Article indexing queue not set. Article " + articleId + " will not be re-indexed.");
    }
  }

  public void startIndexingAllArticles() throws Exception {
     // Message content is unimportant here
    messageSender.sendMessage(Routes.SEARCH_INDEXALL, "start");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void reindexAcademicEditors() throws Exception {
    List<AcademicEditorView> editors = this.raptorService.getAcademicEditor();

    Map<String, String> params = new HashMap<String, String>();
    params.put("separator","\t");
    params.put("f.ae_subject.split","true");
    params.put("f.ae_subject.separator",";");

    StringWriter sw = new StringWriter();
    CSVWriter csvWriter = new CSVWriterBuilder(sw)
      .entryConverter(new AcademicEditorConverter()).strategy(
      new CSVStrategy('\t', '\"', '#', false, true)).build();

    //Add the header row
    sw.write("id\tae_name\tae_last_name\tae_institute\tae_country\tae_subject\tdoc_type\tcross_published_journal_key\n");

    csvWriter.writeAll(editors);

    String csvData = sw.toString();

    //Post the updates
    this.solrHttpService.makeSolrPostRequest(params, csvData, true);

    //Delete old data
    this.solrHttpService.makeSolrPostRequest(
      Collections.<String,String>emptyMap(),
      "<delete><query>timestamp:[* TO NOW-1HOUR] AND doc_type:(academic_editor OR section_editor)</query></delete>",
      false);
  }

  /**
   * A parser class for the csv engine
   */
  private class AcademicEditorConverter implements CSVEntryConverter<AcademicEditorView> {
    @Override
    public String[] convertEntry(AcademicEditorView view) {
      List<String> result = new ArrayList<String>();

      result.add(view.getId());
      result.add(view.getName());
      result.add(view.getLastName());
      result.add(view.getInstitute());
      result.add(view.getCountry());
      result.add(StringUtils.join(view.getSubjects(), ';'));
      result.add(view.getType());
      result.add(view.getJournalKey());

      return result.toArray(new String[result.size()]);
    }
  }

  /**
   * Index one article. Disables filters so can be applied in any journal context.
   *
   * @param articleId Article ID
   * @throws Exception If operation fails
   */
  @Transactional(readOnly = true)
  public void indexArticle(String articleId) throws Exception {

    if (indexingQueue == null) {
      throw new ApplicationException("Article indexing queue not set. Article " + articleId + " will not be re-indexed.");
    }

    Document doc = articleDocumentService.getFullDocument(articleId);
    String strkImagURI = getStrikingImage(articleId);

    if (doc == null) {
      log.error("Search indexing failed for " + articleId + ". Returned document is NULL.");
      return;
    }
    doc = addStrikingImage(doc, strkImagURI);
    messageSender.sendMessage(indexingQueue, doc);
  }

  /**
   * Same as indexArticle() except that it doesn't disable filters.
   *
   * @param articleId Article ID
   * @throws Exception If operation fails
   */
  private void indexOneArticle(String articleId) throws Exception {

    Document doc = articleDocumentService.getFullDocument(articleId);
    String strkImagURI = getStrikingImage(articleId);

    if (doc == null) {
      log.error("Search indexing failed for " + articleId + ". Returned document is NULL.");
      return;
    }
    doc = addStrikingImage(doc, strkImagURI);
    messageSender.sendMessage(Routes.SEARCH_INDEX, doc);
  }

  /**
   *  Add the article-strkImg tag as a child  to the article-meta
   *  tag of the article ml.
   */
  private Document addStrikingImage(Document doc, String strkImagURI ) {
    NodeList metaNodeLst = doc.getElementsByTagName("article-meta");
    Node metaNode = metaNodeLst.item(0);
    Element strkImgElem = doc.createElement("article-strkImg");

    strkImgElem.setTextContent(strkImagURI);
    metaNode.appendChild(strkImgElem.cloneNode(true));
    return doc;
  }

  /**
   * Transfer object for 3 values
   */
  private static class Result {
    public final int total;
    public final int failed;
    public final boolean partialUpdate;

    private Result(int total, int failed, boolean partialUpdate) {
      this.total = total;
      this.failed = failed;
      this.partialUpdate = partialUpdate;
    }
  }

  /**
   * Return the striking image URI using the doi for the article.
   *
   * @param doi
   * @return striking image uri
   */
  private String getStrikingImage(String doi) {
    // get the list of articles
    List<String> articleStrkImg = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Article.class)
            .add(Restrictions.eq("state", Article.STATE_ACTIVE))
            .add(Restrictions.eq("doi", doi))
            .setProjection(Projections.property("strkImgURI")),
        0, incrementLimitSize
    );

    String rslt = "";
    if ((articleStrkImg.size() != 0) && (articleStrkImg.get(0) != null))
      rslt = articleStrkImg.get(0);

    return rslt;
  }

}
