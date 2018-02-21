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
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentLookupColumns;
import com.google.sampling.experiential.cloudsql.columns.OutputServerColumns;
import com.google.sampling.experiential.dao.CSDataTypeDao;
import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSExperimentUserDao;
import com.google.sampling.experiential.dao.CSExperimentVersionMappingDao;
import com.google.sampling.experiential.dao.CSGroupTypeInputMappingDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.CSPivotHelperDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.Group;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
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
    eventColInsertList.add(new Column(EventServerColumns.EXPERIMENT_VERSION_MAPPING_ID));
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
  public boolean insertEventAndOutputs(Event event) throws SQLException, ParseException {
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
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList eventExprList = new ExpressionList();
    ExpressionList outputExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    List<Expression>  out = Lists.newArrayList();
    Insert eventInsert = new Insert();
    Insert outputInsert = new Insert();
    CSExperimentUserDao euImpl = new CSExperimentUserDaoImpl();
    CSExperimentVersionMappingDao evmDaoImpl = new CSExperimentVersionMappingDaoImpl();
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
      ExperimentVersionMapping evmForThisGroup = findMatchingEVMRecord(event, allEVMInVersion);
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
  
  private ExperimentVersionMapping findMatchingEVMRecord(Event event, Map<String, ExperimentVersionMapping> allEVMMap) throws SQLException{
    String groupNameInEvent = event.getExperimentGroupName();
    List<String> inputVariableNamesInEvent = event.getWhatKeys();
    Set<String> inputVariableNamesInMatchingGroup = null;
    Group crtGroup = null;
    String crtGroupName = null;
    List<Input> predefinedFeatureInputLst = null;
    boolean mixedEventsPossible = false;
    ExperimentVersionMapping returnEVM = null;
    CSExperimentVersionMappingDao daoImpl = new CSExperimentVersionMappingDaoImpl();
    CSGroupTypeInputMappingDao gtDaoImpl = new CSGroupTypeInputMappingDaoImpl();
    Long expId = Long.parseLong(event.getExperimentId());
    // if event is posted for a version where we do not have experiment mapping records
    if (allEVMMap == null) {
      
//      closestVersion = daoImpl.getClosestExperimentVersion(expId, event.getExperimentVersion());
//      daoImpl.copyFrom(expId, closestVersion, event.getExperimentVersion());
//      allEVMMap = daoImpl.getAllGroupsInVersion(Long.parseLong(event.getExperimentId()), event.getExperimentVersion());
      daoImpl.copyClosestVersion(expId, event.getExperimentVersion());
      allEVMMap = daoImpl.getAllGroupsInVersion(expId, event.getExperimentVersion());
      if (allEVMMap  == null) {
        allEVMMap = Maps.newHashMap();
        returnEVM = daoImpl.createGroupWithInputs(expId, event.getExperimentName(), event.getExperimentVersion(), event.getExperimentGroupName(), event.getWho(), event.getWhatKeys());
        allEVMMap.put(event.getExperimentGroupName(), returnEVM);
      }
    }
    Iterator<String> grpItr = allEVMMap.keySet().iterator();
    
    while(grpItr.hasNext()) {
      crtGroupName = grpItr.next();
      // older versions of some experiments have the survey group name associated with sensor type data as well. 
      // With newer version, each experiment will have different group name for each of the sensor data like app_usage, accessibility etc.
      // if the evm record in the cloud sql table says it has been copied over from one of the latest versions, then we can receive events which contain mixed groups
      if (allEVMMap.get(crtGroupName).getSource() != null) {
        mixedEventsPossible = true;
        break;
      }
    }
    
    if( !mixedEventsPossible) {
        return allEVMMap.get(crtGroupName);
    } else {
      // it could be predefined feature inputs or survey inputs
      Map<String, List<Input>> featureInputs = gtDaoImpl.getAllFeatureInputs();
      Iterator<String> featureItr = featureInputs.keySet().iterator();
      String currentFeatureName = null;
      while ( featureItr.hasNext()) {
        currentFeatureName = featureItr.next();
        predefinedFeatureInputLst = featureInputs.get(currentFeatureName);
        // if feature name is system, then even if one of the system variables comes in the outputs, we can use the system group id.
        // event variable names should contain all of predefined list
        if (currentFeatureName.equalsIgnoreCase("system")) {
          if (inputVariableNamesInEvent.contains(getVariableNamesFromInputLst(predefinedFeatureInputLst))) {
            returnEVM = allEVMMap.get(currentFeatureName);
            break;
          }
        } else {
          if (inputVariableNamesInEvent.containsAll(getVariableNamesFromInputLst(predefinedFeatureInputLst))) {
            returnEVM = allEVMMap.get(currentFeatureName);
            if (returnEVM == null) {
              // so we add a grp with these inputs to this version
              returnEVM = daoImpl.createGroupWithPredefinedInputs(expId, event.getExperimentVersion(), event.getExperimentGroupName(), predefinedFeatureInputLst, currentFeatureName);
              allEVMMap.put(event.getExperimentGroupName(), returnEVM);
              break;
            }
          } 
        }
        return returnEVM;
      }// while
      // if none of the predefined feature inputs match, then it must be survey grp under which the event is getting posted
      returnEVM = allEVMMap.get(event.getExperimentGroupName());
      if ( allEVMMap.size() > 0 ) {
        
      }
      // here too it can be null when older versions had a grp name, and now the latest versions do not have that grp name
      if ( returnEVM == null) { 
        returnEVM = daoImpl.createGroupWithInputs(expId, event.getExperimentName(), event.getExperimentVersion(), event.getExperimentGroupName(), event.getWho(), event.getWhatKeys());
        allEVMMap.put(event.getExperimentGroupName(), returnEVM);
      }
      return returnEVM;
    }
  
  }
  
  private Set<String> getVariableNamesFromInputLst(List<Input> inputLst) {
    Set<String> inputSet = Sets.newHashSet();
    for (Input i : inputLst) { 
      inputSet.add(i.getName().getLabel());
    }
    return inputSet;
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
      event.setExperimentId(rs.getLong(ExperimentLookupColumns.EXPERIMENT_ID));
      event.setExperimentName(rs.getString(ExperimentLookupColumns.EXPERIMENT_NAME));
      event.setExperimentVersion(rs.getInt(ExperimentLookupColumns.EXPERIMENT_VERSION));

      Date scheduleDate = rs.getTimestamp(EventServerColumns.SCHEDULE_TIME);
      DateTime scheduledDateTime = scheduleDate != null ? new DateTime(scheduleDate): null;
      event.setScheduledTime(scheduledDateTime);

      Date responseDate = rs.getTimestamp(EventServerColumns.RESPONSE_TIME);
      DateTime responseDateTime = responseDate != null ? new DateTime(responseDate) : null;
      event.setResponseTime(responseDateTime);

      event.setExperimentGroupName(rs.getString(ExperimentLookupColumns.GROUP_NAME));
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

}
