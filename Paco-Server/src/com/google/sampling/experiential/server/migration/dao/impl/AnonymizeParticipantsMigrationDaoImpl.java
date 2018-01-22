package com.google.sampling.experiential.server.migration.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentLookupColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentUserColumns;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.google.sampling.experiential.dao.CSExperimentLookupDao;
import com.google.sampling.experiential.dao.CSExperimentUserDao;
import com.google.sampling.experiential.dao.dataaccess.ExperimentLookupTracking;
import com.google.sampling.experiential.dao.dataaccess.PartialEvent;
import com.google.sampling.experiential.dao.impl.CSExperimentLookupDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentUserDaoImpl;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.ExperimentService;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.migration.dao.AnonymizeParticipantsMigrationDao;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class AnonymizeParticipantsMigrationDaoImpl implements AnonymizeParticipantsMigrationDao {
  public static final Logger log = Logger.getLogger(AnonymizeParticipantsMigrationDaoImpl.class.getName());
  
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
    CSExperimentUserDao daoImpl = new CSExperimentUserDaoImpl();
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
    CSExperimentUserDao exptUserDaoImpl = new CSExperimentUserDaoImpl();
    CSExperimentLookupDao exptLookupDaoImpl = new CSExperimentLookupDaoImpl();
    Long totalRecordsUpdated = 0L;
    while (itr.hasNext()) {
      expTracking = itr.next();
      log.info("currently updating expid: " + expTracking.getExperimentId() + " expName:" + expTracking.getExperimentName() + " expVersion:" + expTracking.getExperimentVersion()  + " who:"+ expTracking.getWho()+ " group name:" + expTracking.getGroupName());
      PacoId lookupId = exptLookupDaoImpl.getExperimentLookupIdAndCreate(expTracking.getExperimentId(), expTracking.getExperimentName(), expTracking.getGroupName(), expTracking.getExperimentVersion(), true);
      log.info("Look up id is " + lookupId.getId());
      PacoId anonId = exptUserDaoImpl.getAnonymousIdAndCreate(expTracking.getExperimentId(), expTracking.getWho(), true);
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
      CSExperimentUserDao exptUserDaoImpl = new CSExperimentUserDaoImpl();
      CSExperimentLookupDao exptLookupDaoImpl = new CSExperimentLookupDaoImpl();
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
        PacoId lookupId = exptLookupDaoImpl.getExperimentLookupIdAndCreate(expId, expName, groupName, expVersion, true);
        log.info("Look up id is " + lookupId);
        lookupObj.setLookupId(lookupId.getId().intValue());
        // find anon id
        PacoId anonId = exptUserDaoImpl.getAnonymousIdAndCreate(expId, who, true);
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
