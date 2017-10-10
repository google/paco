package com.google.sampling.experiential.server.viz.appusage;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.server.ACLHelper;
import com.google.sampling.experiential.server.CloudSQLDao;
import com.google.sampling.experiential.server.CloudSQLDaoImpl;
import com.google.sampling.experiential.server.CloudSqlSearchServlet;
import com.google.sampling.experiential.server.EventQueryStatus;
import com.google.sampling.experiential.server.ExperimentAccessManager;
import com.google.sampling.experiential.shared.EventDAO;
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

public class CloudSqlRequestProcessor {
  private static Map<String, Class> validColumnNamesDataTypeInDb = Maps.newHashMap();
  private static List<String> dateColumns = Lists.newArrayList();
  static final String ID = "_id";
  static final DateTimeFormatter dtf = DateTimeFormat.forPattern("ZZ");
  private static final String STAR = "*";

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

  public static EventQueryStatus processSearchQuery(String loggedInUser, String reqBody, DateTimeZone tzForClient) {
    EventQueryStatus eventQueryResult = new EventQueryStatus();
    // NOTE: Group by, having and projection columns related functionality can
    // be toggled on and off with the following flag
    boolean enableGrpByAndProjection = false;

    CloudSQLDao impl = new CloudSQLDaoImpl();
    try {

      SQLQuery sqlQueryObj = QueryJsonParser.parseSqlQueryFromJson(reqBody, enableGrpByAndProjection);

      String plainSql = SearchUtil.getPlainSql(sqlQueryObj);
      Select selStatement = SearchUtil.getJsqlSelectStatement(plainSql);
      // Only when we allow projection, and when the user might ask for date
      // columns, we need to retrieve the timezone column to
      // display the date correctly
      if (enableGrpByAndProjection && isTimezoneNeeded(sqlQueryObj.getProjection())) {
        SelectUtils.addExpression(selStatement, new Column(EventServerColumns.CLIENT_TIME_ZONE));
      }

      QueryPreprocessor qProcessor = new QueryPreprocessor(selStatement, validColumnNamesDataTypeInDb, true,
                                                           dateColumns);

      if (qProcessor.probableSqlInjection() != null) {
        eventQueryResult.setErrorMessage(ErrorMessages.PROBABLE_SQL_INJECTION + qProcessor.probableSqlInjection());
        return eventQueryResult;
      }
      if (qProcessor.getInvalidDataType() != null) {
        eventQueryResult.setErrorMessage(ErrorMessages.INVALID_DATA_TYPE.getDescription()
                                         + qProcessor.getInvalidDataType());
        return eventQueryResult;
      }
      if (qProcessor.getInvalidColumnName() != null) {
        eventQueryResult.setErrorMessage(ErrorMessages.INVALID_COLUMN_NAME.getDescription()
                                         + qProcessor.getInvalidColumnName());
        return eventQueryResult;
      }

      if (qProcessor.isOutputColumnsPresent()) {
        SearchUtil.addJoinClause(selStatement);
      }

      List<Long> adminExperimentsinDB = ExperimentAccessManager.getExistingExperimentIdsForAdmin(loggedInUser, 0, null)
                                                               .getExperiments();
      String aclQuery = ACLHelper.getModifiedQueryBasedOnACL(selStatement, loggedInUser, adminExperimentsinDB,
                                                             qProcessor);

      long startTime = System.currentTimeMillis();
      boolean withOutputs = true;
      // aclQuery will have the eventId in itself, so we send the individual param eventId as null
      List<EventDAO> eventList = impl.getEvents(aclQuery, withOutputs);
      long diff = System.currentTimeMillis() - startTime;
      CloudSqlSearchServlet.log.info("complete search qry took " + diff);

      eventQueryResult.setEvents(eventList);
    } catch (JSONException jsonEx) {
      eventQueryResult.setErrorMessage(ErrorMessages.JSON_EXCEPTION.getDescription() + jsonEx);
    } catch (JSQLParserException e) {
      eventQueryResult.setErrorMessage(ErrorMessages.ADD_DEFAULT_COLUMN_EXCEPTION.getDescription() + e);
    } catch (SQLException sqle) {
      eventQueryResult.setErrorMessage(ErrorMessages.SQL_EXCEPTION.getDescription() + sqle);
    } catch (ParseException e) {
      eventQueryResult.setErrorMessage(ErrorMessages.TEXT_PARSE_EXCEPTION.getDescription() + e);
    } catch (Exception e) {
      if (e.toString().contains(ErrorMessages.UNAUTHORIZED_ACCESS.getDescription())) {
        eventQueryResult.setErrorMessage(e.getMessage());
      } else {
        eventQueryResult.setErrorMessage(ErrorMessages.GENERAL_EXCEPTION.getDescription() + e);
      }
    }
    return eventQueryResult;
  }

  private static boolean isTimezoneNeeded(String[] projCols) {
    boolean timezoneNeeded = false;
    for (String eachCol : projCols) {
      if (eachCol.equals(STAR)) {
        return false;
      } else if (dateColumns.contains(eachCol)) {
        return true;
      }
    }
    return timezoneNeeded;
  }

}

