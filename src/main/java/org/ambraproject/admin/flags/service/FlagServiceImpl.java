/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.flags.service;

import org.ambraproject.admin.views.FlagView;
import org.ambraproject.service.cache.Cache;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationCitation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.Flag;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alex Kudlick 3/23/12
 */
public class FlagServiceImpl extends HibernateServiceImpl implements FlagService {

  private static final Logger log = LoggerFactory.getLogger(FlagServiceImpl.class);

  private Cache articleHtmlCache;

  @Required
  public void setArticleHtmlCache(Cache articleHtmlCache) {
    this.articleHtmlCache = articleHtmlCache;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<FlagView> getFlaggedComments() {
    log.debug("Loading up all flagged annotations");
    List<Flag> flags = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Flag.class)
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .addOrder(Order.asc("created"))
    );

    log.debug("Found {} flagged annotations", flags.size());
    List<FlagView> results = new ArrayList<FlagView>(flags.size());
    for (Flag flag : flags) {
      results.add(new FlagView(flag));
    }
    return results;
  }

  @Override
  public void deleteFlags(Long... flagIds) {
    log.debug("Removing flags: {}", Arrays.toString(flagIds));
    hibernateTemplate.deleteAll(hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Flag.class)
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .add(Restrictions.in("ID", flagIds))
    ));
  }

  /**
   * Delete comment by id
   * @param commentIds the ids of the comment to be deleted
   */

  @Override
  public void deleteFlagAndComment(Long... commentIds) {
    log.debug("Removing comments and associated flag for flagId: {}", Arrays.toString(commentIds));

    //get all the flags for given flag id
    List<Flag> flags = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Flag.class)
            .add(Restrictions.in("ID", commentIds))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    );

    Map<Long,Annotation> annotationMap = new HashMap<Long, Annotation>();

    Set<Flag> flagsToDelete = new HashSet<Flag>();

    //for each flag get the associated annotation object and make a list of it.
    //also get all the other flags related to this comment
    for (Flag flag : flags) {
      Annotation annotation = flag.getFlaggedAnnotation();
      annotationMap.put(annotation.getID(), annotation);

      //make a list of flags to delete
      List<Flag> flags1 = hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Flag.class)
              .createAlias("flaggedAnnotation","a")
              .add(Restrictions.eq("a.ID", annotation.getID()))
              .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
      );

      flagsToDelete.addAll(flags1);
    }

    //first delete all the flags
    hibernateTemplate.deleteAll(flagsToDelete);

    // for each comment delete the comment tree
    for(Iterator it=annotationMap.entrySet().iterator(); it.hasNext();) {
      Annotation annotation = (Annotation)((Map.Entry)it.next()).getValue();
      listDeleteCommentTree(annotation);
      //if we deleted an inline note, kick the article out of cache
      if (annotation.getType() == AnnotationType.NOTE) {
        String doi = getArticleDoi(annotation);
        articleHtmlCache.remove(doi);
      }
    }
  }

  private String getArticleDoi(Annotation annotation) {
    return (String) hibernateTemplate.findByCriteria(
              DetachedCriteria.forClass(Article.class)
                  .add(Restrictions.eq("ID", annotation.getArticleID()))
                  .setProjection(Projections.property("doi"))
          ).get(0);
  }

  /**
   * Delete the comment and their children
   * @param annotation
   */
  public void listDeleteCommentTree(Annotation annotation) {

    //get all the children of the this annotation   
    List<Annotation> annotationChild = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Annotation.class)
            .add(Restrictions.eq("parentID", annotation.getID()))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    );

    //check if child has more child and if yes again call this method to get the child list
    if(annotationChild.size() > 0) {
      for(Annotation annotation1: annotationChild) {
        listDeleteCommentTree(annotation1);
      }
    }

    //now delete the annotation
    hibernateTemplate.delete(annotation);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void convertToType(AnnotationType newType, Long... flagIds) {
    List<Flag> flags = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Flag.class)
            .add(Restrictions.in("ID", flagIds))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    );

    for (Flag flag : flags) {
      Annotation annotation = flag.getFlaggedAnnotation();
      annotation.setType(newType);
      log.debug("Converting annotation {} to {}", annotation.getID(), newType);
      
      if (annotation.getAnnotationCitation() != null) {
        //if this is already a correction and we're converting back to a note, delete the citation
        hibernateTemplate.delete(annotation.getAnnotationCitation());
        annotation.setAnnotationCitation(null);
      }

      if (newType.isCorrection() && !(newType == AnnotationType.MINOR_CORRECTION)) {
        Article article = (Article) hibernateTemplate.get(Article.class, annotation.getArticleID());
        log.debug("Creating citation for article {}", article.getDoi());
        annotation.setAnnotationCitation(new AnnotationCitation(article));
      }
      hibernateTemplate.delete(flag);
      hibernateTemplate.update(annotation);
      articleHtmlCache.remove(getArticleDoi(annotation));
    }
  }
}
