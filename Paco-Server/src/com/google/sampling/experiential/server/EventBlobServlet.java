package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pacoapp.paco.shared.model2.JsonConverter;

/**
 * Servlet that handles the uploading of event blobs
 *
 */
@SuppressWarnings("serial")
public class EventBlobServlet extends HttpServlet {
  
  public static final Logger log = Logger.getLogger(EventBlobServlet.class.getName());

  String gsBucketName = System.getProperty("com.pacoapp.eventBlobBucketName");  
  
  private static final String BLOB_KEY_PARAM = "blob-key";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      String who = AuthUtil.getEmailOfUser(req, user);
      
      List<List<String>> blobKeyMap = Lists.newArrayList();
      
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      
      Map<String, List<BlobKey>> uploads = blobstoreService.getUploads(req);
      Map<String, List<BlobInfo>> blobInfos = blobstoreService.getBlobInfos(req);
      
      Set<String> blobKeySet = uploads.keySet();
      List<BlobAcl> blobAcls = Lists.newArrayList();
       
      for (String blobKey : blobKeySet) {
        List<BlobKey> value = uploads.get(blobKey);
        if (value != null && !value.isEmpty()) {
          List<BlobInfo> blobInfoList = blobInfos.get(blobKey);
          BlobInfo currentBlobInfo = blobInfoList.get(0);
          String gcsFullObjectName = currentBlobInfo.getGsObjectName();
          String objectName = gcsFullObjectName.substring(gcsFullObjectName.lastIndexOf("/") + 1);
          String contentType = currentBlobInfo.getContentType();
          String media = URLEncoder.encode(contentType);
          String experimentIdForBlob = req.getParameter("experimentId_" + blobKey).trim();          
          String keyString = value.get(0).getKeyString();
          blobKeyMap.add(Lists.newArrayList(blobKey, createBlobGcsUrl(media, keyString)));
          blobAcls.add(new BlobAcl(keyString, experimentIdForBlob, who, gsBucketName, objectName));
        }
      }
      
      BlobAclStore.getInstance().saveAcls(blobAcls);
            
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("application/json");

      
      ObjectMapper mapper = JsonConverter.getObjectMapper();
      String json = mapper.writeValueAsString(blobKeyMap);
      
      PrintWriter out = resp.getWriter();
      out.print(json);
      out.flush();
      out.close();
    }
  }

  public static String createBlobGcsUrl(String media, String keyString) {
    return "/eventblobs?mt=" + media + "&" + BLOB_KEY_PARAM + "=" + keyString;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      String blobKeyParam = req.getParameter(BLOB_KEY_PARAM);

      if (Strings.isNullOrEmpty(blobKeyParam)) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "bad or missing parameters");
      } else {
        String emailOfUser = AuthUtil.getEmailOfUser(req, user);
        boolean isOwner = false;
        BlobAcl acl = BlobAclStore.getInstance().getAcl(blobKeyParam);
        if (acl != null) {
          isOwner = acl.getWho().equals(emailOfUser);
        }
        boolean isAuthorized = isOwner
                               || ExperimentAccessManager.isAdminForExperiment(emailOfUser,
                                                                               Long.parseLong(acl.getExperimentId()));
        if (isAuthorized) {
          BlobKey blobKey = new BlobKey(blobKeyParam);
          resp.setHeader("Content-Disposition", "filename=\""+ acl.getObjectName() + "\"");
          BlobstoreServiceFactory.getBlobstoreService().serve(blobKey, resp);
        } else {
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "bad or missing parameters");
        }
      }
    }
  }



}