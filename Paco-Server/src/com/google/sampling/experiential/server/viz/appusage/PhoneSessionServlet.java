package com.google.sampling.experiential.server.viz.appusage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.sampling.experiential.server.AuthUtil;
import com.google.sampling.experiential.server.EventQueryStatus;
import com.google.sampling.experiential.server.TimeUtil;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.Constants;

/**
 * Demonstration of building a visualization with the new sqlSearch api
 *
 */
@SuppressWarnings("serial")
public class PhoneSessionServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(PhoneSessionServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      String userEmail = AuthUtil.getEmailOfUser(req, user);
      Long experimentId = getExperimentId(req);
      String who = req.getParameter("who");
      if (who == null) {
        who = userEmail;
      }
      if (experimentId != null) {
        DateTimeZone tzForClient = TimeUtil.getTimeZoneForClient(req);
        produceAppUsageChart(userEmail, who, experimentId, resp, tzForClient);
      }
    }
  }


  private void produceAppUsageChart(String userEmail, String who, Long experimentId, HttpServletResponse resp, DateTimeZone timezone) throws IOException {
    DateMidnight today = new DateMidnight();
    String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATETIME_FORMAT);

    String startTime = today.minusDays(31).toString(dateTimeFormatter);
    String endTime = today.minusDays(29).toString(dateTimeFormatter);
    String query = "{ query : " +
                   "        { criteria : \" experiment_id = ? " +
                                          " and who = ? and (group_name = ? or text = ? or text = ? ) and response_time between ? and ?\"," +
                                          "          values : [" +experimentId.toString() + ", " + "\"" + who + "\"," +
                                          " \"app_logging\", \"userPresent\", \"userNotPresent\", \"" + startTime + "\", \"" + endTime + "\"]," +
                                          "        },  order : \"response_time\"" +
                                          "         " +
                                          "      };";

    EventQueryStatus result = CloudSqlRequestProcessor.processSearchQuery(userEmail, query, timezone);
    if (result.getStatus() != Constants.SUCCESS) {
      String resultAsString = JsonConverter.getObjectMapper().writeValueAsString(result);
      resp.getWriter().println(resultAsString);
      return;
    }

    final List<EventDAO> events = result.getEvents();
    PhoneSessionLog sessions = PhoneSessionLog.buildSessions(events);
    String page = JsonConverter.getObjectMapper().writeValueAsString(sessions);
    resp.getWriter().println(page);
  }



  private Long getExperimentId(HttpServletRequest req) {
    String experimentIdString = req.getParameter("experimentId");
    if (!Strings.isNullOrEmpty(experimentIdString)) {
      try {
        return new Long(experimentIdString);
      } catch (NumberFormatException e) {
      }
    }
    return null;
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}