package com.google.sampling.experiential.server.migration.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.cloudsql.columns.ChoiceCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDefinitionColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentUserColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeInputMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.InformedConsentColumns;
import com.google.sampling.experiential.cloudsql.columns.InputCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.cloudsql.columns.OutputServerColumns;
import com.google.sampling.experiential.cloudsql.columns.PivotHelperColumns;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.google.sampling.experiential.dao.CSExperimentDefinitionDao;
import com.google.sampling.experiential.dao.CSExperimentUserDao;
import com.google.sampling.experiential.dao.CSExperimentVersionMappingDao;
import com.google.sampling.experiential.dao.CSFailedEventDao;
import com.google.sampling.experiential.dao.CSGroupTypeDao;
import com.google.sampling.experiential.dao.CSGroupTypeInputMappingDao;
import com.google.sampling.experiential.dao.CSInputCollectionDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.dao.CSPivotHelperDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.GroupTypeInputMapping;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputCollection;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.dao.dataaccess.PivotHelper;
import com.google.sampling.experiential.dao.impl.CSExperimentDefinitionDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentUserDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentVersionMappingDaoImpl;
import com.google.sampling.experiential.dao.impl.CSFailedEventDaoImpl;
import com.google.sampling.experiential.dao.impl.CSGroupTypeDaoImpl;
import com.google.sampling.experiential.dao.impl.CSGroupTypeInputMappingDaoImpl;
import com.google.sampling.experiential.dao.impl.CSInputCollectionDaoImpl;
import com.google.sampling.experiential.dao.impl.CSInputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSOutputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSPivotHelperDaoImpl;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.ExperimentDAOConverter;
import com.google.sampling.experiential.server.ExperimentService;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.migration.dao.CopyExperimentMigrationDao;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentGroup;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.model2.Input2;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

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
             ExperimentColumns.EXPERIMENT_NAME + " VARCHAR(500) NOT NULL," +
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
             GroupColumns.GROUP_TYPE_ID + " INT(11) NULL DEFAULT NULL, " +
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
             InputColumns.RESPONSE_TYPE_ID + " INT(11) NULL," +
             InputColumns.LIKERT_STEPS + " TINYINT(4) NULL," +
             InputColumns.LEFT_LABEL + " VARCHAR(100) NULL," +
             InputColumns.RIGHT_LABEL + " VARCHAR(100) NULL," +
             InputColumns.PARENT_ID + " BIGINT(20) NULL," + 
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
             ExperimentVersionMappingColumns.SOURCE + " VARCHAR(100) NULL, " +
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
     final String createTableSql11 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ GroupTypeColumns.TABLE_NAME+"` (" +
             GroupTypeColumns.GROUP_TYPE_ID + " INT NOT NULL AUTO_INCREMENT," +
             GroupTypeColumns.GROUP_TYPE_NAME + " VARCHAR(250) NOT NULL," +
             " PRIMARY KEY (`" + GroupTypeColumns.GROUP_TYPE_ID + "`)) " +  
             " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql12 = "CREATE TABLE IF NOT EXISTS `pacodb`.`" + GroupTypeInputMappingColumns.TABLE_NAME  + "` ( " + 
             GroupTypeInputMappingColumns.GROUP_TYPE_INPUT_MAPPING_ID + " int(11) NOT NULL AUTO_INCREMENT, " + 
             GroupTypeInputMappingColumns.GROUP_TYPE_ID + " varchar(45) DEFAULT NULL, " + 
             GroupTypeInputMappingColumns.INPUT_ID + " bigint(20) DEFAULT NULL, " +
             "PRIMARY KEY (`"+ GroupTypeInputMappingColumns.GROUP_TYPE_INPUT_MAPPING_ID +"`), " + 
             " UNIQUE KEY `groupt_type_id_input_id_UNIQUE` (`" + GroupTypeInputMappingColumns.GROUP_TYPE_ID + "`, `"+GroupTypeInputMappingColumns.INPUT_ID+"`)) " +
             " ENGINE=InnoDB DEFAULT CHARSET = utf8mb4" ;
     final String createTableSql13 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ PivotHelperColumns.TABLE_NAME+"` (" +
             PivotHelperColumns.EXPERIMENT_VERSION_MAPPING_ID + " BIGINT(20) NOT NULL," +
             PivotHelperColumns.ANON_WHO + " INT NOT NULL," +
             PivotHelperColumns.INPUT_ID + " BIGINT(20) NOT NULL," +
             PivotHelperColumns.EVENTS_POSTED + " BIGINT(20) NOT NULL DEFAULT 0," +
             PivotHelperColumns.PROCESSED + " BIT(1) DEFAULT 0," +
             " PRIMARY KEY (`" + PivotHelperColumns.EXPERIMENT_VERSION_MAPPING_ID + "`, `"+ PivotHelperColumns.ANON_WHO +"`, `"+ PivotHelperColumns.INPUT_ID+"`)) " +  
             " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql14 = "CREATE TABLE IF NOT EXISTS `pacodb`.`" + ExperimentDefinitionColumns.TABLE_NAME + "` (" +
             " `" + ExperimentDefinitionColumns.ID + "` bigint(20) NOT NULL, " +
              " `" + ExperimentDefinitionColumns.VERSION + "` int(11) NOT NULL, " +
              " `" + ExperimentDefinitionColumns.SOURCE_JSON + "` json DEFAULT NULL, " +
             " PRIMARY KEY (`" + ExperimentDefinitionColumns.ID +"`,`" + ExperimentDefinitionColumns.VERSION + "`)) " +
            " DEFAULT CHARACTER SET = utf8mb4" ;
     
    qry = new String[] { createTableSql1, createTableSql2, createTableSql3, createTableSql4, createTableSql5, 
                         createTableSql6, createTableSql7, createTableSql8, createTableSql9, createTableSql10, 
                         createTableSql11, createTableSql12, createTableSql13, createTableSql14};
    
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
  public boolean anonymizeParticipantsCreateTables() throws SQLException {
    boolean isComplete = false;
    String[] qry = new String[2];
    final String createTableSql1 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ UserColumns.TABLE_NAME+"` (" +
            UserColumns.USER_ID + " INT NOT NULL AUTO_INCREMENT," +
            UserColumns.WHO + " VARCHAR(500) NOT NULL," +
            " PRIMARY KEY (`" + UserColumns.USER_ID + "`)," +
            " UNIQUE INDEX `who_unique_index` (`"+ UserColumns.WHO + "` ASC))";
    final String createTableSql2 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExperimentUserColumns.TABLE_NAME +"` (" +
            ExperimentUserColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
            ExperimentUserColumns.USER_ID + " INT NOT NULL," +
            ExperimentUserColumns.EXP_USER_ANON_ID + " INT NOT NULL," +
            ExperimentUserColumns.USER_TYPE + " CHAR(1) NOT NULL," +
            " PRIMARY KEY (`" + ExperimentUserColumns.EXPERIMENT_ID+ "`,`" +ExperimentUserColumns.USER_ID+ "`), "+ 
            " UNIQUE KEY `experiment_id_anon_id_UNIQUE` (`experiment_id`,`experiment_user_anon_id`))";

    qry = new String[] { createTableSql1, createTableSql2};
    
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
  public boolean insertPredefinedRecords() throws SQLException {
    
    final String insertDataTypeSql1 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('open text', 1, 0, 0)";
    final String insertDataTypeSql2 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('open text', 0, 0, 0)";
    final String insertDataTypeSql3 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('list', 1, 0, 1)";
    final String insertDataTypeSql4 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('list', 1, 1, 1)";
    final String insertDataTypeSql5 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('likert', 1, 0, 0)";
    final String insertDataTypeSql7 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('number', 1, 0, 0)";
    final String insertDataTypeSql8 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('likert_smileys', 0, 0, 0)";
    final String insertDataTypeSql9 = "INSERT INTO `pacodb`.`data_type` (`name`, `is_numeric`, `multi_select`, `response_mapping_required`) VALUES ('undefined', 0, 0, 0)";
        
    final String insertDataTypeSql10 = "INSERT INTO `pacodb`.`group_type` (`group_type_name`) VALUES ('system')";
    final String insertDataTypeSql11 = "INSERT INTO `pacodb`.`group_type` (`group_type_name`) VALUES ('survey')";
    final String insertDataTypeSql12 = "INSERT INTO `pacodb`.`group_type` (`group_type_name`) VALUES ('logAppUsage')";
    final String insertDataTypeSql13 = "INSERT INTO `pacodb`.`group_type` (`group_type_name`) VALUES ('logNotification')";
    final String insertDataTypeSql14 = "INSERT INTO `pacodb`.`group_type` (`group_type_name`) VALUES ('logAccessibility')";
    final String insertDataTypeSql15 = "INSERT INTO `pacodb`.`group_type` (`group_type_name`) VALUES ('phoneStatus')";
        

        
    String[] qry = new String[] { 
                                  insertDataTypeSql1, insertDataTypeSql2,
                                  insertDataTypeSql3, insertDataTypeSql4,
                                  insertDataTypeSql5,
                                  insertDataTypeSql7, insertDataTypeSql8,
                                  insertDataTypeSql9, insertDataTypeSql10,
                                  insertDataTypeSql11, insertDataTypeSql12,
                                  insertDataTypeSql13, insertDataTypeSql14
                                  ,insertDataTypeSql15
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
      // pop grp type input
      populateGroupTypeInput();
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
            " ADD COLUMN `input_id` BIGINT(20) NULL AFTER `archive_flag`," +
            " ADD INDEX `fk_text_input_id_idx` (`input_id` ASC)";
    final String addNewColumnsSql2 = "ALTER TABLE `pacodb`.`outputs`  " + 
                  " ADD CONSTRAINT `fk_text_input_id` " +  
                  "   FOREIGN KEY (`input_id`) " + 
                  "  REFERENCES `pacodb`.`input` (`input_id`) "  +
                  " ON DELETE NO ACTION " + 
                  "  ON UPDATE NO ACTION ";
    final String addNewColumnsSql3 = "ALTER TABLE `pacodb`.`events`  " + 
    " ADD COLUMN `experiment_version_mapping_id` BIGINT(20) NULL AFTER `sort_date`, " +
    " ADD COLUMN `who_bk` BIGINT(20) NULL AFTER `experiment_version_mapping_id`," +
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
  
  @Override
  public boolean copyExperimentMigrateFromDataStoreToCloudSql()  throws SQLException {
    CSExperimentVersionMappingDao evMappingDaoImpl = new CSExperimentVersionMappingDaoImpl();
    CSExperimentUserDao expUserDaoImpl = new CSExperimentUserDaoImpl();
    
    final ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();
    ExperimentQueryResult experimentsQueryResults = experimentService.getAllExperiments(null);
    List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();

    log.info("Retrieved " + experimentList.size() + "experiments");

    if (experimentList == null || experimentList.isEmpty()) {
      return false;
    }

    for (ExperimentDAO eachExperiment : experimentList) {
      try {
        List<String> adminLstInRequest = eachExperiment.getAdmins();
        List<String> partLstInRequest = eachExperiment.getPublishedUsers();
        expUserDaoImpl.ensureUserId(eachExperiment.getId(), Sets.newHashSet(adminLstInRequest), Sets.newHashSet(partLstInRequest));
        evMappingDaoImpl.updateExperimentVersionMapping(eachExperiment);
      } catch (Exception e) {
        log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    return true;
  }
  
  @Override
  public boolean copyExperimentSplitGroupsAndPersist()  throws SQLException {
    CSExperimentDefinitionDao expDefDao = new CSExperimentDefinitionDaoImpl();
    CSGroupTypeInputMappingDao gtimDao = new CSGroupTypeInputMappingDaoImpl();
    final ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();
    ExperimentQueryResult experimentsQueryResults = experimentService.getAllExperiments(null);
    List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();
    
    log.info("Retrieved " + experimentList.size() + "experiments");

    if (experimentList == null || experimentList.isEmpty()) {
      return false;
    }
    List<ExperimentGroup> predefinedGroups = null;
    ExperimentDAOConverter daoConverter = new ExperimentDAOConverter();
    // add predefined system group
    ExperimentGroup systemGroup = null;
    
    for (ExperimentDAO eachExperiment : experimentList) {
      try {
        predefinedGroups = Lists.newArrayList();
        // add predefined system group
        systemGroup = gtimDao.createSystemExperimentGroupForGroupType(GroupTypeEnum.SYSTEM, eachExperiment.getRecordPhoneDetails());

        // take crt backup in cloud sql
        //TODO uncomment for the first time
        expDefDao.insertExperimentDefinition(eachExperiment.getId(), eachExperiment.getVersion(), JsonConverter.jsonify(Lists.newArrayList(eachExperiment), null, null, null));
       
        // chk if splitting is neccessary
        for (ExperimentGroup eg: eachExperiment.getGroups()) {
          List<Input2> crtInputList =  eg.getInputs();
          Integer inputSize = 0;
          if (crtInputList != null) {
            inputSize = crtInputList.size();
          }
          if ( inputSize > 0) {
            if (eg.getAccessibilityListen() || eg.getGroupType().equals(GroupTypeEnum.ACCESSIBILITY)) {
              ExperimentGroup accListen = new ExperimentGroup();
              String lowerCaseGroupTypeName = GroupTypeEnum.ACCESSIBILITY.toString().toLowerCase();
              List<Input> inputLst = gtimDao.getAllFeatureInputs().get(lowerCaseGroupTypeName);
              accListen.setName(lowerCaseGroupTypeName);
              accListen.setGroupType(GroupTypeEnum.ACCESSIBILITY);
              accListen.setInputs(daoConverter.convertToInput2(inputLst));
              accListen.setStartDate(eg.getStartDate());
              accListen.setEndDate(eg.getEndDate());
              accListen.setFeedback(eg.getFeedback());
              accListen.setAccessibilityListen(true);
              
              eg.setAccessibilityListen(false);
              
              predefinedGroups.add(accListen);
            }
            if (eg.getLogShutdown() || eg.getGroupType().equals(GroupTypeEnum.PHONESTATUS)) {
              ExperimentGroup logPhoneActions = new ExperimentGroup();
              String lowerCaseGroupTypeName = GroupTypeEnum.PHONESTATUS.toString().toLowerCase();
              List<Input> inputLst = gtimDao.getAllFeatureInputs().get(lowerCaseGroupTypeName);
              logPhoneActions.setName(lowerCaseGroupTypeName);
              logPhoneActions.setGroupType(GroupTypeEnum.PHONESTATUS);
              logPhoneActions.setInputs(daoConverter.convertToInput2(inputLst));
              logPhoneActions.setStartDate(eg.getStartDate());
              logPhoneActions.setEndDate(eg.getEndDate());
              logPhoneActions.setFeedback(eg.getFeedback());
              logPhoneActions.setLogShutdown(true);
              
              eg.setLogShutdown(false);
              
              predefinedGroups.add(logPhoneActions);
              
            }
      
            if ( eg.getLogActions() || eg.getGroupType().equals(GroupTypeEnum.APPUSAGE))  {
              ExperimentGroup appUsage = new ExperimentGroup();
              String lowerCaseGroupTypeName = GroupTypeEnum.APPUSAGE.toString().toLowerCase();
              
              List<Input> inputLst = gtimDao.getAllFeatureInputs().get(lowerCaseGroupTypeName);
              appUsage.setName(lowerCaseGroupTypeName);
              appUsage.setGroupType(GroupTypeEnum.APPUSAGE);
              appUsage.setInputs(daoConverter.convertToInput2(inputLst));
              appUsage.setStartDate(eg.getStartDate());
              appUsage.setEndDate(eg.getEndDate());
              appUsage.setFeedback(eg.getFeedback());
              appUsage.setLogActions(true);
              
              eg.setLogActions(false);
              
              predefinedGroups.add(appUsage);
              
            }
            if (eg.getLogNotificationEvents() || eg.getGroupType().equals(GroupTypeEnum.NOTFICATION)) {
              ExperimentGroup logNotifGrp = new ExperimentGroup();
              String lowerCaseGroupTypeName = GroupTypeEnum.NOTFICATION.toString().toLowerCase();
              
              List<Input> inputLst = gtimDao.getAllFeatureInputs().get(lowerCaseGroupTypeName);
              logNotifGrp.setName(lowerCaseGroupTypeName);
              logNotifGrp.setGroupType(GroupTypeEnum.NOTFICATION);
              logNotifGrp.setInputs(daoConverter.convertToInput2(inputLst));
              logNotifGrp.setStartDate(eg.getStartDate());
              logNotifGrp.setEndDate(eg.getEndDate());
              logNotifGrp.setFeedback(eg.getFeedback());
              logNotifGrp.setLogNotificationEvents(true);
              
              eg.setLogNotificationEvents(false);
              
              predefinedGroups.add(logNotifGrp);
              
            }
          } 
        }
      } catch (Exception e) {
        log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
      }
      predefinedGroups.add(systemGroup);
      List<ExperimentGroup> origGroups = eachExperiment.getGroups();
      ExperimentGroup matchingGroupInDS = null;
      // add all predefined grps, only if its not already present
      for (ExperimentGroup egt : predefinedGroups) {
        matchingGroupInDS = eachExperiment.getGroupByName(egt.getName());
        if (matchingGroupInDS == null) {
          origGroups.add(egt);
        }
      }
    
      
      log.info("Splitted or Added with System Group, Experiment Id : " + eachExperiment.getId()) ;
      // upgrade version and persist in data store and in cloud sql
      eachExperiment.setVersion(eachExperiment.getVersion() + 1);
      // save json for bkup in cs
      expDefDao.insertExperimentDefinition(eachExperiment.getId(), eachExperiment.getVersion(), JsonConverter.jsonify(Lists.newArrayList(eachExperiment), null, null, null));
      // save splitted updated json in ds
      experimentService.saveExperiment(eachExperiment, eachExperiment.getCreator(), new DateTime().getZone(), false);
    } // for loop on expt in ds
    log.info("splitting groups for all experiments finished");
    return true;
  }


  private boolean populateGroupTypeInput()  throws SQLException {
    
    CSInputDao inputDaoImpl = new CSInputDaoImpl();
    CSGroupTypeInputMappingDao predfinedDaoImpl = new CSGroupTypeInputMappingDaoImpl();
    CSGroupTypeDao groupTypeDapImpl = new CSGroupTypeDaoImpl();
  
    try {
      //  System
      Input openTextJoined = new Input("joined", false, null, new DataType("open text", true, false), "joined", 0, null, null, null);
      Input openTextSchedule = new Input("schedule", false, null, new DataType("open text", false, false), "schedule", 0, null, null, null);
      // record Phone Details
      Input openTextModel = new Input("model", false, null, new DataType("open text", true, false), "model", 0, null, null, null);
      Input openTextAndroid = new Input("android", false, null, new DataType("open text", false, false), "android", 0, null, null, null);
      Input openTextMake = new Input("make", false, null, new DataType("open text", true, false), "make", 0, null, null, null);
      Input openTextCarrier = new Input("carrier", false, null, new DataType("open text", false, false), "carrier", 0, null, null, null);
      Input openTextDisplay = new Input("display", false, null, new DataType("open text", true, false), "display", 0, null, null, null);
            
      inputDaoImpl.insertInput(openTextJoined);
      inputDaoImpl.insertInput(openTextSchedule);
      inputDaoImpl.insertInput(openTextModel);
      inputDaoImpl.insertInput(openTextAndroid);
      inputDaoImpl.insertInput(openTextMake);
      inputDaoImpl.insertInput(openTextCarrier);
      inputDaoImpl.insertInput(openTextDisplay);
      
      Integer grpTypeSystemId = groupTypeDapImpl.getGroupTypeId("system");
      
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextJoined));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextSchedule));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextModel));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextAndroid));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextMake));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextCarrier));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextDisplay));
      
      // BackGround - AppUsage
      Input openTextAppUsage = new Input("apps_used", false, null, new DataType("open text", true, false), "apps_used", 0, null, null,  null);
      Input openTextAppUsageRaw = new Input("apps_used_raw", false, null, new DataType("open text", false, false), "apps_used_raw", 0, null, null, null);
      Input openTextForeGround = new Input("foreground", false, null, new DataType("open text", false, false), "foreground", 0, null, null, null);
      Integer grpTypeAppUsageId = groupTypeDapImpl.getGroupTypeId("logAppUsage");
      inputDaoImpl.insertInput(openTextAppUsage);
      inputDaoImpl.insertInput(openTextAppUsageRaw);
      inputDaoImpl.insertInput(openTextForeGround);
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextAppUsage));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextAppUsageRaw));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextForeGround));
      
      // BackGround - phoneEvent
      Input openTextPhoneOn = new Input("phoneOn", false, null, new DataType("open text", true, false), "phoneOn", 0, null, null, null);
      Input openTextPhoneOff = new Input("phoneOff", false, null, new DataType("open text", false, false), "phoneOff", 0, null, null, null);
      Integer grpTypePhoneOnId = groupTypeDapImpl.getGroupTypeId("phoneStatus");
      inputDaoImpl.insertInput(openTextPhoneOn);
      inputDaoImpl.insertInput(openTextPhoneOff);
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypePhoneOnId, openTextPhoneOn));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypePhoneOnId, openTextPhoneOff));
      
      // BackGround - logAccessibility  accessibilityEventText, accessibilityEventPackage, accessibilityEventText, accessibilityEventType
      Input openTextAccEventText = new Input("accessibilityEventText", false, null, new DataType("open text", true, false), "accessibilityEventText", 0, null, null, null);
      Input openTextAccEventPackage = new Input("accessibilityEventPackage", false, null, new DataType("open text", false, false), "accessibilityEventPackage", 0, null, null, null);
      Input openTextAccEventClass = new Input("accessibilityEventClass", false, null, new DataType("open text", true, false), "accessibilityEventClass", 0, null, null, null);
      Input openTextAccEventType = new Input("accessibilityEventType", false, null, new DataType("open text", false, false), "accessibilityEventType", 0, null, null, null);
      
      Integer grpTypeAccId = groupTypeDapImpl.getGroupTypeId("logAccessibility");
      
      inputDaoImpl.insertInput(openTextAccEventText);
      inputDaoImpl.insertInput(openTextAccEventPackage);
      inputDaoImpl.insertInput(openTextAccEventClass);
      inputDaoImpl.insertInput(openTextAccEventType);
      
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventText));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventPackage));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventClass));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventType));
      
      
      // BackGround - logNotification
      Integer grpTypeNotificationId = groupTypeDapImpl.getGroupTypeId("logNotification");
      
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventText));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventPackage));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventClass));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventType));
            
      
    } catch (SQLException sqle) {
      log.warning("SQLException while adding new cols" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new cols" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    }
    return true;
  }
  
  
  @Override
  public boolean populatePivotTableHelper()  throws SQLException {
    final String insertToPivotHelperSql = "insert into pivot_helper(experiment_version_mapping_id, anon_who, input_id)  select evm.experiment_version_mapping_id, eu.experiment_user_anon_id, ic.input_id from experiment_version_mapping evm " 
                                        + " join experiment e on evm.experiment_facet_id = e.experiment_facet_id "
                                        + " join experiment_user eu on evm.experiment_id = eu.experiment_id "
                                        + " join input_collection ic on ic.experiment_ds_id = evm.experiment_id and  evm.input_collection_id=ic.input_collection_id";
    Connection conn = null;
    PreparedStatement statementInsertToLookup = null;
    try {
      log.info("populate pivot table helper - start");
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementInsertToLookup = conn.prepareStatement(insertToPivotHelperSql);
      log.info(insertToPivotHelperSql);
      statementInsertToLookup.execute();
      log.info("Inserted pivot helper from experiment, experiment_user, input collection tables");
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
  public boolean processPivotTableHelper()  throws SQLException {
    final String processPivotHelperSql = "select * from pivot_helper where processed = b'0' ";
    final String eventQry = "update events e  " + 
                            " join outputs o on e._id=o.event_id set e.experiment_version_mapping_id=?, o.input_id = ?, e.who_bk=? " + 
                            " where e._id>0 and o.event_id>0 and  e.experiment_id = ? and e.experiment_version=? and e.group_name=? and o.text=? ";
    final String getFullPivotHelperDetailsQry = "select evm.experiment_id, evm.experiment_version, g.group_name, u.who, esi.label from experiment_version_mapping evm "
            + " join  `group` g on evm.group_id=g.group_id "
            + " join pivot_helper pv on pv.experiment_version_mapping_id = evm.experiment_version_mapping_id"
            + " join  experiment_user eu on evm.experiment_id=eu.experiment_id  and eu.experiment_user_anon_id=pv.anon_who"
            + " join  input_collection ic on ic.experiment_ds_id=evm.experiment_id  and evm.input_collection_id = ic.input_collection_id"
            + " join  `input` i on i.input_id=ic.input_id and i.input_id=pv.input_id"
            + " join extern_string_input esi on i.name_id=esi.extern_string_input_id"
            + " join  user u on u.user_id=eu.user_id "
            + " where evm.experiment_version_mapping_id=? and ic.input_id=? and pv.anon_who=?";
    
    Connection conn = null;
    PreparedStatement statementPivotHelper = null;
    PreparedStatement statementPivotHelperDetails = null;
    PreparedStatement statementUpdateEventsTable = null;
    
    ResultSet rsPivotHelper = null;
    Integer currentWhoAnonId = null;
    Long currentInputId = null;
    Long currentEVMappingId = null;
    ResultSet rsDetails = null;
    Long exptId = null;
    Integer expVersion = null;
    String groupName = null;
    String inputVariableName = null;
    String whoEmail = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementPivotHelper = conn.prepareStatement(processPivotHelperSql);
      statementPivotHelperDetails = conn.prepareStatement(getFullPivotHelperDetailsQry);
      statementUpdateEventsTable = conn.prepareStatement(eventQry);
      
      CSPivotHelperDao daoImpl = new CSPivotHelperDaoImpl();
      log.info(processPivotHelperSql);
      rsPivotHelper = statementPivotHelper.executeQuery();
      while(rsPivotHelper.next()) {
        currentWhoAnonId = rsPivotHelper.getInt(PivotHelperColumns.ANON_WHO);
        currentInputId = rsPivotHelper.getLong(PivotHelperColumns.INPUT_ID);
        currentEVMappingId = rsPivotHelper.getLong(PivotHelperColumns.EXPERIMENT_VERSION_MAPPING_ID);
        log.info("processing:EV Mapping "+ currentEVMappingId + ",who:" + currentWhoAnonId + ",input var name" + currentInputId);
        log.info(getFullPivotHelperDetailsQry);
        statementPivotHelperDetails.setLong(1, currentEVMappingId);
        statementPivotHelperDetails.setLong(2, currentInputId);
        statementPivotHelperDetails.setLong(3, currentWhoAnonId);
        
        rsDetails = statementPivotHelperDetails.executeQuery();
        long updateCt =0;
        while (rsDetails.next()) {
          exptId = rsDetails.getLong(ExperimentVersionMappingColumns.EXPERIMENT_ID);
          expVersion =  rsDetails.getInt(ExperimentVersionMappingColumns.EXPERIMENT_VERSION);
          groupName =  rsDetails.getString(GroupColumns.NAME);
          inputVariableName = rsDetails.getString(ExternStringInputColumns.LABEL);
          whoEmail = rsDetails.getString(UserColumns.WHO);
          log.info("processing 2nd qry:EV Mapping "+ currentEVMappingId + ",who:" + whoEmail + ",input var name" + inputVariableName);
          
          // update events and outputs
          statementUpdateEventsTable.setLong(1, currentEVMappingId);
          statementUpdateEventsTable.setLong(2, currentInputId);
          statementUpdateEventsTable.setInt(3, currentWhoAnonId);
          statementUpdateEventsTable.setLong(4, exptId);
          statementUpdateEventsTable.setInt(5, expVersion);
          statementUpdateEventsTable.setString(6, groupName);
          statementUpdateEventsTable.setString(7, inputVariableName);
          log.info(statementUpdateEventsTable.toString());
          statementUpdateEventsTable.addBatch();
          
          updateCt = statementUpdateEventsTable.executeUpdate();
         
        }
        // since we do a join on events and outputs, the update count adds the records once for events, once for outputs
        if (updateCt >=2) {
          updateCt = updateCt/2;
        }
        log.info("updating pv evt posted ct for "+ currentEVMappingId + "who annon id "+ currentWhoAnonId + "input id" + currentInputId + "update ct :" + updateCt);
        daoImpl.updatePivotHelperStatus(currentEVMappingId, currentWhoAnonId, currentInputId, updateCt);

      }
    } catch (SQLException sqle) {
      log.warning("SQLException while updating pv helper" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while updating pv helper" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (rsDetails != null) {
          rsDetails.close();
        }
        if (rsPivotHelper != null) {
          rsPivotHelper.close();
        }
        if (statementPivotHelper != null) {
          statementPivotHelper.close();
        }
        if (statementPivotHelperDetails != null) {
          statementPivotHelperDetails.close();
        }
        if (statementUpdateEventsTable != null) {
          statementUpdateEventsTable.close();
        }
        if (statementUpdateEventsTable != null) {
          statementUpdateEventsTable.close();
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
  public boolean updateEventTableGroupNameNull() throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    String updateQuery1 = "update events e  join outputs o on e._id=o.event_id set e.group_name ='system' where e._id>0  and o.text in ('joined','schedule','make','model','display','carrier','android') and o.event_id>0 and e.group_name is null";
    String updateQuery2 = "update events e  set e.group_name ='unknown' where e._id>0 and e.group_name is null";
    
    String[] qry = new String[] { updateQuery1, updateQuery2 };
    
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for (String s : qry) {
        statementUpdateEvent = conn.prepareStatement(s);
        long updateCt = statementUpdateEvent.executeUpdate(); 
      }
       
      log.info("updated group name from null to system and unknown");
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
  
  @Override
  public boolean processOlderVersionsAndAnonUsersInEventTable()  throws SQLException {
    CSExperimentVersionMappingDao daoImpl = new CSExperimentVersionMappingDaoImpl();
    CSFailedEventDao failedDaoImpl = new CSFailedEventDaoImpl();
    CSExperimentUserDao expUserDao = new CSExperimentUserDaoImpl();
    CSInputCollectionDao icDaoImpl = new CSInputCollectionDaoImpl();
    CSOutputDao outDaoImpl = new  CSOutputDaoImpl();
    final String unprocessedEventRecordQuery = "select experiment_id, experiment_version, group_name, who, text, experiment_name, _id from events e join outputs o on e._id=o.event_id where ((experiment_version_mapping_id is null or o.input_id is null) and experiment_id is not null) limit 1";
    Connection conn = null;
    PreparedStatement statementUnprocessedEventRecord = null;
    ResultSet rsUnprocessedEventQuery = null;
    ResultSet rsDetails = null;
    Long expId = null;
    Integer expVersion = null;
    String groupName = null;
    Long eventId = null;
    String whoEmail = null;
    List<String> inputVariableNames = Lists.newArrayList();
    InputOrderAndChoice currentIOC = null;
    InputCollection currentInputCollection = null;
    Input newCreatedInput = null;
    Map<String, ExperimentVersionMapping> allEVMRecords = null;
    Map<String, ExperimentVersionMapping> newEVMRecordMap = null;
    
    ExperimentVersionMapping newEVMRecord = null;
    ExperimentVersionMapping deletedExptEVMRecord = null;
    
    List<PivotHelper> pvHelperList = Lists.newArrayList();
    boolean olderVersion = false;
    boolean publicUser = false;
    String experimentName = null;
    boolean deletedExperiment = false;
    boolean doFurtherProcessing = false;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      statementUnprocessedEventRecord = conn.prepareStatement(unprocessedEventRecordQuery);
      log.info(unprocessedEventRecordQuery);
      rsUnprocessedEventQuery = statementUnprocessedEventRecord.executeQuery();
      while(rsUnprocessedEventQuery.next()) {
        doFurtherProcessing = true;
        expId = rsUnprocessedEventQuery.getLong(ExperimentVersionMappingColumns.EXPERIMENT_ID);
        expVersion = rsUnprocessedEventQuery.getInt(ExperimentVersionMappingColumns.EXPERIMENT_VERSION) ;
        whoEmail = rsUnprocessedEventQuery.getString(EventServerColumns.WHO);
        groupName =  rsUnprocessedEventQuery.getString(EventServerColumns.GROUP_NAME);
        inputVariableNames.add(rsUnprocessedEventQuery.getString(OutputServerColumns.TEXT));
        experimentName = rsUnprocessedEventQuery.getString(EventServerColumns.EXPERIMENT_NAME);
        eventId = rsUnprocessedEventQuery.getLong(Constants.UNDERSCORE_ID);
      }
      if (eventId != null) {
        List<WhatDAO> whats = outDaoImpl.getOutputs(eventId);
        for (WhatDAO w : whats) {
          if (!inputVariableNames.contains(w.getName())) {
            inputVariableNames.add(w.getName());
          }
        }
      }
      if (doFurtherProcessing) {
        allEVMRecords = daoImpl.getAllGroupsInVersion(expId, expVersion);
        
        if (allEVMRecords == null || allEVMRecords.size() == 0) {
          daoImpl.copyClosestVersion(expId, expVersion);
          allEVMRecords = daoImpl.getAllGroupsInVersion(expId, expVersion);
          if (allEVMRecords == null) {
            deletedExperiment = true;
            failedDaoImpl.insertFailedEvent("expId: " + expId + "expVersion: "+ expVersion + ",who:"+ whoEmail , "Did not find any closestVersion. ", "Did not find any closestVersion.");
            deletedExptEVMRecord = daoImpl.createMappingForDeletedExperiment(expId, experimentName, expVersion, whoEmail, groupName, inputVariableNames);
            allEVMRecords = Maps.newHashMap();
            allEVMRecords.put(groupName, deletedExptEVMRecord);
          } else {
            olderVersion = true;
            log.info("older version");
          }
        } 
        
        // find anon user id, if not present create it
        PacoId anonId = expUserDao.getAnonymousIdAndCreate(expId, whoEmail, true);
        if (anonId.getIsCreatedWithThisCall()) {
          //  TODO insert to pivothelper all groups all inputs
          log.info("anon user");
          publicUser = true;
        }
        if (olderVersion || publicUser || deletedExperiment) { 
          log.info("older version or pub user or deleted experiment");
          pvHelperList.addAll(convertToPivotHelper(allEVMRecords, anonId));
        }
        
        // find group name exists, if not create it with all inputs listed
        if (allEVMRecords.get(groupName) == null) {
          log.info("grp name not present");
          newEVMRecordMap = Maps.newHashMap();
          newEVMRecord = daoImpl.createGroupWithInputs(expId, experimentName, expVersion, groupName, whoEmail, inputVariableNames);
          allEVMRecords.put(groupName, newEVMRecord);// ??
          newEVMRecordMap.put(groupName, newEVMRecord);
          pvHelperList.addAll(convertToPivotHelper(newEVMRecordMap, anonId));
          log.info("pv list" + pvHelperList.size());
        } else {
          // find input id, if not present create it
          log.info("finding input id, if not create it");
          
          for (String s : inputVariableNames) {
            currentInputCollection = allEVMRecords.get(groupName).getInputCollection();
            if (currentInputCollection != null) { 
              currentIOC = currentInputCollection.getInputOrderAndChoices().get(s);
            }
            if (currentIOC == null) {
              // probably scripted or variable existed in old version only
              log.info("creating scripted undefined input");
              
              newCreatedInput = icDaoImpl.addUndefinedInputToCollection(expId, allEVMRecords.get(groupName).getInputCollection().getInputCollectionId(), s);
              pvHelperList.add(new PivotHelper(allEVMRecords.get(groupName).getExperimentVersionMappingId(), anonId.getId().intValue(), newCreatedInput.getInputId().getId(), false));
              log.info("pv list" + pvHelperList.size());
            }
          }
        }
        // create in pivot table helper
        CSPivotHelperDao phDaoImpl = new CSPivotHelperDaoImpl();
        if (pvHelperList != null && pvHelperList.size() > 0) {
          phDaoImpl.insertPivotHelper(pvHelperList);
          log.info("inserted records to pv_helper:" + pvHelperList.size());
          return true;
        }
      } else {
        log.info("finished processing all event records");
      }
      return false;
      
    } catch (SQLException sqle) {
      log.warning("SQLException while inserting to lookup" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while inserting to lookup" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (rsDetails != null) {
          rsDetails.close();
        }
        if (rsUnprocessedEventQuery != null) {
          rsUnprocessedEventQuery.close();
        }
        if (statementUnprocessedEventRecord != null) {
          statementUnprocessedEventRecord.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
  }
  
  private List<PivotHelper> convertToPivotHelper(Map<String, ExperimentVersionMapping> allEVMRecords, PacoId anonWhoId) {
    Set<Entry<String, ExperimentVersionMapping>> eset = allEVMRecords.entrySet();
    Iterator<Entry<String, ExperimentVersionMapping>> entryItr = eset.iterator();
    Entry<String, ExperimentVersionMapping> es = null;
    List<PivotHelper> pvList = Lists.newArrayList();
    PivotHelper pvh = null;
    Map<String, InputOrderAndChoice> varNameInputObject = Maps.newHashMap();
    InputOrderAndChoice ioc = null;
    String currentVarName = null;
    Iterator<String> varNameItr = null;
    while(entryItr.hasNext()) {
      es = entryItr.next();
      varNameInputObject = es.getValue().getInputCollection().getInputOrderAndChoices();
      varNameItr = varNameInputObject.keySet().iterator();
      while (varNameItr.hasNext()) {
        currentVarName = varNameItr.next();
        ioc = varNameInputObject.get(currentVarName);
        pvh = new PivotHelper();
        pvh.setAnonWhoId(anonWhoId.getId().intValue());
        pvh.setEventsPosted(0L);
        pvh.setInputId(ioc.getInput().getInputId().getId());
        pvh.setProcessed(false);
        pvh.setExpVersionMappingId(es.getValue().getExperimentVersionMappingId());
        pvList.add(pvh);
      }
    }
    return pvList;
  }
  @Override
  public boolean copyExperimentRenameOldEventColumns() throws SQLException{
    Connection conn = null;
    PreparedStatement statementRename = null;
    String createQuery = "ALTER TABLE `pacodb`.`events` " +
            " CHANGE COLUMN `experiment_id` `experiment_id_old` BIGINT(20) NULL DEFAULT NULL ," +
            " CHANGE COLUMN `experiment_name` `experiment_name_old` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL , " + 
            " CHANGE COLUMN `experiment_version` `experiment_version_old` INT(11) NULL DEFAULT NULL , " +
            " CHANGE COLUMN `group_name` `group_name_old` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL,"
            + "CHANGE COLUMN `who` `who_old` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL,"
            + "CHANGE COLUMN `who_bk` `who` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL";
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
