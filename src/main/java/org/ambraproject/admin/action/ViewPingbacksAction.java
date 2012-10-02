package org.ambraproject.admin.action;

import org.ambraproject.admin.service.PingbackViewService;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Populate a page with overviews of all pingbacks stored in the system.
 * <p/>
 * This is implemented on the basis of short-term needs, under the assumption that the total number of pingbacks will be
 * small enough that the code will not scale unduly poorly. It may need to be refactored to handle a larger number of
 * pingbacks (with pagination, for example) or removed entirely if use shows that it is impractical.
 */
public class ViewPingbacksAction extends BaseAdminActionSupport {

  private PingbackViewService pingbackViewService;

  @Required
  public void setPingbackViewService(PingbackViewService pingbackViewService) {
    this.pingbackViewService = pingbackViewService;
  }

  // Template output
  private List<PingbackViewService.PingbackWithArticle> pingbacksByDate;
  private List<PingbackViewService.ArticleWithPingbackCount> articlesWithPingbackCounts;

  /**
   * Getter required by FreeMarker template.
   *
   * @return a list of pingbacks and the articles they belong to, sorted by date
   */
  public List<PingbackViewService.PingbackWithArticle> getPingbacksByDate() {
    return pingbacksByDate;
  }

  /**
   * Getter required by FreeMarker template.
   *
   * @return a list of articles and their pingback counts, sorted by pingback count
   */
  public List<PingbackViewService.ArticleWithPingbackCount> getArticlesWithPingbackCounts() {
    return articlesWithPingbackCounts;
  }

  @Override
  public String execute() {
    pingbacksByDate = pingbackViewService.listPingbacksByDate();
    articlesWithPingbackCounts = pingbackViewService.listArticlesByPingbackCount();
    return SUCCESS;
  }

}
