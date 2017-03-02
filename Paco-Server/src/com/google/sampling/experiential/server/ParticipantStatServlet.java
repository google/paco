package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.ParticipationStats.ParticipantParticipationStat;
import com.google.sampling.experiential.server.stats.participation.ParticipationStatsService;
import com.google.sampling.experiential.server.stats.participation.ResponseStat;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;

public class ParticipantStatServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(EventServlet.class.getName());
  public static final String DEV_HOST = "<Your machine name here>";
  private UserService userService;

  /**
   * Produces a json output for the stats mainpage
   *
   * endpoint /participation?experimentId=&who=&limit=&cursor=
   *
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    User user = AuthUtil.getWhoFromLogin();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      resp.setContentType("text/json;charset=UTF-8");

      String experimentIdStr = req.getParameter("experimentId");
      if (experimentIdStr == null || experimentIdStr.isEmpty()) {
        resp.getWriter().write("No experiment id specified");
        return;
      } else {
        Long experimentId = null;
        try {
          experimentId = Long.parseLong(experimentIdStr);
        } catch (NumberFormatException nfe) {
          resp.getWriter().write("Invalid experiment id specified");
          return;
        }

        String whoParam = req.getParameter("who");
        DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
        if (!ExperimentAccessManager.isAdminForExperiment(AuthUtil.getEmailOfUser(req, user), experimentId)
            && !isQueryingOwnStats(AuthUtil.getEmailOfUser(req, user), whoParam)) {
          resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
        ExperimentDAO experiment = ExperimentServiceFactory.getExperimentService().getExperiment(experimentId);


        String v2Stats = req.getParameter("statv2");
        if (Strings.isNullOrEmpty(v2Stats)) {
          if (Strings.isNullOrEmpty(whoParam)) {
            computeOldStatsFromCounters(req, resp, user, experimentId, whoParam, timeZoneForClient);
          } else {
            // inline of the reportType = who in computeStatsFromCounters below
            ParticipationStatsService ps = new ParticipationStatsService();
            List<ResponseStat> participationStats = ps.getDailyTotalsForParticipant(experimentId, whoParam);
            PrintWriter writer = resp.getWriter();
            ObjectMapper mapper = JsonConverter.getObjectMapper();
            //mapper.setDateFormat(new SimpleDateFormat("yyyy/MM/dd"));
            if (participationStats != null) {
              writer.write(mapper.writeValueAsString(participationStats));
            } else {
              writer.write("Could not compute stats. Please check server for errors.");
            }
          }

        } else {
          computeStatsFromCounters(req, resp, user, experimentId, whoParam, timeZoneForClient);
        }

      }
    }
  }

  private void computeStatsFromCounters(HttpServletRequest req, HttpServletResponse resp, User user, Long experimentId,
                                        String whoParam, DateTimeZone timeZoneForClient) throws IOException {
    String experimentGroupName = req.getParameter("experimentGroupName");
    String reportType = req.getParameter("reportType");
    String dateParam = req.getParameter("date"); //
    DateTime date = null;
    if (!Strings.isNullOrEmpty(dateParam)) {
      date = com.pacoapp.paco.shared.util.TimeUtil.parseDateWithoutZone(dateParam);
    }

    ParticipationStatsService ps =  new ParticipationStatsService();
    List<ResponseStat> participationStats = null;
    if (Strings.isNullOrEmpty(reportType) || reportType.equals("today")) {
      if (!Strings.isNullOrEmpty(experimentGroupName)) {
        participationStats = ps.getTotalByParticipantOnDateForGroup(experimentId, experimentGroupName, new DateTime());
      } else {
        participationStats = ps.getTotalByParticipantOnDate(experimentId, new DateTime());
      }
    } else if (reportType.equals("date")) {
      if (date == null) {
        throw new IllegalArgumentException("Must specify date correctly for reportType=date.");
      }
      if (!Strings.isNullOrEmpty(experimentGroupName)) {
        participationStats = ps.getTotalByParticipantOnDateForGroup(experimentId, experimentGroupName, date);
      } else {
        participationStats = ps.getTotalByParticipantOnDate(experimentId, date);
      }
    } else if (reportType.equals("total")) {
      if (!Strings.isNullOrEmpty(experimentGroupName)) {
        participationStats = ps.getTotalByParticipantForGroup(experimentId, experimentGroupName);
      } else {
        participationStats = ps.getTotalByParticipant(experimentId);
      }
    } else if (reportType.equals("who") || !Strings.isNullOrEmpty(whoParam)) {
      if (!Strings.isNullOrEmpty(experimentGroupName)) {
        participationStats = ps.getDailyTotalsForParticipantForGroup(experimentId, experimentGroupName, whoParam);
      } else {
        participationStats = ps.getDailyTotalsForParticipant(experimentId, whoParam);
      }
    } else if (reportType.equals("totalEventCounts")) {
      participationStats = Lists.newArrayList(ps.getTotalResponseCount(experimentId)); // todo write a single object instead of a list
    }
    PrintWriter writer = resp.getWriter();
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    if (participationStats != null && !participationStats.isEmpty()) {
      writer.write(mapper.writeValueAsString(participationStats));
    } else if (participationStats != null && participationStats.isEmpty()) {
      writer.write("{ \"message\" : \"No data\"}");
    } else {
      writer.write("{ \"message\" : \"Could not compute stats. Please check server for errors.\"}");
    }
  }

  private void computeOldStatsFromCounters(HttpServletRequest req, HttpServletResponse resp, User user,
                                           Long experimentId, String whoParam, DateTimeZone timeZoneForClient) throws IOException {
    // computes the overview stats for the project stats page using the new counters
    ParticipationStatsService ps =  new ParticipationStatsService();
    List<ResponseStat> totalParticipationStats = ps.getTotalByParticipant(experimentId);

    Map<String, ResponseStat> todayResponseMap = Maps.newConcurrentMap();
    List<ResponseStat> todayParticipationStats = ps.getTotalByParticipantOnDate(experimentId, new DateTime());
    for (ResponseStat responseStat : todayParticipationStats) {
      todayResponseMap.put(responseStat.who, responseStat);
    }

    List<ParticipantParticipationStat> participantStats = Lists.newArrayList();

    for (ResponseStat totalResponseStat : totalParticipationStats) {
      ResponseStat todayWho = todayResponseMap.get(totalResponseStat.who);
      participantStats.add(new ParticipationStats.ParticipantParticipationStat(totalResponseStat.who,
                                                                               todayWho != null ? (todayWho.schedR + todayWho.missedR) : 0,
                                                                               todayWho != null ? todayWho.schedR : 0,
                                                                               todayWho != null ? todayWho.selfR : 0,
                                                                               totalResponseStat.schedR + totalResponseStat.missedR,
                                                                               totalResponseStat.schedR,
                                                                               totalResponseStat.selfR));
    }


    ParticipationStats participationStats = new ParticipationStats(participantStats, null);

    PrintWriter writer = resp.getWriter();
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    writer.write(mapper.writeValueAsString(participationStats));
  }



  private void computeStatsFromEventsTable(HttpServletRequest req, HttpServletResponse resp, User user,
                                           Long experimentId, String whoParam, DateTimeZone timeZoneForClient)
                                                                                                              throws IOException,
                                                                                                              JsonGenerationException,
                                                                                                              JsonMappingException {
    String fullQuery = "experimentId=" + experimentId;
    if (!Strings.isNullOrEmpty(whoParam)) {
      fullQuery += ":who=" + whoParam;
    }
    List<Query> queryFilters = new QueryParser().parse(fullQuery);
    String cursor = req.getParameter("cursor");
    String limitStr = req.getParameter("limit");
    int limit = 0;
    if (!Strings.isNullOrEmpty(limitStr)) {
      try {
        limit = Integer.parseInt(limitStr);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }

    EventQueryResultPair eventQueryResultPair = EventRetriever.getInstance()
                                                              .getEventsInBatchesOneBatch(queryFilters,
                                                                                          AuthUtil.getEmailOfUser(req,
                                                                                                                  user),
                                                                                          timeZoneForClient, limit,
                                                                                          cursor);

    Map<String, ParticipantReport> participantReports = Maps.newConcurrentMap();
    for (Event event : eventQueryResultPair.getEvents()) {
      ParticipantReport participantReport = participantReports.get(event.getWho());
      if (participantReport == null) {
        participantReport = new ParticipantReport(event.getWho(), timeZoneForClient);
        participantReports.put(event.getWho(), participantReport);
      }
      participantReport.addEvent(event);
    }

    List<ParticipantParticipationStat> participantStats = Lists.newArrayList();

    List<ParticipantReport> participantReportValues = Lists.newArrayList(participantReports.values());
    for (ParticipantReport report : participantReportValues) {
      report.computeStats();
      participantStats.add(new ParticipationStats.ParticipantParticipationStat(
                                                                               report.getWho(),
                                                                               report.getTodaysScheduledCount(),
                                                                               report.getTodaysSignaledResponseCount(),
                                                                               report.getTodaysSelfReportResponseCount(),
                                                                               report.getScheduledCount(),
                                                                               report.getSignaledResponseCount(),
                                                                               report.getSelfReportResponseCount()));
    }

    Collections.sort(participantStats);
    String nextCursor = eventQueryResultPair.getCursor();
    if (nextCursor == null || nextCursor.equals(cursor)) {
      nextCursor = null;
    }
    ParticipationStats participationStats = new ParticipationStats(participantStats, nextCursor);

    PrintWriter writer = resp.getWriter();
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    writer.write(mapper.writeValueAsString(participationStats));
  }

  private boolean isQueryingOwnStats(String emailOfUser, String whoParam) {
    return whoParam != null && whoParam.toLowerCase().equals(emailOfUser);
  }

}