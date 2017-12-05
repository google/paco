package com.google.sampling.experiential.server.migration;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.datastore.CatchupFailureServerColumns;
import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.datastore.ExperimentLookupColumns;
import com.google.sampling.experiential.datastore.ExperimentUserColumns;
import com.google.sampling.experiential.datastore.FailedEventServerColumns;
import com.google.sampling.experiential.datastore.UserColumns;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.CloudSQLDaoImpl;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.ExperimentService;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.TimeUtil;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
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
  private static List<Column> eventColInsertList = Lists.newArrayList();
  private static List<Column> eventColSearchList = Lists.newArrayList();
  
  private static int DUP_CTR = 0;
  public static final String ID = "_id";
  private static List<Column> outputColList = Lists.newArrayList();
  private static List<Column> failedColList = Lists.newArrayList();
  private static List<Column> cursorColList = Lists.newArrayList();
  private static List<Column> missedEventsColList = Lists.newArrayList();
  private static List<Column> streamingColList = Lists.newArrayList();
  private static List<Column> catchupFailureColList = Lists.newArrayList();

  static {
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_ID));
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_NAME));
    eventColSearchList.add(new Column(EventServerColumns.EXPERIMENT_VERSION));
    eventColSearchList.add(new Column(EventServerColumns.GROUP_NAME));
    
    eventColInsertList.add(new Column(EventServerColumns.SCHEDULE_TIME));
    eventColInsertList.add(new Column(EventServerColumns.RESPONSE_TIME));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_TRIGGER_ID));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_TRIGGER_SPEC_ID));
    eventColInsertList.add(new Column(EventServerColumns.ACTION_ID));
    eventColInsertList.add(new Column(EventServerColumns.WHO));
    eventColInsertList.add(new Column(EventServerColumns.WHEN));
    eventColInsertList.add(new Column(EventServerColumns.WHEN_FRAC_SEC));
    eventColInsertList.add(new Column(EventServerColumns.PACO_VERSION));
    eventColInsertList.add(new Column(EventServerColumns.APP_ID));
    eventColInsertList.add(new Column(EventServerColumns.JOINED));
    eventColInsertList.add(new Column(EventServerColumns.SORT_DATE));
    eventColInsertList.add(new Column(EventServerColumns.CLIENT_TIME_ZONE));
    eventColInsertList.add(new Column(Constants.UNDERSCORE_ID));
    eventColInsertList.add(new Column(EventServerColumns.EXPERIMENT_LOOKUP_ID));

    outputColList.add(new Column(OutputBaseColumns.EVENT_ID));
    outputColList.add(new Column(OutputBaseColumns.NAME));
    outputColList.add(new Column(OutputBaseColumns.ANSWER));
  
    failedColList.add(new Column(FailedEventServerColumns.EVENT_JSON));
    failedColList.add(new Column(FailedEventServerColumns.REASON));
    failedColList.add(new Column(FailedEventServerColumns.COMMENTS));

    cursorColList.add(new Column("`cursor`"));
    cursorColList.add(new Column("`current_time`"));
    cursorColList.add(new Column("`current_time_fractional_sec`"));

    missedEventsColList.add(new Column("`output_info`"));
    missedEventsColList.add(new Column("`current_time`"));
    missedEventsColList.add(new Column("`current_time_fractional_sec`"));

    streamingColList.add(new Column("`start_time`"));
    streamingColList.add(new Column("`start_time_fractional_sec`"));
    streamingColList.add(new Column("`current_time`"));

    catchupFailureColList.add(new Column(CatchupFailureServerColumns.INSERTION_TYPE));
    catchupFailureColList.add(new Column(CatchupFailureServerColumns.EVENT_ID));
    catchupFailureColList.add(new Column(CatchupFailureServerColumns.TEXT));
    catchupFailureColList.add(new Column(CatchupFailureServerColumns.FAILURE_REASON));

  }

  @Override
  public boolean insertEventsInBatch(List<Event> events) {
    if (events == null) {
      log.warning(ErrorMessages.NOT_VALID_DATA.getDescription());
      return false;
    }
    boolean newBatch = true;
    boolean unknownException = false;
    Timestamp whenTs = null;
    int whenFrac = 0;
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    boolean retVal = false;
    //startCount for setting parameter index
    int i = 1 ;
    int eventCtr = 0;
    ExpressionList eventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert eventInsert = new Insert();
    CloudSQLDaoImpl daoImpl = new CloudSQLDaoImpl();
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      conn.setAutoCommit(false);
      eventInsert.setTable(new Table(EventBaseColumns.TABLE_NAME));
      eventInsert.setUseValues(true);
      eventExprList.setExpressions(exp);
      eventInsert.setItemsList(eventExprList);
      eventInsert.setColumns(eventColInsertList);
      // Adding ? for prepared stmt
      for (Column c : eventColInsertList) {
        ((ExpressionList) eventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateEvent = conn.prepareStatement(eventInsert.toString());
      while (true) {
        if(!newBatch || events.size()<1000) {
          log.info("Is it New Batch"+newBatch + ". Batch size:" + events.size());
        }
        try {
          for(eventCtr = 0; eventCtr < events.size(); eventCtr++) {
            Event event = events.get(eventCtr);
            statementCreateEvent.setTimestamp(i++, event.getScheduledTime() != null ? new Timestamp(event.getScheduledTime().getTime()): null);
            statementCreateEvent.setTimestamp(i++, event.getResponseTime() != null ? new Timestamp(event.getResponseTime().getTime()): null);
            statementCreateEvent.setLong(i++, event.getActionTriggerId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
            statementCreateEvent.setLong(i++, event.getActionTriggerSpecId() != null ? new Long(event.getActionTriggerId()) : java.sql.Types.NULL);
            statementCreateEvent.setLong(i++, event.getActionId() != null ? new Long(event.getActionId()) : java.sql.Types.NULL);
            PacoId anonId = daoImpl.getAnonymousIdAndCreate(Long.parseLong(event.getExperimentId()), event.getWho(), true);
            statementCreateEvent.setString(i++, anonId.getId().toString());
            if (event.getWhen() != null) {
              whenTs = new Timestamp(event.getWhen().getTime());
              whenFrac = TimeUtil.getFractionalSeconds(whenTs);
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
            PacoId experimentLookupId = daoImpl.getExperimentLookupIdAndCreate(Long.parseLong(event.getExperimentId()), event.getExperimentName(), event.getExperimentGroupName(), event.getExperimentVersion(), true);
            statementCreateEvent.setLong(i++, experimentLookupId.getId());

            statementCreateEvent.addBatch();
            i = 1;
          }// for loop
          statementCreateEvent.executeBatch();
          conn.commit();
          retVal = true;
          break;
        } catch (BatchUpdateException trb) {
            int updateResult[] = trb.getUpdateCounts();
            newBatch = false;
            conn.rollback();
            List<Event> toRemove = Lists.newArrayList();
            // primary key violation
            if(trb.getErrorCode() == 1062 && trb.getCause() instanceof MySQLIntegrityConstraintViolationException) {
              for (int y=0; y<updateResult.length;y++) {
                if (updateResult[y]== Statement.EXECUTE_FAILED) {
                  log.warning("response values for position"+ y + "->"+updateResult[y] + "->"+ events.get(y).getId());
                  toRemove.add(events.get(y));
                }
              }
              events.removeAll(toRemove);
            } else { // any other failure
              log.warning(trb.getErrorCode() + "unknown constraint failed: so, break" + ExceptionUtil.getStackTraceAsString(trb));
              unknownException = true;
              for (int y=0; y<updateResult.length;y++) {
                if (updateResult[y]== Statement.EXECUTE_FAILED) {
                  log.info("response values for position"+ y + "->"+updateResult[y] + "->"+ events.get(y).getId());
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
      log.info(ErrorMessages.GENERAL_EXCEPTION.getDescription() + e.getCause());
      e.printStackTrace(System.out);
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
        if(!newBatch || outputs.size()<1000) {
          log.info("Is it New Batch"+newBatch + ". Batch size:" + outputs.size());
        }
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
          int updateResult[] = trb.getUpdateCounts();
          newBatch = false;
          conn.rollback();
          //combination primary key failure
          if(trb.getErrorCode() == 1062 && trb.getCause() instanceof MySQLIntegrityConstraintViolationException) {
            for (int y=0; y<updateResult.length;y++) {
              if (updateResult[y]== Statement.EXECUTE_FAILED) {
                log.info("response values for position"+ y + "->"+updateResult[y] + "->"+outputs.get(y).toString());
                String origKey = outputs.get(y).getText();
                if (origKey != null) {
                  outputs.get(y).setText(origKey + "-DUP-" +(DUP_CTR++) );
                } else {
                  outputs.get(y).setText("DUP-"+ (DUP_CTR++));
                }
              }
            }
          } else  if(trb.getErrorCode() == 1452 && trb.getCause() instanceof MySQLIntegrityConstraintViolationException) {// foreign key failure
            int lastErrorPos = 0;
            long lastEventId = 0;
            for (int y=0; y<updateResult.length;y++) {
              if (updateResult[y]== Statement.EXECUTE_FAILED) {
                lastErrorPos = y;
                lastEventId = outputs.get(y).getEventId();
              }
            }

            log.warning("Last Error position is:"+ lastErrorPos);
            // remove the last eventid from the complete batch, and persist in missed events table
            for (int k = 0; k < outputs.size();k++) {
              if (outputs.get(k).getEventId() == lastEventId) {
                persistMissedEvent(conn, outputs.get(k).toString());
                outputs.remove(k);
              }
            }
          } else {
            log.warning(trb.getErrorCode() + "unknown constraint failed: so, break" + ExceptionUtil.getStackTraceAsString(trb));
            unknownException = true;
            for (int y=0; y<updateResult.length;y++) {
              if (updateResult[y]== Statement.EXECUTE_FAILED) {
                log.warning("response values for position"+ y + "->"+updateResult[y] + "->"+outputs.get(y).toString());
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
      log.warning(ExceptionUtil.getStackTraceAsString(e));
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

  @Override
  public Long getEarliestWhen() throws SQLException, ParseException {
    Connection conn = null;
    Timestamp earliestTs = null;
    Long retTime = null;
    int fracSec = 0;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;
    String query  = "select `when`, when_fractional_sec from events order by `when` asc, when_fractional_sec asc limit 1";

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectEvent = conn.prepareStatement(query);
      rs = statementSelectEvent.executeQuery();

      if (rs != null) {
        while (rs.next()) {
          earliestTs = rs.getTimestamp(1);
          fracSec = rs.getInt(2);
        }
        retTime = earliestTs.getTime() + fracSec;
      }
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
    return retTime;
  }

  @Override
  public Long getEarliestStreaming() throws SQLException, ParseException {
    Connection conn = null;
    Date earliestDate = null;
    int fracSec = 0;
    Timestamp stTime = null;
    PreparedStatement statementSelectEvent = null;
    ResultSet rs = null;
    String query  = "select `start_time`,start_time_fractional_sec from streaming order by `current_time` desc limit 1";

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementSelectEvent = conn.prepareStatement(query);
      rs = statementSelectEvent.executeQuery();

      if (rs != null) {
        while (rs.next()) {
          stTime = rs.getTimestamp(1);
          fracSec = rs.getInt(2);
        }

        earliestDate = new Date(stTime.getTime() + fracSec);
      }
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
  public boolean insertCatchupFailure(String insertType, Long eventId, String text, String comments) {
    Connection conn = null;
    PreparedStatement statementCreateCatchupFailure = null;
    boolean retVal = false;
    ExpressionList catchupFailureExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert catchupFailureInsert = new Insert();

    try {
      log.info("Inserting catchup " + insertType + " failure");
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      conn.setAutoCommit(false);
      catchupFailureInsert.setTable(new Table(CatchupFailureServerColumns.TABLE_NAME));
      catchupFailureInsert.setUseValues(true);
      catchupFailureExprList.setExpressions(exp);
      catchupFailureInsert.setItemsList(catchupFailureExprList);
      catchupFailureInsert.setColumns(catchupFailureColList);
      // Adding ? for prepared stmt
      for (Column c : catchupFailureColList) {
        ((ExpressionList) catchupFailureInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementCreateCatchupFailure = conn.prepareStatement(catchupFailureInsert.toString());
      statementCreateCatchupFailure.setString(1, insertType);
      statementCreateCatchupFailure.setLong(2, eventId);
      statementCreateCatchupFailure.setString(3, text);
      statementCreateCatchupFailure.setString(4, comments);

      statementCreateCatchupFailure.execute();
      conn.commit();
      retVal = true;
    } catch(SQLException sqle) {
      log.severe("Exception while inserting to catchup failure table" + eventId);
    }
    finally {
      try {
        if (statementCreateCatchupFailure != null) {
          statementCreateCatchupFailure.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
  }

  @Override
  public String createTables(String stepNo) throws SQLException {
    String retString = null;
    Connection conn = null;
    PreparedStatement statementCreateEvent = null;
    PreparedStatement statementCreateOutput = null;
    PreparedStatement statementCreateFailedEvent = null;
    PreparedStatement statementCreateMigrationCursor = null;
    PreparedStatement statementCreateMissingEventIds = null;
    PreparedStatement statementCreateStreaming = null;
    PreparedStatement statementCreateCatchupFailure = null;
    try {

      conn = CloudSQLConnectionManager.getInstance().getConnection();
      // TODO Sub Partition size for the experiment hash bucket
      final String createEventsTableSql = "CREATE TABLE `" + EventServerColumns.TABLE_NAME
                                          + "` (" +"`" + Constants.UNDERSCORE_ID + "` bigint(20) NOT NULL ,"+ "`"
                                          + EventServerColumns.SCHEDULE_TIME + "` datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.RESPONSE_TIME + "` datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_TRIGGER_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.ACTION_TRIGGER_SPEC_ID + "` bigint(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.WHO + "` varchar(500) NOT NULL," + "`"
                                          + EventServerColumns.PACO_VERSION + "` varchar(20) DEFAULT NULL," + "`"
                                          + EventServerColumns.APP_ID + "` varchar(25) DEFAULT NULL,"
                                          // when column already has the back tick
                                          + EventServerColumns.WHEN + " datetime DEFAULT NULL," + "`"
                                          + EventServerColumns.WHEN_FRAC_SEC + "` int(11) DEFAULT '0', " +  "`"
                                          + EventServerColumns.ARCHIVE_FLAG + "` tinyint(4) NOT NULL DEFAULT '0'," + "`"
                                          + EventServerColumns.JOINED + "` tinyint(1)  DEFAULT NULL," + "`"
                                          + EventServerColumns.SORT_DATE + "` datetime  DEFAULT NULL," + "`"
                                          + EventServerColumns.CLIENT_TIME_ZONE + "` varchar(20) DEFAULT NULL,"
                                          + "PRIMARY KEY (`" + Constants.UNDERSCORE_ID + "`)," + "KEY `when_index` ("
                                          + EventServerColumns.WHEN + ")," + "KEY `exp_id_resp_time_index` (`"
                                          + EventServerColumns.EXPERIMENT_ID + "`,`" + EventServerColumns.RESPONSE_TIME
                                          + "`)," + "KEY `exp_id_when_index` (`" + EventServerColumns.EXPERIMENT_ID
                                          + "`," + EventServerColumns.WHEN + ","+ EventServerColumns.WHEN_FRAC_SEC+")," + "KEY `exp_id_who_when_index` (`"
                                          + EventServerColumns.EXPERIMENT_ID + "`,`" + EventServerColumns.WHO + "`,"
                                          + EventServerColumns.WHEN + ","+ EventServerColumns.WHEN_FRAC_SEC  + ")) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

      final String createOutputsTableSql = "CREATE TABLE `" + OutputBaseColumns.TABLE_NAME+ "` (" + "`"
                                           + OutputBaseColumns.EVENT_ID + "` bigint(20) NOT NULL," + "`"
                                           + OutputBaseColumns.NAME + "` varchar(750) NOT NULL," + "`"
                                           + OutputBaseColumns.ANSWER + "` varchar(1000) DEFAULT NULL," + "`"
                                           + OutputBaseColumns.ARCHIVE_FLAG + "` tinyint(4) NOT NULL DEFAULT '0',"
                                           + "PRIMARY KEY (`" + OutputBaseColumns.EVENT_ID + "`,`"
                                           + OutputBaseColumns.NAME + "`)," + "KEY `events_id_index` (`"
                                           + OutputBaseColumns.EVENT_ID + "`)," + "KEY `text_index` (`"
                                           + OutputBaseColumns.NAME + "`), "
                                           + " CONSTRAINT `events_id_fk` FOREIGN KEY (`"+ OutputBaseColumns.EVENT_ID + "`) REFERENCES `"+ EventBaseColumns.TABLE_NAME +"` (`"+ ID +"`) ON DELETE CASCADE ON UPDATE NO ACTION " +
                                           ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

      final String createFailedEventsTableSql = "CREATE TABLE `" +  FailedEventServerColumns.TABLE_NAME +  "` (" + "`"
                                            + FailedEventServerColumns.ID + "` bigint(20) NOT NULL AUTO_INCREMENT," + "`"
                                            + FailedEventServerColumns.EVENT_JSON + "` varchar(3000) NOT NULL," + "`"
                                            + FailedEventServerColumns.FAILED_INSERT_TIME + "` datetime  DEFAULT CURRENT_TIMESTAMP," + "`"
                                            + FailedEventServerColumns.REASON + "` varchar(500) DEFAULT NULL," + "`"
                                            + FailedEventServerColumns.COMMENTS + "` varchar(1000) DEFAULT NULL," +"`"
                                            + FailedEventServerColumns.REPROCESSED + "` varchar(10) DEFAULT 'false',"
                                            + "PRIMARY KEY (`" + FailedEventServerColumns.ID + "`)"+") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4";
      final String createCatchupFailureTableSql = "CREATE TABLE `" +  CatchupFailureServerColumns.TABLE_NAME +  "` (" + "`"
                                            + CatchupFailureServerColumns.ID + "` bigint(20) NOT NULL AUTO_INCREMENT," + "`"
                                            + CatchupFailureServerColumns.INSERTION_TYPE + "` varchar(20) NOT NULL," + "`"
                                            + CatchupFailureServerColumns.EVENT_ID + "` bigint(20)  DEFAULT NULL," + "`"
                                            + CatchupFailureServerColumns.TEXT + "` varchar(750) DEFAULT NULL," + "`"
                                            + CatchupFailureServerColumns.FAILURE_REASON + "` varchar(750) DEFAULT NULL," +"`"
                                            + CatchupFailureServerColumns.FAILURE_TIME + "` datetime DEFAULT CURRENT_TIMESTAMP,"
                                            + "PRIMARY KEY (`" + CatchupFailureServerColumns.ID + "`)"+") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4";
      final String createMigrationCursorTableSql = "CREATE TABLE `migration_cursor` ( " +
                                                    "`id` bigint(20) NOT NULL AUTO_INCREMENT," +
                                                    "`cursor` varchar(400) DEFAULT NULL, " +
                                                    "`current_time` datetime DEFAULT NULL," +
                                                    "`current_time_fractional_sec` int(11) DEFAULT '0'," +
                                                    "PRIMARY KEY (`id`) " +
                                                    ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1";
      final String createMissingEventIdsTableSql = "CREATE TABLE `missing_event_ids` (" +
                                                    "`id` bigint(20) NOT NULL AUTO_INCREMENT," +
                                                    "`output_info` varchar(3000) DEFAULT NULL," +
                                                    "`current_time` datetime DEFAULT NULL," +
                                                    " `current_time_fractional_sec` int(11) DEFAULT '0'," +
                                                    "PRIMARY KEY (`id`) " +
                                                    ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1";
     final String createStreamingTableSql = "CREATE TABLE `streaming` ( " +
                                             "`id` bigint(20) NOT NULL AUTO_INCREMENT, " +
                                             " `start_time` datetime DEFAULT NULL, " +
                                             " `start_time_fractional_sec` int(11) DEFAULT '0'," +
                                             " `current_time` datetime DEFAULT NULL, " +
                                             " PRIMARY KEY (`id`) " +
                                             ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1";
      if (stepNo.equalsIgnoreCase("one")) {

        statementCreateEvent = conn.prepareStatement(createEventsTableSql);
        log.info(createEventsTableSql);
        statementCreateEvent.execute();
        log.info("created events");
        // TODO better handling
        retString = "created events table. ";

        statementCreateOutput = conn.prepareStatement(createOutputsTableSql);
        log.info(createOutputsTableSql);
        statementCreateOutput.execute();
        log.info("created outputs");
        // TODO better handling
        retString = retString + "Created outputs table. ";

        statementCreateFailedEvent = conn.prepareStatement(createFailedEventsTableSql);
        log.info(createFailedEventsTableSql);
        statementCreateFailedEvent.execute();
        log.info("created failed events");
        // TODO better handling
        retString = retString + "Created FailedEvents table. ";

        statementCreateMigrationCursor = conn.prepareStatement(createMigrationCursorTableSql);
        log.info(createMigrationCursorTableSql);
        statementCreateMigrationCursor.execute();
        log.info("created migration cursor");
        // TODO better handling
        retString = retString + "Created MigrationCursor table. ";

        statementCreateMissingEventIds = conn.prepareStatement(createMissingEventIdsTableSql);
        log.info(createMissingEventIdsTableSql);
        statementCreateMissingEventIds.execute();
        log.info("created missing events");
        // TODO better handling
        retString = retString + "Created missingEventIds table. ";

        statementCreateStreaming = conn.prepareStatement(createStreamingTableSql);
        log.info(createStreamingTableSql);
        statementCreateStreaming.execute();
        log.info("created streaming");
        // TODO better handling
        retString = retString + "Created Streaming table. ";
      } else  if (stepNo.equalsIgnoreCase("two")) {
        statementCreateCatchupFailure = conn.prepareStatement(createCatchupFailureTableSql);
        log.info(createCatchupFailureTableSql);
        statementCreateCatchupFailure.execute();
        log.info("created catchup failure");
        // TODO better handling
        retString = "Created Catchup failure table. ";
      }
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
        if (statementCreateMigrationCursor != null) {
          statementCreateMigrationCursor.close();
        }
        if (statementCreateMissingEventIds != null) {
          statementCreateMissingEventIds.close();
        }
        if (statementCreateStreaming != null) {
          statementCreateStreaming.close();
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

  @Override
  public boolean eventV5AddNewColumns() throws SQLException{
    final String addNewColumnsSql = "ALTER TABLE `pacodb`.`"+ EventBaseColumns.TABLE_NAME  +"` " +
                                 " ADD COLUMN `" + EventBaseColumns.SCHEDULE_TIME + "` DATETIME NULL DEFAULT NULL AFTER `" + EventServerColumns.CLIENT_TIME_ZONE+ "`, " +
                                 " ADD COLUMN `" + EventBaseColumns.RESPONSE_TIME + "` DATETIME NULL DEFAULT NULL AFTER `" + EventServerColumns.SCHEDULE_TIME + "`, " +
                                 " ADD COLUMN `" + EventServerColumns.SORT_DATE + "` DATETIME NULL DEFAULT NULL AFTER `" + EventServerColumns.RESPONSE_TIME + "`";
    Connection conn = null;
    PreparedStatement statementAddNewCol = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementAddNewCol = conn.prepareStatement(addNewColumnsSql);
      log.info(addNewColumnsSql);
      statementAddNewCol.execute();
      log.info("Added new columns");
    } catch (SQLException sqle) {
      log.warning("SQLException while adding new cols" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new cols" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementAddNewCol != null) {
          statementAddNewCol.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
          log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;
  }

  @Override
  public boolean eventV5RenameExistingColumns() throws SQLException {
    final String renameExistingColumns = "ALTER TABLE `pacodb`.`"+ EventBaseColumns.TABLE_NAME  +"` " +
                                           " CHANGE COLUMN `" + EventBaseColumns.SCHEDULE_TIME + "` `" + EventServerColumns.SCHEDULE_TIME_UTC + "` DATETIME NULL DEFAULT NULL , " +
                                           " CHANGE COLUMN `" + EventBaseColumns.RESPONSE_TIME + "` `" + EventServerColumns.RESPONSE_TIME_UTC + "` DATETIME NULL DEFAULT NULL , " +
                                           " CHANGE COLUMN `" + EventServerColumns.SORT_DATE + "` `" + EventServerColumns.SORT_DATE_UTC + "` DATETIME NULL DEFAULT NULL";
    Connection conn = null;
    PreparedStatement statementRenameExistingCol = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementRenameExistingCol = conn.prepareStatement(renameExistingColumns);
      log.info(renameExistingColumns);
      statementRenameExistingCol.execute();
      log.info("Renamed existing columns");
    } catch (SQLException sqle) {
      log.warning("SQLException while renaming existing columns" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while renaming existing columns" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementRenameExistingCol != null) {
          statementRenameExistingCol.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;
  }

  @Override
  public boolean eventV5UpdateNewColumnsWithValues() throws SQLException{
    final String updateValuesExistingColumns1 = "update `pacodb`.`" + EventBaseColumns.TABLE_NAME  +"` set " + EventBaseColumns.RESPONSE_TIME + " = CONVERT_TZ(" + EventServerColumns.RESPONSE_TIME_UTC + ",'+00:00'," + EventServerColumns.CLIENT_TIME_ZONE + ") where " + EventServerColumns.RESPONSE_TIME_UTC + " is not null and _id >0";
    final String updateValuesExistingColumns2 = "update `pacodb`.`" + EventBaseColumns.TABLE_NAME  +"` set " + EventBaseColumns.SCHEDULE_TIME + "  = CONVERT_TZ(" + EventServerColumns.SCHEDULE_TIME_UTC + ",'+00:00'," + EventServerColumns.CLIENT_TIME_ZONE + ") where " + EventServerColumns.SCHEDULE_TIME_UTC + " is not null and _id >0";
    final String updateValuesExistingColumns3 = "update `pacodb`.`" + EventBaseColumns.TABLE_NAME  +"` set " + EventServerColumns.SORT_DATE + " = CONVERT_TZ(" + EventServerColumns.SORT_DATE_UTC + ",'+00:00'," + EventServerColumns.CLIENT_TIME_ZONE + ") where " + EventServerColumns.SORT_DATE_UTC + " is not null and _id >0";
    String[] qry = new String[] { updateValuesExistingColumns1, updateValuesExistingColumns2, updateValuesExistingColumns3};
    Connection conn = null;
    PreparedStatement statementUpdateValuesCol = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for ( int i = 0; i < qry.length; i++) {
        statementUpdateValuesCol  = conn.prepareStatement(qry[i]);
        log.info(qry[i]);
        statementUpdateValuesCol.execute();
      }
      log.info("Updated columns with values");
    } catch (SQLException sqle) {
      log.warning("SQLException while updating values" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while updating values" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementUpdateValuesCol != null) {
          statementUpdateValuesCol.close();
        }

        if (conn != null) {
        conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;
  }

  @Override
  public boolean eventV5RemoveOldIndexes() throws SQLException{
    boolean isComplete = false;
    String[] qry = new String[3];
    final String removeOldIndexSql1 = "ALTER TABLE `pacodb`.`events` " +
            " DROP INDEX `exp_id_when_index` , " +
            " DROP INDEX `exp_id_who_when_index` , " +
            " DROP INDEX `exp_id_resp_time_index` , " +
            " DROP INDEX `when_index` ";
    final String removeOldIndexSql2 = "ALTER TABLE `pacodb`.`outputs` " +
            " DROP INDEX `events_id_index` ";
    final String removeOldIndexSql3 = "ALTER TABLE `pacodb`.`outputs` " +
            " DROP INDEX `text_index` ";
    qry = new String[] { removeOldIndexSql1, removeOldIndexSql2, removeOldIndexSql3};
    Connection conn = null;
    PreparedStatement statementRemoveOldIndex = null;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for ( int i = 0; i < qry.length; i++) {
        statementRemoveOldIndex = conn.prepareStatement(qry[i]);
        log.info(qry[i]);
        statementRemoveOldIndex.execute();
      }
      isComplete = true;
    } catch (SQLException sqle) {
      log.warning("SQLException while removing old index" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while removing old index" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementRemoveOldIndex != null) {
          statementRemoveOldIndex.close();
        }

        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }

    return isComplete;

  }

  @Override
  public boolean eventV5AddNewIndexes() throws SQLException {
    String[] qry = null;
    final String addNewIndexSql1 = "ALTER TABLE `pacodb`.`events` " +
            " ADD INDEX `exp_id_grp_who_index`  (`experiment_id` ASC, `group_name`(100) ASC, `who` ASC) , " +
            " ADD INDEX `exp_id_sort_date_index` (`experiment_id` ASC, `sort_date` DESC), " +
            " ADD INDEX `exp_id_who_index`  (`experiment_id` ASC, `who` ASC)  ";
    qry = new String[] { addNewIndexSql1 };
    Connection conn = null;
    PreparedStatement statementAddNewIndex = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for ( int i = 0; i < qry.length; i++) {
        statementAddNewIndex = conn.prepareStatement(qry[i]);
        log.info(qry[i]);
        statementAddNewIndex.execute();
      }
      log.info("Added New Indexes");
    } catch (SQLException sqle) {
      log.warning("SQLException while adding new index" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new index" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementAddNewIndex != null) {
          statementAddNewIndex.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;

  }

  @Override
  public boolean persistCursor(String cursor) {
    Connection conn = null;
    PreparedStatement statementPersistCursor = null;
    boolean retVal = false;
    Timestamp crTimestamp = null;
    int crFracSec = 0;
    ExpressionList persistCursorExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert persistCursorInsert = new Insert();

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      persistCursorInsert.setTable(new Table("migration_cursor"));
      persistCursorInsert.setUseValues(true);
      persistCursorExprList.setExpressions(exp);
      persistCursorInsert.setItemsList(persistCursorExprList);
      persistCursorInsert.setColumns(cursorColList);
      // Adding ? for prepared stmt
      for (Column c : cursorColList) {
        ((ExpressionList) persistCursorInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementPersistCursor = conn.prepareStatement(persistCursorInsert.toString());
      statementPersistCursor.setString(1, cursor);

      crTimestamp = new Timestamp(System.currentTimeMillis());
      crFracSec =  TimeUtil.getFractionalSeconds(crTimestamp);
      statementPersistCursor.setTimestamp(2, crTimestamp);
      statementPersistCursor.setInt(3, crFracSec);

      statementPersistCursor.execute();
      conn.commit();
      retVal = true;
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to migration cursor" + cursor);
      log.warning("Exception while inserting to migration cursors table" + sqle);
    }
    finally {
      try {
        if (statementPersistCursor != null) {
          statementPersistCursor.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
  }



  @Override
  public boolean persistMissedEvent(Connection conn, String origMigrationOutput) {

    PreparedStatement statementPersistMissedEvent = null;
    boolean retVal = false;
    Timestamp crTimestamp = null;
    int crFracSec = 0;
    ExpressionList persistMissedEventExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert persistMissedEventInsert = new Insert();
    String modMigrationOutput = origMigrationOutput;

    try {
      conn.setAutoCommit(false);
      persistMissedEventInsert.setTable(new Table("missing_event_ids"));
      persistMissedEventInsert.setUseValues(true);
      persistMissedEventExprList.setExpressions(exp);
      persistMissedEventInsert.setItemsList(persistMissedEventExprList);
      persistMissedEventInsert.setColumns(missedEventsColList);
      // Adding ? for prepared stmt
      for (Column c : missedEventsColList) {
        ((ExpressionList) persistMissedEventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }
      if(origMigrationOutput != null && origMigrationOutput.length() > 2999) {
        modMigrationOutput = origMigrationOutput.substring(0, 2999);
      }
      statementPersistMissedEvent = conn.prepareStatement(persistMissedEventInsert.toString());
      statementPersistMissedEvent.setString(1, modMigrationOutput);

      crTimestamp = new Timestamp(System.currentTimeMillis());
      crFracSec =  TimeUtil.getFractionalSeconds(crTimestamp);
      statementPersistMissedEvent.setTimestamp(2, crTimestamp);
      statementPersistMissedEvent.setInt(3, crFracSec);

      statementPersistMissedEvent.execute();
      conn.commit();
      retVal = true;
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to missed events table" + sqle);
    }
    finally {
      try {
        if (statementPersistMissedEvent != null) {
          statementPersistMissedEvent.close();
        }

      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
  }

  @Override
  public boolean persistStreamingStart(DateTime startTime) {
    Connection conn = null;
    PreparedStatement statementStreamingStart = null;
    boolean retVal = false;
    Timestamp stTimestamp = null;
    int stFracSec = 0;
    ExpressionList streamingExprList = new ExpressionList();
    List<Expression> exp = Lists.newArrayList();
    Insert persistMissedEventInsert = new Insert();

    try {
      log.info("Inserting streaming start ");
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      conn.setAutoCommit(false);
      persistMissedEventInsert.setTable(new Table("streaming"));
      persistMissedEventInsert.setUseValues(true);
      streamingExprList.setExpressions(exp);
      persistMissedEventInsert.setItemsList(streamingExprList);
      persistMissedEventInsert.setColumns(streamingColList);
      // Adding ? for prepared stmt
      for (Column c : streamingColList) {
        ((ExpressionList) persistMissedEventInsert.getItemsList()).getExpressions().add(new JdbcParameter());
      }

      statementStreamingStart = conn.prepareStatement(persistMissedEventInsert.toString());
      if(startTime != null) {
        stTimestamp = new Timestamp(startTime.getMillis());
        stFracSec = TimeUtil.getFractionalSeconds(stTimestamp);
      }
      statementStreamingStart.setTimestamp(1, stTimestamp);
      statementStreamingStart.setInt(2, stFracSec);
      statementStreamingStart.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

      statementStreamingStart.execute();
      conn.commit();
      retVal = true;
    } catch(SQLException sqle) {
      log.warning("Exception while inserting to streaming" + startTime);
      log.warning("Exception while inserting to streaming table" + sqle);
    }
    finally {
      try {
        if (statementStreamingStart != null) {
          statementStreamingStart.close();
        }
        if (conn != null) {
          conn.close();
        }

      } catch (SQLException ex1) {
        log.info(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription() + ex1);
      }
    }
    return retVal;
  }
  
  @Override
  public boolean anonymizeParticipantsCreateTables() throws SQLException{
    boolean isComplete = false;
    String[] qry = new String[3];
    final String createTableSql1 = "CREATE TABLE `pacodb`.`"+ ExperimentLookupColumns.TABLE_NAME+"` (" +
            ExperimentLookupColumns.EXPERIMENT_LOOKUP_ID + " INT NOT NULL AUTO_INCREMENT," +
            ExperimentLookupColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
            ExperimentLookupColumns.GROUP_NAME + " VARCHAR(500) NOT NULL," +
            ExperimentLookupColumns.EXPERIMENT_NAME + " VARCHAR(500) NOT NULL," +
            ExperimentLookupColumns.EXPERIMENT_VERSION + " INT(11) NOT NULL," +
            " PRIMARY KEY (`"+ ExperimentLookupColumns.EXPERIMENT_LOOKUP_ID +"`))" ;
//            " INDEX `exp_id_version_index` (`"+ ExperimentLookupServerColumns.EXPERIMENT_ID + "` ASC, `"+ ExperimentLookupServerColumns.EXPERIMENT_VERSION + "` ASC))";
    final String createTableSql2 = "CREATE TABLE `pacodb`.`"+ UserColumns.TABLE_NAME+"` (" +
            UserColumns.USER_ID + " INT NOT NULL AUTO_INCREMENT," +
            UserColumns.WHO + " VARCHAR(500) NOT NULL," +
            " PRIMARY KEY (`" + UserColumns.USER_ID + "`)," +
            " UNIQUE INDEX `who_unique_index` (`"+ UserColumns.WHO + "` ASC))";
    final String createTableSql3 = "CREATE TABLE `pacodb`.`"+ ExperimentUserColumns.TABLE_NAME +"` (" +
            ExperimentUserColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
            ExperimentUserColumns.USER_ID + " INT NOT NULL," +
            ExperimentUserColumns.EXP_USER_ANON_ID + " INT NOT NULL," +
            ExperimentUserColumns.USER_TYPE + " CHAR(1) NOT NULL," +
            " PRIMARY KEY (`" + ExperimentUserColumns.EXPERIMENT_ID+ "`,`" +ExperimentUserColumns.USER_ID+ "`), "+ 
            " UNIQUE KEY `experiment_id_anon_id_UNIQUE` (`experiment_id`,`experiment_user_anon_id`))";
    final String createTableSql4 = "CREATE TABLE `pacodb`.`"+ ExperimentLookupColumns.TABLE_NAME+"_tracking` (" +
            "tracking_id INT NOT NULL AUTO_INCREMENT," +
            ExperimentLookupColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
            ExperimentLookupColumns.GROUP_NAME + " VARCHAR(500)," +
            ExperimentLookupColumns.EXPERIMENT_NAME + " VARCHAR(500) NOT NULL," +
            ExperimentLookupColumns.EXPERIMENT_VERSION + " INT(11) NOT NULL," +
            EventServerColumns.WHO + "  VARCHAR(500) NOT NULL," +
            "updated_events CHAR(1) NOT NULL DEFAULT 'N'," +
            " PRIMARY KEY (`tracking_id`))" ;
            
    qry = new String[] { createTableSql1, createTableSql2, createTableSql3, createTableSql4};
    
    Connection conn = null;
    PreparedStatement statementCreateTable = null;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for ( int i = 0; i < qry.length; i++) {
        statementCreateTable = conn.prepareStatement(qry[i]);
        log.info(qry[i]);
        statementCreateTable.execute();
      }
      isComplete = true;
    } catch (SQLException sqle) {
      log.warning("SQLException while creating tables" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while creating tables" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementCreateTable != null) {
          statementCreateTable.close();
        }

        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }

    return isComplete;
  }
  
  @Override
  public boolean anonymizeParticipantsAddColumnToEventTable() throws SQLException{
    final String addNewColumnsSql = "ALTER TABLE `pacodb`.`"+ EventBaseColumns.TABLE_NAME  +"` " +
                                 " ADD COLUMN `" + EventServerColumns.EXPERIMENT_LOOKUP_ID + "` INT AFTER `" + EventServerColumns.SORT_DATE + "` ";
    Connection conn = null;
    PreparedStatement statementAddNewCol = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementAddNewCol = conn.prepareStatement(addNewColumnsSql);
      log.info(addNewColumnsSql);
      statementAddNewCol.execute();
      log.info("Added new columns");
    } catch (SQLException sqle) {
      log.warning("SQLException while adding new cols" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new cols" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementAddNewCol != null) {
          statementAddNewCol.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
          log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;
  }

  @Override
  public boolean anonymizeParticipantsMigrateToUserAndExptUser()  throws SQLException {
    CloudSQLDaoImpl daoImpl = new CloudSQLDaoImpl();
    final ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();

    ExperimentQueryResult experimentsQueryResults = experimentService.getAllExperiments(null);
    List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();

    log.info("Retrieved " + experimentList.size() + "experiments");

    if (experimentList == null || experimentList.isEmpty()) {
      return false;
    }

    for (ExperimentDAO eachExperiment : experimentList) {
      List<String> adminLstInRequest = eachExperiment.getAdmins();
      List<String> partLstInRequest = eachExperiment.getPublishedUsers();
      daoImpl.ensureUserId(eachExperiment.getId(), Sets.newHashSet(adminLstInRequest), Sets.newHashSet(partLstInRequest));
    }
    return true;
  }
  
  @Override
  public boolean anonymizeParticipantsMigrateToExperimentLookup()  throws SQLException {
    final String insertToLookupSql = "INSERT INTO experiment_lookup(experiment_id, experiment_name, group_name, experiment_version) SELECT distinct experiment_id, experiment_name, group_name, experiment_version FROM events where experiment_id > 0 and experiment_version >= 0 and experiment_name is not null";
    Connection conn = null;
    PreparedStatement statementInsertToLookup = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementInsertToLookup = conn.prepareStatement(insertToLookupSql);
      log.info(insertToLookupSql);
      statementInsertToLookup.execute();
      log.info("Inserted experiment info from events into lookup table");
    } catch (SQLException sqle) {
      log.warning("SQLException while inserting to lookup" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while inserting to lookup" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementInsertToLookup != null) {
          statementInsertToLookup.close();
        }
        if (conn != null) {
        conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;
  }
  
  @Override
  public boolean anonymizeParticipantsModifyExperimentNameFromNullToBlank()  throws SQLException {
    final String updateExptNameSql = "update events set experiment_name='' where experiment_name is null and experiment_id > 0";
    Connection conn = null;
    PreparedStatement statementUpdateExptName = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementUpdateExptName = conn.prepareStatement(updateExptNameSql);
      log.info(updateExptNameSql);
      statementUpdateExptName.execute();
      log.info("Experiment name changed from null to blank");
    } catch (SQLException sqle) {
      log.warning("SQLException while changing expt name" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while changing expt name" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementUpdateExptName != null) {
          statementUpdateExptName.close();
        }
        if (conn != null) {
        conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;
  }
  
  @Override
  public boolean anonymizeParticipantsMigrateToExperimentLookupTracking()  throws SQLException {
    final String insertToLookupTrackingSql = "INSERT INTO experiment_lookup_tracking(experiment_id, `experiment_name`, group_name, experiment_version, who) SELECT distinct experiment_id, experiment_name, group_name, experiment_version, who FROM events where experiment_id > 0 and experiment_version >= 0 and experiment_name is not null";
    Connection conn = null;
    PreparedStatement statementInsertToLookup = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementInsertToLookup = conn.prepareStatement(insertToLookupTrackingSql);
      log.info(insertToLookupTrackingSql);
      statementInsertToLookup.execute();
      log.info("Inserted experiment info and who from events into lookup tracking table");
    } catch (SQLException sqle) {
      log.warning("SQLException while inserting to lookup tracking" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while inserting to lookup tracking" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementInsertToLookup != null) {
          statementInsertToLookup.close();
        }
        if (conn != null) {
        conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;
  }
  
  @Override
  public boolean anonymizeParticipantsUpdateEventWhoAndLookupIdByTracking() throws SQLException {
    final String updateValueForLookupid1 = "update `pacodb`.`" + EventBaseColumns.TABLE_NAME  +"` set "+ EventServerColumns.EXPERIMENT_LOOKUP_ID + " = ?, " + EventServerColumns.WHO + " = ? where " + EventServerColumns.EXPERIMENT_ID + " = ? and " + EventServerColumns.EXPERIMENT_NAME + " = ? and " + EventServerColumns.GROUP_NAME + " = ? and " + EventServerColumns.EXPERIMENT_VERSION + " = ? and " + EventServerColumns.WHO + " = ?" ;
    final String updateValueForLookupid2 = "update `pacodb`.`" + EventBaseColumns.TABLE_NAME  +"` set "+ EventServerColumns.EXPERIMENT_LOOKUP_ID + " = ?, " + EventServerColumns.WHO + " = ? where " + EventServerColumns.EXPERIMENT_ID + " = ? and " + EventServerColumns.EXPERIMENT_NAME + " = ? and " + EventServerColumns.GROUP_NAME + " is null and " + EventServerColumns.EXPERIMENT_VERSION + " = ? and " + EventServerColumns.WHO + " = ?" ;
    
    Connection conn = null;
    PreparedStatement statementUpdateEventValues = null;
    ExperimentLookupTracking expTracking = null;
    List<ExperimentLookupTracking> expTrackingWithNullGroupNames = null;
    List<ExperimentLookupTracking> expTrackingWithNotNullGroupNames = null;
    while (true) {
      try {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        log.info("Fetch next 20 records from tracking");
        List<ExperimentLookupTracking> toBeUpdated = getRecordsFromLookupTracking(20, 'N');
        if (toBeUpdated.isEmpty()) {
          log.info("List is empty, so break from the job");
          break;
        }
        Iterator<ExperimentLookupTracking> itr = toBeUpdated.iterator();
        expTrackingWithNullGroupNames = Lists.newArrayList();
        expTrackingWithNotNullGroupNames = Lists.newArrayList();
        
        while (itr.hasNext()) {
          expTracking = itr.next(); 
          if (expTracking.getGroupName() != null) {
            expTrackingWithNotNullGroupNames.add(expTracking);
          } else {
            expTrackingWithNullGroupNames.add(expTracking);
          }
          log.info("expTrackingid"+ expTracking.getTrackingId() + "-->" + expTrackingWithNullGroupNames.size() + "--" + expTrackingWithNotNullGroupNames.size());
        }
        
        if (expTrackingWithNotNullGroupNames.size() > 0) {
          log.info("In not null list");
          statementUpdateEventValues  = conn.prepareStatement(updateValueForLookupid1);
          updateEventTable(expTrackingWithNotNullGroupNames, statementUpdateEventValues);
        }
        if(expTrackingWithNullGroupNames.size() > 0) {
          log.info("In null list");
          statementUpdateEventValues  = conn.prepareStatement(updateValueForLookupid2);
          updateEventTable(expTrackingWithNullGroupNames, statementUpdateEventValues);
        }
        log.info("Finished updating 20 records from tracking");
      } catch (SQLException sqle) {
        anonymizeParticipantsUpdateLookupTracking(expTracking, 'F');
        log.warning("SQLException while updating values" + ExceptionUtil.getStackTraceAsString(sqle));
      } catch (Exception e) {
        anonymizeParticipantsUpdateLookupTracking(expTracking, 'F');
        log.warning("GException while updating values" + ExceptionUtil.getStackTraceAsString(e));
      } finally {
        try {
          if (statementUpdateEventValues != null) {
            statementUpdateEventValues.close();
          }
          if (conn != null) {
          conn.close();
          }
        } catch (SQLException e) {
          log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
        }
      }
      log.info("Updated look up columns with values for all records in event table");
    }
    return true;
  }
  
  private void updateEventTable(List<ExperimentLookupTracking> expTrackingLst, PreparedStatement statementUpdateValuesCol) throws SQLException {
    ExperimentLookupTracking expTracking = null;
    Iterator<ExperimentLookupTracking> itr = expTrackingLst.iterator();
    CloudSQLDaoImpl daoImpl = new CloudSQLDaoImpl();
    Long totalRecordsUpdated = 0L;
    while (itr.hasNext()) {
      expTracking = itr.next();
      log.info("currently updating expid: " + expTracking.getExperimentId() + " expName:" + expTracking.getExperimentName() + " expVersion:" + expTracking.getExperimentVersion()  + " who:"+ expTracking.getWho()+ " group name:" + expTracking.getGroupName());
      PacoId lookupId = daoImpl.getExperimentLookupIdAndCreate(expTracking.getExperimentId(), expTracking.getExperimentName(), expTracking.getGroupName(), expTracking.getExperimentVersion(), true);
      log.info("Look up id is " + lookupId.getId());
      PacoId anonId = daoImpl.getAnonymousIdAndCreate(expTracking.getExperimentId(), expTracking.getWho(), true);
      log.info("Anon id for " + expTracking.getWho() + " is " + anonId.getId());
     
      int ctr = 1;
      statementUpdateValuesCol.setInt(ctr++, lookupId.getId().intValue());
      statementUpdateValuesCol.setString(ctr++, anonId.getId().toString());
      statementUpdateValuesCol.setLong(ctr++, expTracking.getExperimentId());
      statementUpdateValuesCol.setString(ctr++, expTracking.getExperimentName());
      if (expTracking.getGroupName() != null) {
        statementUpdateValuesCol.setString(ctr++, expTracking.getGroupName());
      }
      statementUpdateValuesCol.setInt(ctr++, expTracking.getExperimentVersion());
      statementUpdateValuesCol.setString(ctr++, expTracking.getWho());
      log.info("update stmt:"+ statementUpdateValuesCol.toString());
      int crQueryUpdatedCount = statementUpdateValuesCol.executeUpdate();
      totalRecordsUpdated += crQueryUpdatedCount;
      anonymizeParticipantsUpdateLookupTracking(expTracking, 'Y');
      log.info("current update ended. Records updated in this batch: "+ crQueryUpdatedCount + " Total Number of records updated: "+ totalRecordsUpdated);
    }
    if (statementUpdateValuesCol != null) {
      statementUpdateValuesCol.close();
    }
  }

  private boolean anonymizeParticipantsUpdateLookupTracking(ExperimentLookupTracking tracking, Character result) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    String updateQuery = "update experiment_lookup_tracking set updated_events =? where tracking_id= ?";
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
     
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      statementUpdateEvent.setString(1, result.toString());
      statementUpdateEvent.setInt(2, tracking.getTrackingId());
      statementUpdateEvent.executeUpdate();
      log.info("update " + tracking.getTrackingId() + "  as " +result );
    } finally {
      try {
        if (statementUpdateEvent != null) {
          statementUpdateEvent.close();
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
  
  private List<PartialEvent> getEventRecordsWithNoLookupId(int limit) throws SQLException {
    Connection conn = null;
    List<PartialEvent> updateList = Lists.newArrayList();
    ResultSet rs = null;
    String query = "select * from events where experiment_lookup_id is null order by _id limit "+ limit;
   
    PreparedStatement statementGetFromEvent = null;
    
    Long eventId = null;
    Long expId = null;
    String expName = null;
    String groupName = null;
    Integer expVersion = null;
    String who = null;

    Long selectRecordsCt = 0L;
    PartialEvent lookupObj = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
  
      CloudSQLDaoImpl daoImpl = new CloudSQLDaoImpl();
      statementGetFromEvent = conn.prepareStatement(query);
      rs = statementGetFromEvent.executeQuery();
      while (rs.next()) {
        lookupObj = new PartialEvent();
        eventId = rs.getLong(Constants.UNDERSCORE_ID);
        log.info("processing: record no:"+ (selectRecordsCt++) + " with event id :"+eventId);
        lookupObj.setEventId(eventId);
        expId = rs.getLong(EventServerColumns.EXPERIMENT_ID);
        expName = rs.getString(EventServerColumns.EXPERIMENT_NAME);
        expVersion = rs.getInt(EventServerColumns.EXPERIMENT_VERSION);
        who = rs.getString(EventServerColumns.WHO);
        // find lookup id and anon id
        PacoId lookupId = daoImpl.getExperimentLookupIdAndCreate(expId, expName, groupName, expVersion, true);
        log.info("Look up id is " + lookupId);
        lookupObj.setLookupId(lookupId.getId().intValue());
        // find anon id
        PacoId anonId = daoImpl.getAnonymousIdAndCreate(expId, who, true);
        log.info("Anon id for " + who + " is " + anonId);
        lookupObj.setAnonId(anonId.getId().intValue());
        updateList.add(lookupObj);
      }
      log.info("get size :"+ updateList.size());
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementGetFromEvent != null) {
          statementGetFromEvent.close();
        }
      
        if (conn != null) {
          conn.close();
        }
      
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }
    return updateList;    
  }
  
  @Override
  public boolean anonymizeParticipantsUpdateEventWhoAndLookupIdSerially() throws SQLException {
    long ctr = 0;
    int batchSize = 1000;
    while (true) {
     log.info("Looping iteration count: " + ctr++);
     List<PartialEvent> toBeUpdatedLst = getEventRecordsWithNoLookupId(batchSize);
     if (toBeUpdatedLst.isEmpty()) {
       break;
     }
     updateEventWhoAndLookupByBatch(toBeUpdatedLst);
    }
    log.info("All event records updated with Lookup id and anonymous id");
 
    return true;
  }
  
  private boolean updateEventWhoAndLookupByBatch(List<PartialEvent> partialEventLst) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    PartialEvent singleLookup = null;
    String updateQuery = "update events set experiment_lookup_id= ?, who =? where _id= ?";
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
      statementUpdateEvent = conn.prepareStatement(updateQuery);
      Iterator<PartialEvent> itr = partialEventLst.iterator();
      // add update qry to batch
      while (itr.hasNext()) {
        singleLookup = itr.next();
        statementUpdateEvent.setInt(1, singleLookup.getLookupId());
        statementUpdateEvent.setString(2, singleLookup.getAnonId()+"");
        statementUpdateEvent.setLong(3, singleLookup.getEventId());
        statementUpdateEvent.addBatch();
      }
      int[] update = statementUpdateEvent.executeBatch();
      log.info("updated records: " + update.length );
    } finally {
      try {
        if (statementUpdateEvent != null) {
          statementUpdateEvent.close();
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
  
  private List<ExperimentLookupTracking> getRecordsFromLookupTracking(int noOfRecords, Character status) throws SQLException {
    Connection conn = null;
    ResultSet rs = null;
    String query = "select * from experiment_lookup_tracking where experiment_version is not null and updated_events='"+ status+"' order by tracking_id asc limit "+ noOfRecords;
    List<ExperimentLookupTracking> recList = Lists.newArrayList();
    ExperimentLookupTracking lookupTrackingObj = null;
    PreparedStatement statementGetFromExpTracking = null;
      try {
        conn = CloudSQLConnectionManager.getInstance().getConnection();
        setNames(conn);
        statementGetFromExpTracking = conn.prepareStatement(query);
        log.info(statementGetFromExpTracking.toString());
        rs = statementGetFromExpTracking.executeQuery();
        while (rs.next()){
          lookupTrackingObj = new ExperimentLookupTracking();
          lookupTrackingObj.setExperimentId(rs.getLong("experiment_id"));
          lookupTrackingObj.setExperimentName(rs.getString("experiment_name"));
          lookupTrackingObj.setExperimentVersion(rs.getInt("experiment_version"));
          lookupTrackingObj.setGroupName(rs.getString("group_name"));
          lookupTrackingObj.setWho(rs.getString("who"));
          lookupTrackingObj.setTrackingId(rs.getInt("tracking_id"));
          recList.add(lookupTrackingObj);
        }
      } finally {
        try {
          if ( rs != null) {
            rs.close();
          }
          if (statementGetFromExpTracking != null) {
            statementGetFromExpTracking.close();
          }
          if (conn != null) {
            conn.close();
          }
        } catch (SQLException ex1) {
          log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
        }
      }
   
    return recList;
  }

  @Override
  public boolean anonymizeParticipantsTakeBackupEventIdWho() throws SQLException{
    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement statementBackup = null;
    String createQuery = "CREATE TABLE IF NOT EXISTS idwho_bk SELECT _id, who from events";
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
     
      statementBackup = conn.prepareStatement(createQuery);
      statementBackup.execute();
      log.info("backup created" );
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementBackup != null) {
          statementBackup.close();
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
  public boolean anonymizeParticipantsRenameOldEventColumns() throws SQLException{
    Connection conn = null;
    PreparedStatement statementRename = null;
    String createQuery = "ALTER TABLE `pacodb`.`events` " +
            " CHANGE COLUMN `experiment_id` `experiment_id_old` BIGINT(20) NULL DEFAULT NULL ," +
            " CHANGE COLUMN `experiment_name` `experiment_name_old` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL , " + 
            " CHANGE COLUMN `experiment_version` `experiment_version_old` INT(11) NULL DEFAULT NULL , " +
            " CHANGE COLUMN `group_name` `group_name_old` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL";
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      setNames(conn);
     
      statementRename = conn.prepareStatement(createQuery);
      statementRename.execute();
      log.info("backup created" );
    } finally {
      try {
        if (statementRename != null) {
          statementRename.close();
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
