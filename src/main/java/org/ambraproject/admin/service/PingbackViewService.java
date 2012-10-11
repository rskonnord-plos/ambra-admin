package org.ambraproject.admin.service;

import org.ambraproject.service.hibernate.HibernateService;

import java.util.Date;
import java.util.List;

/**
 * Produces overviews of all pingbacks in the system.
 *
 * @see org.ambraproject.admin.action.ViewPingbacksAction
 */
public interface PingbackViewService extends HibernateService {

  /**
   * Produce a list of all pingbacks stored in the system, paired with the articles that they belong to, sorted by the
   * pingbacks' creation timestamps in ascending order.
   *
   * @return the list of pingback-article pairs
   */
  public abstract List<PingbackWithArticle> listPingbacksByDate();

  /**
   * Product a list of all articles that have at least one pingback, each paired with the number of pingbacks it has,
   * sorted by pingback count in descending order.
   *
   * @return the list of articles with their pingback counts
   */
  public abstract List<ArticleWithPingbackCount> listArticlesByPingbackCount();

  /**
   * A pingback paired with the article it belongs to.
   */
  public static class PingbackWithArticle {
    private final String sourceUrl;
    private final String sourceTitle;
    private final String articleUrl;
    private final String articleTitle;
    private final Date timestamp;

    public PingbackWithArticle(String sourceUrl, String sourceTitle, String articleUrl, String articleTitle, Date timestamp) {
      this.sourceUrl = sourceUrl;
      this.sourceTitle = sourceTitle;
      this.articleUrl = articleUrl;
      this.articleTitle = articleTitle;
      this.timestamp = timestamp;
    }

    public String getSourceUrl() {
      return sourceUrl;
    }

    public String getSourceTitle() {
      return sourceTitle;
    }

    public String getArticleUrl() {
      return articleUrl;
    }

    public String getArticleTitle() {
      return articleTitle;
    }

    public Date getTimestamp() {
      return timestamp;
    }
  }

  /**
   * An article paired with the number of pingbacks that it has.
   */
  public static class ArticleWithPingbackCount {
    private final String articleUrl;
    private final String articleTitle;
    private final long pingbackCount;

    public ArticleWithPingbackCount(String articleUrl, String articleTitle, long pingbackCount) {
      this.articleUrl = articleUrl;
      this.articleTitle = articleTitle;
      this.pingbackCount = pingbackCount;
    }

    public String getArticleUrl() {
      return articleUrl;
    }

    public String getArticleTitle() {
      return articleTitle;
    }

    public long getPingbackCount() {
      return pingbackCount;
    }
  }

}
