package com.google.sampling.experiential.server.viz.appusage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;
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
        produceAppUsageChart(userEmail, who, experimentId, resp, tzForClient);
      }
    }
    //makeFakeChart(resp);
  }

  private void makeFakeChart(ServletResponse resp) {
    String json = "[{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497386315527,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\","
            + "\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497386312000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,"
            + "\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-06-13T13:38:32.039-07:00\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497386315480,"
            + "\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497386311000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,"
            + "\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"phoneOn\",\"answer\":\"true\"}]}"
            + ",{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497386315426,"
            + "\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317595000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,"
            + "\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Google App\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497386315364,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497317594000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\","
            + "\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497386314414,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497317593000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},"
            + "{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ScheduleListActivity\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497317594153,"
            + "\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317585000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,"
            + "\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ScheduleDetailActivity\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497317594095,"
            + "\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317584000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ScheduleListActivity\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497317594049,"
            + "\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317578000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},"
            + "{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317593980,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317578000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},"
            + "{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317593930,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317578000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497317593877,"
            + "\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317577000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317593832,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317576000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},"
            + "{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.PostJoinInstructionsActivity\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317578199,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317574000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},"
            + "{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317578121,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317571000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},"
            + "{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317578047,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317568000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},"
            + "{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317577966,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317355000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\","
            + "\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317577900,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497317355000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-06-12T18:29:15.254-07:00\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497317577803,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497317355000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":"
            + "[{\"name\":\"phoneOn\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497317577705,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":"
            + "1497312427000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,"
            + "\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497317576655,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312346000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FeedbackActivity\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,"
            + "\"who\":\"bobevans999@gmail.com\",\"when\":1497312382600,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\","
            + "\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312344000,\"experimentVersion\":44,\"timezone\":\"-07:00\","
            + "\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,"
            + "\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312376993,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312342000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.android.camera/com.android.camera.Camera\"},{\"name\":\"apps_used\",\"answer\":\"Camera\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312372178,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312340000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312368372,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312338000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\","
            + "\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312364818,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312337000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312360350,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312334000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,"
            + "\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Camera\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.android.camera/com.android.camera.Camera\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312355290,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312331000,"
            + "\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312346452,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\","
            + "\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312329000,\"experimentVersion\":44,\"timezone\":\"-07:00\","
            + "\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":"
            + "[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312319156,"
            + "\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312123000,"
            + "\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,"
            + "\"who\":\"bobevans999@gmail.com\",\"when\":1497312316378,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\","
            + "\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497312115000,\"experimentVersion\":44,\"timezone\":\"-07:00\","
            + "\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":"
            + "[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\","
            + "\"when\":1497312308746,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310740000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\""
            + ":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FeedbackActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312174377,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310727000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,"
            + "\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312169865,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310725000,\"experimentVersion\":44,\"timezone\":\"-07:00\","
            + "\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\","
            + "\"answer\":\"Camera\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.android.camera/com.android.camera.Camera\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312165721,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\","
            + "\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310721000,\"experimentVersion\":44,\"timezone\":\"-07:00\","
            + "\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,"
            + "\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},"
            + "{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312162474,\"appId\":\"Android\","
            + "\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310720000,\"experimentVersion\":44,"
            + "\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,"
            + "\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},"
            + "{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,"
            + "\"who\":\"bobevans999@gmail.com\",\"when\":1497312159162,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\","
            + "\"responseTime\":1497310720000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,"
            + "\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},"
            + "{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312155794,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310720000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312141741,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310719000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497312130006,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310718000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.PostJoinInstructionsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310826703,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310725000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.android.camera/com.android.camera.Camera\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Camera\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310814782,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310721000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310803366,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310720000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310798907,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310720000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310792370,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310720000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310785760,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310719000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310741473,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310718000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.PostJoinInstructionsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310719585,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310716000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310719511,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310714000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310719434,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310709000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310719360,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310705000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310719282,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310702000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310719205,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310699000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.SettingsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310719116,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310661000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310648213,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310608000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310648124,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310606000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Google App\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310648041,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310602000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"System UI\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.android.systemui/com.android.systemui.recents.RecentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647946,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310583000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647860,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310579000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647782,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310505000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647706,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310464000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647622,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310461000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.SettingsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647522,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310459000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647446,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310454000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647282,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310448000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647202,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310442000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647132,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310441000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310647062,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310441000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646973,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310441000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646896,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310441000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646818,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310440000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.PostJoinInstructionsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646662,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310437000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646596,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310435000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646511,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310424000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646426,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310423000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.SettingsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646338,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310418000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646226,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310416000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646110,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310411000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310646012,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310406000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310645923,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310389000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-06-12T16:33:09.661-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1497310392975,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1497310389000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"phoneOn\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879693989,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493879688000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-05-03T23:34:48.347-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879693724,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493879688000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"phoneOn\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879693422,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493856709000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userNotPresent\",\"answer\":\"2017-05-03T17:11:49.380-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879693121,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493856709000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userNotPresent\",\"answer\":\"2017-05-03T17:11:49.378-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879692847,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493856709000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userNotPresent\",\"answer\":\"2017-05-03T17:11:49.375-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879692561,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493856709000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userNotPresent\",\"answer\":\"2017-05-03T17:11:49.375-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879692295,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493856498000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-05-03T17:08:18.727-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879692028,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493856498000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-05-03T17:08:18.705-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879691740,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493855986000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-05-03T16:59:46.118-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879691461,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493855986000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-05-03T16:59:46.072-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879691196,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493854573000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-05-03T16:36:13.417-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879690897,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493854573000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-05-03T16:36:13.393-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879690608,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837353000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Google App\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879690328,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837350000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493879690014,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837345000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FeedbackActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837348067,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837340000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutor\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837347781,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837339000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837347497,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837339000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837347216,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837338000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837346924,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837338000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837346650,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837337000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.PostJoinInstructionsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837339532,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837335000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837339243,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837333000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837338952,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837321000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837338645,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837243000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837338364,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837241000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.SettingsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837338080,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837236000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837150784,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837144000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"userPresent\",\"answer\":\"2017-05-03T11:45:44.212-07:00\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837150347,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1493837144000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"phoneOn\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1493837148265,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491945888000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933082129,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933072000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.TroubleshootingActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933081850,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933061000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933081573,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933055000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentExecutorCustomRendering\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933081311,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933054000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentGroupPicker\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933081041,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933053000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933080747,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933051000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Google App\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933080469,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933044000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Camera\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.android.camera/com.android.camera.Camera\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933080205,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933043000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\"},{\"name\":\"apps_used\",\"answer\":\"Google App\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933079929,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933041000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.android.settings/com.android.settings.Settings\"},{\"name\":\"apps_used\",\"answer\":\"Settings\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933079658,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933039000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Google App\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.google.android.googlequicksearchbox/com.google.android.launcher.GEL\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933079374,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933037000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.MyExperimentsActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933079094,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933037000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.FindMyExperimentsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933078799,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933037000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933078482,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933037000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933078179,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933036000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.PostJoinInstructionsActivity\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933077889,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933033000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Settings\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.android.settings/com.android.settings.Settings$UsageAccessSettingsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933077589,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933031000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.PostJoinInstructionsActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933077267,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933031000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"foreground\",\"answer\":\"true\"},{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\",\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.InformedConsentActivity\"}]},{\"experimentId\":6582776115494912,\"who\":\"bobevans999@gmail.com\",\"when\":1491933076970,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933030000,\"experimentVersion\":44,\"timezone\":\"-07:00\",\"experimentGroupName\":\"app_logging\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,\"responses\":[{\"name\":\"apps_used\",\"answer\":\"Paco\"},{\"name\":\"apps_used_raw\","
            + "\"answer\":\"com.pacoapp.paco/com.pacoapp.paco.ui.ExperimentDetailActivity\"},{\"name\":\"foreground\",\"answer\":\"true\"}]},{\"experimentId\":6582776115494912,"
            + "\"who\":\"bobevans999@gmail.com\",\"when\":1491933032251,\"appId\":\"Android\",\"pacoVersion\":\"4.2.24\",\"experimentName\":\"Sql Viz Demo\",\"responseTime\":1491933030000,"
            + "\"experimentVersion\":44,\"timezone\":\"-07:00\",\"joined\":false,\"missedSignal\":false,\"emptyResponse\":false,"
            + "\"responses\":[{\"name\":\"schedule\",\"answer\":\"app_esm:[], app_logging:[], location_logging:[]\"},{\"name\":\"joined\",\"answer\":\"true\"}]}]";
    try {
      List<EventDAO> events = JsonConverter.getObjectMapper().readValue(json, new TypeReference<List<EventDAO>>() {
      });
      String page = makeAppUsagePage("bobevans999@gmail.com", 6582776115494912l, events);
      resp.getWriter().println(page);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void produceAppUsageChart(String userEmail, String who, Long experimentId, HttpServletResponse resp, DateTimeZone timezone) throws IOException {
    //String jsonSqlRequest = "select * from events where experimentId = " + experimentId.toString() + " and who = " + userEmail;

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

    EventQueryStatus result = CloudSqlRequestProcessor.processSearchQuery(userEmail, query, timezone);
    if (result.getStatus() != Constants.SUCCESS) {
      String resultAsString = JsonConverter.getObjectMapper().writeValueAsString(result);
      resp.getWriter().println(resultAsString);
      return;
    }

    final List<EventDAO> events = result.getEvents();
    //String page = JsonConverter.getObjectMapper().writeValueAsString(events);
    String page = makeAppUsagePage(userEmail, experimentId, events);
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