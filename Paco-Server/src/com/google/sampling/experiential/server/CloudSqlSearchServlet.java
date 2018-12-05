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
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryJsonParser;
import com.pacoapp.paco.shared.util.QueryPreprocessor;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.SelectUtils;

@SuppressWarnings("serial")
public class CloudSqlSearchServlet extends HttpServlet {
  public static final Logger log = Logger.getLogger(CloudSqlSearchServlet.class.getName());
  private static Map<String, Class> validColumnNamesDataTypeInDb = Maps.newHashMap();
  private static List<String> dateColumns = Lists.newArrayList();
  private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("ZZ");
  private static String CLIENT_REQUEST = "ClientReq";
  
  
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
      Select clientJsqlStatement = null;
      Select optimizedSelect = null;
      resp.setContentType("text/plain");
      // NOTE: Group by, having and projection columns related functionality can be toggled on and off with the following flag 
      boolean enableGrpByAndProjection = true;

      CloudSQLDao impl = new CloudSQLDaoImpl();
      try {
        String postBodyString = RequestProcessorUtil.getBody(req);
        sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(postBodyString, enableGrpByAndProjection);
        if (sqlQueryObj == null) {
          sendErrorMessage(resp, mapper,
                           ErrorMessages.JSON_PARSER_EXCEPTION + postBodyString);
          return;
        }
        String plainSql = SearchUtil.getPlainSql(sqlQueryObj);
        clientJsqlStatement = SearchUtil.getJsqlSelectStatement(plainSql);

        QueryPreprocessor qProcessor = new QueryPreprocessor(clientJsqlStatement, validColumnNamesDataTypeInDb, true, dateColumns,
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
          SearchUtil.addJoinClause(clientJsqlStatement);
        }
        if(sqlQueryObj.isFullEventAndOutputs()) {
          optimizedSelect = modifyToOptimizePerformance(clientJsqlStatement);
        } else {
          optimizedSelect = clientJsqlStatement;
        }
        
        List<Long> adminExperimentsinDB = ExperimentAccessManager.getExistingExperimentIdsForAdmin(loggedInUser, 0,
                                                                                                   null)
                                                                 .getExperiments();
        aclQuery = ACLHelper.getModifiedQueryBasedOnACL(optimizedSelect, loggedInUser, adminExperimentsinDB, qProcessor);
        log.info("opt performance query with acl:" + aclQuery);
        long startTime = System.currentTimeMillis();

        if (sqlQueryObj.isFullEventAndOutputs()) {
          evtList = impl.getEvents(aclQuery, tzForClient, null);
          evQryStatus.setEvents(evtList);
          evQryStatus.setStatus(Constants.SUCCESS);
          results = mapper.writeValueAsString(evQryStatus);
        } else {
          JSONArray resultsArray = impl.getResultSetAsJson(aclQuery, tzForClient, null);
          JSONObject resultset = new JSONObject();
          resultset.put("customResponse", resultsArray);
          resultset.put("status", Constants.SUCCESS);
          results = resultset.toString();
        }
        long diff = System.currentTimeMillis() - startTime;
        log.info("complete search qry took " + diff);
        resp.getWriter().println(results);
      } catch (JSONException jsonEx) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(jsonEx);
        log.warning( ErrorMessages.JSON_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.JSON_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (JSQLParserException e) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(e);
        log.warning( ErrorMessages.ADD_DEFAULT_COLUMN_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.ADD_DEFAULT_COLUMN_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (SQLException sqle) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(sqle);
        log.warning( ErrorMessages.SQL_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.SQL_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (ParseException e) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(e);
        log.warning( ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + exceptionString);
        sendErrorMessage(resp, mapper, ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + exceptionString);
        return;
      } catch (Exception e) {
        String exceptionString = ExceptionUtil.getStackTraceAsString(e);
        if (e.toString().contains(ErrorMessages.UNAUTHORIZED_ACCESS.getDescription())) {
          log.warning( ErrorMessages.UNAUTHORIZED_ACCESS.getDescription() + exceptionString);
          sendErrorMessage(resp, mapper,  e.getMessage());
          return;
        } else {
          log.warning( ErrorMessages.GENERAL_EXCEPTION.getDescription() + exceptionString);
          sendErrorMessage(resp, mapper, ErrorMessages.GENERAL_EXCEPTION.getDescription()+ e);
          return;
        }
      }
    }
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
  
  private static void modifyProjectionColumnsToClientQuery(Select selStatement) {
    SelectBody sb = selStatement.getSelectBody();
    SelectItem si1 = new SelectExpressionItem();
    ((SelectExpressionItem)si1).setExpression(new Column(Constants.UNDERSCORE_ID));
    SelectItem si2 = new SelectExpressionItem();
    ((SelectExpressionItem)si2).setExpression(new Column(OutputBaseColumns.NAME));
    List<SelectItem> siList = Lists.newArrayList();
    siList.add(si1);
    siList.add(si2);
    ((PlainSelect)sb).setSelectItems(siList);  
  }
  

  //instead of currentSelect which is --> select * from events inner join outputs on events._id=outputs.event_id where <conditions> <limit><group><order> 
   // do a late fetch as
   // optimizedSelect(firstJoinObj) which is -->               select * from events inner join ouputs on events._id=outputs.event_id
   // (secondJoinObj with subselect)                                          inner join (select * from events inner join outputs on events._id=outputs.event_id where <conditions> <limit><group><order>) as clientReq 
   // (with secondJoinCondition)                                                    on events._id=clientReq._id and outputs.event_id=clientReq._id and outputs.text=clientReq.text
   private static Select modifyToOptimizePerformance(Select currentSelect) throws JSQLParserException {
     PlainSelect optimizedPlainSelect = null;
     Select optimizedSelect = null;
     Expression firstJoinOnExp = null;
     Expression secondJoinOnExp = null;
     List<Join> jList = Lists.newArrayList();
     FromItem ft = new Table(OutputBaseColumns.TABLE_NAME); 
     Join firstJoinObj = new Join();
     Join secondJoinObj = new Join();
     SubSelect clientReqQuery = new SubSelect();
     StringBuffer secondJoinCondition = new StringBuffer();
     
     // even though it is a select * , when we optimize we change it to get only _id and text
     modifyProjectionColumnsToClientQuery(currentSelect);
     clientReqQuery.setSelectBody(currentSelect.getSelectBody());
     clientReqQuery.setAlias(new Alias(CLIENT_REQUEST));
     
     // second join on condition
     secondJoinCondition.append(EventServerColumns.TABLE_NAME + "." + Constants.UNDERSCORE_ID+ " = " +CLIENT_REQUEST + "." + Constants.UNDERSCORE_ID + " AND ");
     secondJoinCondition.append(OutputBaseColumns.TABLE_NAME + "." + OutputBaseColumns.EVENT_ID + " = "  + CLIENT_REQUEST + "." + Constants.UNDERSCORE_ID + " AND ");
     secondJoinCondition.append(OutputBaseColumns.TABLE_NAME + "." + OutputBaseColumns.NAME+ " = "  + CLIENT_REQUEST + "." + OutputBaseColumns.NAME);
          
     try {
       firstJoinOnExp = CCJSqlParserUtil.parseCondExpression(EventServerColumns.TABLE_NAME + "." + Constants.UNDERSCORE_ID+ " = " +OutputBaseColumns.TABLE_NAME+ "."+OutputBaseColumns.EVENT_ID);
       secondJoinOnExp = CCJSqlParserUtil.parseCondExpression(secondJoinCondition.toString());
     } catch (JSQLParserException e) {  
       log.warning(ErrorMessages.JSON_PARSER_EXCEPTION.getDescription()+ e.getMessage());
     }
     
     firstJoinObj.setOnExpression(firstJoinOnExp);
     firstJoinObj.setInner(true);
     firstJoinObj.setRightItem(ft);
     
     secondJoinObj.setOnExpression(secondJoinOnExp);
     secondJoinObj.setInner(true);
     secondJoinObj.setRightItem(clientReqQuery);
     
     jList.add(firstJoinObj);
     jList.add(secondJoinObj);
     optimizedSelect = SelectUtils.buildSelectFromTableAndSelectItems(new Table(EventBaseColumns.TABLE_NAME), new AllColumns());
     optimizedPlainSelect = ((PlainSelect) optimizedSelect.getSelectBody());
     optimizedPlainSelect.setJoins(jList);
     return optimizedSelect;
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