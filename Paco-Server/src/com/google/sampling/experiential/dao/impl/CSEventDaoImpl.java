package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.dao.CSEventDao;
import com.google.sampling.experiential.dao.CSExperimentUserDao;
import com.google.sampling.experiential.dao.CSExperimentVersionMappingDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.PacoId;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;
import com.pacoapp.paco.shared.util.TimeUtil;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CSEventDaoImpl implements CSEventDao {
  public static final Logger log = Logger.getLogger(CSEventDaoImpl.class.getName());
  private static List<Column> eventColInsertList = Lists.newArrayList();
  private CSExperimentUserDao exptUserDaoImpl = new CSExperimentUserDaoImpl();
  private CSExperimentVersionMappingDao experimentVersionMappingDaoImpl = new CSExperimentVersionMappingDaoImpl();
  private static List<Column> eventColList = Lists.newArrayList();
  
  static {
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_ID));
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_NAME));
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_VERSION));
    eventColList.add(new Column(EventServerColumns.SCHEDULE_TIME_UTC));
    eventColList.add(new Column(EventServerColumns.RESPONSE_TIME_UTC));
    eventColList.add(new Column(EventServerColumns.GROUP_NAME));
    eventColList.add(new Column(EventServerColumns.ACTION_TRIGGER_ID));
    eventColList.add(new Column(EventServerColumns.ACTION_TRIGGER_SPEC_ID));
    eventColList.add(new Column(EventServerColumns.ACTION_ID));
    eventColList.add(new Column(EventServerColumns.WHO));
    eventColList.add(new Column(EventServerColumns.WHEN));
    eventColList.add(new Column(EventServerColumns.WHEN_FRAC_SEC));
    eventColList.add(new Column(EventServerColumns.PACO_VERSION));
    eventColList.add(new Column(EventServerColumns.APP_ID));
    eventColList.add(new Column(EventServerColumns.JOINED));
    eventColList.add(new Column(EventServerColumns.SORT_DATE_UTC));
    eventColList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColList.add(new Column(Constants.UNDERSCORE_ID));
    eventColList.add(new Column(EventServerColumns.RESPONSE_TIME));
    eventColList.add(new Column(EventServerColumns.SCHEDULE_TIME));
    eventColList.add(new Column(EventServerColumns.SORT_DATE));
    
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
  }
  
  @Override
  public boolean insertSingleEventOnly(Event event) throws NumberFormatException, Exception {
    if (event == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }

    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    boolean retVal = false;
    Timestamp whenTs = null;
    Long expIdLong = null;
    ExperimentVersionMapping evm = null;
    int whenFrac = 0;
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList eventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert eventInsert = new Insert();
     try {
      log.info("Inserting event->" + event.getId());
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      expIdLong = Long.parseLong(event.getExperimentId());
      eventInsert.setTable(new Table(EventServerColumns.TABLE_NAME));
      eventInsert.setUseValues(true);
      eventExprList.setExpressions(exp);
      eventInsert.setItemsList(eventExprList);
      eventInsert.setColumns(eventColInsertList);
      // Adding ? for prepared stmt
      for (Column c : eventColInsertList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
      statementCreateEvent.setLong(i++, event.getActionTriggerId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionTriggerSpecId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionId() != null ? new Long(event.getActionId()) : java.sql.Types.NULL);
      PacoId anonId = exptUserDaoImpl.getAnonymousIdAndCreate(expIdLong, event.getWho(), true);
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
      evm = experimentVersionMappingDaoImpl.ensureEVMRecord(Long.parseLong(event.getExperimentId()), event.getId(), event.getExperimentName(), event.getExperimentVersion(), event.getExperimentGroupName(), event.getWho(), event.getWhat(), true);
      statementCreateEvent.setLong(i++, evm.getExperimentVersionMappingId());
      
      statementCreateEvent.execute();

      conn.commit();
      retVal = true;
    } finally {
      try {
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
  public boolean insertSingleEventOnlyWithExperimentInfo(Event event) throws SQLException, ParseException {
    if (event == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }

    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    boolean retVal = false;
    Timestamp whenTs = null;
    int whenFrac = 0;
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList eventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert eventInsert = new Insert();
     try {
      log.info("Inserting event->" + event.getId());
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      eventInsert.setTable(new Table(EventServerColumns.TABLE_NAME));
      eventInsert.setUseValues(true);
      eventExprList.setExpressions(exp);
      eventInsert.setItemsList(eventExprList);
      eventInsert.setColumns(eventColList);
      // Adding ? for prepared stmt
      for (Column c : eventColList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      statementCreateEvent.setLong(i++, Long.parseLong(event.getExperimentId()));
      statementCreateEvent.setString(i++, event.getExperimentName());
      statementCreateEvent.setInt(i++, event.getExperimentVersion());
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
      statementCreateEvent.setString(i++, event.getExperimentGroupName());
      statementCreateEvent.setLong(i++, event.getActionTriggerId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionTriggerSpecId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
      statementCreateEvent.setLong(i++, event.getActionId() != null ? new Long(event.getActionId()) : java.sql.Types.NULL);
      statementCreateEvent.setString(i++, event.getWho());
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

      statementCreateEvent.execute();

      conn.commit();
      retVal = true;
    } finally {
      try {
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
  public boolean updateGroupName(Long eventId, String oldGrpName, String newGrpName) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent1 = null;
    String updateQuery1 = "update events set group_name=? where _id=?";
    PreparedStatement statementInsertOldGroupName = null;
    String insertQuery1 = "insert into event_old_group_name(old_group_name,event_id) values (?,?)";
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      
      statementUpdateEvent1 = conn.prepareStatement(updateQuery1);
      statementUpdateEvent1.setString(1, newGrpName);
      statementUpdateEvent1.setLong(2, eventId);
      statementUpdateEvent1.executeUpdate();
      
      statementInsertOldGroupName = conn.prepareStatement(insertQuery1);
      statementInsertOldGroupName.setString(1, oldGrpName);
      statementInsertOldGroupName.setLong(2,  eventId);
      statementInsertOldGroupName.execute();
      
      log.info("updated  grp name in events table" +eventId + "--" + oldGrpName  + "--" + newGrpName);
    } finally {
      try {
        if (statementUpdateEvent1 != null) {
          statementUpdateEvent1.close();
        }
        if (statementInsertOldGroupName != null) {
          statementInsertOldGroupName.close();
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
 
}

