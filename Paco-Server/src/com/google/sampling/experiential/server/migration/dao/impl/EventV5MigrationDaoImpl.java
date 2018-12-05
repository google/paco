package com.google.sampling.experiential.server.migration.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.dao.EventV5MigrationDao;
import com.pacoapp.paco.shared.model2.EventBaseColumns;

public class EventV5MigrationDaoImpl implements EventV5MigrationDao {
  public static final Logger log = Logger.getLogger(EventV5MigrationDaoImpl.class.getName());
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

 }