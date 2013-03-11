/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.service.impl;

import org.ambraproject.models.ArticleRelationship;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.ambraproject.filestore.FSIDMapper;
import org.ambraproject.filestore.FileStoreService;
import org.ambraproject.models.Annotation;
import org.ambraproject.models.AnnotationType;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleView;
import org.ambraproject.models.Flag;
import org.ambraproject.models.Syndication;
import org.ambraproject.models.Trackback;
import org.ambraproject.models.UserRole.Permission;
import org.ambraproject.service.hibernate.HibernateServiceImpl;
import org.ambraproject.service.journal.JournalService;
import org.apache.commons.io.FileUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.ambraproject.admin.service.DocumentManagementService;
import org.ambraproject.admin.service.OnDeleteListener;
import org.ambraproject.admin.service.OnPublishListener;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.permission.PermissionsService;
import org.w3c.dom.Document;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author alan Manage documents on server. Ingest and access ingested documents.
 */
public class DocumentManagementServiceImpl extends HibernateServiceImpl implements DocumentManagementService {
  private static final Logger log = LoggerFactory.getLogger(DocumentManagementServiceImpl.class);

  private ArticleService articleService;
  private PermissionsService permissionsService;
  private FileStoreService fileStoreService;

  private String documentDirectory;
  private String ingestedDocumentDirectory;
  private String documentPrefix;
  private JournalService journalService;
  private String plosDoiUrl;
  private String plosEmail;

  private List<OnPublishListener> onPublishListeners;
  private List<OnDeleteListener> onDeleteListeners;
  private String xslDefaultTemplate;
  private Map<String, String> xslTemplateMap;


  /**
   * Set the xsl style sheet to use
   *
   * @param xslTemplateMap - A map of classpath-relative location of an xsl stylesheets to use to process the article
   *                       xml
   */
  @Required
  public void setXslTemplateMap(Map<String, String> xslTemplateMap) {
    this.xslTemplateMap = xslTemplateMap;
  }

  @Required
  public void setArticleService(final ArticleService articleService) {
    this.articleService = articleService;
  }

  @Required
  public void setDocumentDirectory(final String documentDirectory) {
    this.documentDirectory = documentDirectory;
  }

  @Required
  public void setDocumentPrefix(final String documentPrefix) {
    this.documentPrefix = documentPrefix;
  }

  public String getDocumentDirectory() {
    return documentDirectory;
  }

  public String getIngestedDocumentDirectory() {
    return this.ingestedDocumentDirectory;
  }

  /**
   * Set the {@link FileStoreService} to use to store files
   *
   * @param fileStoreService - the filestore to use
   */
  @Required
  public void setFileStoreService(FileStoreService fileStoreService) {
    this.fileStoreService = fileStoreService;
  }

  @Required
  public void setIngestedDocumentDirectory(final String ingestedDocumentDirectory) {
    this.ingestedDocumentDirectory = ingestedDocumentDirectory;
  }

  public void setOnPublishListeners(List<OnPublishListener> onPublishListeners) {
    this.onPublishListeners = onPublishListeners;
  }

  public void setOnDeleteListeners(List<OnDeleteListener> onDeleteListeners) {
    this.onDeleteListeners = onDeleteListeners;
  }

  /**
   * Setter for XSL Templates.  Takes in a string as the filename and searches for it in resource path and then as a
   * URI.
   *
   * @param xslTemplate The xslTemplate to set.
   * @throws java.net.URISyntaxException
   */
  @Required
  public void setXslDefaultTemplate(String xslTemplate) throws URISyntaxException {
    this.xslDefaultTemplate = xslTemplate;
  }

  /**
   * Unpublishes an article
   *
   * @param objectURI URI of the article to delete
   * @throws Exception if id is invalid or Sending of delete message failed.
   */
  @Override
  @Transactional(rollbackFor = {Throwable.class})
  public void unPublish(String objectURI, final String authId) throws Exception {
    permissionsService.checkPermission(Permission.INGEST_ARTICLE, authId);

    URI id = URI.create(objectURI);

    articleService.setState(objectURI, authId, Article.STATE_UNPUBLISHED);

    removeFromCrossPubbedJournals(id);

    //When an article is 'unpublished'
    //It should be deleted from any places where it has been syndicated to
    invokeOnDeleteListeners(objectURI);
  }

  /**
   * Disables an article, when an article is disabled, it should not appear in the system
   *
   * @param objectURI URI of the article to delete
   * @throws Exception if id is invalid or Sending of delete message failed.
   */
  @Override
  @Transactional(rollbackFor = {Throwable.class})
  public void disable(String objectURI, final String authId) throws Exception {
    permissionsService.checkPermission(Permission.INGEST_ARTICLE, authId);

    URI id = URI.create(objectURI);

    articleService.setState(objectURI, authId, Article.STATE_DISABLED);

    removeFromCrossPubbedJournals(id);

    //When an article is 'disabled' it should be deleted from any places where it has been
    //syndicated to and removed from the file store
    //The only way to re-enable this in a correct way is to re-ingest the article.
    removeFromFileSystem(objectURI);
    invokeOnDeleteListeners(objectURI);
  }

  @Override
  @Transactional(rollbackFor = {Throwable.class})
  public void delete(String articleDoi, final String authId) throws Exception {
    permissionsService.checkPermission(Permission.DELETE_ARTICLES, authId);

    log.debug("Deleting Article:" + articleDoi);

    //delete the article from the db
    List articles = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Article.class)
            .add(Restrictions.eq("doi", articleDoi))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY));

    if (articles.size() == 0) {
      throw new NoSuchArticleIdException(articleDoi);
    }

    //delete any views on the article
    hibernateTemplate.deleteAll(
        hibernateTemplate.findByCriteria(
            DetachedCriteria.forClass(ArticleView.class)
                .add(Restrictions.eq("articleID", ((Article) articles.get(0)).getID()))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
        )
    );

    //delete any trackbacks on the article
    hibernateTemplate.deleteAll(
        hibernateTemplate.findByCriteria(
            DetachedCriteria.forClass(Trackback.class)
                .add(Restrictions.eq("articleID", ((Article) articles.get(0)).getID()))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
        )
    );

    //delete any syndications on the article
    hibernateTemplate.deleteAll(
        hibernateTemplate.findByCriteria(
            DetachedCriteria.forClass(Syndication.class)
                .add(Restrictions.eq("doi", ((Article) articles.get(0)).getDoi()))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY))
    );

    //unlink any foreign article relationships
    hibernateTemplate.bulkUpdate("update ArticleRelationship set otherArticleID = null where otherArticleID = ?",
        ((Article) articles.get(0)).getID());

    //delete any annotations on the article (need to do this recursivly b/c of replies
    List<Annotation> topLevelAnnotations = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Annotation.class)
            .add(Restrictions.eq("articleID", ((Article) articles.get(0)).getID()))
            .add(Restrictions.ne("type", AnnotationType.REPLY))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    );

    for (Annotation annotation : topLevelAnnotations) {
      deleteRepliesRecursively(annotation);
    }

    hibernateTemplate.delete(articles.get(0));


    removeFromFileSystem(articleDoi);

    invokeOnDeleteListeners(articleDoi);
  }

  @Transactional
  private void deleteRepliesRecursively(Annotation annotation) {
    //delete any flags on the annotation first
    hibernateTemplate.deleteAll(hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Flag.class)
            .createAlias("flaggedAnnotation", "an")
            .add(Restrictions.eq("an.ID", annotation.getID()))
            .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
    ));
    List<Annotation> replies = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Annotation.class)
            .add(Restrictions.eq("parentID", annotation.getID()))
    );
    for (Annotation reply : replies) {
      deleteRepliesRecursively(reply);
    }
    hibernateTemplate.delete(annotation);
  }

  @Override
  public void removeFromFileSystem(String articleUri) throws Exception {
    String articleRoot = FSIDMapper.zipToFSID(articleUri, "");
    Map<String, String> files = fileStoreService.listFiles(articleRoot);

    for (String file : files.keySet()) {
      String fullFile = FSIDMapper.zipToFSID(articleUri, file);
      fileStoreService.deleteFile(fullFile);
    }

    //We leave the directory in place as mogile doesn't really support removing of keys
    //fileStoreService.deleteFile(articleRoot);
  }

  /**
   * Revert the data out of the ingested queue
   *
   * @param uri the article uri
   * @throws java.io.IOException on an error
   */
  @Override
  public void revertIngestedQueue(String uri) throws IOException {
    // delete any crossref submission file
    File queueDir = new File(documentDirectory);
    File ingestedDir = new File(ingestedDocumentDirectory);
    File ingestedXmlFile = new File(ingestedDir, uri.replaceAll("[:/.]", "_") + ".xml");

    if (log.isDebugEnabled())
      log.debug("Deleting '" + ingestedXmlFile + "'");

    try {
      FileUtils.forceDelete(ingestedXmlFile);
    } catch (FileNotFoundException fnfe) {
      log.info("'" + ingestedXmlFile + "' does not exist - cannot delete: ", fnfe);
    }

    // move zip back to ingestion queue
    if (!queueDir.equals(ingestedDir)) {
      // strip 'info:doi/10.1371/journal.'
      String fname = uri.substring(documentPrefix.length()) + ".zip";
      File fromFile = new File(ingestedDir, fname);
      File toFile = new File(queueDir, fname);

      try {
        if (log.isDebugEnabled())
          log.debug("Moving '" + fromFile + "' to '" + toFile + "'");
        FileUtils.moveFile(fromFile, toFile);
      } catch (FileNotFoundException fnfe) {
        log.info("Could not move '" + fromFile + "' to '" + toFile + "': ", fnfe);
      }
    }
  }

  /**
   * @return List of filenames of files in uploadable directory on server
   */
  @Override
  public List<String> getUploadableFiles() {
    List<String> documents = new ArrayList<String>();
    File dir = new File(documentDirectory);
    if (dir.isDirectory()) {
      Collections.addAll(documents, dir.list(new FilenameFilter() {
        //check the file extensions

        public boolean accept(File file, String fileName) {
          for (String extension : new String[]{".tar", ".tar.bz", ".tar.bz2",
              ".tar.gz", ".tb2", ".tbz", ".tbz2", ".tgz", ".zip"}) {
            if (fileName.endsWith(extension)) {
              return true;
            }
          }
          return false;
        }
      }));

    }

    Collections.sort(documents);
    return documents;
  }

  /**
   * Move the file to the ingested directory and generate cross-ref.
   *
   * @param file the file to move
   * @param doi  the associated article
   * @throws java.io.IOException on an error
   */
  public void generateIngestedData(File file, String doi)
      throws IOException {
    // Delete the previously ingested article if it exist.
    FileUtils.deleteQuietly(new File(ingestedDocumentDirectory, file.getName()));
    FileUtils.moveFileToDirectory(file, new File(ingestedDocumentDirectory), true);
    log.info("Relocated: " + file + ":" + doi);
  }

  /**
   * @param uris uris to be published.
   * @return a list of messages describing what was successful and what failed
   */
  @Override
  @Transactional(rollbackFor = {Throwable.class})
  public List<String> publish(String[] uris, final String authId) {
    final List<String> msgs = new ArrayList<String>();

    // publish articles
    for (String article : uris) {
      try {
        // mark article as active
        articleService.setState(article, authId, Article.STATE_ACTIVE);
        invokeOnPublishListeners(article, authId);

        msgs.add("Published: " + article);
        log.info("Published article: '" + article + "'");
      } catch (Exception e) {
        log.error("Could not publish article: '" + article + "'", e);
        msgs.add("Error publishing: '" + article + "' - " + e.toString());
      }
    }
    return msgs;
  }

  /**
   * Generate the xml doc to send to crossref
   * <p/>
   *
   * @param articleXml - the article xml
   * @param articleId  - the article Id
   * @throws javax.xml.transform.TransformerException
   *          - if there's a problem transforming the article xml
   */
  @Override
  public void generateCrossrefInfoDoc(Document articleXml, URI articleId) throws TransformerException {
    log.info("Generating crossref info doc for article " + articleId);

    try {
      Transformer t = getTranslet(articleXml);
      t.setParameter("plosDoiUrl", plosDoiUrl);
      t.setParameter("plosEmail", plosEmail);

      File target_xml =
          new File(ingestedDocumentDirectory, getCrossrefDocFileName(articleId));

      t.transform(new DOMSource(articleXml, articleId.toString()), new StreamResult(target_xml));
    } catch (Exception e) {
      throw new TransformerException(e);
    }
  }

  /**
   * Get a translet, compiled stylesheet, for the xslTemplate. If the doc is null use the default template. If the doc
   * is not null then get the DTD version. IF the DTD version does not exist use the default template else use the
   * template associated with that version.
   *
   * @param doc the dtd version of document
   * @return Translet for the xslTemplate.
   * @throws javax.xml.transform.TransformerException
   *          TransformerException.
   */
  private Transformer getTranslet(Document doc) throws TransformerException {
    Transformer transformer;
    StreamSource templateStream = null;
    String templateName;
    try {
      String key = doc.getDocumentElement().getAttribute("dtd-version").trim();
      if ((doc == null) || (!xslTemplateMap.containsKey(key)) || (key.equalsIgnoreCase(""))) {
        templateStream = getAsStream(xslDefaultTemplate);
      } else {
        templateName = xslTemplateMap.get(key);
        templateStream = getAsStream(templateName);
      }
    } catch (Exception e) {
      log.error("XmlTransform not found", e);
    }

    final TransformerFactory tFactory = TransformerFactory.newInstance();
    Templates translet = tFactory.newTemplates(templateStream);
    transformer = translet.newTransformer();
    return transformer;
  }

  /**
   * @param filenameOrURL filenameOrURL
   * @return the local or remote file or url as a java.io.File
   * @throws java.net.URISyntaxException URISyntaxException
   */
  public StreamSource getAsStream(final String filenameOrURL) throws URISyntaxException,
      IOException {
    final URL resource = DocumentManagementServiceImpl.class.getResource(filenameOrURL);

    if (resource != null) {
      return new StreamSource(resource.openStream());
    }

    return new StreamSource(new File(filenameOrURL));
  }

  /**
   * Invokes all objects that are registered to listen to article publish event.
   *
   * @param articleId Article ID
   * @param authId the authorization ID of the current user
   *
   * @throws Exception If listener method failed
   */
  private void invokeOnPublishListeners(String articleId, String authId) throws Exception {
    if (onPublishListeners != null) {
      for (OnPublishListener listener : onPublishListeners) {
        listener.articlePublished(articleId, authId);
      }
    }
  }

  private void removeFromCrossPubbedJournals(URI id) {
//    for (Journal j : journalService.getAllJournals()) {
//      List<URI> col = j.getSimpleCollection();
//      if (col != null)
//        while (col.contains(id))
//          col.remove(id);
//    }
  }

  /**
   * Invokes all objects that are registered to listen to article delete event.
   *
   * @param articleId Article ID
   * @throws Exception If listener method failed
   */
  private void invokeOnDeleteListeners(String articleId) throws Exception {
    if (onDeleteListeners != null) {
      for (OnDeleteListener listener : onDeleteListeners) {
        listener.articleDeleted(articleId);
      }
    }
  }

  /**
   * Convert from an article id to the name of the crossref info doc for that file
   *
   * @param articleId - the article id
   * @return a string usable as a distinct filename - ':', '/' and '.' -&gt; '_'
   */
  private String getCrossrefDocFileName(URI articleId) {
    return articleId.toString().replace(':', '_').replace('/', '_').replace('.', '_') + ".xml";
  }

  /**
   * Sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Sets the PermissionsService.
   *
   * @param permService The PermissionsService to set.
   */
  @Required
  public void setPermissionsService(PermissionsService permService) {
    this.permissionsService = permService;
  }

  /**
   * @param plosDoiUrl The plosDxUrl to set.
   */
  @Required
  public void setPlosDoiUrl(String plosDoiUrl) {
    this.plosDoiUrl = plosDoiUrl;
  }

  /**
   * @param plosEmail The plosEmail to set.
   */
  @Required
  public void setPlosEmail(String plosEmail) {
    this.plosEmail = plosEmail;
  }
}
