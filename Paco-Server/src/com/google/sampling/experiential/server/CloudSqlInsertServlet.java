package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pacoapp.paco.shared.util.ErrorMessages;

@SuppressWarnings("serial")
public class CloudSqlInsertServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlInsertServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    throw new ServletException(ErrorMessages.METHOD_NOT_SUPPORTED.getDescription());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    setCharacterEncoding(req, resp);
    // Level 1 Validation
    if (req.getHeader("X-AppEngine-QueueName") == null) {
      throw new IllegalStateException(ErrorMessages.MISSING_APP_HEADER.getDescription());
    }
    String requestBody = RequestProcessorUtil.getBody(req);
    boolean persistInCloudSqlOnly = true;
    // should not send this event to cloud sql insert queue again
    // sending who value as null. This will be populated from 'who' value in json.
    String results = EventJsonUploadProcessor.create().processJsonEvents(persistInCloudSqlOnly, requestBody,
                                                                         null, null,
                                                                         null);
    resp.getWriter().println(results);
 
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}