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
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.ErrorMessages;

@SuppressWarnings("serial")
public class CloudSqlSearchServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlSearchServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    throw new ServletException(ErrorMessages.METHOD_NOT_SUPPORTED.getDescription());
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      String loggedInUser = AuthUtil.getEmailOfUser(req, user);
      String reqBody = RequestProcessorUtil.getBody(req);
      DateTimeZone tzForClient = TimeUtil.getTimeZoneForClient(req);

      EventQueryStatus eventQueryResult = CloudSqlRequestProcessor.processSearchQuery(loggedInUser,
                                                                                      reqBody,
                                                                                      tzForClient);
      resp.setContentType("text/plain");
      resp.getWriter().println(JsonConverter.getObjectMapper().writeValueAsString(eventQueryResult));
    }
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}