package com.google.sampling.experiential.server.viz.appusage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.users.User;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.server.AuthUtil;
import com.google.sampling.experiential.server.EventQueryStatus;
import com.google.sampling.experiential.server.PacoResponse;
import com.google.sampling.experiential.server.QueryFactory;
import com.google.sampling.experiential.server.RequestProcessorUtil;
import com.google.sampling.experiential.server.SearchQuery;
import com.google.sampling.experiential.server.TimeUtil;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.QueryJsonParser;

/**
 * Demonstration of building a visualization with the new sqlSearch api
 *
 */
@SuppressWarnings("serial")
public class PhoneSessionServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(PhoneSessionServlet.class.getName());

  String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    System.out.println("TEST Enttering PhoneSessionServlet.doGet");
    log.info("DoGet of PhoneSessionServlet 1");
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      log.info("DoGet of PhoneSessionServlet 2");
      
    
      String userEmail = AuthUtil.getEmailOfUser(req, user);
      Long experimentId = getExperimentId(req);
      String groupName = getGroupName(req);
      String who = req.getParameter("who");
      Float pacoProtocol = RequestProcessorUtil.getPacoProtocolVersionAsFloat(req);

      DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATETIME_FORMAT);
      String startDatetimeParam = req.getParameter("startTime");
      String endDatetimeParam = req.getParameter("endTime");
      String startTime = null;
      String endTime = null;

      if (!Strings.isNullOrEmpty(startDatetimeParam) && !Strings.isNullOrEmpty(endDatetimeParam)) {
        try {
        startTime = DateTime.parse(startDatetimeParam, dateTimeFormatter).toString(dateTimeFormatter);
        endTime = DateTime.parse(endDatetimeParam, dateTimeFormatter).toString(dateTimeFormatter);
        } catch (Exception e) {
          log.fine("Could not parse startTime or endTime");
          startTime = null;
          endTime = null;
        }
      }
      
      if (experimentId != null) {
        DateTimeZone tzForClient = TimeUtil.getTimeZoneForClient(req);
        try {
          log.info("pre produceAppUsageChart");
          produceAppUsageChart(userEmail, who, experimentId, resp, tzForClient, groupName, startTime, endTime, pacoProtocol);
          log.info("post produceAppUsageChart");
        } catch (Exception e) {
          throw new ServletException("Exception while processing app usage information"+ e.getMessage());
        }
      }
    }
  }


  private String getGroupName(HttpServletRequest req) {
    return req.getParameter("groupName");

  }


  private void produceAppUsageChart(String userEmail, String who, Long experimentId, HttpServletResponse resp,
                                    DateTimeZone timezone, String groupName, String startTime, String endTime, Float pacoProtocol) throws Exception {
    log.info("Entering produceAppUsageChart");
    
    Boolean oldMethodFlag = Constants.USE_OLD_FORMAT_FLAG;
    String responseTimeClause = "";
    String responseTimeValues = "";
    if (!Strings.isNullOrEmpty(startTime) && !Strings.isNullOrEmpty(endTime)) {
      responseTimeClause = "and response_time between ? and ?";
      responseTimeValues  = ", \"" + startTime + "\", \""+ endTime + "\"";
    }

    String whoClause = "";
    String whoValue = "";
    List<String> whoList = Lists.newArrayList();
    if (!Strings.isNullOrEmpty(who)) {
      if (who.indexOf(",") == -1) {
        whoList.add(who);
        whoClause = "and who = ? ";
        whoValue = "\"" + who + "\", ";
      } else {
        whoClause = "and who in (";

        whoList = Lists.newArrayList(Splitter.on(",").omitEmptyStrings().split(who));
        List<String> placeHolders = Lists.newArrayList();
        for (String currentWho : whoList) {
          placeHolders.add("?");
          whoValue = whoValue + "\"" + currentWho + "\",";
        }
        whoClause = whoClause + Joiner.on(',').join(placeHolders) + ") ";

      }
    }

    String groupClause = "";
    String groupValue = "";

    if (!Strings.isNullOrEmpty(groupName)) {
      groupClause = "group_name = ? or ";
      groupValue = "\"" + groupName + "\", ";
    }

    String query = "{ query : " + "        { criteria : \" experiment_id = ? "
              + whoClause
              + " and ("
              + groupClause
              + "text = ? or text = ? or text = ? or text = ? ) "
              + responseTimeClause
              + "\","
              + "values : [" + experimentId.toString() + ", "
              +  whoValue
              + groupValue
              + "\"apps_used\", \"apps_used_raw\", \"userPresent\", \"userNotPresent\""
              + responseTimeValues
              + "],"
              + "}, "
              + "order : \"who,response_time\"};";
    boolean enableGrpByAndProjection = true;
    SQLQuery sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(query, enableGrpByAndProjection);
    
    SearchQuery searchQuery = QueryFactory.createSearchQuery(sqlQueryObj, pacoProtocol);
    
    
    long qryStartTime = System.currentTimeMillis();
    
    log.info("Starting phoneSession query");
    PacoResponse pr = searchQuery.process(userEmail, oldMethodFlag);
    
    long diff = System.currentTimeMillis() - qryStartTime;
    
    log.info("complete search qry took " + diff + " milliseconds");
    
    
    EventQueryStatus evQryStatus = null;
    String page = "Unable to retrieve user app sessions";
    if ( pr != null && pr instanceof EventQueryStatus) {
      if (Constants.SUCCESS.equals(pr.getStatus())) {
        evQryStatus = (EventQueryStatus)pr;
        final List<EventDAO> events = evQryStatus.getEvents();
        Map<String, List<EventDAO>> eventByWho = toMap(events);
        List<PhoneSessionLog> allUserSessions = Lists.newArrayList();
        for (String currentWho : eventByWho.keySet()) {
          List<EventDAO> userEvents = eventByWho.get(currentWho);
          PhoneSessionLog sessions = PhoneSessionLog.buildSessions(currentWho, userEvents);
          allUserSessions.add(sessions);
        }
        page = JsonConverter.getObjectMapper().writeValueAsString(allUserSessions);
      }
    }
    resp.getWriter().println(page);
  }



  private Map<String, List<EventDAO>> toMap(List<EventDAO> events) {
    Map<String, List<EventDAO>> result = Maps.newHashMap();
    for (EventDAO eventDAO : events) {
      List<EventDAO> whoseEvents = result.get(eventDAO.getWho());
      if (whoseEvents == null) {
        whoseEvents = Lists.newArrayList();
        result.put(eventDAO.getWho(), whoseEvents);
      }
      whoseEvents.add(eventDAO);
    }
    return result;
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