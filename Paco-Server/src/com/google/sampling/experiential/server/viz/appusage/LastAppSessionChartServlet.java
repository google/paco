package com.google.sampling.experiential.server.viz.appusage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.sampling.experiential.server.AuthUtil;
import com.google.sampling.experiential.server.EventQueryStatus;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.PacoResponse;
import com.google.sampling.experiential.server.QueryFactory;
import com.google.sampling.experiential.server.SearchQuery;
import com.google.sampling.experiential.server.TimeUtil;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryJsonParser;

import net.sf.jsqlparser.JSQLParserException;

/**
 * Demonstration of building a visualization with the new sqlSearch api
 *
 */
@SuppressWarnings("serial")
public class LastAppSessionChartServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(LastAppSessionChartServlet.class.getName());

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
        try {
          produceAppUsageChart(userEmail, who, experimentId, resp, tzForClient);
        } catch (Exception e) {
          log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
        }
      }
    }
  }

  private void produceAppUsageChart(String userEmail, String who, Long experimentId, HttpServletResponse resp, DateTimeZone timezone) throws JSQLParserException, Exception {
    Boolean oldMethodFlag = Constants.USE_OLD_FORMAT_FLAG;
    DateMidnight today = new DateMidnight();
    
    String DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATETIME_FORMAT);

    //    Long startTime = today.getMillis();
//    Long endTime = today.plusDays(1).getMillis();
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

    SQLQuery sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(query, true);
    SearchQuery sq = QueryFactory.createSearchQuery(sqlQueryObj, 5.0f);
    PacoResponse pr = sq.process(userEmail, oldMethodFlag);
    EventQueryStatus evQryStatus = null;
    String page = "Unable to retrieve user app sessions";
    if ( pr != null && pr instanceof EventQueryStatus) {
      if (Constants.SUCCESS.equals(pr.getStatus())) {
        evQryStatus = (EventQueryStatus) pr;
        final List<EventDAO> events = evQryStatus.getEvents();
        page = makeAppUsagePage(userEmail, experimentId, events);
      }
    }
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

  private String makeAppUsagePage(String userEmail, Long experimentId, List<EventDAO> events) throws JsonGenerationException, JsonMappingException, IOException {
    String eventJson = JsonConverter.getObjectMapper().writeValueAsString(events);
    return // "<html><head><script src='https://code.jquery.com/jquery-1.12.4.min.js'></script><script>" + "\n" +
    "<html><head><script src='js/jquery-2.1.3.js'></script><script>\n"  +
"var MIN_SESSION_BREAK_MILLIS = 120000; // 2 minutes between sessions" + "\n" +
"\n" + "\n" +
"var events = " + eventJson + ";" + "\n" +
"getAnswerForName = function(event, name) {" + "\n" +
"   for (var i in event.responses) {" + "\n" +
"        var response = event.responses[i];" + "\n" +
"        if (response[\"name\"] === name) {" + "\n" +
"            return response[\"answer\"];" + "\n" +
"        }" + "\n" +
"    }" + "\n" +
"    return null; " + "\n" +
"};" + "\n" +
"" + "\n" +
"getAppName = function(event) {" + "\n" +
"    return getAnswerForName(event, \"apps_used\");" + "\n" +
"};" + "\n" +
"" + "\n" +
"getUserPresent = function(event) {" + "\n" +
"    return getAnswerForName(event, \"userPresent\");" + "\n" +
"};" + "\n" +
"" + "\n" +
"getUserNotPresent = function(event) {" + "\n" +
"    return getAnswerForName(event, \"userNotPresent\");" + "\n" +
"};" + "\n" +
"" + "\n" +
"/** " + "\n" +
" * events are in newest -> oldest order " + "\n" +
" * return list of appUses" + "\n" +
" */" + "\n" +
"getAppUses = function(events) {" + "\n" +
"  var appUses = [];" + "\n" +
"  var reversed = events; //events.reverse();" + "\n" +
"  " + "\n" +
"  var currentAppUsed;" + "\n" +
"  var appUseStartTime;" + "\n" +
"  " + "\n" +
"  for (var i in reversed) {" + "\n" +
"      var event = reversed[i];" + "\n" +
"      var responseTimeField = event[\"responseTime\"];" + "\n" +
"      if (!responseTimeField) {" + "\n" +
"          continue;" + "\n" +
"      }" + "\n" +
"      //var rt = responseTimeField.replace(/\\//g, '-'); "+ "\n" +
"      //var eventResponseTime = new Date(rt).getTime();" + "\n" +
"      var eventResponseTime = responseTimeField;" + "\n" +
"      " + "\n" +
"      var currentAppName = getAppName(event);" + "\n" +
"      var userPresentTime = getUserPresent(event);" + "\n" +
"      var userNotPresentTime = getUserNotPresent(event);" + "\n" +
"      " + "\n" +
"      if (!currentAppName && !userPresentTime && !userNotPresentTime) {" + "\n" +
"          continue;" + "\n" +
"      }" + "\n" +
"      " + "\n" +
"      if (currentAppName && currentAppUsed == null) { " + "\n" +
"          // initial case" + "\n" +
"          currentAppUsed = currentAppName;" + "\n" +
"          appUseStartTime = eventResponseTime;" + "\n" +
"      } else if (currentAppName && currentAppName === currentAppUsed) { " + "\n" +
"          // continued same app usage" + "\n" +
"          // do nothing" + "\n" +
"      } else if (currentAppName && currentAppName !== currentAppUsed) { " + "\n" +
"          // switched app directly" + "\n" +
"          var appDuration = (eventResponseTime - appUseStartTime);" + "\n" +
"          var newAppUsage = { appName : currentAppUsed, startTime : appUseStartTime, duration : appDuration };" + "\n" +
"          appUses.push(newAppUsage);" + "\n" +
"          currentAppUsed = currentAppName;" + "\n" +
"          appUseStartTime = eventResponseTime;" + "\n" +
"      } else if (userNotPresentTime && currentAppUsed) { " + "\n" +
"          // closed phone" + "\n" +
"          var appDuration = (eventResponseTime - appUseStartTime) ;" + "\n" +
"          var newAppUsage = { appName : currentAppUsed, startTime : appUseStartTime, duration : appDuration };" + "\n" +
"          appUses.push(newAppUsage);" + "\n" +
"          currentAppUsed = null;" + "\n" +
"          appUseStartTime = null;" + "\n" +
"      }" + "\n" +
"  }" + "\n" +
"  return appUses;" + "\n" +
"};" + "\n" +
"" + "\n" +
"getSessions = function(appUses) {" + "\n" +
"    var sessions = [];" + "\n" +
"    var currentSession;" + "\n" +
"    var lastAppEndTime;" + "\n" +
"    " + "\n" +
"    for (var i in appUses) {" + "\n" +
"        var currentAppUse = appUses[i];" + "\n" +
"        var currentAppStartTime = currentAppUse[\"startTime\"];" + "\n" +
"        var currentAppEndTime = currentAppStartTime + currentAppUse[\"duration\"];" + "\n" +
"        if (!lastAppEndTime || ((currentAppStartTime - lastAppEndTime) > MIN_SESSION_BREAK_MILLIS)) {" + "\n" +
"            currentSession = []" + "\n" +
"            sessions.push(currentSession);" + "\n" +
"        } " + "\n" +
"        currentSession.push(currentAppUse);" + "\n" +
"        lastAppEndTime = currentAppEndTime;" + "\n" +
"    }" + "\n" +
"    return sessions;" + "\n" +
"};" + "\n" +
"" + "\n" +
"makeHtmlCardForApp = function(i, appName, secondsAppWasUsed) {" + "\n" +
"  return \"<div id='sess-app-\" + i + " + "\n" +
"    \"' class='sess-app' style='width:90px; height:80px; margin:10px 10px 10px 0; padding:0 10px; border-radius:6px; float: left; display: table; text-align: center; background-color: rgb(66,133,244);'>\" +" + "\n" +
"    \"  <span style='font-size:15px; max-width: 90px; display: table-cell; vertical-align: middle; word-wrap: break-word; color: rgb(255,255,255);'>\" +" + "\n" +
"    appName + \"<br/>\" +" + "\n" +
"    \"    <span style='font-size:12px;'>(\" + secondsAppWasUsed + \" sec)</span>\" +" + "\n" +
"    \"  </span>\" +" + "\n" +
"    \"</div>\";" + "\n" +
"};" + "\n" +
"" + "\n" +
"makeHtmlSummary = function(sessionStartTime, totalDuration) {" + "\n" +
"  return \"<div class='' id='paco-session'>\" +" + "\n" +
"  \"  <div class='mdl-card__supporting-text'>\" +" + "\n" +
"  \"    Your most recent session, starting on \" + sessionStartTime + " + "\n" +
"  \"    lasting \" + (totalDuration / 1000) + \" seconds.\" +" + "\n" +
"  \"  </div>\" +" + "\n" +
"  \"</div>\";" + "\n" +
"};" + "\n" +
"" + "\n" +
"/**" + "\n" +
" * Print a summary, then print a card for each app used in the sessions" + "\n" +
" * inputs :" + "\n" +
" *     lastSessionApps = [{" + "\n" +
" *       appName : \"\",     // String" + "\n" +
" *       startTime : \"\",   // Date" + "\n" +
" *       duration : \"\"     // int seconds" + "\n" +
" *     }, " + "\n" +
" *     ...]" + "\n" +
" *" + "\n" +
" *     rootDiv : html div to paint into  " + "\n" +
" */" + "\n" +
"drawSession = function(lastSessionApps, rootDiv) {" + "\n" +
"    // Add the date to the top of the card" + "\n" +
"    var totalDuration = 0;" + "\n" +
"    for (var i in lastSessionApps) {" + "\n" +
"        totalDuration += lastSessionApps[i][\"duration\"];" + "\n" +
"    }" + "\n" +
"    var sessionStartTime = new Date(lastSessionApps[0][\"startTime\"]);    " + "\n" +
"    $(\"#\" + rootDiv).append(" + "\n" +
"        makeHtmlSummary(sessionStartTime, totalDuration)" + "\n" +
"    );" + "\n" +
"    " + "\n" +
"    for (var i in lastSessionApps) {" + "\n" +
"      var appName = lastSessionApps[i][\"appName\"];" + "\n" +
"      var secondsAppWasUsed = lastSessionApps[i][\"duration\"] / 1000;" + "\n" +
"        $(\"#paco-session\").append(" + "\n" +
"          makeHtmlCardForApp(i, appName, secondsAppWasUsed)" + "\n" +
"        );" + "\n" +
"    }" + "\n" +
"};" + "\n" +
"" + "\n" +
"function main(experiment, experimentGroup, form_root) {" + "\n" +
"    try {" + "\n" +
"      var today = new Date();" + "\n" +
"      var day = today.getDate();" + "\n" +
"      var month = today.getMonth();" + "\n" +
"      var year = today.getFullYear();" + "\n" +
"      var startTime = new Date(year , month, day).getTime();" + "\n" +
"      var endTime = new Date(year , month, day + 1).getTime();" + "\n" +
"      " + "\n" +
"      // query for today's apps used and phone on and off events" + "\n" +
//"      var query = { query : " + "\n" +
//"        { criteria : \" (group_name = ? or text = ? or text = ? ) and response_time > ? and response_time < ?\"," + "\n" +
//"          values : ['app_logging', \"userPresent\", \"userNotPresent\", startTime, endTime]," + "\n" +
//"          order : \"response_time\"" + "\n" +
//"        } " + "\n" +
//"      };" + "\n" +
//"      var events = paco.db.getEventsByQuery(JSON.stringify(query));    " + "\n" +
"      " + "\n" +
"      var appUses = getAppUses(events);" + "\n" +
"      //confirm(\"App Uses: \" + JSON.stringify(appUses, null, 2));" + "\n" +
"      var sessions = getSessions(appUses);" + "\n" +
"      //confirm(\"Sessions Count: \" + JSON.stringify(sessions, null, 2));" + "\n" +
"      var lastSession = sessions[sessions.length - 1];" + "\n" +
"      drawSession(lastSession, \"paco-form\");" + "\n" +
"    } catch (e) {" + "\n" +
"        confirm(\"Error: \" + JSON.stringify(e.stack, null, 2));" + "\n" +
"    }" + "\n" +
"};" + "\n" +
"run = function() { main(null, null, null);};" + "\n" +
"</script></head><body onload=run()> " + "\n" +
"<div id=\"paco-form\"></div></body></html>" + "\n";
  }
}