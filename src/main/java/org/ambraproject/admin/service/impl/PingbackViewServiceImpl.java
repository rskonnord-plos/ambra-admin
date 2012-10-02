package org.ambraproject.admin.service.impl;

import org.ambraproject.admin.service.PingbackViewService;
import org.ambraproject.models.Article;
import org.ambraproject.models.Pingback;
import org.ambraproject.service.hibernate.HibernateServiceImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PingbackViewServiceImpl extends HibernateServiceImpl implements PingbackViewService {

  private static final String PINGBACKS_BY_DATE_HQL
      = "from Pingback as p, Article as a where p.articleID = a.ID order by p.created asc";
  private static final String ARTICLES_BY_PINGBACK_COUNT_HQL
      = "select a, count(distinct p.ID) as c "
      + "from Pingback as p, Article as a where p.articleID = a.ID order by c desc";

  /**
   * {@inheritDoc}
   */
  @Override
  public List<PingbackWithArticle> listPingbacksByDate() {
    List<?> results = hibernateTemplate.find(PINGBACKS_BY_DATE_HQL);
    List<PingbackWithArticle> view = new ArrayList<PingbackWithArticle>(results.size());
    for (Iterator<?> iterator = results.iterator(); iterator.hasNext(); ) {
      Object[] result = (Object[]) iterator.next();
      view.add(new PingbackWithArticle((Pingback) result[0], (Article) result[1]));
    }
    return view;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ArticleWithPingbackCount> listArticlesByPingbackCount() {
    List<?> results = hibernateTemplate.find(ARTICLES_BY_PINGBACK_COUNT_HQL);
    List<ArticleWithPingbackCount> view = new ArrayList<ArticleWithPingbackCount>(results.size());
    for (Iterator<?> iterator = results.iterator(); iterator.hasNext(); ) {
      Object[] result = (Object[]) iterator.next();
      view.add(new ArticleWithPingbackCount((Article) result[0], (Long) result[1]));
    }
    return view;
  }

}
