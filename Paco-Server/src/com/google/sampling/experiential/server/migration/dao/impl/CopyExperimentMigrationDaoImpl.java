package com.google.sampling.experiential.server.migration.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.cloudsql.columns.ChoiceCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDefinitionColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentGroupVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentUserColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupDetailColumns;
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
import com.google.sampling.experiential.dao.CSGroupTypeDao;
import com.google.sampling.experiential.dao.CSGroupTypeInputMappingDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.dao.CSPivotHelperDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.GroupTypeInputMapping;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.dao.impl.CSExperimentDefinitionDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentUserDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentVersionMappingDaoImpl;
import com.google.sampling.experiential.dao.impl.CSGroupTypeDaoImpl;
import com.google.sampling.experiential.dao.impl.CSGroupTypeInputMappingDaoImpl;
import com.google.sampling.experiential.dao.impl.CSInputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSOutputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSPivotHelperDaoImpl;
import com.google.sampling.experiential.model.What;
import com.google.sampling.experiential.server.CloudSQLConnectionManager;
import com.google.sampling.experiential.server.ExceptionUtil;
import com.google.sampling.experiential.server.ExperimentDAOConverter;
import com.google.sampling.experiential.server.ExperimentService;
import com.google.sampling.experiential.server.ExperimentServiceFactory;
import com.google.sampling.experiential.server.PacoId;
import com.google.sampling.experiential.server.QueryConstants;
import com.google.sampling.experiential.server.migration.dao.CopyExperimentMigrationDao;
import com.google.sampling.experiential.shared.EventDAO;
import com.google.sampling.experiential.shared.WhatDAO;
import com.pacoapp.paco.shared.model2.ExperimentDAO;
import com.pacoapp.paco.shared.model2.ExperimentQueryResult;
import com.pacoapp.paco.shared.model2.GroupTypeEnum;
import com.pacoapp.paco.shared.model2.JsonConverter;
import com.pacoapp.paco.shared.model2.ValidationMessage;
import com.pacoapp.paco.shared.util.Constants;
import com.pacoapp.paco.shared.util.ErrorMessages;

public class CopyExperimentMigrationDaoImpl implements CopyExperimentMigrationDao {
  public static final Logger log = Logger.getLogger(CopyExperimentMigrationDaoImpl.class.getName());
  
  @Override
  public boolean copyExperimentCreateTables() throws SQLException {
    boolean isComplete = false;
    String[] qry = null;
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
            ExternStringListLabelColumns.LABEL + " VARCHAR(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL," +
            " PRIMARY KEY (`"+ ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID +"`)," +
            " UNIQUE KEY `type_UNIQUE` (`" + ExternStringListLabelColumns.LABEL + "`(500)))" +
            " DEFAULT CHARACTER SET = utf8mb4" ;
    final String createTableSql3 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExternStringInputColumns.TABLE_NAME+"` (" +
            ExternStringInputColumns.EXTERN_STRING_INPUT_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
            ExternStringInputColumns.LABEL + " VARCHAR(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
            " PRIMARY KEY (`"+ ExternStringInputColumns.EXTERN_STRING_INPUT_ID +"`)," +
            " UNIQUE KEY `type_UNIQUE` (`" + ExternStringInputColumns.LABEL + "`(500)))" +
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
     
     final String createTableSql5 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExperimentDetailColumns.TABLE_NAME+"` (" +
             ExperimentDetailColumns.EXPERIMENT_DETAIL_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
             ExperimentDetailColumns.EXPERIMENT_NAME + " VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
             ExperimentDetailColumns.DESCRIPTION + " VARCHAR(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL," +
             ExperimentDetailColumns.CREATOR + " BIGINT(20) NOT NULL," +
             ExperimentDetailColumns.CONTACT_EMAIL + " VARCHAR(200) NULL," +
             ExperimentDetailColumns.ORGANIZATION + " VARCHAR(2500)  NULL," +
             ExperimentDetailColumns.INFORMED_CONSENT_ID + " BIGINT(20) NULL DEFAULT NULL," +
             ExperimentDetailColumns.MODIFIED_DATE + " datetime NULL," +
             ExperimentDetailColumns.PUBLISHED + " BIT(1) NULL DEFAULT 0," +
             ExperimentDetailColumns.RINGTONE_URI + "  VARCHAR(200) NULL," +
             ExperimentDetailColumns.POST_INSTALL_INSTRUCTIONS + " MEDIUMTEXT NULL," +
             ExperimentDetailColumns.DELETED + " bit(1) NULL DEFAULT 0," +
             " PRIMARY KEY (`"+ ExperimentDetailColumns.EXPERIMENT_DETAIL_ID +"`))" +
             " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql6 = "CREATE TABLE IF NOT EXISTS `pacodb`."+ GroupDetailColumns.TABLE_NAME+" (" +
             GroupDetailColumns.GROUP_DETAIL_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
             GroupDetailColumns.NAME + " VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL," +
             GroupDetailColumns.GROUP_TYPE_ID + " INT(11) NULL DEFAULT NULL, " +
             GroupDetailColumns.CUSTOM_RENDERING + " MEDIUMTEXT NULL," +
             GroupDetailColumns.END_OF_DAY_GROUP + " VARCHAR(500) NULL DEFAULT NULL," +
             GroupDetailColumns.FIXED_DURATION + " BIT(1) NULL," +
             GroupDetailColumns.START_DATE + " datetime NULL," +
             GroupDetailColumns.END_DATE + " datetime NULL," +
             GroupDetailColumns.RAW_DATA_ACCESS + " BIT(1) NULL," +
             " PRIMARY KEY (`"+ GroupDetailColumns.GROUP_DETAIL_ID +"`))" +
             " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql7 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ InputColumns.TABLE_NAME+"` (" +
             InputColumns.INPUT_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
             InputColumns.NAME_ID + " BIGINT(20) NOT NULL," +
             InputColumns.TEXT_ID + " BIGINT(20) NOT NULL," +
             InputColumns.REQUIRED + " BIT(1) NULL DEFAULT 0," +
             InputColumns.CONDITIONAL + " VARCHAR(200) NULL," +
             InputColumns.RESPONSE_DATA_TYPE_ID + " INT(11) NULL," +
             InputColumns.LIKERT_STEPS + " TINYINT(4) NULL," +
             InputColumns.LEFT_LABEL + " VARCHAR(500) NULL," +
             InputColumns.RIGHT_LABEL + " VARCHAR(500) NULL," +
             InputColumns.PARENT_ID + " BIGINT(20) NULL," + 
             " PRIMARY KEY (`"+ InputColumns.INPUT_ID +"`)," +
             " INDEX `name_extern_string_fk_idx` (`" + InputColumns.NAME_ID  + "` ASC)," + 
             " INDEX `text_extern_string_fk_idx` (`" + InputColumns.TEXT_ID + "` ASC)," + 
             " INDEX `response_type_fk_idx` (`" + InputColumns.RESPONSE_DATA_TYPE_ID + "` ASC)," + 
             " CONSTRAINT `ih_es_name_fk` FOREIGN KEY (`" + InputColumns.NAME_ID + "`) REFERENCES `" + 
                 ExternStringInputColumns.TABLE_NAME +"`(`" + ExternStringInputColumns.EXTERN_STRING_INPUT_ID + "`)  ON DELETE NO ACTION ON UPDATE NO ACTION," + 
             " CONSTRAINT `ih_es_text_fk` FOREIGN KEY (`" + InputColumns.TEXT_ID + "`) REFERENCES `" + 
                 ExternStringInputColumns.TABLE_NAME +"`(`" + ExternStringInputColumns.EXTERN_STRING_INPUT_ID + "`)  ON DELETE NO ACTION ON UPDATE NO ACTION," +  
             " CONSTRAINT `ih_es_response_type_fk` FOREIGN KEY (`" + InputColumns.RESPONSE_DATA_TYPE_ID + "`) REFERENCES `" + 
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
     final String createTableSql9 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExperimentGroupVersionMappingColumns.TABLE_NAME+"` (" +
             ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
             ExperimentGroupVersionMappingColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
             ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION + " INT(11) NOT NULL," +
             ExperimentGroupVersionMappingColumns.EXPERIMENT_DETAIL_ID + " BIGINT(20) NOT NULL," +
             ExperimentGroupVersionMappingColumns.GROUP_DETAIL_ID + " BIGINT(20) NOT NULL," +
             ExperimentGroupVersionMappingColumns.INPUT_COLLECTION_ID + " BIGINT(20) NULL," +
             ExperimentGroupVersionMappingColumns.EVENTS_POSTED + " BIT(1) DEFAULT 0," +
             ExperimentGroupVersionMappingColumns.SOURCE + " VARCHAR(100) NULL, " +
             " PRIMARY KEY (`" + ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID + "`)," +  
             " UNIQUE KEY `experiment_id_version_group_unique` (`" + ExperimentGroupVersionMappingColumns.EXPERIMENT_ID + "`,"
                     + "`" + ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION + "`,"
                     + "`" + ExperimentGroupVersionMappingColumns.GROUP_DETAIL_ID + "`)," +
              "KEY `experiment_history_fk_idx` (`" + ExperimentGroupVersionMappingColumns.EXPERIMENT_DETAIL_ID + "`)," +
              "KEY `group_history_fk_idx` (`" + ExperimentGroupVersionMappingColumns.GROUP_DETAIL_ID + "`)," +
              " CONSTRAINT `experiment_history_fk` FOREIGN KEY (`" + ExperimentGroupVersionMappingColumns.EXPERIMENT_DETAIL_ID + "`) REFERENCES `" +
                     ExperimentDetailColumns.TABLE_NAME + "` (`" + ExperimentDetailColumns.EXPERIMENT_DETAIL_ID + "`) ON DELETE NO ACTION ON UPDATE NO ACTION," +
              " CONSTRAINT `group_history_fk` FOREIGN KEY (`" + ExperimentGroupVersionMappingColumns.GROUP_DETAIL_ID + "`) REFERENCES " +
                     GroupDetailColumns.TABLE_NAME + " (`" + GroupDetailColumns.GROUP_DETAIL_ID + "`) ON DELETE NO ACTION ON UPDATE NO ACTION)" +
              " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql10 = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ InformedConsentColumns.TABLE_NAME+"` (" +
             InformedConsentColumns.INFORMED_CONSENT_ID + " BIGINT(20) NOT NULL," +
             InformedConsentColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
             InformedConsentColumns.INFORMED_CONSENT + " LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL," +
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
             PivotHelperColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID + " BIGINT(20) NOT NULL," +
             PivotHelperColumns.ANON_WHO + " INT NOT NULL," +
             PivotHelperColumns.INPUT_ID + " BIGINT(20) NOT NULL," +
             PivotHelperColumns.EVENTS_POSTED + " BIGINT(20) NOT NULL DEFAULT 0," +
             PivotHelperColumns.PROCESSED + " BIT(1) DEFAULT 0," +
             " PRIMARY KEY (`" + PivotHelperColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID + "`, `"+ PivotHelperColumns.ANON_WHO +"`, `"+ PivotHelperColumns.INPUT_ID+"`)) " +  
             " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql14 = "CREATE TABLE IF NOT EXISTS `pacodb`.`" + ExperimentDefinitionColumns.TABLE_NAME + "` (" +
             " `" + ExperimentDefinitionColumns.ID + "` bigint(20) NOT NULL, " +
              " `" + ExperimentDefinitionColumns.VERSION + "` int(11) NOT NULL, " +
              " `" + ExperimentDefinitionColumns.SOURCE_JSON + "` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL, " +
              " `" + ExperimentDefinitionColumns.CONVERTED_JSON + "` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL, " +
              " `" + ExperimentDefinitionColumns.MIGRATION_STATUS + "` int(11) DEFAULT NULL, " +
              " `" + ExperimentDefinitionColumns.ERROR_MESSAGE + "` varchar(500) DEFAULT NULL, " +
             " PRIMARY KEY (`" + ExperimentDefinitionColumns.ID +"`,`" + ExperimentDefinitionColumns.VERSION + "`)) " +
            " DEFAULT CHARACTER SET = utf8mb4" ;
     final String createTableSql15 = "CREATE TABLE IF NOT EXISTS `pacodb`.`event_old_group_name` (`event_id` BIGINT(20) NOT NULL,`old_group_name` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL,PRIMARY KEY (`event_id`))";
     final String createTableSql16 = "CREATE TABLE IF NOT EXISTS `pacodb`.`" + ExperimentDefinitionColumns.TABLE_NAME + "_bk` (" +
             " `" + ExperimentDefinitionColumns.ID + "` bigint(20) NOT NULL, " +
              " `" + ExperimentDefinitionColumns.VERSION + "` int(11) NOT NULL, " +
              " `" + ExperimentDefinitionColumns.SOURCE_JSON + "` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL, " +
             " PRIMARY KEY (`" + ExperimentDefinitionColumns.ID +"`,`" + ExperimentDefinitionColumns.VERSION + "`)) " +
            " DEFAULT CHARACTER SET = utf8mb4" ;
     
    qry = new String[] { createTableSql1, createTableSql2, createTableSql3, createTableSql4, createTableSql5, 
                         createTableSql6, createTableSql7, createTableSql8, createTableSql9, createTableSql10, 
                         createTableSql11, createTableSql12, createTableSql13,
                         createTableSql14, createTableSql15, createTableSql16};
    
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
  public boolean insertPredefinedRecords() throws Exception {
    
    final String insertDataTypeSql1 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('open text', 1, 0, 0)";
    final String insertDataTypeSql2 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('open text', 0, 0, 0)";
    final String insertDataTypeSql3 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('list', 1, 0, 1)";
    final String insertDataTypeSql4 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('list', 1, 1, 1)";
    final String insertDataTypeSql5 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('likert', 1, 0, 0)";
    final String insertDataTypeSql7 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('number', 1, 0, 0)";
    final String insertDataTypeSql8 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('likert_smileys', 0, 0, 0)";
    final String insertDataTypeSql9 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('undefined', 0, 0, 0)";
    final String insertDataTypeSql10 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('photo', 0, 0, 0)";
    
    final String insertDataTypeSql11 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('', 0, 0, 0)";
    final String insertDataTypeSql12 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('location', 0, 0, 0)";
    final String insertDataTypeSql13 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('audio', 0, 0, 0)";
    final String insertDataTypeSql14 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('number', 1, 1, 0)";
    final String insertDataTypeSql15 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('', 1, 1, 0)";
    final String insertDataTypeSql16 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('likert', 1, 1, 0)";
    final String insertDataTypeSql17 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('open text', 0, 1, 0)";
    final String insertDataTypeSql18 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('', 0, 1, 0)";
    final String insertDataTypeSql19 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('graph', 0, 0, 0)";
    final String insertDataTypeSql20 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('likert_smileys', 0, 1, 0)";

    final String insertDataTypeSql21 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('location', 0, 1, 0)";
    final String insertDataTypeSql22 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('photo', 0, 1, 0)";
    final String insertDataTypeSql23 = "INSERT INTO `pacodb`.`data_type` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('audio', 0, 1, 0)";

        
    final String insertDataTypeSql24 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.SYSTEM+"')";
    final String insertDataTypeSql25 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.SURVEY+"')";
    final String insertDataTypeSql26 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.APPUSAGE_ANDROID+"')";
    final String insertDataTypeSql27 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.NOTIFICATION+"')";
    final String insertDataTypeSql28 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.ACCESSIBILITY+"')";
    final String insertDataTypeSql29 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.PHONESTATUS+"')";
        

        
    String[] qry = new String[] { 
                                  insertDataTypeSql1, insertDataTypeSql2,
                                  insertDataTypeSql3, insertDataTypeSql4,
                                  insertDataTypeSql5,
                                  insertDataTypeSql7, insertDataTypeSql8,
                                  insertDataTypeSql9, insertDataTypeSql10,
                                  insertDataTypeSql11, insertDataTypeSql12,
                                  insertDataTypeSql13, insertDataTypeSql14
                                  ,insertDataTypeSql15,
                                  insertDataTypeSql16, insertDataTypeSql17,
                                  insertDataTypeSql18, insertDataTypeSql19,
                                  insertDataTypeSql20, insertDataTypeSql21,
                                  insertDataTypeSql22, insertDataTypeSql23,
                                  insertDataTypeSql24, insertDataTypeSql25,
                                  insertDataTypeSql26, insertDataTypeSql27,
                                  insertDataTypeSql28, insertDataTypeSql29
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
    final String addNewColumnsSql1 = "ALTER TABLE `pacodb`.`"+OutputServerColumns.TABLE_NAME+"` " +
            " ADD COLUMN `"+OutputServerColumns.INPUT_ID+"` BIGINT(20) NULL AFTER `archive_flag`," +
            " ADD INDEX `fk_text_input_id_idx` (`input_id` ASC)";
    final String addNewColumnsSql2 = "ALTER TABLE `pacodb`.`"+OutputServerColumns.TABLE_NAME+"`  " + 
                  " ADD CONSTRAINT `fk_text_input_id` " +  
                  "   FOREIGN KEY (`"+OutputServerColumns.INPUT_ID+"`) " + 
                  "  REFERENCES `pacodb`.`"+InputColumns.TABLE_NAME+"` (`"+InputColumns.INPUT_ID+"`) "  +
                  " ON DELETE NO ACTION " + 
                  "  ON UPDATE NO ACTION ";
    final String addNewColumnsSql3 = "ALTER TABLE `pacodb`.`events`  " + 
    " ADD COLUMN `"+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID  + "` BIGINT(20) NULL AFTER `sort_date`, " +
    " ADD COLUMN `who_bk` BIGINT(20) NULL AFTER `"+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID  + "`," +
    " ADD INDEX `fk_exp_group_version_mapping_idx` (`"+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID  + "` ASC)";
    final String addNewColumnsSql4 = "ALTER TABLE `pacodb`.`events`  " +
    " ADD CONSTRAINT `fk_exp_group_version_mapping` " +
    " FOREIGN KEY (`"+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID  + "`) " + 
    " REFERENCES `pacodb`.`"+ ExperimentGroupVersionMappingColumns.TABLE_NAME  + "` (`"+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID  + "`) " + 
    " ON DELETE NO ACTION " +
    " ON UPDATE NO ACTION ";
    final String addNewColumnsSql5 = "ALTER TABLE `pacodb`.`"+ InputCollectionColumns.TABLE_NAME  + "` " + 
    " ADD INDEX `ic_input_id_fk_idx` (`"+ InputCollectionColumns.INPUT_ID  + "` ASC)";
    final String addNewColumnsSql6 = "ALTER TABLE `pacodb`.`"+ InputCollectionColumns.TABLE_NAME  + "`" +
    " ADD CONSTRAINT `ic_input_id_fk` " +
    " FOREIGN KEY (`"+ InputCollectionColumns.INPUT_ID  + "`) " + 
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
    final String addNewColumnsSql9 = "ALTER TABLE `pacodb`.`"+ ExperimentDetailColumns.TABLE_NAME  + "` " + 
    " ADD INDEX `e_ic_informed_consent_fk_idx` (`informed_consent_id` ASC)";
    final String addNewColumnsSql10 = "ALTER TABLE `pacodb`.`"+ ExperimentDetailColumns.TABLE_NAME  + "`" +
    " ADD CONSTRAINT `e_ic_informed_consent_fk` " +
    " FOREIGN KEY (`"+ InformedConsentColumns.INFORMED_CONSENT_ID  + "`) " + 
    " REFERENCES `pacodb`.`"+ InformedConsentColumns.TABLE_NAME  + "` (`"+ InformedConsentColumns.INFORMED_CONSENT_ID  + "`) " + 
    " ON DELETE NO ACTION " +
    " ON UPDATE NO ACTION ";
    
    
    String[] qry = new String[] { 
              addNewColumnsSql1, addNewColumnsSql2 ,
              addNewColumnsSql3, addNewColumnsSql4,
              addNewColumnsSql5, 
              addNewColumnsSql6,
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
  public boolean copyExperimentPopulateExperimentBundleTables()  throws SQLException {
    CSExperimentVersionMappingDao evMappingDaoImpl = new CSExperimentVersionMappingDaoImpl();
    CSExperimentUserDao expUserDaoImpl = new CSExperimentUserDaoImpl();
    CSExperimentDefinitionDao expDefDaoImpl = new CSExperimentDefinitionDaoImpl();
    List<ExperimentDAO> experimentList = readFromCloudSql(false);

    log.info("Retrieved " + experimentList.size() + "experiments");

    if (experimentList == null || experimentList.isEmpty()) {
      return false;
    }

    for (ExperimentDAO eachExperiment : experimentList) {
      try {
        List<String> adminLstInRequest = eachExperiment.getAdmins();
        List<String> partLstInRequest = eachExperiment.getPublishedUsers();
        expUserDaoImpl.ensureUserId(eachExperiment.getId(), Sets.newHashSet(adminLstInRequest), Sets.newHashSet(partLstInRequest));
        evMappingDaoImpl.ensureExperimentVersionMapping(eachExperiment);
        expDefDaoImpl.updateMigrationStatus(eachExperiment.getId(), eachExperiment.getVersion() -1, null);
      } catch (SQLException sqle) {
        String exMsg = ExceptionUtil.getStackTraceAsString(sqle);
        if (exMsg == null) {
          exMsg = "";
        } else  if (exMsg.length() > 450) {
          exMsg = exMsg.substring(0,449);
        }
        expDefDaoImpl.updateMigrationStatus(eachExperiment.getId(), eachExperiment.getVersion()-1, exMsg);
      } catch (Exception e) {
        log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
        String exMsg = ExceptionUtil.getStackTraceAsString(e);
        if (exMsg == null) {
          exMsg = "";
        } else  if (exMsg.length() > 450) {
          exMsg = exMsg.substring(0,449);
        }
        expDefDaoImpl.updateMigrationStatus(eachExperiment.getId(), eachExperiment.getVersion(), exMsg);
      } 
    }
    return true;
  }
  
  private List<ExperimentDAO> readFromDataStore() {
    final ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();
    ExperimentQueryResult experimentsQueryResults = experimentService.getAllExperiments(null);
    List<ExperimentDAO> experimentList = experimentsQueryResults.getExperiments();
    log.info("Retrieved " + experimentList.size() + "experiments");
    return experimentList;
  }
  
  private List<ExperimentDAO> readFromCloudSql(boolean sourceJson) throws SQLException {
    List<ExperimentDAO> experimentList = Lists.newArrayList();
    Connection conn = null;
    ResultSet rs = null;
    String experimentJson1 = null;
    ExperimentDAO experiment1 = null;
    PreparedStatement statementSelectExperimentJson = null;
    String query = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      query = QueryConstants.GET_ALL_EXPERIMENT_JSON.toString();
      if (sourceJson) {
        query = query + " where "+ExperimentDefinitionColumns.MIGRATION_STATUS +" = 0";
      } else {
        query = query + " where "+ExperimentDefinitionColumns.MIGRATION_STATUS +" = 1 and error_message is null and converted_json is not null";
      }
      statementSelectExperimentJson = conn.prepareStatement(query);
      log.info(query);
      rs = statementSelectExperimentJson.executeQuery();
      while(rs.next()) {
        if (sourceJson) {
          experimentJson1 = rs.getString(ExperimentDefinitionColumns.SOURCE_JSON);
          experiment1 = JsonConverter.fromSingleEntityJson(experimentJson1.substring(1,experimentJson1.length()-1));
        } else {
          experimentJson1 = rs.getString(ExperimentDefinitionColumns.CONVERTED_JSON);
          experiment1 = JsonConverter.fromSingleEntityJson(experimentJson1);
        }
        experimentList.add(experiment1);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectExperimentJson != null) {
          statementSelectExperimentJson.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    
    log.info("Retrieved " + experimentList.size() + "experiments");
    return experimentList;
  }
  
  private List<ExperimentDAO> readFromCloudSqlBk() throws SQLException {
    List<ExperimentDAO> experimentList = Lists.newArrayList();
    Connection conn = null;
    ResultSet rs = null;
    String experimentJson1 = null;
    ExperimentDAO experiment1 = null;
    PreparedStatement statementSelectExperimentJson = null;
    String query = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      query = QueryConstants.GET_ALL_EXPERIMENT_JSON_BK.toString();
      log.info("from bk to exp def "+ query);
      statementSelectExperimentJson = conn.prepareStatement(query);
      rs = statementSelectExperimentJson.executeQuery();
      while(rs.next()) {
        experimentJson1 = rs.getString(ExperimentDefinitionColumns.SOURCE_JSON);
        experiment1 = JsonConverter.fromSingleEntityJson(experimentJson1.substring(1,experimentJson1.length()-1));
        experimentList.add(experiment1);
      }
    } finally {
      try {
        if ( rs != null) {
          rs.close();
        }
        if (statementSelectExperimentJson != null) {
          statementSelectExperimentJson.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }

    
    log.info("Retrieved " + experimentList.size() + "experiments");
    return experimentList;
  }
  
  @Override
  public boolean copyExperimentTakeBackupInCloudSql()  throws SQLException {
    CSExperimentDefinitionDao expDefDao = new CSExperimentDefinitionDaoImpl();
    if ( expDefDao.getTotalRecordsInExperimentDefinition() == 0) {
      List<ExperimentDAO> experimentList = readFromDataStore();
      if (experimentList == null || experimentList.isEmpty()) {
        return false;
      }
      
      for (ExperimentDAO eachExperiment : experimentList) {
        try {
          expDefDao.insertExperimentDefinitionBk(eachExperiment.getId(), eachExperiment.getVersion(), JsonConverter.jsonify(Lists.newArrayList(eachExperiment), null, null, null));
        } catch (Exception e) {
          log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
        }
      } // for loop on expt in ds
      log.info("backup from datastore to cloud sql for all experiments finished");
    }
    return true;
  }
  
  @Override
  public boolean copyExperimentSplitGroupsAndPersist()  throws SQLException, Exception {
    CSExperimentDefinitionDao expDefDao = new CSExperimentDefinitionDaoImpl();
    ExperimentDAOConverter daoConverter = new ExperimentDAOConverter();
    String errorMessage = null;
    final ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();
    List<ExperimentDAO> experimentList = readFromCloudSql(true);
    
    if (experimentList == null || experimentList.isEmpty()) {
      return false;
    }
    
    for (ExperimentDAO eachExperiment : experimentList) {
      try {
        daoConverter.splitGroups(eachExperiment, false);
        // save splitted updated json in ds
        List<ValidationMessage> vmList = experimentService.saveExperiment(eachExperiment, eachExperiment.getCreator(), new DateTime().getZone(), false, false);
        if (vmList != null && vmList.size() > 0) {
          for (ValidationMessage vm : vmList) {
            errorMessage = vm.getMsg();
            expDefDao.updateMigrationStatus(eachExperiment.getId(), eachExperiment.getVersion() - 1, errorMessage);
          }
        } else {
          // update cs backup table
          expDefDao.updateSplitJson(eachExperiment.getId(), eachExperiment.getVersion() -1, JsonConverter.jsonify(eachExperiment));
        }        
      } catch (Exception e) {
        log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
        // not sure if version was incremented or not
        if (errorMessage == null) {
          errorMessage = e.getMessage();
        }
        expDefDao.updateMigrationStatus(eachExperiment.getId(), eachExperiment.getVersion(), errorMessage);
        expDefDao.updateMigrationStatus(eachExperiment.getId(), eachExperiment.getVersion() - 1, errorMessage);
      }
    } // for loop on expt in ds
    log.info("splitting groups for all experiments finished");
    return true;
  }
  
 
  @Override
  public boolean insertIntoExperimentDefinition()  throws SQLException, Exception {
    CSExperimentDefinitionDao expDefDao = new CSExperimentDefinitionDaoImpl();
    List<ExperimentDAO> experimentList = readFromCloudSqlBk();
    
    if (experimentList == null || experimentList.isEmpty()) {
      return false;
    }
    
    for (ExperimentDAO eachExperiment : experimentList) {
      try {
        expDefDao.insertExperimentDefinition(eachExperiment.getId(), eachExperiment.getVersion(), JsonConverter.jsonify(Lists.newArrayList(eachExperiment), null, null, null));
      } catch (Exception e) {
        log.warning(ErrorMessages.GENERAL_EXCEPTION.getDescription() + ExceptionUtil.getStackTraceAsString(e));
        throw e;
      }
    } // for loop on expt in ds
    log.info("splitting groups for all experiments finished");
    return true;
  }
  

  private boolean populateGroupTypeInput()  throws Exception {
    
    CSInputDao inputDaoImpl = new CSInputDaoImpl();
    CSGroupTypeInputMappingDao predfinedDaoImpl = new CSGroupTypeInputMappingDaoImpl();
    CSGroupTypeDao groupTypeDapImpl = new CSGroupTypeDaoImpl();
    DataType openTextDataType = new DataType("open text", false, false); 
  
    try {
      //  System
      Input openTextJoined = new Input("joined", false, null, new DataType("open text", true, false), "joined", 0, null, null, null);
      Input openTextSchedule = new Input("schedule", false, null, openTextDataType, "schedule", 0, null, null, null);
      // record Phone Details
      Input openTextModel = new Input("model", false, null, openTextDataType, "model", 0, null, null, null);
      Input openTextAndroid = new Input("android", false, null, openTextDataType, "android", 0, null, null, null);
      Input openTextMake = new Input("make", false, null, openTextDataType, "make", 0, null, null, null);
      Input openTextCarrier = new Input("carrier", false, null, openTextDataType, "carrier", 0, null, null, null);
      Input openTextDisplay = new Input("display", false, null, openTextDataType, "display", 0, null, null, null);
            
      inputDaoImpl.insertInput(openTextJoined);
      inputDaoImpl.insertInput(openTextSchedule);
      inputDaoImpl.insertInput(openTextModel);
      inputDaoImpl.insertInput(openTextAndroid);
      inputDaoImpl.insertInput(openTextMake);
      inputDaoImpl.insertInput(openTextCarrier);
      inputDaoImpl.insertInput(openTextDisplay);
      
      Integer grpTypeSystemId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.SYSTEM.name());
      
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextJoined));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextSchedule));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextModel));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextAndroid));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextMake));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextCarrier));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeSystemId, openTextDisplay));
      
      // BackGround - AppUsage
      Input openTextAppUsage = new Input("apps_used", false, null, openTextDataType, "apps_used", 0, null, null,  null);
      Input openTextAppUsageRaw = new Input("apps_used_raw", false, null, openTextDataType, "apps_used_raw", 0, null, null, null);
      Input openTextForeGround = new Input("foreground", false, null, openTextDataType, "foreground", 0, null, null, null);
      Integer grpTypeAppUsageId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.APPUSAGE_ANDROID.name());
      inputDaoImpl.insertInput(openTextAppUsage);
      inputDaoImpl.insertInput(openTextAppUsageRaw);
      inputDaoImpl.insertInput(openTextForeGround);
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextAppUsage));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextAppUsageRaw));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextForeGround));
      
      // BackGround - phoneEvent
      Input openTextPhoneOn = new Input("phoneOn", false, null, openTextDataType, "phoneOn", 0, null, null, null);
      Input openTextPhoneOff = new Input("phoneOff", false, null, openTextDataType, "phoneOff", 0, null, null, null);
      Integer grpTypePhoneOnId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.PHONESTATUS.name());
      inputDaoImpl.insertInput(openTextPhoneOn);
      inputDaoImpl.insertInput(openTextPhoneOff);
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypePhoneOnId, openTextPhoneOn));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypePhoneOnId, openTextPhoneOff));
      
      // BackGround - logAccessibility  accessibilityEventText, accessibilityEventPackage, accessibilityEventText, accessibilityEventType
      Input openTextAccEventText = new Input("accessibilityEventText", false, null, openTextDataType, "accessibilityEventText", 0, null, null, null);
      Input openTextAccEventPackage = new Input("accessibilityEventPackage", false, null, openTextDataType, "accessibilityEventPackage", 0, null, null, null);
      Input openTextAccEventClass = new Input("accessibilityEventClass", false, null, openTextDataType, "accessibilityEventClass", 0, null, null, null);
      Input openTextAccEventType = new Input("accessibilityEventType", false, null, openTextDataType, "accessibilityEventType", 0, null, null, null);
      Input openTextAccEventContentDescription = new Input("accessibilityEventContentDescription", false, null, openTextDataType, "accessibilityEventContentDescription", 0, null, null, null);
      
      Integer grpTypeAccId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.ACCESSIBILITY.name());
      
      inputDaoImpl.insertInput(openTextAccEventText);
      inputDaoImpl.insertInput(openTextAccEventPackage);
      inputDaoImpl.insertInput(openTextAccEventClass);
      inputDaoImpl.insertInput(openTextAccEventType);
      inputDaoImpl.insertInput(openTextAccEventContentDescription);
      
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventText));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventPackage));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventClass));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventType));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAccId, openTextAccEventContentDescription));
      
      
      // BackGround - logNotification
      Integer grpTypeNotificationId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.NOTIFICATION.name());
      
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventText));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventPackage));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventClass));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventType));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeNotificationId, openTextAccEventContentDescription));
            
      
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
    final String insertToPivotHelperSql = QueryConstants.INSERT_TO_PIVOT_HELPER.toString();
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
      log.warning("SQLException while inserting to pivot helper" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while inserting to pivot helper" + ExceptionUtil.getStackTraceAsString(e));
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
  public boolean updateEventTableGroupNameNull() throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdateEvent = null;
    String updateQuery1 = "update events e  join outputs o on e._id=o.event_id set e.group_name ='SYSTEM' where e._id>0  and o.text in ('joined','schedule','make','model','display','carrier','android') and o.event_id>0 and e.group_name is null";
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
  
  private  Set<What> convertToWhats(List<WhatDAO> whatDaos) {
    Set<What> daos = Sets.newHashSet();
    for (WhatDAO currentWhat : whatDaos) {
      daos.add(new What(currentWhat.getName(), currentWhat.getValue()));
    }
    return daos;
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
  
  @Override
  public EventDAO getSingleUnprocessedEvent(Connection conn) throws SQLException {
    EventDAO eventDao = null;
    PreparedStatement statementUnprocessedEventRecord = null;
    ResultSet rsUnprocessedEventQuery = null;
    String textName = null;
    CSOutputDao outDaoImpl = new  CSOutputDaoImpl();
    String unprocessedEventRecordQuery = QueryConstants.UNPROCESSED_EVENT_QUERY.toString();
    CSExperimentDefinitionDao expDefDaoImpl = new CSExperimentDefinitionDaoImpl();
    
    try {
      List<Long> erroredExperimentIds = expDefDaoImpl.getErroredExperimentDefinition();
      if (erroredExperimentIds != null && erroredExperimentIds.size() > 0) {
        unprocessedEventRecordQuery = unprocessedEventRecordQuery.replace(" limit 1", " and experiment_id not in(?) and experiment_id in (91013) limit 1");
        unprocessedEventRecordQuery = unprocessedEventRecordQuery.replace("?", questionMark(erroredExperimentIds));
      } else {
        unprocessedEventRecordQuery = unprocessedEventRecordQuery.replace(" limit 1", " and experiment_id in (91013) limit 1");
      }
      log.info(unprocessedEventRecordQuery);
      statementUnprocessedEventRecord = conn.prepareStatement(unprocessedEventRecordQuery);
      if (erroredExperimentIds != null && erroredExperimentIds.size() > 0) {
        int i = 1;
        for (Long expId : erroredExperimentIds) {
          statementUnprocessedEventRecord.setLong(i++, expId);
        }
      }
      log.info(unprocessedEventRecordQuery.toString());
      rsUnprocessedEventQuery = statementUnprocessedEventRecord.executeQuery();
      if (rsUnprocessedEventQuery.next()) {
        eventDao = new EventDAO();
        eventDao.setExperimentId(rsUnprocessedEventQuery.getLong(ExperimentGroupVersionMappingColumns.EXPERIMENT_ID));
        eventDao.setExperimentVersion(rsUnprocessedEventQuery.getInt(ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION));
        eventDao.setWho(rsUnprocessedEventQuery.getString(EventServerColumns.WHO));
        eventDao.setExperimentGroupName(rsUnprocessedEventQuery.getString(EventServerColumns.GROUP_NAME));
        eventDao.setExperimentName(rsUnprocessedEventQuery.getString(EventServerColumns.EXPERIMENT_NAME));
        eventDao.setId(rsUnprocessedEventQuery.getLong(Constants.UNDERSCORE_ID));
        textName = rsUnprocessedEventQuery.getString(OutputServerColumns.TEXT);
        List<WhatDAO> whats = outDaoImpl.getOutputs(eventDao.getId());
        eventDao.setWhat(whats);
      }
      return eventDao;

    } catch (SQLException sqle) {
      log.warning("SQLException while inserting to lookup" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while inserting to lookup" + ExceptionUtil.getStackTraceAsString(e));
      throw e;
    } finally {
      try {
        if (rsUnprocessedEventQuery != null) {
          rsUnprocessedEventQuery.close();
        }
        if (statementUnprocessedEventRecord != null) {
          statementUnprocessedEventRecord.close();
        }
      } catch (SQLException e) {
        log.warning("Exception in finally block" + ExceptionUtil.getStackTraceAsString(e));
      }
    }
    
  }
  
  @Override
  public void processOlderVersionsAndAnonUsersInEventTable(Connection conn)  throws Exception {
    CSExperimentVersionMappingDao daoImpl = new CSExperimentVersionMappingDaoImpl();
    CSPivotHelperDao phDaoImpl = new CSPivotHelperDaoImpl();
    CSExperimentUserDao euImpl = new CSExperimentUserDaoImpl();
    
    boolean migrationFlag = true;
    PreparedStatement statementUpdateEventsTable = null;
    final String updateEventAndOutputsQuery = "update events e  " + 
            " join outputs o on e._id=o.event_id set e.experiment_group_version_mapping_id=?, o.input_id = ?, e.who_bk=? " + 
            " where e._id>0 and o.event_id>0 and  e.experiment_id = ? and e.experiment_version=? and e.group_name=? and o.text=? and (o.input_id is null or e.experiment_group_version_mapping_id is null) and e.who=?";
    EventDAO singleEvent = getSingleUnprocessedEvent(conn);
    Long eId = singleEvent.getId();
    log.info("processing eventId "+ eId);
    try {
      if ( singleEvent != null) {
        Set<What> whatSet = convertToWhats(singleEvent.getWhat());
        // find matching evm
        ExperimentVersionMapping evm = daoImpl.ensureEVMRecord(singleEvent.getExperimentId(), singleEvent.getId(), singleEvent.getExperimentName(), singleEvent.getExperimentVersion(), singleEvent.getExperimentGroupName(), singleEvent.getWho(), whatSet, migrationFlag);
        if ( evm == null) {
          log.info("probably deleted experiment" + singleEvent.getExperimentId());
          return;
        }
        log.info("matching evm: " + evm.getExperimentVersionMappingId());
        // update evm in event table, and input id in output table
        
        statementUpdateEventsTable = conn.prepareStatement(updateEventAndOutputsQuery);
        PacoId anonId = euImpl.getAnonymousIdAndCreate(singleEvent.getExperimentId(), singleEvent.getWho(), true);
        // update events and outputs
        for (WhatDAO singleWhat : singleEvent.getWhat()) {
          int updateCt = 0;
          statementUpdateEventsTable.setLong(1, evm.getExperimentVersionMappingId());
          // find input id
          InputOrderAndChoice matchingIOC = evm.getInputCollection().getInputOrderAndChoices().get(singleWhat.getName());
          log.info("matchingioc" +matchingIOC);
          Input input = matchingIOC != null ? matchingIOC.getInput() : null;
          Long inputId = input != null ? input.getInputId().getId() : 0L;
          if (inputId != 0) {
            statementUpdateEventsTable.setLong(2, inputId);
            statementUpdateEventsTable.setInt(3, anonId.getId().intValue());
            statementUpdateEventsTable.setLong(4, singleEvent.getExperimentId());
            statementUpdateEventsTable.setInt(5, singleEvent.getExperimentVersion());
            statementUpdateEventsTable.setString(6, evm.getGroupInfo().getName());
            statementUpdateEventsTable.setString(7, singleWhat.getName());
            statementUpdateEventsTable.setString(8, singleEvent.getWho());
            
            log.info(statementUpdateEventsTable.toString());
            updateCt = statementUpdateEventsTable.executeUpdate();
            if (updateCt >= 2) {
              updateCt = updateCt / 2;
            }
            phDaoImpl.updatePivotHelperStatus(evm.getExperimentVersionMappingId(), anonId.getId().intValue(), inputId, new Long(updateCt));
          }
        }
        if (!evm.isEventsPosted()) {
          daoImpl.updateEventsPosted(evm.getExperimentVersionMappingId());
        }
      }
    } catch (SQLException sqle) {
      log.info("sqlException while performing event udpate" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } finally {
      try {
        if (statementUpdateEventsTable != null) {
          statementUpdateEventsTable.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }      
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
