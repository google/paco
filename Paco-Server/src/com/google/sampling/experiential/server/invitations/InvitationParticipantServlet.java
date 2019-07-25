package com.google.sampling.experiential.server.invitations;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;

import com.google.appengine.api.users.User;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.server.AllFieldsSearchQuery;
import com.google.sampling.experiential.server.AuthUtil;
import com.google.sampling.experiential.server.EventDAOQueryResultPair;
import com.google.sampling.experiential.server.EventJsonDownloader;
import com.google.sampling.experiential.server.EventQueryResultPair;
import com.google.sampling.experiential.server.EventQueryStatus;
import com.google.sampling.experiential.server.ExperimentAccessManager;
import com.google.sampling.experiential.server.HttpUtil;
import com.google.sampling.experiential.server.PacoResponse;
import com.google.sampling.experiential.server.QueryFactory;
import com.google.sampling.experiential.server.RequestProcessorUtil;
import com.google.sampling.experiential.server.SearchQuery;
import com.google.sampling.experiential.server.TimeUtil;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.comm.Outcome;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.QueryJsonParser;

import net.sf.jsqlparser.JSQLParserException;

@SuppressWarnings("serial")
public class InvitationParticipantServlet extends HttpServlet {

  public static final Logger log = Logger.getLogger(InvitationParticipantServlet.class.getName());

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.info("doGet of InvitationParticipantServlet");
    resp.setContentType("application/json;charset=UTF-8");
    logPacoClientVersion(req);

    User user = AuthUtil.getWhoFromLogin();
    String email = null;
    if (user != null) {
      email = AuthUtil.getEmailOfUser(req, user);
    } else {
      AuthUtil.redirectUserToLogin(req, resp);
    }

    String participantParam = req.getParameter("participantId");
    if (!Strings.isNullOrEmpty(participantParam)) {
      getEventsForParticipant(req, resp, email, participantParam);
    } else {
      getEventsForAllParticipants(req,resp,email);
    }
  }

  private void getEventsForAllParticipants(HttpServletRequest req, HttpServletResponse resp, String email) throws IOException {
    String experimentIdParam = req.getParameter("experimentId");

    if (Strings.isNullOrEmpty(experimentIdParam) ) {
      resp.getWriter().println(JsonConverter.convertToJsonString(createErrorOutcome("arguments missing")));
      return;
    } else if (!ExperimentAccessManager.isUserAdmin(new Long(experimentIdParam), email)) {
      resp.getWriter().println(JsonConverter.convertToJsonString(createErrorOutcome("unauthorized")));
      return;
    } else {
      try {
        String sqlQueryString = buildSqlQueryForAllParticipants(experimentIdParam);
        AllFieldsSearchQuery searchQuery = new AllFieldsSearchQuery(null, 5.0f);
        PacoResponse serverResponse = searchQuery.executeAcledQuery(sqlQueryString, false);
        
        if (serverResponse != null && serverResponse instanceof EventQueryStatus) {
          if (Constants.SUCCESS.equals(serverResponse.getStatus())) {
            EventQueryStatus queryResponse = (EventQueryStatus) serverResponse;
            final List<EventDAO> events = queryResponse.getEvents();
            boolean anon = getAnonFromReq(req);
            boolean inlineBlobs = getInlineBlobsFromReq(req);
            boolean fullBlobAddress = getFullBlobAddressFromReq(req, true);
            Float pacoProtocol = RequestProcessorUtil.getPacoProtocolVersionAsFloat(req);            
            String json = EventJsonDownloader.jsonifyEventDAOs(anon, inlineBlobs, pacoProtocol, events, null, fullBlobAddress);            
            resp.getWriter().println(json);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  private void logPacoClientVersion(HttpServletRequest req) {
    String pacoVersion = req.getHeader("paco.version");
    if (pacoVersion != null) {
      log.info("Paco version of request = " + pacoVersion);
    }
  }

  public List<Outcome> createErrorOutcome(String msg) {
    Outcome outcome = new Outcome(0, msg);
    List<Outcome> outcomes = Lists.newArrayList(outcome);
    return outcomes;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req,resp);
  }

  private void getEventsForParticipant(HttpServletRequest req, HttpServletResponse resp, String email, String participantIdParam) throws IOException {
    String experimentIdParam = req.getParameter("experimentId");

    if (Strings.isNullOrEmpty(experimentIdParam) || Strings.isNullOrEmpty(participantIdParam)) {
      resp.getWriter().println(JsonConverter.convertToJsonString(createErrorOutcome("arguments missing")));
      return;
    } else if (!ExperimentAccessManager.isUserAdmin(new Long(experimentIdParam), email)) {
      resp.getWriter().println(JsonConverter.convertToJsonString(createErrorOutcome("unauthorized")));
      return;
    } else {
      try {
        String sqlQueryString = buildSqlQuery(experimentIdParam, participantIdParam);
        AllFieldsSearchQuery searchQuery = new AllFieldsSearchQuery(null, 5.0f);
        PacoResponse serverResponse = searchQuery.executeAcledQuery(sqlQueryString, false);
        
        if (serverResponse != null && serverResponse instanceof EventQueryStatus) {
          if (Constants.SUCCESS.equals(serverResponse.getStatus())) {
            EventQueryStatus queryResponse = (EventQueryStatus) serverResponse;
            final List<EventDAO> events = queryResponse.getEvents();
            boolean anon = getAnonFromReq(req);
            boolean inlineBlobs = getInlineBlobsFromReq(req);
            boolean fullBlobAddress = getFullBlobAddressFromReq(req, true);
            Float pacoProtocol = RequestProcessorUtil.getPacoProtocolVersionAsFloat(req);            
            String json = EventJsonDownloader.jsonifyEventDAOs(anon, inlineBlobs, pacoProtocol, events, null, fullBlobAddress);
            resp.getWriter().println(json);
          } else {
            resp.getWriter().println(JsonConverter.convertToJsonString(serverResponse));
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
    
  }

  public static boolean getInlineBlobsFromReq(HttpServletRequest req) {
    return getBooleanFromReq(req, "includePhotos", false);
  }

  public static boolean getBooleanFromReq(HttpServletRequest req, String paramName, boolean defaultValue) {
    String anonStr = HttpUtil.getParam(req, paramName);
    boolean anon = defaultValue;
    if (!Strings.isNullOrEmpty(anonStr)) {
      anon = Boolean.parseBoolean(anonStr);
    }
    return anon;
  }

  public static boolean getAnonFromReq(HttpServletRequest req) {
    return getBooleanFromReq(req, "anon", false);
  }
  
  public static boolean getFullBlobAddressFromReq(HttpServletRequest req, boolean defaultValue) {
    return getBooleanFromReq(req, "fullBlobAddress", defaultValue);
  }

  private String buildSqlQuery(String experimentIdParam, String participantIdParam) {
    String query = "SELECT * FROM events" + 
            " INNER JOIN outputs ON _id = outputs.event_id " + 
            " INNER JOIN experiment_version_group_mapping ON events.experiment_version_group_mapping_id = experiment_version_group_mapping.experiment_version_group_mapping_id " + 
            "           AND experiment_version_group_mapping.events_posted = 1 " + 
            " INNER JOIN experiment_detail ON " + 
            "    experiment_detail.experiment_detail_id = experiment_version_group_mapping.experiment_detail_id " + 
            " INNER JOIN group_detail ON " +  
            "    group_detail.group_detail_id = experiment_version_group_mapping.group_detail_id " + 
            " LEFT JOIN input_collection ON input_collection.input_collection_id = experiment_version_group_mapping.input_collection_id " + 
            "           AND experiment_version_group_mapping.experiment_id = input_collection.experiment_ds_id " + 
            " INNER JOIN input ON input.input_id = input_collection.input_id " + 
            "           AND outputs.input_id = input.input_id " + 
            " INNER JOIN extern_string_input AS esi1 ON esi1.extern_string_input_id = input.name_id " + 
            "    LEFT JOIN choice_collection ON choice_collection.choice_collection_id = input_collection.choice_collection_id " + 
            "           AND experiment_version_group_mapping.experiment_id = choice_collection.experiment_ds_id " + 
            "           AND outputs.answer = choice_collection.choice_order " + 
            " LEFT JOIN extern_string_list_label ON extern_string_list_label.extern_string_list_label_id = choice_collection.choice_id " + 
            " where _id in" + 
            " (SELECT _id FROM events" + 
            " INNER JOIN outputs ON _id = outputs.event_id " + 
            "    INNER JOIN experiment_version_group_mapping ON events.experiment_version_group_mapping_id = experiment_version_group_mapping.experiment_version_group_mapping_id " + 
            "           AND experiment_version_group_mapping.events_posted = 1 " + 
            "    LEFT JOIN input_collection ON input_collection.input_collection_id = experiment_version_group_mapping.input_collection_id " + 
            "           AND experiment_version_group_mapping.experiment_id = input_collection.experiment_ds_id " + 
            " INNER JOIN input ON input.input_id = input_collection.input_id " + 
            "           AND outputs.input_id = input.input_id " + 
            " INNER JOIN extern_string_input AS esi1 ON esi1.extern_string_input_id = input.name_id " + 
            "    LEFT JOIN choice_collection ON choice_collection.choice_collection_id = input_collection.choice_collection_id " + 
            "           AND experiment_version_group_mapping.experiment_id = choice_collection.experiment_ds_id " + 
            "           AND outputs.answer = choice_collection.choice_order " + 
            " LEFT JOIN extern_string_list_label ON extern_string_list_label.extern_string_list_label_id = choice_collection.choice_id " + 
            "    WHERE experiment_version_group_mapping.experiment_id = " + experimentIdParam + " " + 
            "   AND esi1.label = 'participantId' " + 
            "        AND answer = '" + participantIdParam + "'" + 
            " ORDER BY events._id DESC);";
      return query;
  }

  private String buildSqlQueryForAllParticipants(String experimentIdParam) {
    String query = "SELECT * FROM events" + 
            " INNER JOIN outputs ON _id = outputs.event_id " + 
            " INNER JOIN experiment_version_group_mapping ON events.experiment_version_group_mapping_id = experiment_version_group_mapping.experiment_version_group_mapping_id " + 
            "           AND experiment_version_group_mapping.events_posted = 1 " + 
            " INNER JOIN experiment_detail ON " + 
            "    experiment_detail.experiment_detail_id = experiment_version_group_mapping.experiment_detail_id " + 
            " INNER JOIN group_detail ON " +  
            "    group_detail.group_detail_id = experiment_version_group_mapping.group_detail_id " + 
            " LEFT JOIN input_collection ON input_collection.input_collection_id = experiment_version_group_mapping.input_collection_id " + 
            "           AND experiment_version_group_mapping.experiment_id = input_collection.experiment_ds_id " + 
            " INNER JOIN input ON input.input_id = input_collection.input_id " + 
            "           AND outputs.input_id = input.input_id " + 
            " INNER JOIN extern_string_input AS esi1 ON esi1.extern_string_input_id = input.name_id " + 
            "    LEFT JOIN choice_collection ON choice_collection.choice_collection_id = input_collection.choice_collection_id " + 
            "           AND experiment_version_group_mapping.experiment_id = choice_collection.experiment_ds_id " + 
            "           AND outputs.answer = choice_collection.choice_order " + 
            " LEFT JOIN extern_string_list_label ON extern_string_list_label.extern_string_list_label_id = choice_collection.choice_id " + 
            " where _id in" + 
            " (SELECT _id FROM events" + 
            " INNER JOIN outputs ON _id = outputs.event_id " + 
            "    INNER JOIN experiment_version_group_mapping ON events.experiment_version_group_mapping_id = experiment_version_group_mapping.experiment_version_group_mapping_id " + 
            "           AND experiment_version_group_mapping.events_posted = 1 " + 
            "    LEFT JOIN input_collection ON input_collection.input_collection_id = experiment_version_group_mapping.input_collection_id " + 
            "           AND experiment_version_group_mapping.experiment_id = input_collection.experiment_ds_id " + 
            " INNER JOIN input ON input.input_id = input_collection.input_id " + 
            "           AND outputs.input_id = input.input_id " + 
            " INNER JOIN extern_string_input AS esi1 ON esi1.extern_string_input_id = input.name_id " + 
            "    LEFT JOIN choice_collection ON choice_collection.choice_collection_id = input_collection.choice_collection_id " + 
            "           AND experiment_version_group_mapping.experiment_id = choice_collection.experiment_ds_id " + 
            "           AND outputs.answer = choice_collection.choice_order " + 
            " LEFT JOIN extern_string_list_label ON extern_string_list_label.extern_string_list_label_id = choice_collection.choice_id " + 
            "    WHERE experiment_version_group_mapping.experiment_id = " + experimentIdParam + " " + 
            " ORDER BY events._id DESC);";
      return query;
  }


}