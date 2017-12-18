package com.google.sampling.experiential.server;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.datastore.ExperimentLookupColumns;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.model2.SQLQuery;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.QueryPreprocessor;
import com.pacoapp.paco.shared.util.SearchUtil;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public abstract class SearchQuery {
  Float pacoProtocol;
  SQLQuery sqlQueryObj;
  boolean isClientTzNeedsAdded = false;
  Select jsqlStatement = null;
  QueryPreprocessor qPreProcessor = null;
  private static Map<String, Class> validColumnNamesDataTypeInDb = Maps.newHashMap();
  static List<String> localDateColumns = Lists.newArrayList();
  private static List<String> utcDateColumns = Lists.newArrayList();
  private static List<String> allDateColumns = Lists.newArrayList();
 
  public static final Logger log = Logger.getLogger(SearchQuery.class.getName());
  static{
    validColumnNamesDataTypeInDb.put(Constants.UNDERSCORE_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.EXPERIMENT_ID, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.EXPERIMENT_NAME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.EXPERIMENT_VERSION, LongValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.SCHEDULE_TIME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.RESPONSE_TIME, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.SCHEDULE_TIME_UTC, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.RESPONSE_TIME_UTC, StringValue.class);
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
    validColumnNamesDataTypeInDb.put(EventServerColumns.SORT_DATE_UTC, StringValue.class);
    validColumnNamesDataTypeInDb.put(EventServerColumns.CLIENT_TIME_ZONE, StringValue.class);
    validColumnNamesDataTypeInDb.put(OutputBaseColumns.NAME, StringValue.class);
    validColumnNamesDataTypeInDb.put(OutputBaseColumns.ANSWER, StringValue.class);
    localDateColumns.add(EventServerColumns.RESPONSE_TIME);
    localDateColumns.add(EventServerColumns.SCHEDULE_TIME);
    localDateColumns.add(EventServerColumns.SORT_DATE);
    utcDateColumns.add(EventServerColumns.RESPONSE_TIME_UTC);
    utcDateColumns.add(EventServerColumns.SCHEDULE_TIME_UTC);
    utcDateColumns.add(EventServerColumns.SORT_DATE_UTC);
    utcDateColumns.add(EventServerColumns.WHEN);
    allDateColumns.addAll(localDateColumns);
    allDateColumns.addAll(utcDateColumns);
  }
  
  public abstract PacoResponse executeAcledQuery(String query) throws SQLException, ParseException, JSONException;
  public abstract void addOptimizationToQuery() throws JSQLParserException;
  
  public PacoResponse process(String loggedInUser) throws JSQLParserException, Exception {
    EventQueryStatus evStatus = null;
    PacoResponse pacoResp = null;
    // 1.client tz needed
    if (isClientTzNeedsAdded) { 
      List<String> projList = Lists.newArrayList(sqlQueryObj.getProjection());
      if (sqlQueryObj.getGroupBy() == null && (projList.contains(EventBaseColumns.RESPONSE_TIME) || (projList.contains(EventBaseColumns.SCHEDULE_TIME)))) {
        sqlQueryObj.addClientTzToProjection();
      }
    }
    // 2. do query preprocessing and validation
    evStatus = queryPreprocessingAndValidation();
    if (Constants.SUCCESS.equals(evStatus.getStatus())) {
      // 3. add join
      addJoinClauses();
      // 4. getOptimized Query
      addOptimizationToQuery();
      // 5. Add acl-ing to query
      String acledQuery = addAclToQuery(loggedInUser);
      // 6. execute acl-ed query
      pacoResp = executeAcledQuery(acledQuery);
    } else {
      return evStatus;
    }
    return pacoResp;
  }
 
  public EventQueryStatus queryPreprocessingAndValidation() throws JSQLParserException, Exception { 
    EventQueryStatus evQueryStatus = new EventQueryStatus();
    String plainSql = SearchUtil.getPlainSql(sqlQueryObj);
    Select clientJsqlStatement = SearchUtil.getJsqlSelectStatement(plainSql);
    boolean webRequest = true;
    evQueryStatus.setStatus(Constants.SUCCESS);
    QueryPreprocessor qProcessor = new QueryPreprocessor(clientJsqlStatement, validColumnNamesDataTypeInDb, webRequest, allDateColumns);
    if (qProcessor.probableSqlInjection() != null) {
      evQueryStatus.setErrorMessage(ErrorMessages.PROBABLE_SQL_INJECTION + qProcessor.probableSqlInjection());
      return evQueryStatus;
    }
    if (qProcessor.getInvalidDataType() != null) {
      evQueryStatus.setErrorMessage(ErrorMessages.INVALID_DATA_TYPE.getDescription() + qProcessor.getInvalidDataType());
      return evQueryStatus;
    }
    if (qProcessor.getInvalidColumnName() != null) {
      evQueryStatus.setErrorMessage(ErrorMessages.INVALID_COLUMN_NAME.getDescription()+ qProcessor.getInvalidColumnName());
      return evQueryStatus;
    }
    if (sqlQueryObj.getGroupBy() != null && !isValidGroupBy(sqlQueryObj.getProjection(), Arrays.asList(sqlQueryObj.getGroupBy().split(",")))) {
      evQueryStatus.setErrorMessage(ErrorMessages.INVALID_GROUPBY.getDescription() + Arrays.toString(sqlQueryObj.getProjection()));
      return evQueryStatus;
    }
    jsqlStatement = clientJsqlStatement;
    qPreProcessor = qProcessor;
    return evQueryStatus;
  }
  
  public void addJoinClauses() throws JSQLParserException { 
    addExperimentLookupJoinClause(jsqlStatement);
  }
  
  public String addAclToQuery(String loggedInUser) throws Exception { 
    List<Long> adminExperimentsinDB = ExperimentAccessManager.getExistingExperimentIdsForAdmin(loggedInUser, 0,
                                                                                               null)
                                                             .getExperiments();
    String aclQuery = ACLHelper.getModifiedQueryBasedOnACL(jsqlStatement, loggedInUser, adminExperimentsinDB, qPreProcessor);
    log.info("opt performance query with acl:" + aclQuery);
    return aclQuery;
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
  
  private static void addExperimentLookupJoinClause(Select selStatement) throws JSQLParserException {
    PlainSelect ps = null;
    Expression joinExp = null;
    List<Join> jList = Lists.newArrayList();
    Join joinObj = new Join();
    FromItem ft = new Table(ExperimentLookupColumns.TABLE_NAME); 
    try {
      joinExp = CCJSqlParserUtil.parseCondExpression(EventServerColumns.TABLE_NAME + "." + EventServerColumns.EXPERIMENT_LOOKUP_ID + " = " + ExperimentLookupColumns.TABLE_NAME + "." + ExperimentLookupColumns.EXPERIMENT_LOOKUP_ID);
    } catch (JSQLParserException e) {
      e.printStackTrace();
    }
    joinObj.setOnExpression(joinExp);
    joinObj.setInner(true);
    joinObj.setRightItem(ft);
    jList.add(joinObj);
    ps = ((PlainSelect) selStatement.getSelectBody());
    ps.setJoins(jList);
  }
}
