package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    UserService userService = UserServiceFactory.getUserService();
    User user = getWhoFromLogin();

    if (user == null) {
      resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
    } else {
      resp.setContentType("text/html;charset=UTF-8");

      String experimentId = req.getParameter("experimentId");
      if (experimentId == null || experimentId.isEmpty()) {
        resp.getWriter().write("No results");
        return;
      } else {
        Experiment experiment = ExperimentRetriever.getInstance().getExperiment(experimentId);
        List<Query> queryFilters = new QueryParser().parse("experimentId=" + experimentId);
        List<Event> events = EventRetriever.getInstance().getEvents(queryFilters, user.getEmail(),
                                                                    EventServlet.getTimeZoneForClient(req));
        Map<String, ParticipantReport> participantReports = Maps.newConcurrentMap();
        for (Event event : events) {
          ParticipantReport participantReport = participantReports.get(event.getWho());
          if (participantReport == null) {
            participantReport = new ParticipantReport(event.getWho());
            participantReports.put(event.getWho(), participantReport);
          }
          participantReport.addEvent(event);
        }
        
        PrintWriter writer = resp.getWriter();
        writer.write("<html><head><title>Participant Stats for " + experiment.getTitle() + "</title>" +
        "<style type=\"text/css\">"+
            "body {font-family: verdana,arial,sans-serif;color:#333333}" +
          "table.gridtable {font-family: verdana,arial,sans-serif;font-size:11px;color:#333333;border-width: 1px;border-color: #666666;border-collapse: collapse;}" +
          "table.gridtable th {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #dedede;}" +
          "table.gridtable td {border-width: 1px;padding: 8px;border-style: solid;border-color: #666666;background-color: #ffffff;}" +
          "</style>" +
      		"</head><body>");
        writer.write("<div style=\"float: left;\">");
        writer.write("<h2>" + experiment.getTitle() +" Participant Stats</h2>");
        writer.write("<p>Sorted by lowest signaled response rate for today</p>");
        writer.write("<div><span style=\"font-weight: bold;\">Number of Joined Participants: </span>");
        writer.write("<span>" + participantReports.keySet().size() +"</span></div>");
        writer.write("<hr/>");
        writer.write("<table class=\"gridtable\">");
        writer.write("<tr style=\"font-weight: bold; text-align:left;\">");
        writer.write("<th>Who</th>" +
        		"<th style=\"background-color: #a9a9a9;\">Today's Signaled Response %</th>" +
        		"<th>Today's Signal Count</th>" +
        		"<th>Today's Self Report Count</th>" +
            "<th>Total Signaled Response %</th>" +
            "<th>Total Signaled Count</th>" +
            "<th>Total Self Report Count</th></tr>");

        List<ParticipantReport> participantReportValues = Lists.newArrayList(participantReports.values());
        for (ParticipantReport report : participantReportValues) {
          report.computeStats();
        }
        
        Collections.sort(participantReportValues, new Comparator<ParticipantReport>() {
          @Override
          public int compare(ParticipantReport participantReport1, ParticipantReport participantReport2) {
            //return arg0.getWho().toLowerCase().compareTo(arg1.getWho().toLowerCase());
            int report1ResponseRate = participantReport1.getTodaysSignaledResponseRate();
            int report2ResponseRate = participantReport2.getTodaysSignaledResponseRate();
            if (report1ResponseRate == report2ResponseRate) {
              return 0;
            } else if (report1ResponseRate > report2ResponseRate) {
              return 1;
            } else {
              return -1;
            }
          }
          
        });
        for (ParticipantReport report : participantReportValues) {
          writer.write("<tr style=\"text-align:right;\">");
          writer.write("<td style=\"text-align:left;\">" + report.getWho() + "</td>");
          writer.write("<td style=\"background-color: #dedede;\">" + Integer.toString(report.getTodaysSignaledResponseRate()) + "</td>");
          writer.write("<td>" + Integer.toString(report.getTodaysScheduledCount()) + "</td>");
          writer.write("<td>" + Integer.toString(report.getTodaysSelfReportResponseCount()) + "</td>");
          writer.write("<td>" + Integer.toString(report.getSignaledResponseRate()) + "</td>");
          writer.write("<td>" + Integer.toString(report.getScheduledCount()) + "</td>");
          writer.write("<td>" + Integer.toString(report.getSelfReportResponseCount()) + "</td>");          
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