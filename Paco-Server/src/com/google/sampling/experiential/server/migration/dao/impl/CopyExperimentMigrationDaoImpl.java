package com.google.sampling.experiential.server.migration.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.google.sampling.experiential.cloudsql.columns.ChoiceCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupColumns;
import com.google.sampling.experiential.cloudsql.columns.InformedConsentColumns;
import com.google.sampling.experiential.cloudsql.columns.InputCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.migration.dao.CopyExperimentMigrationDao;

public class CopyExperimentMigrationDaoImpl implements CopyExperimentMigrationDao {
  public static final Logger log = Logger.getLogger(CopyExperimentMigrationDaoImpl.class.getName());
  
  @Override
  public boolean copyExperimentCreateTables() throws SQLException {
    boolean isComplete = false;
    String[] qry = new String[8];
    final String createTableSql1 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ DataTypeColumns.TABLE_NAME+"` (" +
            DataTypeColumns.DATA_TYPE_ID + " INT NOT NULL AUTO_INCREMENT," +
            DataTypeColumns.NAME + " VARCHAR(100) NOT NULL," +
            DataTypeColumns.IS_NUMERIC + " BIT(1) DEFAULT NULL," +
            DataTypeColumns.MULTI_SELECT + "  BIT(1) DEFAULT NULL," +
            DataTypeColumns.RESPONSE_MAPPING_REQUIRED + " BIT(1) DEFAULT NULL," +
            " PRIMARY KEY (`"+ DataTypeColumns.DATA_TYPE_ID +"`)," +
            " UNIQUE KEY `name_type_UNIQUE` (`" + DataTypeColumns.NAME + "`, `" + DataTypeColumns.IS_NUMERIC + "`,`"
                    + DataTypeColumns.MULTI_SELECT + "`,`" + DataTypeColumns.RESPONSE_MAPPING_REQUIRED + "`)) " +
            " DEFAULT CHARACTER SET = utf8mb4" ; 
    final String createTableSql2 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExternStringListLabelColumns.TABLE_NAME+"` (" +
            ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
            ExternStringListLabelColumns.LABEL + " VARCHAR(500) NOT NULL," +
            " PRIMARY KEY (`"+ ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID +"`)," +
            " UNIQUE KEY `type_UNIQUE` (`" + ExternStringListLabelColumns.LABEL + "`))" +
            " DEFAULT CHARACTER SET = utf8mb4" ;
    final String createTableSql3 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExternStringInputColumns.TABLE_NAME+"` (" +
            ExternStringInputColumns.EXTERN_STRING_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
            ExternStringInputColumns.LABEL + " VARCHAR(500) NOT NULL," +
            " PRIMARY KEY (`"+ ExternStringInputColumns.EXTERN_STRING_ID +"`)," +
            " UNIQUE KEY `type_UNIQUE` (`" + ExternStringInputColumns.LABEL + "`))" +
            " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql4 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ChoiceCollectionColumns.TABLE_NAME+"` (" +
            ChoiceCollectionColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
            ChoiceCollectionColumns.CHOICE_COLLECTION_ID + " BIGINT(20) NOT NULL," +
            ChoiceCollectionColumns.CHOICE_ID + " BIGINT(20) NOT NULL," +
            ChoiceCollectionColumns.CHOICE_ORDER + " INT(11) NOT NULL," +
            " PRIMARY KEY (`" + ChoiceCollectionColumns.EXPERIMENT_ID +"`,`" + ChoiceCollectionColumns.CHOICE_COLLECTION_ID + 
            "`,`" + ChoiceCollectionColumns.CHOICE_ID + "`)," +
            " INDEX `extern_string_fk` (`" + ChoiceCollectionColumns.CHOICE_ID + "` ASC) ," +
            " CONSTRAINT `extern_string_constraint` FOREIGN KEY (`" + ChoiceCollectionColumns.CHOICE_ID+ "`)" +
            " REFERENCES `pacodb`.`" + ExternStringListLabelColumns.TABLE_NAME + "` (" + ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID + "))" + 
            " DEFAULT CHARACTER SET = utf8mb4" ;
     
     final String createTableSql5 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExperimentColumns.TABLE_NAME+"` (" +
             ExperimentColumns.EXPERIMENT_FACET_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
             ExperimentColumns.TITLE + " VARCHAR(500) NOT NULL," +
             ExperimentColumns.DESCRIPTION + " VARCHAR(500) NULL," +
             ExperimentColumns.CREATOR + " BIGINT(20) NOT NULL," +
             ExperimentColumns.CONTACT_EMAIL + " VARCHAR(200) NULL," +
             ExperimentColumns.ORGANIZATION + " VARCHAR(200)  NULL," +
             ExperimentColumns.INFORMED_CONSENT_ID + " BIGINT(20) NULL DEFAULT NULL," +
             ExperimentColumns.MODIFIED_DATE + " datetime NULL," +
             ExperimentColumns.PUBLISHED + " BIT(1) NULL DEFAULT 0," +
             ExperimentColumns.RINGTONE_URI + "  VARCHAR(200) NULL," +
             ExperimentColumns.POST_INSTALL_INSTRUCTIONS + " VARCHAR(500) NULL," +
             ExperimentColumns.DELETED + " bit(1) NULL DEFAULT 0," +
             " PRIMARY KEY (`"+ ExperimentColumns.EXPERIMENT_FACET_ID +"`))" +
             " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql6 = "CREATE TABLE IF NOT EXISTS `pacodb`."+ GroupColumns.TABLE_NAME+" (" +
             GroupColumns.GROUP_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
             GroupColumns.NAME + " VARCHAR(500) NOT NULL," +
             GroupColumns.CUSTOM_RENDERING + " VARCHAR(500) NULL," +
             GroupColumns.END_OF_DAY_GROUP + " VARCHAR(500) NULL DEFAULT NULL," +
             GroupColumns.FIXED_DURATION + " BIT(1) NULL," +
             GroupColumns.START_DATE + " datetime NULL," +
             GroupColumns.END_DATE + " datetime NULL," +
             GroupColumns.RAW_DATA_ACCESS + " BIT(1) NULL," +
             " PRIMARY KEY (`"+ GroupColumns.GROUP_ID +"`))" +
             " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql7 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ InputColumns.TABLE_NAME+"` (" +
             InputColumns.INPUT_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
             InputColumns.NAME_ID + " BIGINT(20) NOT NULL," +
             InputColumns.TEXT_ID + " BIGINT(20) NOT NULL," +
             InputColumns.REQUIRED + " BIT(1) NULL DEFAULT 0," +
             InputColumns.CONDITIONAL + " VARCHAR(200) NULL," +
             InputColumns.CHANNEL + " VARCHAR(20) NULL," +
             InputColumns.RESPONSE_TYPE_ID + " INT(11) NULL," +
             InputColumns.LIKERT_STEPS + " TINYINT(4) NULL," +
             InputColumns.LEFT_LABEL + " VARCHAR(100) NULL," +
             InputColumns.RIGHT_LABEL + " VARCHAR(100) NULL," +
             " PRIMARY KEY (`"+ InputColumns.INPUT_ID +"`)," +
             " INDEX `name_extern_string_fk_idx` (`" + InputColumns.NAME_ID  + "` ASC)," + 
             " INDEX `text_extern_string_fk_idx` (`" + InputColumns.TEXT_ID + "` ASC)," + 
             " INDEX `response_type_fk_idx` (`" + InputColumns.RESPONSE_TYPE_ID + "` ASC)," + 
             " CONSTRAINT `ih_es_name_fk` FOREIGN KEY (`" + InputColumns.NAME_ID + "`) REFERENCES `" + 
                 ExternStringInputColumns.TABLE_NAME +"`(`" + ExternStringInputColumns.EXTERN_STRING_ID + "`)  ON DELETE NO ACTION ON UPDATE NO ACTION," + 
             " CONSTRAINT `ih_es_text_fk` FOREIGN KEY (`" + InputColumns.TEXT_ID + "`) REFERENCES `" + 
                 ExternStringInputColumns.TABLE_NAME +"`(`" + ExternStringInputColumns.EXTERN_STRING_ID + "`)  ON DELETE NO ACTION ON UPDATE NO ACTION," +  
             " CONSTRAINT `ih_es_response_type_fk` FOREIGN KEY (`" + InputColumns.RESPONSE_TYPE_ID + "`) REFERENCES `" + 
                 DataTypeColumns.TABLE_NAME +"`(`" + DataTypeColumns.DATA_TYPE_ID + "`)  ON DELETE NO ACTION ON UPDATE NO ACTION)"  +
             " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql8 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ InputCollectionColumns.TABLE_NAME+"` (" +
             InputCollectionColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
             InputCollectionColumns.INPUT_COLLECTION_ID + " BIGINT(20) NOT NULL," +
             InputCollectionColumns.INPUT_ID + " BIGINT(20) NOT NULL," +
             InputCollectionColumns.CHOICE_COLLECTION_ID + " BIGINT(20) NULL," +
             InputCollectionColumns.INPUT_ORDER + " INT(11) NOT NULL," +
             " PRIMARY KEY (`"+ InputCollectionColumns.INPUT_COLLECTION_ID +"`,`" +
                     InputCollectionColumns.EXPERIMENT_ID +"`,`" +
                     InputCollectionColumns.INPUT_ID +"`)) " +
           " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql9 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExperimentVersionMappingColumns.TABLE_NAME+"` (" +
             ExperimentVersionMappingColumns.EXPERIMENT_VERSION_MAPPING_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
             ExperimentVersionMappingColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
             ExperimentVersionMappingColumns.EXPERIMENT_VERSION + " INT(11) NOT NULL," +
             ExperimentVersionMappingColumns.EXPERIMENT_FACET_ID + " BIGINT(20) NOT NULL," +
             ExperimentVersionMappingColumns.GROUP_ID + " BIGINT(20) NOT NULL," +
             ExperimentVersionMappingColumns.INPUT_COLLECTION_ID + " BIGINT(20) NULL," +
             ExperimentVersionMappingColumns.EVENTS_POSTED + " BIT(1) DEFAULT 0," +
             " PRIMARY KEY (`" + ExperimentVersionMappingColumns.EXPERIMENT_VERSION_MAPPING_ID + "`)," +  
             " UNIQUE KEY `experiment_id_version_group_unique` (`" + ExperimentVersionMappingColumns.EXPERIMENT_ID + "`,"
                     + "`" + ExperimentVersionMappingColumns.EXPERIMENT_VERSION + "`,"
                     + "`" + ExperimentVersionMappingColumns.GROUP_ID + "`)," +
              "KEY `experiment_history_fk_idx` (`" + ExperimentVersionMappingColumns.EXPERIMENT_FACET_ID + "`)," +
              "KEY `group_history_fk_idx` (`" + ExperimentVersionMappingColumns.GROUP_ID + "`)," +
              " CONSTRAINT `experiment_history_fk` FOREIGN KEY (`" + ExperimentVersionMappingColumns.EXPERIMENT_FACET_ID + "`) REFERENCES `" +
                     ExperimentColumns.TABLE_NAME + "` (`" + ExperimentColumns.EXPERIMENT_FACET_ID + "`) ON DELETE NO ACTION ON UPDATE NO ACTION," +
              " CONSTRAINT `group_history_fk` FOREIGN KEY (`" + ExperimentVersionMappingColumns.GROUP_ID + "`) REFERENCES " +
                     GroupColumns.TABLE_NAME + " (`" + GroupColumns.GROUP_ID + "`) ON DELETE NO ACTION ON UPDATE NO ACTION)" +
              " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql10 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ InformedConsentColumns.TABLE_NAME+"` (" +
             InformedConsentColumns.INFORMED_CONSENT_ID + " BIGINT(20) NOT NULL," +
             InformedConsentColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
             InformedConsentColumns.INFORMED_CONSENT + " VARCHAR(1000) NOT NULL," +
             " PRIMARY KEY (`" + InformedConsentColumns.INFORMED_CONSENT_ID + "`, `"+ InformedConsentColumns.EXPERIMENT_ID +"`)) " +  
             " DEFAULT CHARACTER SET = utf8mb4" ;
     
     
     
    qry = new String[] { createTableSql1, createTableSql2, createTableSql3, createTableSql4, createTableSql5, 
                         createTableSql6, createTableSql7, createTableSql8, createTableSql9, createTableSql10};
    
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
  public boolean addDataTypes() throws SQLException {
    
    final String insertDataTypeSql1 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('open text', 1, 0, 0)";
    final String insertDataTypeSql2 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('open text', 0, 0, 0)";
    final String insertDataTypeSql3 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('list', 1, 0, 0)";
    final String insertDataTypeSql4 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('list', 1, 1, 0)";
    final String insertDataTypeSql5 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('likert', 1, 0, 0)";
    final String insertDataTypeSql6 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('open text', 0, 0, 0)";
    final String insertDataTypeSql7 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('number', 1, 0, 0)";
    final String insertDataTypeSql8 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('likert_smileys', 0, 0, 0)";
        
    String[] qry = new String[] { 
                                  insertDataTypeSql1, insertDataTypeSql2 ,
                                  insertDataTypeSql3, insertDataTypeSql4,
                                  insertDataTypeSql5, insertDataTypeSql6,
                                  insertDataTypeSql7, insertDataTypeSql8
                                  };
    
    Connection conn = null;
    PreparedStatement statementModifyExisting = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for ( int i = 0; i < qry.length; i++) {
        statementModifyExisting = conn.prepareStatement(qry[i]);
        log.info(qry[i]);
        statementModifyExisting.execute();
      }
    } catch (SQLException sqle) {
      log.warning("SQLException while adding new cols" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new cols" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementModifyExisting != null) {
          statementModifyExisting.close();
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
  public boolean addModificationsToExistingTables()  throws SQLException {
    final String addNewColumnsSql1 = "ALTER TABLE `pacodb`.`outputs` " +
            " ADD COLUMN `text_id` BIGINT(20) NULL AFTER `archive_flag`," +
            " ADD INDEX `fk_text_input_id_idx` (`text_id` ASC)";
    final String addNewColumnsSql2 = "ALTER TABLE `pacodb`.`outputs`  " + 
                  " ADD CONSTRAINT `fk_text_input_id` " +  
                  "   FOREIGN KEY (`text_id`) " + 
                  "  REFERENCES `pacodb`.`input` (`input_id`) "  +
                  " ON DELETE NO ACTION " + 
                  "  ON UPDATE NO ACTION ";
    final String addNewColumnsSql3 = "ALTER TABLE `pacodb`.`events`  " + 
    " ADD COLUMN `experiment_version_mapping_id` BIGINT(20) NULL AFTER `experiment_lookup_id`, " + 
    " ADD INDEX `fk_exp_version_mapping_idx` (`experiment_version_mapping_id` ASC)";
    final String addNewColumnsSql4 = "ALTER TABLE `pacodb`.`events`  " +
    " ADD CONSTRAINT `fk_exp_version_mapping` " +
    " FOREIGN KEY (`experiment_version_mapping_id`) " + 
    " REFERENCES `pacodb`.`experiment_version_mapping` (`experiment_version_mapping_id`) " + 
    " ON DELETE NO ACTION " +
    " ON UPDATE NO ACTION ";
    final String addNewColumnsSql5 = "ALTER TABLE `pacodb`.`input_collection` " + 
    " ADD INDEX `ic_input_id_fk_idx` (`input_id` ASC)";
    final String addNewColumnsSql6 = "ALTER TABLE `pacodb`.`input_collection`" +
    " ADD CONSTRAINT `ic_input_id_fk` " +
    " FOREIGN KEY (`input_id`) " + 
    " REFERENCES `pacodb`.`input` (`input_id`) " + 
    " ON DELETE NO ACTION " +
    " ON UPDATE NO ACTION ";
    final String addNewColumnsSql7 = "ALTER TABLE `pacodb`.`experiment_user` " + 
    " ADD INDEX `eu_u_userid_fk_idx` (`user_id` ASC)";
    final String addNewColumnsSql8 = "ALTER TABLE `pacodb`.`experiment_user`" +
    " ADD CONSTRAINT `eu_u_userid_fk` " +
    " FOREIGN KEY (`user_id`) " + 
    " REFERENCES `pacodb`.`user` (`user_id`) " + 
    " ON DELETE NO ACTION " +
    " ON UPDATE NO ACTION ";
    final String addNewColumnsSql9 = "ALTER TABLE `pacodb`.`experiment` " + 
    " ADD INDEX `e_ic_informed_consent_fk_idx` (`informed_consent_id` ASC)";
    final String addNewColumnsSql10 = "ALTER TABLE `pacodb`.`experiment`" +
    " ADD CONSTRAINT `e_ic_informed_consent_fk` " +
    " FOREIGN KEY (`informed_consent_id`) " + 
    " REFERENCES `pacodb`.`informed_consent` (`informed_consent_id`) " + 
    " ON DELETE NO ACTION " +
    " ON UPDATE NO ACTION ";
    
    
    String[] qry = new String[] { 
              addNewColumnsSql1, addNewColumnsSql2 ,
              addNewColumnsSql3, addNewColumnsSql4,
              addNewColumnsSql5, addNewColumnsSql6,
              addNewColumnsSql7, addNewColumnsSql8,
              addNewColumnsSql9, addNewColumnsSql10,
              };
    Connection conn = null;
    PreparedStatement statementModifyExisting = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for ( int i = 0; i < qry.length; i++) {
        statementModifyExisting = conn.prepareStatement(qry[i]);
        log.info(qry[i]);
        statementModifyExisting.execute();
      }
    } catch (SQLException sqle) {
      log.warning("SQLException while adding new cols" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new cols" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (statementModifyExisting != null) {
          statementModifyExisting.close();
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
