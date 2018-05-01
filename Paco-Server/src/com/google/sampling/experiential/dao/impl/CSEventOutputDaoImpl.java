package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentGroupVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.OutputServerColumns;
import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSExperimentUserDao;
import com.google.sampling.experiential.dao.CSExperimentVersionGroupMappingDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSPivotHelperDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.QueryConstants;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.TimeUtil;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSEventOutputDaoImpl implements CSEventOutputDao {
  public static final Logger log = Logger.getLogger(CSEventOutputDaoImpl.class.getName());
  private static List<Column> eventColInsertList = Lists.newArrayList();
  private static List<Column> eventColSearchList = Lists.newArrayList();
  private static List<Column> outputColList = Lists.newArrayList();
  public static final String WHEN = "when";

  static {
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_ID));
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_NAME));
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_VERSION));
    eventColSearchList.add(new Column(EventServerColumns.GROUP_NAME));
    
    eventColInsertList.add(new Column(EventServerColumns.SCHEDULE_TIME_UTC));
    eventColInsertList.add(new Column(EventServerColumns.RESPONSE_TIME_UTC));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_TRIGGER_ID));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_TRIGGER_SPEC_ID));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_ID));
    eventColInsertList.add(new Column(EventServerColumns.WHO));
    eventColInsertList.add(new Column(EventServerColumns.WHEN));
    eventColInsertList.add(new Column(EventServerColumns.WHEN_FRAC_SEC));
    eventColInsertList.add(new Column(EventServerColumns.PACO_VERSION));
    eventColInsertList.add(new Column(EventServerColumns.APP_ID));
    eventColInsertList.add(new Column(EventServerColumns.JOINED));
    eventColInsertList.add(new Column(EventServerColumns.SORT_DATE_UTC));
    eventColInsertList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColInsertList.add(new Column(Constants.UNDERSCORE_ID));
    eventColInsertList.add(new Column(EventServerColumns.RESPONSE_TIME));
    eventColInsertList.add(new Column(EventServerColumns.SCHEDULE_TIME));
    eventColInsertList.add(new Column(EventServerColumns.SORT_DATE));
    eventColInsertList.add(new Column(EventServerColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID));
    eventColSearchList.addAll(eventColInsertList);
    
    outputColList.add(new Column(OutputServerColumns.EVENT_ID));
    outputColList.add(new Column(OutputServerColumns.NAME));
    outputColList.add(new Column(OutputServerColumns.ANSWER));
    outputColList.add(new Column(OutputServerColumns.INPUT_ID));
  }

  @Override
  public JSONArray getResultSetAsJson(String query, List<String> dateColumns) throws SQLException, ParseException, JSONException {
    Connection conn = null;
    JSONArray multipleRecords = null;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;
    DateTime dateFromDb = null;
    DateTime dateInLocal = null;
    String offsetHrsStr = null;
    int offsetHrs = 0;
    String colName = null;
    String colValue = null;
    Object anyObject = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();

      statementSelectEvent = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      rs = statementSelectEvent.executeQuery();
      if (rs != null) {
        ResultSetMetaData rsmd = rs.getMetaData();
        multipleRecords = new JSONArray();
        JSONObject eachRecord = null;
        while (rs.next()) {
          eachRecord = new JSONObject();
          for ( int i = 1; i <= rsmd.getColumnCount(); i++) {
            colName = rsmd.getColumnName(i);
            anyObject = rs.getObject(i);
            colValue =  anyObject != null ? anyObject.toString() : null;
            //if client timezone, then do not write to json
            if (!(colName.equalsIgnoreCase(EventServerColumns.CLIENT_TIME_ZONE))) {
              //if date columns in projection, then display along with corresponding timezone
              if(dateColumns.contains(colName)) {
                offsetHrsStr =  rs.getString(EventServerColumns.CLIENT_TIME_ZONE);
                offsetHrs = com.google.sampling.experiential.server.TimeUtil.getIntFromOffsetString(offsetHrsStr);
                dateFromDb = new DateTime(rs.getTimestamp(colName).getTime());
                // change the tz with value stored in db
                dateInLocal = dateFromDb.withZoneRetainFields(DateTimeZone.forOffsetHours(offsetHrs));
                colValue = dateInLocal.toString();
              }
              eachRecord.put(colName, colValue);
            }
          }
          multipleRecords.put(eachRecord);
        }
      }
    } finally {
      try {
       if( rs != null) {
         rs.close();
       }
        if (statementSelectEvent != null) {
          statementSelectEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
   return multipleRecords;
  }
  
  @Override
  public boolean insertEventAndOutputs(Event event) throws Exception {
    if (event == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }

    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateEventOutput = null;
    boolean retVal = false;
    Timestamp whenTs = null;
    Long pvUpdateEvmId = null;
    Integer pvUpdateAnonWhoId = null;
    Long expIdLong = null;
    int whenFrac = 0;
    boolean migrationFlag = false;
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList eventExprList = new ExpressionList();
    ExpressionList outputExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    List<Expression>  out = Lists.newArrayList();
    Insert eventInsert = new Insert();
    Insert outputInsert = new Insert();
    CSExperimentUserDao euImpl = new CSExperimentUserDaoImpl();
    CSExperimentVersionGroupMappingDao evmDaoImpl = new CSExperimentVersionGroupMappingDaoImpl();
    CSInputCollectionDao icDaoImpl = new CSInputCollectionDaoImpl();
    CSPivotHelperDao pvDaoImpl = new CSPivotHelperDaoImpl();
    List<Long> pvUpdateInputIds = Lists.newArrayList();
    
    try {
      log.info("Inserting event->" + event.getId());
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      eventInsert.setTable(new Table(EventServerColumns.TABLE_NAME));
      outputInsert.setTable(new Table(OutputBaseColumns.TABLE_NAME));
      eventInsert.setUseValues(true);
      outputInsert.setUseValues(true);
      eventExprList.setExpressions(exp);
      outputExprList.setExpressions(out);
      eventInsert.setItemsList(eventExprList);
      outputInsert.setItemsList(outputExprList);
      eventInsert.setColumns(eventColInsertList);
      outputInsert.setColumns(outputColList);
      // Adding ? for prepared stmt
      for (Column c : eventColInsertList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      for (Column c : outputColList) {
        ((ExpressionList) outputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      expIdLong = Long.parseLong(event.getExperimentId());

      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
      statementCreateEvent.setLong(i++, event.getActionTriggerId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionTriggerSpecId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionId() != null ? new Long(event.getActionId()) : java.sql.Types.NULL);
      PacoId anonId = euImpl.getAnonymousIdAndCreate(expIdLong, event.getWho(), true);
      pvUpdateAnonWhoId = anonId.getId().intValue();
      statementCreateEvent.setString(i++, anonId.getId().toString());
      if (event.getWhen() != null) {
        whenTs = new Timestamp(event.getWhen().getTime());
        whenFrac = com.google.sampling.experiential.server.TimeUtil.getFractionalSeconds(whenTs);
      }
      statementCreateEvent.setTimestamp(i++, whenTs);
      statementCreateEvent.setInt(i++, whenFrac);
      statementCreateEvent.setString(i++, event.getPacoVersion());
      statementCreateEvent.setString(i++, event.getAppId());
      Boolean joinFlag = null;
      if (event.getWhat() != null) {
        String joinedStat = event.getWhatByKey(EventServerColumns.JOINED);
        if (joinedStat != null) {
          if (joinedStat.equalsIgnoreCase(Constants.TRUE)) {
            joinFlag = true;
          } else {
            joinFlag = false;
          }
        }
      }
      if (joinFlag == null) {
        statementCreateEvent.setNull(i++, java.sql.Types.BOOLEAN);
      } else {
        statementCreateEvent.setBoolean(i++, joinFlag);
      }
      Long sortDateMillis = null;
      if (event.getResponseTime() != null) {
        sortDateMillis = event.getResponseTime().getTime();
      } else {
        sortDateMillis = event.getScheduledTime().getTime();
      }
      statementCreateEvent.setTimestamp(i++, new Timestamp(sortDateMillis));
      statementCreateEvent.setString(i++, event.getTimeZone());
      statementCreateEvent.setLong(i++, event.getId());
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getResponseTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getScheduledTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, new Timestamp(TimeUtil.convertToLocal(new Date(sortDateMillis), event.getTimeZone()).getMillis()));
      Map<String, ExperimentVersionMapping> allEVMInVersion = evmDaoImpl.getAllGroupsInVersion(Long.parseLong(event.getExperimentId()), event.getExperimentVersion());
      // Rename event group Name from null to System, if its system predefined inputs
      ExperimentVersionMapping evmForThisGroup = findMatchingEVMRecord(event, allEVMInVersion, migrationFlag);
      pvUpdateEvmId = evmForThisGroup.getExperimentVersionMappingId();
      statementCreateEvent.setLong(i++, pvUpdateEvmId);
      statementCreateEvent.execute();

      Set<What> whatSet = event.getWhat();
      if (whatSet != null) {
        statementCreateEventOutput = conn.prepareStatement(outputInsert.toString());
        for (String key : event.getWhatKeys()) {
          String whatAnswer = event.getWhatByKey(key);
          statementCreateEventOutput.setLong(1, event.getId());
          statementCreateEventOutput.setString(2, key);
          statementCreateEventOutput.setString(3, whatAnswer);
          InputOrderAndChoice currentInput = evmForThisGroup.getInputCollection().getInputOrderAndChoices().get(key);
          // for some reason (scripted variable) this particular output does not have input associated, then add this input variable name to the input collection and get the input id
          if ( currentInput == null) {
            // add this variable to the existing input collection
            Input newInput = null;
            currentInput = new InputOrderAndChoice();
            newInput = icDaoImpl.addUndefinedInputToCollection(expIdLong, evmForThisGroup.getInputCollection().getInputCollectionId(), key);
            currentInput.setInput(newInput);
          }
          statementCreateEventOutput.setLong(4, currentInput.getInput().getInputId().getId());
          pvUpdateInputIds.add(currentInput.getInput().getInputId().getId());
          statementCreateEventOutput.addBatch();
        }
        statementCreateEventOutput.executeBatch();
      }
      
      pvDaoImpl.incrementUpdateCtByOne(pvUpdateEvmId, pvUpdateAnonWhoId, pvUpdateInputIds);
     
      conn.commit();
      // After commit, otherwise, goes to lock issue
      if (!evmForThisGroup.isEventsPosted()) {
        evmDaoImpl.updateEventsPosted(pvUpdateEvmId);
      }
      retVal = true;
    } finally {
      try {
        if (statementCreateEventOutput != null) {
          statementCreateEventOutput.close();
        }
        if (statementCreateEvent != null) {
          statementCreateEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
  }
  
  private ExperimentVersionMapping findMatchingEVMRecord(Event event, Map<String, ExperimentVersionMapping> allEVMMap, boolean migrationFlag) throws Exception{
    ExperimentVersionMapping returnEVM = null;
    CSExperimentVersionGroupMappingDao daoImpl = new CSExperimentVersionGroupMappingDaoImpl();
    Long expId = Long.parseLong(event.getExperimentId());
    log.info("event id"+ event.getId());
    Map<String, ExperimentVersionMapping> allEVMRecords = Maps.newHashMap();
    // if event is posted for a version where we do not have experiment mapping records
    daoImpl.ensureEVMRecord(expId,event.getId(), event.getExperimentName(), event.getExperimentVersion(), event.getExperimentGroupName(), event.getWho(), event.getWhat(), migrationFlag, allEVMRecords);
    returnEVM = allEVMRecords.get(event.getExperimentGroupName());
    String mightBeModifiedGroupName = returnEVM.getGroupInfo().getName();
    allEVMMap.put(mightBeModifiedGroupName, returnEVM);
    return returnEVM;
  }
  
  @Override
  public List<EventDAO> getEvents(String query, boolean withOutputs) throws SQLException, ParseException {
    List<EventDAO> evtDaoList = Lists.newArrayList();
    EventDAO event = null;
    Connection conn = null;
    Map<Long, EventDAO> eventMap = null;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      // While we have to retrieve millions of records at a time, we might run
      // into java heap space issue.
      // The following two properties(rs type forward only, concur read only,
      // statement fetch size) of statement are specific to mysql.
      // This signals the driver to stream records one at a time
      statementSelectEvent = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      statementSelectEvent.setFetchSize(Integer.MIN_VALUE);

      Long st1Time = System.currentTimeMillis();
      String selString = statementSelectEvent.toString();
      log.info("step 1 " + selString.substring(selString.indexOf(":")));
      rs = statementSelectEvent.executeQuery();
      if (rs != null) {
        // to maintain the insertion order
        eventMap = Maps.newLinkedHashMap();
        while (rs.next()) {
          event = createEvent(rs, withOutputs);
          com.google.sampling.experiential.server.TimeUtil.adjustTimeZone(event);
          // to group list of whats into event
          EventDAO oldEvent = eventMap.get(event.getId());
          if (oldEvent == null) {
            eventMap.put(event.getId(), event);
          } else {
            // add crt what to old event
            List<WhatDAO> newWhat = event.getWhat();
            if (newWhat != null && oldEvent.getWhat() != null) {
              oldEvent.getWhat().addAll(newWhat);
            }
          }
        }
      }
      log.info("query took " + (System.currentTimeMillis() - st1Time));
    } finally {
      try {
        if(rs != null) {
          rs.close();
        }
        if (statementSelectEvent != null) {
          statementSelectEvent.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    if (eventMap != null && eventMap.size() > 0) {
      evtDaoList = Lists.newArrayList(eventMap.values());
    }

    return evtDaoList;
  }

  
  private EventDAO createEvent(ResultSet rs, boolean withOutputs) {
    EventDAO event = new EventDAO();
    List<WhatDAO> whatList = Lists.newArrayList();
    WhatDAO singleWhat = null;
    try {
      event.setExperimentId(rs.getLong(ExperimentGroupVersionMappingColumns.EXPERIMENT_ID));
      event.setExperimentName(rs.getString(ExperimentDetailColumns.EXPERIMENT_NAME));
      event.setExperimentVersion(rs.getInt(ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION));

      Date scheduleDate = rs.getTimestamp(EventServerColumns.SCHEDULE_TIME);
      DateTime scheduledDateTime = scheduleDate != null ? new DateTime(scheduleDate): null;
      event.setScheduledTime(scheduledDateTime);

      Date responseDate = rs.getTimestamp(EventServerColumns.RESPONSE_TIME);
      DateTime responseDateTime = responseDate != null ? new DateTime(responseDate) : null;
      event.setResponseTime(responseDateTime);

      event.setExperimentGroupName(rs.getString(GroupDetailColumns.NAME));
      event.setActionTriggerId(rs.getLong(EventServerColumns.ACTION_TRIGGER_ID));
      event.setActionTriggerSpecId(rs.getLong(EventServerColumns.ACTION_TRIGGER_SPEC_ID));
      event.setActionId(rs.getLong(EventServerColumns.ACTION_ID));
      event.setWho(rs.getString(EventServerColumns.WHO));
      event.setWhen(new DateTime(rs.getTimestamp(WHEN).getTime() + rs.getInt(EventServerColumns.WHEN_FRAC_SEC)));
      event.setPaco_version(rs.getString(EventServerColumns.PACO_VERSION));
      event.setAppId(rs.getString(EventServerColumns.APP_ID));
      event.setJoined(rs.getBoolean(EventServerColumns.JOINED));
      //sort date cannot be null, so its safe to have new datetime
      event.setSortDate(new DateTime(rs.getTimestamp(EventServerColumns.SORT_DATE)));
      event.setTimezone(rs.getString(EventServerColumns.CLIENT_TIME_ZONE));
      event.setId(rs.getLong(Constants.UNDERSCORE_ID));
      if (withOutputs) {
        String tempWhatText = rs.getString(OutputBaseColumns.NAME);
        if(tempWhatText != null) {
          singleWhat = new WhatDAO(tempWhatText, rs.getString(OutputBaseColumns.ANSWER));
          whatList.add(singleWhat);
        }
        event.setWhat(whatList);
      }
    } catch (SQLException sqle) {
      log.warning(ErrorMessages.SQL_EXCEPTION.getDescription() + sqle);
    }

    return event;
  }

  @Override
  public boolean deleteAllEventsAndOutputsData(Long experimentId, Integer whoAnonId) throws SQLException {
    Connection conn = null;
    PreparedStatement statementDeleteEvents = null;
    PreparedStatement statementDeleteOutputs = null;
    
    String deleteQuery1 = QueryConstants.DELETE_ALL_OUTPUTS.toString() ;
    String deleteQuery2 = QueryConstants.DELETE_ALL_EVENTS.toString() ;
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      if ( whoAnonId != null) {
        deleteQuery1 = deleteQuery1 + " and who_bk=?";
        deleteQuery2 = deleteQuery2 + " and who_bk=?";
      }
      statementDeleteOutputs = conn.prepareStatement(deleteQuery1);
      statementDeleteOutputs.setLong(1, experimentId);
      if (whoAnonId != null) {
        statementDeleteOutputs.setInt(2, whoAnonId);
      }
      statementDeleteOutputs.execute();
      log.info("Deleted " + statementDeleteOutputs.getUpdateCount() +  " output records  for expt id " + experimentId );
      
      statementDeleteEvents = conn.prepareStatement(deleteQuery2);
      statementDeleteEvents.setLong(1, experimentId);
      if (whoAnonId != null) {
        statementDeleteEvents.setInt(2, whoAnonId);
      }
      statementDeleteEvents.execute();
      log.info("Deleted " + statementDeleteEvents.getUpdateCount() +  " event records  for expt id " + experimentId );
      
      return true;
    } finally {
      try {
        if (statementDeleteEvents != null) {
          statementDeleteEvents.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
  }

}
