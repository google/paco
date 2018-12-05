package com.google.sampling.experiential.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.dao.CSOldEventOutputDao;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
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

public class CSOldEventOutputDaoImpl implements CSOldEventOutputDao {
  public static final Logger log = Logger.getLogger(CSEventOutputDaoImpl.class.getName());
  private static List<Column> eventColList = Lists.newArrayList();
  private static List<Column> outputColList = Lists.newArrayList();
  private static Map<String, Integer> eventsOutputColumns = null;
  public static final String WHEN = "when";

  static {
    eventColList.add(new Column(EventBaseColumns.EXPERIMENT_ID));
    eventColList.add(new Column(EventBaseColumns.EXPERIMENT_NAME));
    eventColList.add(new Column(EventBaseColumns.EXPERIMENT_VERSION));
    eventColList.add(new Column(EventServerColumns.SCHEDULE_TIME_UTC));
    eventColList.add(new Column(EventServerColumns.RESPONSE_TIME_UTC));
    eventColList.add(new Column(EventBaseColumns.GROUP_NAME));
    eventColList.add(new Column(EventBaseColumns.ACTION_TRIGGER_ID));
    eventColList.add(new Column(EventBaseColumns.ACTION_TRIGGER_SPEC_ID));
    eventColList.add(new Column(EventBaseColumns.ACTION_ID));
    eventColList.add(new Column(EventServerColumns.WHO));
    eventColList.add(new Column(EventServerColumns.WHEN));
    eventColList.add(new Column(EventServerColumns.WHEN_FRAC_SEC));
    eventColList.add(new Column(EventServerColumns.PACO_VERSION));
    eventColList.add(new Column(EventServerColumns.APP_ID));
    eventColList.add(new Column(EventServerColumns.JOINED));
    eventColList.add(new Column(EventServerColumns.SORT_DATE_UTC));
    eventColList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColList.add(new Column(Constants.UNDERSCORE_ID));
    eventColList.add(new Column(EventServerColumns.SCHEDULE_TIME));
    eventColList.add(new Column(EventServerColumns.RESPONSE_TIME));
    eventColList.add(new Column(EventServerColumns.SORT_DATE));
    
    outputColList.add(new Column(OutputBaseColumns.EVENT_ID));
    outputColList.add(new Column(OutputBaseColumns.NAME));
    outputColList.add(new Column(OutputBaseColumns.ANSWER));
    eventsOutputColumns = new HashMap<String, Integer>();
        
    for(int ct = 1; ct <= eventColList.size(); ct ++) {
      eventsOutputColumns.put(eventColList.get(ct-1).getColumnName(), ct);
    }
    for(int ct = 0; ct < outputColList.size(); ct ++) {
      eventsOutputColumns.put(outputColList.get(ct).getColumnName(), eventsOutputColumns.size() + ct);
    }
  }
  
  @Override
  public boolean insertEventAndOutputsInOldWay(Event event) throws SQLException, ParseException {
    if (event == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }

    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateEventOutput = null;
    boolean retVal = false;
    Timestamp whenTs = null;
    int whenFrac = 0;
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList eventExprList = new ExpressionList();
    ExpressionList outputExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    List<Expression>  out = Lists.newArrayList();
    Insert eventInsert = new Insert();
    Insert outputInsert = new Insert();
    Long expIdLong = null;

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
      eventInsert.setColumns(eventColList);
      outputInsert.setColumns(outputColList);
      // Adding ? for prepared stmt
      for (Column c : eventColList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      for (Column c : outputColList) {
        ((ExpressionList) outputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      expIdLong = Long.parseLong(event.getExperimentId());

      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      statementCreateEvent.setLong(i++, Long.parseLong(event.getExperimentId()));
      statementCreateEvent.setString(i++, event.getExperimentName());
      if (event.getExperimentVersion() == null) {
        event.setExperimentVersion(0);
      }
      statementCreateEvent.setInt(i++, event.getExperimentVersion());
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
      if (event.getExperimentGroupName() == null) {
        event.setExperimentGroupName(Constants.UNKNOWN);
      }
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
      statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getScheduledTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(TimeUtil.convertToLocal(event.getResponseTime(), event.getTimeZone()).getMillis()): null);
      statementCreateEvent.setTimestamp(i++, new Timestamp(TimeUtil.convertToLocal(new Date(sortDateMillis), event.getTimeZone()).getMillis()));
      // romanian experiment
      if (Long.parseLong(event.getExperimentId()) != 6651607195844608L) {
        statementCreateEvent.execute();
        Set<What> whatSet = event.getWhat();
        if (whatSet != null) {
          statementCreateEventOutput = conn.prepareStatement(outputInsert.toString());
          
          for (String key : event.getWhatKeys()) {
            String whatAnswer = event.getWhatByKey(key);
            statementCreateEventOutput.setLong(1, event.getId());
            statementCreateEventOutput.setString(2, key);
            statementCreateEventOutput.setString(3, whatAnswer);
            statementCreateEventOutput.addBatch();
          }
          statementCreateEventOutput.executeBatch();
        }
      }
      
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
    
}
