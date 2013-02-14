package org.ambraproject.queue;

import org.ambraproject.search.service.IndexingService;
import org.apache.commons.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alex Kudlick 5/9/12
 */
public class DummyIndexingService implements IndexingService {
  private int indexAllCount = 0;
  private List<String> indexedArticles = new ArrayList<String>();

  @Override
  public synchronized void indexArticle(String articleId) throws Exception {
    indexedArticles.add(articleId);
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

  @Override
  public void reindexAcademicEditors() throws Exception {

  }
}
