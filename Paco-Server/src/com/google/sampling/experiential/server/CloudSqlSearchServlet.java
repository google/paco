package com.google.sampling.experiential.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
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
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;

import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryJsonParser;
import com.pacoapp.paco.shared.util.QueryPreprocessor;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.SelectUtils;

@SuppressWarnings("serial")
public class CloudSqlSearchServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlSearchServlet.class.getName());
  private static Map<String, Class> validColumnNamesDataTypeInDb = Maps.newHashMap();
  private static List<String> dateColumns = Lists.newArrayList();
  private static final String ID = "_id";
  private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("ZZ");
  private static final String SUCCESS = "Success";
  private static final String FAILURE = "Failure";
  private static final String STAR = "*";
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    throw new ServletException(ErrorMessages.METHOD_NOT_SUPPORTED.getDescription());
  }

  private void loadColInfo() {
    if (validColumnNamesDataTypeInDb.size() == 0) {
      validColumnNamesDataTypeInDb.put(ID, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.EXPERIMENT_ID, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.EXPERIMENT_SERVER_ID, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.EXPERIMENT_NAME, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.EXPERIMENT_VERSION, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.SCHEDULE_TIME, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.RESPONSE_TIME, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.GROUP_NAME, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.ACTION_TRIGGER_ID, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.ACTION_TRIGGER_SPEC_ID, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.ACTION_ID, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.WHO, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.WHEN, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.PACO_VERSION, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.APP_ID, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.JOINED, LongValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.SORT_DATE, StringValue.class);
      validColumnNamesDataTypeInDb.put(EventBaseColumns.CLIENT_TIME_ZONE, StringValue.class);
      validColumnNamesDataTypeInDb.put(OutputBaseColumns.NAME, StringValue.class);
      validColumnNamesDataTypeInDb.put(OutputBaseColumns.ANSWER, StringValue.class);
    }
    if(dateColumns.size() == 0){
      dateColumns.add(EventBaseColumns.RESPONSE_TIME);
      dateColumns.add(EventBaseColumns.SCHEDULE_TIME);
      dateColumns.add("`"+ EventBaseColumns.WHEN +"`");
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    setCharacterEncoding(req, resp);
    User user = AuthUtil.getWhoFromLogin();
    String loggedInUser = null;
    EventQueryStatus evQryStatus = new EventQueryStatus();
    ObjectMapper mapper = JsonConverter.getObjectMapper();
    DateTimeZone tzForClient = TimeUtil.getTimeZoneForClient(req);
    
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else {
      loadColInfo();
      loggedInUser = AuthUtil.getEmailOfUser(req, user);
      SQLQuery sqlQueryObj = null;
      List<EventDAO> evtList = null;
      String aclQuery = null;
      Select selStatement = null;
      resp.setContentType("text/plain");
      // NOTE: Group by, having and projection columns related functionality can be toggled on and off with the following flag 
      boolean enableGrpByAndProjection = false;

      CloudSQLDao impl = new CloudSQLDaoImpl();
      String reqBody = RequestProcessorUtil.getBody(req);
      try {
        sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(reqBody, enableGrpByAndProjection);
        String plainSql = SearchUtil.getPlainSql(sqlQueryObj);
        selStatement = SearchUtil.getJsqlSelectStatement(plainSql);
        // Only when we allow projection, and when the user might ask for date columns, we need to retrieve the timezone column to 
        // display the date correctly
        if(enableGrpByAndProjection && isTimezoneNeeded(sqlQueryObj.getProjection())){
          
          SelectUtils.addExpression(selStatement, new Column(EventBaseColumns.CLIENT_TIME_ZONE));
        }
        QueryPreprocessor qProcessor = new QueryPreprocessor(selStatement, validColumnNamesDataTypeInDb, true, dateColumns,
                                                             dtf.withZone(tzForClient).print(0));
        if (qProcessor.probableSqlInjection() != null) {
          sendErrorMessage(resp, mapper,
                           ErrorMessages.PROBABLE_SQL_INJECTION + qProcessor.probableSqlInjection());
          return;
        }
        if (qProcessor.getInvalidDataType() != null) {
          sendErrorMessage(resp, mapper, ErrorMessages.INVALID_DATA_TYPE.getDescription() + qProcessor.getInvalidDataType());
          return;
        }
        if (qProcessor.getInvalidColumnName() != null) {
          sendErrorMessage(resp, mapper,  ErrorMessages.INVALID_COLUMN_NAME.getDescription()+ qProcessor.getInvalidColumnName());
          return;
        }
        
        if (qProcessor.isOutputColumnsPresent()) {
          SearchUtil.addJoinClause(selStatement);
        }
        List<Long> adminExperimentsinDB = ExperimentAccessManager.getExistingExperimentIdsForAdmin(loggedInUser, 0,
                                                                                                   null)
                                                                 .getExperiments();
        aclQuery = ACLHelper.getModifiedQueryBasedOnACL(selStatement, loggedInUser, adminExperimentsinDB, qProcessor);
        long startTime = System.currentTimeMillis();
        evtList = impl.getEvents(aclQuery, tzForClient);
        long diff = System.currentTimeMillis() - startTime;
        log.info("complete search qry took " + diff);
        evQryStatus.setEvents(evtList);
        evQryStatus.setStatus(SUCCESS);
        String results = mapper.writeValueAsString(evQryStatus);
        resp.getWriter().println(results);
      } catch (JSONException jsonEx) {
        sendErrorMessage(resp, mapper, ErrorMessages.JSON_EXCEPTION.getDescription() + jsonEx);
        return;
      } catch (JSQLParserException e) {
        sendErrorMessage(resp, mapper, ErrorMessages.ADD_DEFAULT_COLUMN_EXCEPTION.getDescription() + e);
        return;
      } catch (SQLException sqle) {
        sendErrorMessage(resp, mapper, ErrorMessages.SQL_EXCEPTION.getDescription() + sqle);
        return;
      } catch (ParseException e) {
        sendErrorMessage(resp, mapper, ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + e);
        return;
      } catch (Exception e) {
        if (e.toString().contains(ErrorMessages.UNAUTHORIZED_ACCESS.getDescription())) {
          sendErrorMessage(resp, mapper,  e.getMessage());
          return;
        } else {
          sendErrorMessage(resp, mapper, ErrorMessages.GENERAL_EXCEPTION.getDescription()+ e);
          return;
        }
      }
    }
  }
  
  private boolean isTimezoneNeeded(String[] projCols){
    boolean timezoneNeeded = false;
    for (String eachCol : projCols) {
      if (eachCol.equals(STAR)){
        return false;
      } else if(dateColumns.contains(eachCol)){
        return true;
      } 
    }
    return timezoneNeeded;
  }

  private void sendErrorMessage(HttpServletResponse resp, ObjectMapper mapper,
                                String errorMessage) throws JsonGenerationException, JsonMappingException, IOException {
    EventQueryStatus evQryStatus = new EventQueryStatus();
    evQryStatus.setErrorMessage(errorMessage);
    evQryStatus.setStatus(FAILURE);
    String results = mapper.writeValueAsString(evQryStatus);
    resp.getWriter().println(results);
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}