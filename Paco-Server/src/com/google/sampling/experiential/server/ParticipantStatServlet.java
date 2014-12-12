package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTimeZone;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.Experiment;

public class ParticipantStatServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(EventServlet.class.getName());
  public static final String DEV_HOST = "<Your machine name here>";
  private UserService userService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    User user = AuthUtil.getWhoFromLogin();

    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      resp.setContentType("text/html;charset=UTF-8");

      String experimentId = req.getParameter("experimentId");
      if (experimentId == null || experimentId.isEmpty()) {
        resp.getWriter().write("No experiment id specified");
        return;
      } else {
        final boolean alpha = req.getParameter("alpha") != null;
        Experiment experiment = ExperimentRetriever.getInstance().getExperiment(experimentId);
        List<Query> queryFilters = new QueryParser().parse("experimentId=" + experimentId);
        DateTimeZone timeZoneForClient = TimeUtil.getTimeZoneForClient(req);
        List<Event> events = EventRetriever.getInstance().getEvents(queryFilters, user.getEmail(),
                                                                    timeZoneForClient, 0, 20000);
        Map<String, ParticipantReport> participantReports = Maps.newConcurrentMap();
        for (Event event : events) {
          ParticipantReport participantReport = participantReports.get(event.getWho());
          if (participantReport == null) {
            participantReport = new ParticipantReport(event.getWho(), timeZoneForClient);
            participantReports.put(event.getWho(), participantReport);
          }
          participantReport.addEvent(event);
        }

        int totalResponses = 0;
        List<ParticipantReport> participantReportValues = Lists.newArrayList(participantReports.values());
        for (ParticipantReport report : participantReportValues) {
          report.computeStats();
          totalResponses += report.getSelfReportAndSignaledResponseCount();
        }

        PrintWriter writer = resp.getWriter();
        String experimentTitle = "No Title (deleted?)";
        if (experiment != null) {
          experimentTitle = experiment.getTitle();
        }
        writer.write("<html><head><title>Participant Stats for " + experimentTitle + "</title>" +
        "<style type=\"text/css\">"+
            "body {font-family: verdana,arial,sans-serif;color:#333333}" +
          "table.gridtable {font-family: verdana,arial,sans-serif;font-size:11px;color:#333333;border-width: 1px;border-color: " +
          "#666666;border-collapse: collapse;}" +
          "table.gridtable th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;}" +
          "table.gridtable td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #ffffff;}" +
          "</style>" +
      		"</head><body>");
        writer.write("<div style=\"float: left;\">");
        writer.write("<h2>" + experimentTitle +" Participant Stats</h2>");
        if (alpha) {
          writer.write("<p>Sorted alphabetically</p>");
          writer.write("<p><a href=\"/participantStats?experimentId="+ experimentId +"\">Click for sorted by today's signaled response rate</a></p>");
        } else {
          writer.write("<p>Sorted by lowest signaled response rate for today</p>");
          writer.write("<p><a href=\"/participantStats?alpha=true&experimentId="+ experimentId +"\">Click for alphabetically sorted</a></p>");

        }
        writer.write("<div><span style=\"font-weight: bold;\">Number of Joined Participants: </span>");
        writer.write("<span>" + participantReports.keySet().size() +"</span></div>");

        writer.write("<div><span style=\"font-weight: bold;\">Number of Responses: </span>");
        writer.write("<span>" + totalResponses +"</span></div>");

        writer.write("<hr/>");
        writer.write("<table class=\"gridtable\">");
        writer.write("<tr style=\"font-weight: bold; text-align:left;\">");
        writer.write("<th>Who</th>" +
        		"<th>Today's Signal Response<br/>" +
        		"% = Responded / Sent</th>" +
        		"<th>Today's Self Reports</th>" +

            "<th style=\"background-color: #a9a9a9;\">Total Signaled Response<br/>" +
            "% = Responded / Sent</th>" +
            "<th>Total Self Reports</th>" +
            "<th>Total Reports<br/>(signaled + self)</th>" +
            "</tr>");


        Collections.sort(participantReportValues, new Comparator<ParticipantReport>() {
          @Override
          public int compare(ParticipantReport participantReport1, ParticipantReport participantReport2) {
            if (alpha) {
               return participantReport1.getWho().toLowerCase().compareTo(participantReport2.getWho().toLowerCase());
            } else {
              float report1ResponseRate = participantReport1.getSignaledResponseRate();
              float report2ResponseRate = participantReport2.getSignaledResponseRate();
              if (report1ResponseRate == report2ResponseRate) {
                return 0;
              } else if (report1ResponseRate > report2ResponseRate) {
                return 1;
              } else {
                return -1;
              }
            }
          }

        });
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        for (ParticipantReport report : participantReportValues) {
          writer.write("<tr style=\"text-align:right;\">");
          String who = report.getWho();
          String anonymousId = Event.getAnonymousId(who);
          writer.write("<td style=\"text-align:left;\">" + who + "</td>");
          writer.write("<td>" +
              percentFormat.format(report.getTodaysSignaledResponseRate()) + " = ");

          writer.write(Integer.toString(report.getTodaysSignaledResponseCount()) + " / " +
              Integer.toString(report.getTodaysScheduledCount()) +
              "</td>");

          writer.write("<td>" + Integer.toString(report.getTodaysSelfReportResponseCount()) + "</td>");

          writer.write("<td style=\"background-color: #dedede;\">" + percentFormat.format(report.getSignaledResponseRate()) + " = ");

          writer.write(Integer.toString(report.getSignaledResponseCount()) + " / " +
              Integer.toString(report.getScheduledCount()) +
              "</td>");
          writer.write("<td>" + Integer.toString(report.getSelfReportResponseCount()) + "</td>");

          writer.write("<td>" + Integer.toString(report.getSelfReportAndSignaledResponseCount()) + "</td>");
          writer.write("</tr>");
        }
        writer.write("</table></div></body></html>");
      }
    }
  }

  private User getWhoFromLogin() {
    UserService userService = UserServiceFactory.getUserService();
    return userService.getCurrentUser();
  }

}