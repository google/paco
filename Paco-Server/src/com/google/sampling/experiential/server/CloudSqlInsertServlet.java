package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;


@SuppressWarnings("serial")
public class CloudSqlInsertServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlInsertServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
  ServletException {
    throw new ServletException("Method not supported");
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      String requestBody = RequestProcessorUtil.getBody(req);
  
      boolean persistInCloudSqlOnly = true;
  
      //should not send this event to cloud sql insert queue again
      //TODO authenticated users email should be verified
      String results = EventJsonUploadProcessor.create().processJsonEvents(persistInCloudSqlOnly, requestBody, AuthUtil.getEmailOfUser(req, user), null, null);      
      resp.getWriter().println(results);
    }
  }
  private void setCharacterEncoding(HttpServletRequest req, HttpServletResponse resp)
          throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
  
}