package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.LifecycleManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentVersionGroupMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.OutputServerColumns;
import com.google.sampling.experiential.dao.CSEventDao;
import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSExperimentUserDao;
import com.google.sampling.experiential.dao.CSExperimentVersionGroupMappingDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.dao.CSPivotHelperDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionGroupMapping;
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
    eventColInsertList.add(new Column(EventServerColumns.EXPERIMENT_VERSION_GROUP_MAPPING_ID));
    eventColSearchList.addAll(eventColInsertList);
    
    outputColList.add(new Column(OutputServerColumns.EVENT_ID));
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
    ResultSetMetaData rsmd = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();

      statementSelectEvent = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
      rs = statementSelectEvent.executeQuery();
      if (rs != null) {
        rsmd = rs.getMetaData();
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
      statementCreateEvent.setInt(i++, anonId.getId().intValue());
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
      Map<String, ExperimentVersionGroupMapping> allEVMInVersion = evmDaoImpl.getAllGroupsInVersion(expIdLong, event.getExperimentVersion());
      if (allEVMInVersion == null) { 
        evmDaoImpl.createEVGMByCopyingFromLatestVersion(expIdLong, event.getExperimentVersion());
        allEVMInVersion =  evmDaoImpl.getAllGroupsInVersion(expIdLong, event.getExperimentVersion());
        if (allEVMInVersion == null) {
          log.warning("eventId:"+ event.getId() + " not saved in cloud sql");
          throw new Exception("No EVGM records for this experiment, trying to persist event"+ event.getId());
        }
      }
      // Rename event group Name from null to System, if its system predefined inputs
      log.info("Fix system group name alone");
      evmDaoImpl.ensureSystemGroupName(event, allEVMInVersion);
      evmDaoImpl.ensureEVMRecord(expIdLong, event.getId(), event.getExperimentName(), event.getExperimentVersion(), event.getExperimentGroupName(), event.getWho(), event.getWhat(), migrationFlag, allEVMInVersion);
      ExperimentVersionGroupMapping evmForThisGroup = allEVMInVersion.get(event.getExperimentGroupName());
      if (evmForThisGroup == null) {
        log.warning("eventId:"+ event.getId() + " not saved in cloud sql");
        throw new Exception("No EVGM records for event"+ event.getId());
      }
      pvUpdateEvmId = evmForThisGroup.getExperimentVersionMappingId();
      statementCreateEvent.setLong(i++, pvUpdateEvmId);
      statementCreateEvent.execute();

      Set<What> whatSet = event.getWhat();
      if (whatSet != null) {
        statementCreateEventOutput = conn.prepareStatement(outputInsert.toString());
        for (String key : event.getWhatKeys()) {
          String whatAnswer = event.getWhatByKey(key);
          statementCreateEventOutput.setLong(1, event.getId());
          statementCreateEventOutput.setString(2, whatAnswer);
          InputOrderAndChoice currentInput = evmForThisGroup.getInputCollection().getInputOrderAndChoices().get(key);
          // for some reason (scripted variable) this particular output does not have input associated, then add this input variable name to the input collection and get the input id
          if ( currentInput == null) {
            // add this variable to the existing input collection
            Input newInput = null;
            currentInput = new InputOrderAndChoice();
            newInput = icDaoImpl.addUndefinedInputToCollection(expIdLong, evmForThisGroup.getInputCollection().getInputCollectionId(), key);
            currentInput.setInput(newInput);
          }
          statementCreateEventOutput.setLong(3, currentInput.getInput().getInputId().getId());
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
  
  @Override
  public List<EventDAO> getEvents(String query, boolean withOutputs, Boolean withOldColumnNames) throws SQLException, ParseException {
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
          event = createEvent(rs, withOutputs, withOldColumnNames);
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

  
  private EventDAO createEvent(ResultSet rs, boolean withOutputs, Boolean withOldColumnNames) {
    EventDAO event = new EventDAO();
    List<WhatDAO> whatList = Lists.newArrayList();
    WhatDAO singleWhat = null;
    try {
      event.setExperimentId(rs.getLong(ExperimentVersionGroupMappingColumns.EXPERIMENT_ID));
      event.setExperimentName(rs.getString(ExperimentDetailColumns.EXPERIMENT_NAME));
      event.setExperimentVersion(rs.getInt(ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION));

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
        String tempWhatText = null;
        if (withOldColumnNames) {
          tempWhatText = rs.getString(OutputBaseColumns.NAME);
        } else {
          tempWhatText = rs.getString("esi1."+ ExternStringInputColumns.LABEL);
        }
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

  private String questionMark(List<Long> expIds) {
    StringBuffer expIdsList = new StringBuffer();
    for ( Long expId : expIds) { 
      expIdsList.append("?,");
    }
    if (expIds.size() > 0) {
      String chk = expIdsList.substring(0,expIdsList.length()-1);
      return chk;
    }
    return "0";
  }
  
  private List<Long> getEventIdsForExperiment(Long experimentId, Connection conn, PreparedStatement statementGetEventIds) throws SQLException {
    ResultSet rsGetEventIds = null;
    List<Long> eventIds = Lists.newArrayList();
    try {
      rsGetEventIds = statementGetEventIds.executeQuery();
      while (rsGetEventIds.next()) {
        eventIds.add(rsGetEventIds.getLong(1));
      }
      log.info("Selected " + eventIds.size() +  " event ids for expt id " + experimentId);
      return eventIds;
    } finally {
      try {
        if (rsGetEventIds != null) {
          rsGetEventIds.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
  }  
  
  private boolean deleteEventsAndOutputs(Connection conn, List<Long> eventIds) throws SQLException {
    PreparedStatement statementDeleteEvents = null;
    PreparedStatement statementDeleteOutputs = null;
    int i = 1;
    String questionMarkCharacters = null;
    String deleteQuery1 = QueryConstants.DELETE_ALL_OUTPUTS.toString();
    String deleteQuery2 = QueryConstants.DELETE_ALL_EVENTS.toString();
    try {
      questionMarkCharacters = questionMark(eventIds);
      deleteQuery1 = deleteQuery1.replaceFirst("\\?", questionMarkCharacters);
      statementDeleteOutputs = conn.prepareStatement(deleteQuery1);
      deleteQuery2 = deleteQuery2.replaceAll("\\?",  questionMarkCharacters);
      statementDeleteEvents = conn.prepareStatement(deleteQuery2);
      for (Long eventId : eventIds) {
        statementDeleteOutputs.setLong(i++, eventId);
      }
      statementDeleteOutputs.execute();
      
      log.info("Deleted " + statementDeleteOutputs.getUpdateCount() +  " output records" );
      i = 1;
      
      for (Long eventId : eventIds) {
        statementDeleteEvents.setLong(i++, eventId);
      }
     
      statementDeleteEvents.execute();
      log.info("Deleted " + statementDeleteEvents.getUpdateCount() +  " event records" );
      return true;
    } finally {
      try {
        if (statementDeleteOutputs != null) { 
          statementDeleteOutputs.close();
        }
        if (statementDeleteEvents != null) {
          statementDeleteEvents.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }

    }
      
  }

  
  @Override
  public boolean deleteAllEventsAndOutputsData(Long experimentId) throws SQLException {
    boolean oldFormat = true;
    PreparedStatement statementGetEventIds = null;
    String getEventIds =  null;
    Connection conn = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      if (oldFormat) { 
        getEventIds = QueryConstants.GET_EVENT_IDS_OLD_FORMAT_ORDERED_BY_ID.toString() ;
      } else {
        getEventIds = QueryConstants.GET_EVENT_IDS_NEW_FORMAT_ORDERED_BY_ID.toString() ;
      }
      statementGetEventIds = conn.prepareStatement(getEventIds);
      statementGetEventIds.setLong(1, experimentId);
      List<Long> eventIds = getEventIdsForExperiment(experimentId, conn, statementGetEventIds);
      
      while (true) {
        if (LifecycleManager.getInstance().isShuttingDown()) { 
          log.info("app engine current module is going to shut down in.........."+LifecycleManager.getInstance().getRemainingShutdownTime());
        }
        deleteEventsAndOutputs(conn, eventIds);
        eventIds = getEventIdsForExperiment(experimentId, conn, statementGetEventIds);
        if (eventIds.size() == 0) {
          if (oldFormat) { 
            oldFormat = false;
            eventIds = getEventIdsForExperiment(experimentId, conn, statementGetEventIds);
          } else {
            break;  
          }
        } 
      } 
    } finally { 
      try {
        if (statementGetEventIds != null) {
          statementGetEventIds.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    } 
    return true;
  }  

  @Override
  public void resetDupCounterForVariableNames(Long experimentId) throws SQLException {
    Connection conn = null;
    PreparedStatement statementEventsWithDupCtr = null;
    PreparedStatement statementUpdateTextInOutputs = null;
    PreparedStatement statementUpdateEVGMIdInEvents = null;
    ResultSet rs = null;
    CSOutputDao outDao =  new CSOutputDaoImpl();
    CSEventDao eventDaoImpl = new CSEventDaoImpl();
    String getAllEventsWithDupCtr = QueryConstants.GET_EVENT_ID_WITH_DUP_VARIABLE.toString();
    String updateOutputsText = QueryConstants.UPDATE_OUTPUT_TEXT.toString() ;
    String updateEventsEVGMId = QueryConstants.UPDATE_EVENT_EVGM_ID_AS_NULL.toString();
    Boolean oldColumnName = true;
    List<WhatDAO> whats = null;
    try {
      // mock all events with -DUP- outputs as evgm id 1.
      eventDaoImpl.updateAllEventsData(experimentId);
      
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementUpdateTextInOutputs = conn.prepareStatement(updateOutputsText);
      statementEventsWithDupCtr = conn.prepareStatement(getAllEventsWithDupCtr);
      statementUpdateEVGMIdInEvents = conn.prepareStatement(updateEventsEVGMId);
      statementEventsWithDupCtr.setLong(1, experimentId);
      Map<String, String> oldAndNewNameMap = null;
      Iterator<String> oldNameItr = null;
      String oldKeyValue = null;
      Long eventId = null;
     while (true) {
       log.info("Making DUP ctr changes in experiment" + experimentId);
       rs = statementEventsWithDupCtr.executeQuery();
       if(!rs.next()) {
         break;
       } else {
         rs.beforeFirst();
         while (rs.next()) { 
           eventId = rs.getLong(Constants.UNDERSCORE_ID);
           whats = outDao.getOutputs(eventId, oldColumnName);
           oldAndNewNameMap = resetDupVariablesCtr(whats);
           oldNameItr = oldAndNewNameMap.keySet().iterator();
           while (oldNameItr.hasNext()) {
             oldKeyValue = oldNameItr.next();
             statementUpdateTextInOutputs.setString(1, oldAndNewNameMap.get(oldKeyValue));
             statementUpdateTextInOutputs.setLong(2, eventId);
             statementUpdateTextInOutputs.setString(3, oldKeyValue);
             statementUpdateTextInOutputs.addBatch();
           }
           statementUpdateEVGMIdInEvents.setLong(1, eventId);
           statementUpdateEVGMIdInEvents.addBatch();
         }
         statementUpdateTextInOutputs.executeBatch();
         statementUpdateEVGMIdInEvents.executeBatch();
       }      
     }
    } finally {
      try {
        if (rs != null) { 
          rs.close();
        }
        if (statementUpdateEVGMIdInEvents != null) {
          statementUpdateEVGMIdInEvents.close();
        }
        if (statementUpdateTextInOutputs != null) { 
          statementUpdateTextInOutputs.close();
        }
        if (statementEventsWithDupCtr != null) {
          statementEventsWithDupCtr.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
  }
  
  private static NavigableMap<String, WhatDAO> convertListToTreeMap (List<WhatDAO> whats) {
    TreeMap<String, WhatDAO> tm = Maps.newTreeMap();
    for (WhatDAO eachWhat : whats) { 
      tm.put(eachWhat.getName(), eachWhat);
    }
    return tm;
  }
 // All whats of an event which contains -DUP- is sent. Some whats can have no -DUP- in them
 // If an event has two normal variables q1 and q2. They also have q1-DUP-1234, q1-DUP-3444, q2-DUP-445
 // this function when passed prefix 'q1-dup-' will return a map of what names as key and what objects as value.
 // So when prefix 'q1-DUP-' is sent we get back map of size 2. the elements are q1-DUP-1234, q1-DUP=3444
 // when prefix 'q2-DUP-' is sent we get back map of size 1. the element is q2-DUP-445
  private static SortedMap<String, WhatDAO> getByPrefix(
                                                        NavigableMap<String, WhatDAO> myMap,
                                                        String prefix ) {
    return myMap.subMap( prefix, prefix + Character.MAX_VALUE );
  }
  private static Map<String, String> resetDupVariablesCtr(List<WhatDAO> whats) {
    Map<String, String> oldAndNewVarNameMap = Maps.newHashMap();
    NavigableMap<String, WhatDAO> tm = convertListToTreeMap(whats);
    SortedMap<String, WhatDAO> subMap = null;
    List<String> prefixes = Lists.newArrayList();
    String[] variableNameSplitArray = null;
    for (WhatDAO eachWhat : whats) { 
      variableNameSplitArray = eachWhat.getName().split("-DUP-");
      String firstPart = variableNameSplitArray[0]+"-DUP-";
      if (eachWhat.getName().contains("-DUP-") && !prefixes.contains(firstPart)) {
        prefixes.add(firstPart);
      }
    }
    for (String prefix : prefixes) {
      subMap = getByPrefix(tm, prefix);  
      Iterator<String> subMapItr = subMap.keySet().iterator();
      int i = 1;
      // when prefix is 'q1-DUP-', the submap has the values q1-DUP-1234, q1-DUP-3444
      // oldAndNewVarNameMap contains {q1-DUP-1234, q1-DUP-1} , {q1-DUP-3444, q1-DUP-2}, {}....
      while (subMapItr.hasNext()) {
        WhatDAO eachWhat = subMap.get(subMapItr.next());
        oldAndNewVarNameMap.put(eachWhat.getName(), prefix + i);
        i++;        
      }
    }
    return oldAndNewVarNameMap;
  }

  @Override
  public List<Long> getAllInputIdsForEVGMAndUser(Long evgmId, Integer anonWhoId) throws SQLException {
    List<Long> allInputIds = Lists.newArrayList();
    Connection conn = null;
    PreparedStatement getAllInputIdsStatement = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      getAllInputIdsStatement = conn.prepareStatement(QueryConstants.GET_ALL_INPUT_IDS_FOR_EVGM_AND_USER.toString());
      getAllInputIdsStatement.setLong(1, evgmId);
      getAllInputIdsStatement.setInt(2, anonWhoId);
      
      rs = getAllInputIdsStatement.executeQuery();
      while(rs.next()){
        allInputIds.add(rs.getLong(OutputServerColumns.INPUT_ID));
      }
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (getAllInputIdsStatement != null) {
          getAllInputIdsStatement.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    return allInputIds;
  }

  @Override
  public List<String> getAllDistinctTextForExperiment(Long experimentId) throws SQLException {
    List<String> distinctTexts = Lists.newArrayList();
    PreparedStatement statementGetDistinctText = null;
    String getDistinctText =  null;
    Connection conn = null;
    ResultSet rs = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      getDistinctText = QueryConstants.GET_ALL_DISTINCT_TEXT_FOR_EXPERIMENT_ID.toString();
      statementGetDistinctText = conn.prepareStatement(getDistinctText);
      statementGetDistinctText.setLong(1, experimentId);
      log.info("all distinct text: "+ statementGetDistinctText.toString());
      rs = statementGetDistinctText.executeQuery();
      while (rs.next()) {
        distinctTexts.add(rs.getString(1));
      }
    } finally { 
      try {
        if (rs != null) { 
          rs.close();
        }
        if (statementGetDistinctText != null) {
          statementGetDistinctText.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    } 
    return distinctTexts;
  }
  
}
