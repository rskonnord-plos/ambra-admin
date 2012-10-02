package org.ambraproject.admin.service;

import org.ambraproject.models.Article;
import org.ambraproject.models.Pingback;
import org.ambraproject.service.hibernate.HibernateService;

import java.util.List;

public interface PingbackViewService extends HibernateService {

  public abstract List<PingbackWithArticle> listPingbacksByDate();

  public abstract List<ArticleWithPingbackCount> listArticlesByPingbackCount();

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
