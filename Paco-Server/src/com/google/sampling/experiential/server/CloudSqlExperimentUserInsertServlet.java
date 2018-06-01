package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

@SuppressWarnings("serial")
public class CloudSqlExperimentUserInsertServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlExperimentUserInsertServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    throw new ServletException(ErrorMessages.METHOD_NOT_SUPPORTED.getDescription());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    log.info("Exp user insert servlet");
    boolean sendToCloudSql = false;
    boolean saveInOldFormatInDS  = Constants.USE_OLD_FORMAT_FLAG;
    boolean persistInCloudSqlOnly = true;
    User currentUser = null;
    setCharacterEncoding(req, resp);
    // Level 1 Validation
    // This servlet can be invoked only by queue
    if (req.getHeader("X-AppEngine-QueueName") == null) {
      throw new IllegalStateException(ErrorMessages.MISSING_APP_HEADER.getDescription());
    }
    String requestBody = RequestProcessorUtil.getBody(req);
    DateTimeZone timezone = TimeUtil.getTimeZoneForClient(req);
    String appIdHeader = req.getHeader("http.useragent");
    String pacoVersion = req.getHeader("paco.version");
    String results = ExperimentJsonUploadProcessor.create().processJsonExperiments(sendToCloudSql, persistInCloudSqlOnly, saveInOldFormatInDS, requestBody, currentUser, appIdHeader, pacoVersion, timezone);
    resp.getWriter().println(results);
 
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }
}