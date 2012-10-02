package org.ambraproject.admin.service;

import org.ambraproject.models.Article;
import org.ambraproject.models.Pingback;
import org.ambraproject.service.hibernate.HibernateService;

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
    private final Pingback pingback;
    private final Article article;

    public PingbackWithArticle(Pingback pingback, Article article) {
      this.pingback = pingback;
      this.article = article;
    }

    public Pingback getPingback() {
      return pingback;
    }

    public Article getArticle() {
      return article;
    }
  }

  /**
   * An article paired with the number of pingbacks that it has.
   */
  public static class ArticleWithPingbackCount {
    private final Article article;
    private final long pingbackCount;

    public ArticleWithPingbackCount(Article article, long pingbackCount) {
      this.article = article;
      this.pingbackCount = pingbackCount;
    }

    public Article getArticle() {
      return article;
    }

    public long getPingbackCount() {
      return pingbackCount;
    }
  }

}
