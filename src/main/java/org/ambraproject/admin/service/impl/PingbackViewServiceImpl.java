package org.ambraproject.admin.service.impl;

import org.ambraproject.admin.service.PingbackViewService;
import org.ambraproject.models.Article;
import org.ambraproject.models.Pingback;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

public class PingbackViewServiceImpl extends HibernateServiceImpl implements PingbackViewService {
  private static final Logger log = LoggerFactory.getLogger(PingbackViewServiceImpl.class);

  private static final String PINGBACKS_BY_DATE_HQL
      = "from Pingback as p, Article as a where p.articleID = a.ID order by p.created asc";
  private static final String ARTICLES_BY_PINGBACK_COUNT_HQL
      = "select a, count(distinct p.ID) as c "
      + "from Pingback as p, Article as a where p.articleID = a.ID order by c desc";

  private static <E> List<E> forceRandomAccess(List<E> list) {
    return (list instanceof RandomAccess) ? list : new ArrayList<E>(list);
  }

  @Override
  public List<PingbackWithArticle> listPingbacksByDate() {
    final List<?> results = forceRandomAccess(hibernateTemplate.find(PINGBACKS_BY_DATE_HQL));
    return new AbstractList<PingbackWithArticle>() {
      @Override
      public PingbackWithArticle get(int index) {
        final Object[] result = (Object[]) results.get(index);
        return new PingbackWithArticle((Pingback) result[0], (Article) result[1]);
      }

      @Override
      public int size() {
        return results.size();
      }
    };
  }

  @Override
  public List<ArticleWithPingbackCount> listArticlesByPingbackCount() {
    final List<?> results = forceRandomAccess(hibernateTemplate.find(ARTICLES_BY_PINGBACK_COUNT_HQL));
    return new AbstractList<ArticleWithPingbackCount>() {
      @Override
      public ArticleWithPingbackCount get(int index) {
        final Object[] result = (Object[]) results.get(index);
        return new ArticleWithPingbackCount((Article) result[0], (Long) result[1]);
      }

      @Override
      public int size() {
        return results.size();
      }
    };
  }

}
