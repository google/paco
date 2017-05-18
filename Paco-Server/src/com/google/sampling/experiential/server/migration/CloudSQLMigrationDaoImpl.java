package com.google.sampling.experiential.server.migration;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.datastore.FailedEventServerColumns;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

public class CloudSQLMigrationDaoImpl implements CloudSQLMigrationDao {
  public static final Logger log = Logger.getLogger(CloudSQLMigrationDaoImpl.class.getName());
  private static Map<String, Integer> eventsOutputColumns = null;
  private static List<Column> eventColList = Lists.newArrayList();
  private static int DUP_CTR = 0;
  public static final String ID = "_id";
  private static List<Column> outputColList = Lists.newArrayList();
  private static List<Column> failedColList = Lists.newArrayList();
  private static final String selectOutputsSql = "select * from outputs where event_id =?";

  static {
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_ID));
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_NAME));
    eventColList.add(new Column(EventServerColumns.EXPERIMENT_VERSION));
    eventColList.add(new Column(EventServerColumns.SCHEDULE_TIME));
    eventColList.add(new Column(EventServerColumns.RESPONSE_TIME));
    eventColList.add(new Column(EventServerColumns.GROUP_NAME));
    eventColList.add(new Column(EventServerColumns.ACTION_TRIGGER_ID));
    eventColList.add(new Column(EventServerColumns.ACTION_TRIGGER_SPEC_ID));
    eventColList.add(new Column(EventServerColumns.ACTION_ID));
    eventColList.add(new Column(EventServerColumns.WHO));
    eventColList.add(new Column(EventServerColumns.WHEN));
    eventColList.add(new Column(EventServerColumns.PACO_VERSION));
    eventColList.add(new Column(EventServerColumns.APP_ID));
    eventColList.add(new Column(EventServerColumns.JOINED));
    eventColList.add(new Column(EventServerColumns.SORT_DATE));
    eventColList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColList.add(new Column(Constants.UNDERSCORE_ID));
    eventColList.add(new Column(EventServerColumns.INT_RESPONSE_TIME));
    
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
    
    failedColList.add(new Column(FailedEventServerColumns.EVENT_JSON));
    failedColList.add(new Column(FailedEventServerColumns.REASON));
    failedColList.add(new Column(FailedEventServerColumns.COMMENTS));
  }
  
  @Override
  public boolean insertEventsInBatch(List<Event> events) {
    if (events == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }
    
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    boolean retVal = false;
    //startCount for setting parameter index
    int i = 1 ;
    int eventCtr = 0;
    ExpressionList eventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert eventInsert = new Insert();
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      conn.setAutoCommit(false);
      eventInsert.setTable(new Table(EventBaseColumns.TABLE_NAME));
      eventInsert.setUseValues(true);
      eventExprList.setExpressions(exp);
      eventInsert.setItemsList(eventExprList);
      eventInsert.setColumns(eventColList);
      // Adding ? for prepared stmt
      for (Column c : eventColList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
 
      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      for(eventCtr = 0; eventCtr < events.size(); eventCtr++) {
        Event event = events.get(eventCtr);
        statementCreateEvent.setLong(i++, Long.parseLong(event.getExperimentId()));
        statementCreateEvent.setString(i++, event.getExperimentName());
        statementCreateEvent.setInt(i++, event.getExperimentVersion()!=null ? event.getExperimentVersion() : java.sql.Types.NULL);
        statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
        statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
        statementCreateEvent.setString(i++, event.getExperimentGroupName());
        statementCreateEvent.setLong(i++, event.getActionTriggerId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
        statementCreateEvent.setLong(i++, event.getActionTriggerSpecId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
        statementCreateEvent.setLong(i++, event.getActionId() != null ? new Long(event.getActionId()) : java.sql.Types.NULL);
        statementCreateEvent.setString(i++, event.getWho());
        statementCreateEvent.setTimestamp(i++, event.getWhen() != null ? new Timestamp(event.getWhen().getTime()): null);
        statementCreateEvent.setString(i++, event.getPacoVersion());
        statementCreateEvent.setString(i++, event.getAppId());
        statementCreateEvent.setNull(i++, java.sql.Types.BOOLEAN);
        Timestamp ts = null;
        if(event.getResponseTime() != null) {
          ts =  new Timestamp(event.getResponseTime().getTime());
        } else if (event.getScheduledTime() != null) {
          ts = new Timestamp(event.getScheduledTime().getTime());
        } else {
          ts = null;
        }
        statementCreateEvent.setTimestamp(i++, ts);
        statementCreateEvent.setString(i++, event.getTimeZone());
        statementCreateEvent.setLong(i++, event.getId());
        statementCreateEvent.setLong(i++, event.getResponseTime() != null ? event.getResponseTime().getTime(): java.sql.Types.NULL);

        statementCreateEvent.addBatch();
        i = 1;
      }
      statementCreateEvent.executeBatch();
      conn.commit();
      retVal = true;
    } catch (Exception e) {
      log.info(ErrorMessages.GENERAL_EXCEPTION.getDescription() + e.getCause());
      e.printStackTrace(System.out);
    }
    finally {
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
  public boolean insertOutputsInBatch(List<MigrationOutput> outputs) {
    if (outputs == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }
    boolean newBatch = true;
    Connection conn = null;
    PreparedStatement statementCreateOutput = null;
    boolean retVal = false;
    boolean unknownException = false;
    //startCount for setting parameter index
    int i = 1 ;
    ExpressionList outputList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert outputInsert = new Insert();
 
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      setNames(conn);
      outputInsert.setTable(new Table(OutputBaseColumns.TABLE_NAME));
      outputInsert.setUseValues(true);
      outputList.setExpressions(exp);
      outputInsert.setItemsList(outputList);
      outputInsert.setColumns(outputColList);
      // Adding ? for prepared stmt
      for (Column c : outputColList) {
        ((ExpressionList) outputInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
 
      statementCreateOutput = conn.prepareStatement(outputInsert.toString());
     
      while (true) {
      log.info("Is it New Batch"+newBatch);
        try {
          for (MigrationOutput tempOutput : outputs) {
            statementCreateOutput.setLong(i++, tempOutput.getEventId());
            statementCreateOutput.setString(i++, tempOutput.getText());
            statementCreateOutput.setString(i++, tempOutput.getAnswer());
            statementCreateOutput.addBatch();
            i = 1;
          }
          int[] exStatus = statementCreateOutput.executeBatch();
          conn.commit();
          retVal = true;
          break;
        } catch (BatchUpdateException trb) {
          int x[] = trb.getUpdateCounts();
          newBatch = false;
          conn.rollback();
          if(trb.getErrorCode() == 1062 && trb.getCause() instanceof MySQLIntegrityConstraintViolationException) {
            log.warning("integrity constraint: " + getStackTraceAsString(trb));
            for (int y=0; y<x.length;y++) {
              if (x[y]== Statement.EXECUTE_FAILED) {
                log.info("response values for position"+ y + "->"+x[y] + "->"+outputs.get(y).toString());
                String origKey = outputs.get(y).getText();
                if (origKey != null) { 
                  outputs.get(y).setText(origKey + "-DUP-" +(DUP_CTR++) );
                } else {
                  outputs.get(y).setText("DUP-"+ (DUP_CTR++));
                } 
              }
            }
          } else {
            log.warning("unknown constraint failed: so, break" + getStackTraceAsString(trb));
            unknownException = true;
            for (int y=0; y<x.length;y++) {
              if (x[y]== Statement.EXECUTE_FAILED) {
                log.info("response values for position"+ y + "->"+x[y] + "->"+outputs.get(y).toString());
                break;
              }
            }//for
          }//else
        }//catch batch update
        if(unknownException) {
          log.warning("Not sure how to handle");
          break;
        }
      }// while   
    } catch (Exception e) {
      log.info(ErrorMessages.GENERAL_EXCEPTION.getDescription()+ "batch insert failed. so restart from cursor");
      getStackTraceAsString(e);
    }
    finally {
      try {
        if (statementCreateOutput != null) {
          statementCreateOutput.close();
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
  
  private String getStackTraceAsString(Throwable e) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream pw = new PrintStream(out);
    e.printStackTrace(pw);
    final String string = out.toString();
    return string;
  }
  
  @Override
  public Long getEarliestWhen() throws SQLException, ParseException {
    Connection conn = null;
    Date earliestDate = null;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;
    String query  = "select `when` from events order by `when` desc limit 1";

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectEvent = conn.prepareStatement(query);
      
      Long st1Time = System.currentTimeMillis();
      rs = statementSelectEvent.executeQuery();
     
      if (rs != null) {
        while (rs.next()) {
          earliestDate = rs.getTimestamp(1);
        }
      }
      Long st2Time = System.currentTimeMillis();
      
      log.info("step 1 " + query + "took" + (st2Time- st1Time) + earliestDate);
    } finally {
      try {
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
    if (earliestDate != null) {
      return earliestDate.getTime();
    } else {
      return null;
    }
  }
  

  public boolean setNames(Connection conn) throws SQLException { 
    boolean isDone = false;
    java.sql.Statement statementSetNames = null;
  
    try {
      statementSetNames = conn.createStatement();
      final String setNamesSql = "SET NAMES  'utf8mb4'";
      statementSetNames.execute(setNamesSql);
      log.info("set names");

      isDone = true;
    } finally {
      try {
        if (statementSetNames != null) {
          statementSetNames.close();
        }
       
      
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return isDone;
  }

  @Override
  public String createTables() throws SQLException {
    String retString = null;
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateOutput = null;

    PreparedStatement statementCreateFailedEvent = null;

    try {

      conn = CloudSQLConnectionManager.getInstance().getConnection();
      // TODO Sub Partition size for the experiment hash bucket
      final String createEventsTableSql = "CREATE TABLE `" + EventServerColumns.TABLE_NAME 
                                          + "` (" +"`" + Constants.UNDERSCORE_ID + "` bigint(20) NOT NULL ,"+ "`"
                                          + EventServerColumns.EXPERIMENT_ID + "` bigint(20) NOT NULL," + "`"
                                          + EventServerColumns.EXPERIMENT_NAME + "` varchar(500) DEFAULT NULL," + "`"
                                          + EventServerColumns.EXPERIMENT_VERSION + "` int(11) DEFAULT NULL," + "`"
                                          + EventServerColumns.SCHEDULE_TIME + "` datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.RESPONSE_TIME + "` datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.INT_RESPONSE_TIME + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.GROUP_NAME + "` varchar(200) DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_TRIGGER_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_TRIGGER_SPEC_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.WHO + "` varchar(45) NOT NULL," + "`"
                                          + EventServerColumns.PACO_VERSION + "` varchar(45) DEFAULT NULL," + "`"
                                          + EventServerColumns.APP_ID + "` varchar(45) DEFAULT NULL," 
                                          // when column already has the back tick
                                          + EventServerColumns.WHEN + " datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.ARCHIVE_FLAG + "` tinyint(4) NOT NULL DEFAULT '0'," + "`"
                                          + EventServerColumns.JOINED + "` tinyint(1)  DEFAULT NULL," + "`"
                                          + EventServerColumns.SORT_DATE + "` datetime  DEFAULT NULL," + "`"
                                          + EventServerColumns.CLIENT_TIME_ZONE + "` varchar(20) DEFAULT NULL,"
                                          + "PRIMARY KEY (`" + Constants.UNDERSCORE_ID + "`)," + "KEY `when_index` ("
                                          + EventServerColumns.WHEN + ")," + "KEY `exp_id_resp_time_index` (`"
                                          + EventServerColumns.EXPERIMENT_ID + "`,`" + EventServerColumns.RESPONSE_TIME
                                          + "`)," + "KEY `exp_id_when_index` (`" + EventServerColumns.EXPERIMENT_ID
                                          + "`," + EventServerColumns.WHEN + ")," + "KEY `exp_id_who_when_index` (`"
                                          + EventServerColumns.EXPERIMENT_ID + "`,`" + EventServerColumns.WHO + "`,"
                                          + EventServerColumns.WHEN + ")" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

      final String createOutputsTableSql = "CREATE TABLE `" + OutputBaseColumns.TABLE_NAME+ "` (" + "`" 
                                           + OutputBaseColumns.EVENT_ID + "` bigint(20) NOT NULL," + "`"
                                           + OutputBaseColumns.NAME + "` varchar(500) NOT NULL," + "`"
                                           + OutputBaseColumns.ANSWER + "` varchar(500) DEFAULT NULL," + "`"
                                           + OutputBaseColumns.ARCHIVE_FLAG + "` tinyint(4) NOT NULL DEFAULT '0',"
                                           + "PRIMARY KEY (`" + OutputBaseColumns.EVENT_ID + "`,`"
                                           + OutputBaseColumns.NAME + "`)," + "KEY `event_id_index` (`"
                                           + OutputBaseColumns.EVENT_ID + "`)," + "KEY `text_index` (`"
                                           + OutputBaseColumns.NAME + "`), " 
                                           + " CONSTRAINT `event_id_fk` FOREIGN KEY (`"+ OutputBaseColumns.EVENT_ID + "`) REFERENCES `"+ EventBaseColumns.TABLE_NAME +"` (`"+ ID +"`) ON DELETE CASCADE ON UPDATE NO ACTION " +
                                           ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            
      final String createFailedEventsTableSql = "CREATE TABLE `" +  FailedEventServerColumns.TABLE_NAME +  "` (" + "`" 
                                            + FailedEventServerColumns.ID + "` bigint(20) NOT NULL AUTO_INCREMENT," + "`"
                                            + FailedEventServerColumns.EVENT_JSON + "` varchar(3000) NOT NULL," + "`"
                                            + FailedEventServerColumns.FAILED_INSERT_TIME + "` datetime  DEFAULT NULL," + "`"
                                            + FailedEventServerColumns.REASON + "` varchar(500) DEFAULT NULL," + "`"
                                            + FailedEventServerColumns.COMMENTS + "` varchar(1000) DEFAULT NULL,"
                                            + "PRIMARY KEY (`" + FailedEventServerColumns.ID + "`)"+") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4";
     

      statementCreateEvent = conn.prepareStatement(createEventsTableSql);

      statementCreateEvent.execute();
      log.info("created events");
      // TODO better handling
      retString = "created events table. ";
      
      statementCreateOutput = conn.prepareStatement(createOutputsTableSql);
      statementCreateOutput.execute();
      log.info("created outputs");
      // TODO better handling
      retString = retString + "Created outputs table";
      
      statementCreateFailedEvent = conn.prepareStatement(createFailedEventsTableSql);
      statementCreateFailedEvent.execute();
      log.info("created failed events");
      // TODO better handling
      retString = retString + "Created FailedEvents table";
      
    } finally {
      try {
        if (statementCreateEvent != null) {
          statementCreateEvent.close();
        }
        if (statementCreateOutput != null) {
          statementCreateOutput.close();
        }
        if (statementCreateFailedEvent != null) {
          statementCreateFailedEvent.close();
        }
        if (conn != null) {
          conn.close();
        }

      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retString;
  }
}
