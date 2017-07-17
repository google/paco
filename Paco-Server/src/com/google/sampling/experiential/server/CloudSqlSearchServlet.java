package com.google.sampling.experiential.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.users.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.shared.EventDAO;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
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
  private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("ZZ");
  
  
  static{
    validColumnNamesDataTypeInDb.put(Constants.UNDERSCORE_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.EXPERIMENT_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.EXPERIMENT_NAME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.EXPERIMENT_VERSION, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.SCHEDULE_TIME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.RESPONSE_TIME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.GROUP_NAME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.ACTION_TRIGGER_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.ACTION_TRIGGER_SPEC_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.ACTION_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.WHO, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.WHEN, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.WHEN_FRAC_SEC, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.PACO_VERSION, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.APP_ID, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.JOINED, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.SORT_DATE, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.CLIENT_TIME_ZONE, StringValue.class);
    validColumnNamesDataTypeInDb.put(OutputBaseColumns.NAME, StringValue.class);
    validColumnNamesDataTypeInDb.put(OutputBaseColumns.ANSWER, StringValue.class);
    dateColumns.add(EventServerColumns.RESPONSE_TIME);
    dateColumns.add(EventServerColumns.SCHEDULE_TIME);
    dateColumns.add(EventServerColumns.WHEN);
  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    throw new ServletException(ErrorMessages.METHOD_NOT_SUPPORTED.getDescription());
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
      loggedInUser = AuthUtil.getEmailOfUser(req, user);
      SQLQuery sqlQueryObj = null;
      List<EventDAO> evtList = null;
      String aclQuery = null;
      String results = null;
      Select selStatement = null;
      resp.setContentType("text/plain");
      // NOTE: Group by, having and projection columns related functionality can be toggled on and off with the following flag 
      boolean enableGrpByAndProjection = true;

      CloudSQLDao impl = new CloudSQLDaoImpl();
      try {
        String postBodyString = RequestProcessorUtil.getBody(req);
        if(postBodyString.startsWith("{\"message")) {
          JSONObject queryObj = new JSONObject(postBodyString);
          postBodyString = queryObj.getString("message");
        }
        sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(postBodyString, enableGrpByAndProjection);
        String plainSql = SearchUtil.getPlainSql(sqlQueryObj);
        selStatement = SearchUtil.getJsqlSelectStatement(plainSql);

        QueryPreprocessor qProcessor = new QueryPreprocessor(selStatement, validColumnNamesDataTypeInDb, true, dateColumns,
                                                             tzForClient);
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
        if (sqlQueryObj.getGroupBy() != null && !isValidGroupBy(sqlQueryObj.getProjection(), Arrays.asList(sqlQueryObj.getGroupBy().split(",")))) {
          sendErrorMessage(resp, mapper,  ErrorMessages.INVALID_GROUPBY.getDescription() + Arrays.toString(sqlQueryObj.getProjection()));
          return;
        }
        if (qProcessor.isOutputColumnsPresent() || sqlQueryObj.isFullEventAndOutputs()) {
          SearchUtil.addJoinClause(selStatement);
        }
        List<Long> adminExperimentsinDB = ExperimentAccessManager.getExistingExperimentIdsForAdmin(loggedInUser, 0,
                                                                                                   null)
                                                                 .getExperiments();
        aclQuery = ACLHelper.getModifiedQueryBasedOnACL(selStatement, loggedInUser, adminExperimentsinDB, qProcessor);
        long startTime = System.currentTimeMillis();
        if (sqlQueryObj.isFullEventAndOutputs()) {
          evtList = impl.getEvents(aclQuery, tzForClient, null);
          evQryStatus.setEvents(evtList);
          evQryStatus.setStatus(Constants.SUCCESS);
          results = mapper.writeValueAsString(evQryStatus);
        } else {
          JSONArray resultsArray = impl.getResultSetAsJson(aclQuery, tzForClient, null);
          JSONObject resultset = new JSONObject();
          //TODO To be changed to someother variable
          resultset.put("events", resultsArray);
          resultset.put("status", Constants.SUCCESS);
          results = resultset.toString();
        }
        long diff = System.currentTimeMillis() - startTime;
        log.info("complete search qry took " + diff);
        
        resp.getWriter().println(results);
      } catch (JSONException jsonEx) {
        log.warning( ErrorMessages.JSON_EXCEPTION.getDescription() + getStackTraceAsString(jsonEx));
        sendErrorMessage(resp, mapper, ErrorMessages.JSON_EXCEPTION.getDescription() + getStackTraceAsString(jsonEx));
        return;
      } catch (JSQLParserException e) {
        log.warning( ErrorMessages.ADD_DEFAULT_COLUMN_EXCEPTION.getDescription() + getStackTraceAsString(e));
        sendErrorMessage(resp, mapper, ErrorMessages.ADD_DEFAULT_COLUMN_EXCEPTION.getDescription() + getStackTraceAsString(e));
        return;
      } catch (SQLException sqle) {
        log.warning( ErrorMessages.SQL_EXCEPTION.getDescription() + getStackTraceAsString(sqle));
        sendErrorMessage(resp, mapper, ErrorMessages.SQL_EXCEPTION.getDescription() + getStackTraceAsString(sqle));
        return;
      } catch (ParseException e) {
        log.warning( ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + getStackTraceAsString(e));
        sendErrorMessage(resp, mapper, ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + getStackTraceAsString(e));
        return;
      } catch (Exception e) {
        if (e.toString().contains(ErrorMessages.UNAUTHORIZED_ACCESS.getDescription())) {
          log.warning( ErrorMessages.UNAUTHORIZED_ACCESS.getDescription() + getStackTraceAsString(e));
          sendErrorMessage(resp, mapper,  e.getMessage());
          return;
        } else {
          log.warning( ErrorMessages.GENERAL_EXCEPTION.getDescription() + getStackTraceAsString(e));
          sendErrorMessage(resp, mapper, ErrorMessages.GENERAL_EXCEPTION.getDescription()+ e);
          return;
        }
      }
    }
  }
  
  private String getStackTraceAsString(Throwable e) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream pw = new PrintStream(out);
    e.printStackTrace(pw);
    final String string = out.toString();
    return string;
  }
  
  private boolean isValidGroupBy(String[] selectColumnsArr, List<String> groupByCols) {
    // all (plain) columns in projection (except aggregate functions on columns) must be in group by list
    // select experiment_id, count(experiment_version) from events group by who INVALID
    // plain columns->experiment_id; aggregate columns->experiment_version); group by column->who
    // select who, count(experiment_version) from events group by who VALID
    boolean isValidGroupBy = true;
    List<String> selectColumns = Lists.newArrayList();
    for(String s: selectColumnsArr) {
      //aggregate function on columns will have open brackets
      if (!(s.contains("("))) {
        selectColumns.add(s.trim());
      }
    }
    if((selectColumns != null && groupByCols.size() > 0 && !groupByCols.containsAll(selectColumns))) {
      isValidGroupBy = false;
    }
   return isValidGroupBy;
  }
  
  private void sendErrorMessage(HttpServletResponse resp, ObjectMapper mapper,
                                String errorMessage) throws JsonGenerationException, JsonMappingException, IOException {
    EventQueryStatus evQryStatus = new EventQueryStatus();
    evQryStatus.setErrorMessage(errorMessage);
    evQryStatus.setStatus(Constants.FAILURE);
    String results = mapper.writeValueAsString(evQryStatus);
    resp.getWriter().println(results);
  }

  private void setCharacterEncoding(HttpServletRequest req,
                                    HttpServletResponse resp) throws UnsupportedEncodingException {
    req.setCharacterEncoding("UTF-8");
    resp.setCharacterEncoding("UTF-8");
  }

}