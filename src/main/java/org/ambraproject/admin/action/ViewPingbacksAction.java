package org.ambraproject.admin.action;

import org.ambraproject.admin.service.PingbackViewService;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

public class ViewPingbacksAction extends BaseAdminActionSupport {

  private PingbackViewService pingbackViewService;

  @Required
  public void setPingbackViewService(PingbackViewService pingbackViewService) {
    this.pingbackViewService = pingbackViewService;
  }

  // Template output
  private List<PingbackViewService.PingbackWithArticle> pingbacksByDate;
  private List<PingbackViewService.ArticleWithPingbackCount> articlesWithPingbackCounts;

  public List<PingbackViewService.PingbackWithArticle> getPingbacksByDate() {
    return pingbacksByDate;
  }

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
