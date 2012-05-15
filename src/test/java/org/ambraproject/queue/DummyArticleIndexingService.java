package org.ambraproject.queue;

import org.ambraproject.search.service.ArticleIndexingService;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alex Kudlick 5/9/12
 */
public class DummyArticleIndexingService implements ArticleIndexingService {
  private int indexAllCount = 0;
  private List<String> indexedArticles = new ArrayList<String>();

  @Override
  public synchronized void indexArticle(String articleId) throws Exception {
    indexedArticles.add(articleId);
  }

  @Override
  public synchronized void startIndexingAllArticles() throws Exception {
    indexAllCount++;
  }

  @Override
  public synchronized String indexAllArticles() throws Exception {
    indexAllCount++;
    return "Successfully indexed 123 articles";
  }

  public synchronized int getIndexAllCount() {
    return indexAllCount;
  }

  public synchronized List<String> getIndexedArticles() {
    return Collections.unmodifiableList(indexedArticles);
  }

  @Override
  public void articlePublished(String articleId) throws Exception {

  }

  @Override
  public void articleCrossPublished(String articleId) throws Exception {

  }

  @Override
  public void setAmbraConfiguration(Configuration ambraConfiguration) {

  }

  @Override
  public void articleDeleted(String articleId) throws Exception {

  }
}
