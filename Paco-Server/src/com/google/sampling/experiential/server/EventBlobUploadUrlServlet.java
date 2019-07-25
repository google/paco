package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.users.User;

/**
 * Servlet that returns the url for uploading event blobs
 * in to App Engine.
 *
 */
@SuppressWarnings("serial")
public class EventBlobUploadUrlServlet extends HttpServlet {

  String gsBucketName = System.getProperty("com.pacoapp.eventBlobBucketName");  
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
  IOException {
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      
      UploadOptions uploadOptions = UploadOptions.Builder.withGoogleStorageBucketName(gsBucketName);      
      String blobUploadUrl = BlobstoreServiceFactory.getBlobstoreService().createUploadUrl("/eventblobs", uploadOptions);
      
      if (false /*AuthUtil.isDevInstance()*/) {
        String userAgent = req.getHeader("user-agent");
        if (userAgent != null && userAgent.contains("Android"))  { 
          blobUploadUrl = rewriteBlobStoreUrlToAndroidEmulator(blobUploadUrl);
        }
      }
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType("text/plain");

      PrintWriter out = resp.getWriter();
      out.print(blobUploadUrl);
      out.flush();
      out.close();
    }
  }

  public static String rewriteBlobStoreUrlToAndroidEmulator(String blobUploadUrl) {
    try {
      URL url = new URL(blobUploadUrl);
      String host = url.getHost();
      return new URL(url.getProtocol(), "10.0.2.2", url.getPort(), url.getPath()).toString();
    } catch (MalformedURLException e) {     
      e.printStackTrace();
    }
    return blobUploadUrl;
    
  }
}