package org.ambraproject.admin.service.impl;

import org.ambraproject.admin.service.PingbackViewService;
import org.ambraproject.service.hibernate.HibernateServiceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PingbackViewServiceImpl extends HibernateServiceImpl implements PingbackViewService {

  private static final String PINGBACKS_BY_DATE_HQL
      = "select p.url, p.title, a.url, a.title, p.created "
      + "from Pingback as p, Article as a where p.articleID = a.ID order by p.created asc";
  private static final String ARTICLES_BY_PINGBACK_COUNT_HQL
      = "select a.url, a.title, count(distinct p.ID) as c "
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
      view.add(new PingbackWithArticle(
          (String) result[0],
          (String) result[1],
          (String) result[2],
          (String) result[3],
          (Date) result[4]));
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
      view.add(new ArticleWithPingbackCount(
          (String) result[0],
          (String) result[1],
          (Long) result[2]));
    }
    return view;
  }

}
