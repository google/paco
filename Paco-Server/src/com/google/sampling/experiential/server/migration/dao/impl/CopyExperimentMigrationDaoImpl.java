package com.google.sampling.experiential.server.migration.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.sampling.experiential.cloudsql.columns.ChoiceCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDefinitionColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDetailColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentGroupVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentIdVersionGroupNameColumns;
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
import com.google.sampling.experiential.dao.CSEventDao;
import com.google.sampling.experiential.dao.CSEventOutputDao;
import com.google.sampling.experiential.dao.CSExperimentDefinitionDao;
import com.google.sampling.experiential.dao.CSExperimentIdVersionDao;
import com.google.sampling.experiential.dao.CSExperimentUserDao;
import com.google.sampling.experiential.dao.CSExperimentVersionGroupMappingDao;
import com.google.sampling.experiential.dao.CSFailedEventDao;
import com.google.sampling.experiential.dao.CSGroupTypeDao;
import com.google.sampling.experiential.dao.CSGroupTypeInputMappingDao;
import com.google.sampling.experiential.dao.CSInputDao;
import com.google.sampling.experiential.dao.CSOutputDao;
import com.google.sampling.experiential.dao.dataaccess.DataType;
import com.google.sampling.experiential.dao.dataaccess.ExperimentLite;
import com.google.sampling.experiential.dao.dataaccess.ExperimentVersionMapping;
import com.google.sampling.experiential.dao.dataaccess.GroupTypeInputMapping;
import com.google.sampling.experiential.dao.dataaccess.Input;
import com.google.sampling.experiential.dao.dataaccess.InputOrderAndChoice;
import com.google.sampling.experiential.dao.dataaccess.PredefinedInputNames;
import com.google.sampling.experiential.dao.impl.CSEventDaoImpl;
import com.google.sampling.experiential.dao.impl.CSEventOutputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentDefinitionDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentIdVersionDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentUserDaoImpl;
import com.google.sampling.experiential.dao.impl.CSExperimentVersionGroupMappingDaoImpl;
import com.google.sampling.experiential.dao.impl.CSFailedEventDaoImpl;
import com.google.sampling.experiential.dao.impl.CSGroupTypeDaoImpl;
import com.google.sampling.experiential.dao.impl.CSGroupTypeInputMappingDaoImpl;
import com.google.sampling.experiential.dao.impl.CSInputDaoImpl;
import com.google.sampling.experiential.dao.impl.CSOutputDaoImpl;
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
  final String UPDATE_EVENTS_EXPT_VERSION_NULL = "update " + EventServerColumns.TABLE_NAME + " set  " + EventServerColumns.EXPERIMENT_VERSION + " = 0 where " + EventServerColumns.EXPERIMENT_VERSION + " is null and  " + Constants.UNDERSCORE_ID + " > 0";
  final String UPDATE_EVENTS_GROUP_NAME_NULL = "update " + EventServerColumns.TABLE_NAME + "  set " + EventServerColumns.GROUP_NAME + " = 'unknown' where " + Constants.UNDERSCORE_ID +  " > 0 and " + EventServerColumns.GROUP_NAME + " is null";
  final String CREATE_TABLE_DATATYPE = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ DataTypeColumns.TABLE_NAME+"` (" +
          DataTypeColumns.DATA_TYPE_ID + " INT NOT NULL AUTO_INCREMENT," +
          DataTypeColumns.NAME + " VARCHAR(100) NOT NULL," +
          DataTypeColumns.IS_NUMERIC + " BIT(1) DEFAULT NULL," +
          DataTypeColumns.MULTI_SELECT + "  BIT(1) DEFAULT NULL," +
          DataTypeColumns.RESPONSE_MAPPING_REQUIRED + " BIT(1) DEFAULT NULL," +
          " PRIMARY KEY (`"+ DataTypeColumns.DATA_TYPE_ID +"`)," +
          " UNIQUE KEY `name_type_UNIQUE` (`" + DataTypeColumns.NAME + "`, `" + DataTypeColumns.IS_NUMERIC + "`,`"
                  + DataTypeColumns.MULTI_SELECT + "`,`" + DataTypeColumns.RESPONSE_MAPPING_REQUIRED + "`)) " +
          " DEFAULT CHARACTER SET = utf8mb4" ; 
  final String CREATE_TABLE_EXTERN_STRING_LIST_LABEL = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExternStringListLabelColumns.TABLE_NAME+"` (" +
          ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
          ExternStringListLabelColumns.LABEL + " VARCHAR(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL," +
          " PRIMARY KEY (`"+ ExternStringListLabelColumns.EXTERN_STRING_LIST_LABEL_ID +"`)," +
          " UNIQUE KEY `type_UNIQUE` (`" + ExternStringListLabelColumns.LABEL + "`(500)))" +
          " DEFAULT CHARACTER SET = utf8mb4" ;
  final String CREATE_TABLE_EXTERN_STRING_INPUT = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExternStringInputColumns.TABLE_NAME+"` (" +
          ExternStringInputColumns.EXTERN_STRING_INPUT_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
          ExternStringInputColumns.LABEL + " VARCHAR(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL," +
          " PRIMARY KEY (`"+ ExternStringInputColumns.EXTERN_STRING_INPUT_ID +"`)," +
          " UNIQUE KEY `type_UNIQUE` (`" + ExternStringInputColumns.LABEL + "`(500)))" +
          " DEFAULT CHARACTER SET = utf8mb4" ;
   final String CREATE_TABLE_CHOICE_COLLECTION = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ChoiceCollectionColumns.TABLE_NAME+"` (" +
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
   
   final String CREATE_TABLE_EXPERIMENT_DETAIL = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExperimentDetailColumns.TABLE_NAME+"` (" +
           ExperimentDetailColumns.EXPERIMENT_DETAIL_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
           ExperimentDetailColumns.EXPERIMENT_NAME + " VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL," +
           ExperimentDetailColumns.DESCRIPTION + " VARCHAR(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL," +
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
   final String CREATE_TABLE_GROUP_DETAIL = "CREATE TABLE IF NOT EXISTS `pacodb`."+ GroupDetailColumns.TABLE_NAME+" (" +
           GroupDetailColumns.GROUP_DETAIL_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
           GroupDetailColumns.NAME + " VARCHAR(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL," +
           GroupDetailColumns.GROUP_TYPE_ID + " INT(11) NULL DEFAULT NULL, " +
           GroupDetailColumns.CUSTOM_RENDERING + " MEDIUMTEXT NULL," +
           GroupDetailColumns.END_OF_DAY_GROUP + " VARCHAR(500) NULL DEFAULT NULL," +
           GroupDetailColumns.FIXED_DURATION + " BIT(1) NULL," +
           GroupDetailColumns.START_DATE + " datetime NULL," +
           GroupDetailColumns.END_DATE + " datetime NULL," +
           GroupDetailColumns.RAW_DATA_ACCESS + " BIT(1) NULL," +
           " PRIMARY KEY (`"+ GroupDetailColumns.GROUP_DETAIL_ID +"`))" +
           " DEFAULT CHARACTER SET = utf8mb4" ;
   final String CREATE_TABLE_INPUT = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ InputColumns.TABLE_NAME+"` (" +
           InputColumns.INPUT_ID + " BIGINT(20) NOT NULL AUTO_INCREMENT," +
           InputColumns.NAME_ID + " BIGINT(20) NOT NULL," +
           InputColumns.TEXT_ID + " BIGINT(20) NOT NULL," +
           InputColumns.REQUIRED + " BIT(1) NULL DEFAULT 0," +
           InputColumns.CONDITIONAL + "  mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL," +
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
   final String CREATE_TABLE_INPUT_COLLECTION = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ InputCollectionColumns.TABLE_NAME+"` (" +
           InputCollectionColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
           InputCollectionColumns.INPUT_COLLECTION_ID + " BIGINT(20) NOT NULL," +
           InputCollectionColumns.INPUT_ID + " BIGINT(20) NOT NULL," +
           InputCollectionColumns.CHOICE_COLLECTION_ID + " BIGINT(20) NULL," +
           InputCollectionColumns.INPUT_ORDER + " INT(11) NOT NULL," +
           " PRIMARY KEY (`"+ InputCollectionColumns.INPUT_COLLECTION_ID +"`,`" +
                   InputCollectionColumns.EXPERIMENT_ID +"`,`" +
                   InputCollectionColumns.INPUT_ID +"`)) " +
         " DEFAULT CHARACTER SET = utf8mb4" ;
   final String CREATE_TABLE_EXPERIMENT_VERSION_GROUP_MAPPING = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExperimentGroupVersionMappingColumns.TABLE_NAME+"` (" +
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
   final String CREATE_TABLE_INFORMED_CONSENT = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ InformedConsentColumns.TABLE_NAME+"` (" +
           InformedConsentColumns.INFORMED_CONSENT_ID + " BIGINT(20) NOT NULL," +
           InformedConsentColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
           InformedConsentColumns.INFORMED_CONSENT + " LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL," +
           " PRIMARY KEY (`" + InformedConsentColumns.INFORMED_CONSENT_ID + "`, `"+ InformedConsentColumns.EXPERIMENT_ID +"`)) " +  
           " DEFAULT CHARACTER SET = utf8mb4" ;
   final String CREATE_TABLE_GROUP_TYPE = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ GroupTypeColumns.TABLE_NAME+"` (" +
           GroupTypeColumns.GROUP_TYPE_ID + " INT NOT NULL AUTO_INCREMENT," +
           GroupTypeColumns.GROUP_TYPE_NAME + " VARCHAR(250) NOT NULL," +
           " PRIMARY KEY (`" + GroupTypeColumns.GROUP_TYPE_ID + "`)) " +  
           " DEFAULT CHARACTER SET = utf8mb4" ;
   final String CREATE_TABLE_GROUP_TYPE_INPUT_MAPPING = "CREATE TABLE IF NOT EXISTS `pacodb`.`" + GroupTypeInputMappingColumns.TABLE_NAME  + "` ( " + 
           GroupTypeInputMappingColumns.GROUP_TYPE_INPUT_MAPPING_ID + " int(11) NOT NULL AUTO_INCREMENT, " + 
           GroupTypeInputMappingColumns.GROUP_TYPE_ID + " varchar(45) DEFAULT NULL, " + 
           GroupTypeInputMappingColumns.INPUT_ID + " bigint(20) DEFAULT NULL, " +
           "PRIMARY KEY (`"+ GroupTypeInputMappingColumns.GROUP_TYPE_INPUT_MAPPING_ID +"`), " + 
           " UNIQUE KEY `groupt_type_id_input_id_UNIQUE` (`" + GroupTypeInputMappingColumns.GROUP_TYPE_ID + "`, `"+GroupTypeInputMappingColumns.INPUT_ID+"`)) " +
           " ENGINE=InnoDB DEFAULT CHARSET = utf8mb4" ;
   final String CREATE_TABLE_PIVOT_HELPER = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ PivotHelperColumns.TABLE_NAME+"` (" +
           PivotHelperColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID + " BIGINT(20) NOT NULL," +
           PivotHelperColumns.ANON_WHO + " INT NOT NULL," +
           PivotHelperColumns.INPUT_ID + " BIGINT(20) NOT NULL," +
           PivotHelperColumns.EVENTS_POSTED + " BIGINT(20) NOT NULL DEFAULT 0," +
           PivotHelperColumns.PROCESSED + " BIT(1) DEFAULT 0," +
           " PRIMARY KEY (`" + PivotHelperColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID + "`, `"+ PivotHelperColumns.ANON_WHO +"`, `"+ PivotHelperColumns.INPUT_ID+"`)) " +  
           " DEFAULT CHARACTER SET = utf8mb4" ;
   final String CREATE_TABLE_EXPERIMENT_DEFINTION = "CREATE TABLE IF NOT EXISTS `pacodb`.`" + ExperimentDefinitionColumns.TABLE_NAME + "` (" +
           " `" + ExperimentDefinitionColumns.ID + "` bigint(20) NOT NULL, " +
            " `" + ExperimentDefinitionColumns.VERSION + "` int(11) NOT NULL, " +
            " `" + ExperimentDefinitionColumns.SOURCE_JSON + "` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL, " +
            " `" + ExperimentDefinitionColumns.CONVERTED_JSON + "` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL, " +
            " `" + ExperimentDefinitionColumns.MIGRATION_STATUS + "` int(11) DEFAULT NULL, " +
            " `" + ExperimentDefinitionColumns.ERROR_MESSAGE + "` varchar(500) DEFAULT NULL, " +
           " PRIMARY KEY (`" + ExperimentDefinitionColumns.ID +"`,`" + ExperimentDefinitionColumns.VERSION + "`)) " +
          " DEFAULT CHARACTER SET = utf8mb4" ;
   final String CREATE_TABLE_EVENT_OLD_GROUP_NAME = "CREATE TABLE IF NOT EXISTS `pacodb`.`event_old_group_name` (`event_id` BIGINT(20) NOT NULL,`old_group_name` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL,PRIMARY KEY (`event_id`))";
   final String CREATE_TABLE_EXPERIMENT_DEFINITION_BK = "CREATE TABLE IF NOT EXISTS `pacodb`.`" + ExperimentDefinitionColumns.TABLE_NAME + "_bk` (" +
           " `" + ExperimentDefinitionColumns.ID + "` bigint(20) NOT NULL, " +
            " `" + ExperimentDefinitionColumns.VERSION + "` int(11) NOT NULL, " +
            " `" + ExperimentDefinitionColumns.SOURCE_JSON + "` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  DEFAULT NULL, " +
           " PRIMARY KEY (`" + ExperimentDefinitionColumns.ID +"`,`" + ExperimentDefinitionColumns.VERSION + "`)) " +
          " DEFAULT CHARACTER SET = utf8mb4" ;
   final String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ UserColumns.TABLE_NAME+"` (" +
           UserColumns.USER_ID + " INT NOT NULL AUTO_INCREMENT," +
           UserColumns.WHO + " VARCHAR(500) NOT NULL," +
           " PRIMARY KEY (`" + UserColumns.USER_ID + "`)," +
           " UNIQUE INDEX `who_unique_index` (`"+ UserColumns.WHO + "` ASC))";
   final String CREATE_TABLE_EXPERIMENT_USER = "CREATE TABLE IF NOT EXISTS `pacodb`.`"+ ExperimentUserColumns.TABLE_NAME +"` (" +
           ExperimentUserColumns.EXPERIMENT_ID + " BIGINT(20) NOT NULL," +
           ExperimentUserColumns.USER_ID + " INT NOT NULL," +
           ExperimentUserColumns.EXP_USER_ANON_ID + " INT NOT NULL," +
           ExperimentUserColumns.USER_TYPE + " CHAR(1) NOT NULL," +
           " PRIMARY KEY (`" + ExperimentUserColumns.EXPERIMENT_ID+ "`,`" +ExperimentUserColumns.USER_ID+ "`), "+ 
           " UNIQUE KEY `experiment_id_anon_id_UNIQUE` (`" + ExperimentUserColumns.EXPERIMENT_ID +"`,`" + ExperimentUserColumns.EXP_USER_ANON_ID + "`))";
   final String CREATE_TABLE_EXP_ID_VERSION_GROUP_NAME = "CREATE TABLE  IF NOT EXISTS `" + ExperimentIdVersionGroupNameColumns.TABLE_NAME + "` (`"+
           ExperimentIdVersionGroupNameColumns.EXPERIMENT_ID + "` bigint(20) NOT NULL,`" +  
           ExperimentIdVersionGroupNameColumns.EXPERIMENT_VERSION + "` int(11) NOT NULL, `" + 
           ExperimentIdVersionGroupNameColumns.GROUP_NAME + "` VARCHAR(700) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL, `"  +
           ExperimentIdVersionGroupNameColumns.STATUS + "` int(11) default 0, "
           + " PRIMARY KEY (`" + ExperimentIdVersionGroupNameColumns.EXPERIMENT_ID + "`,`" + ExperimentIdVersionGroupNameColumns.EXPERIMENT_VERSION + "`,`" + ExperimentIdVersionGroupNameColumns.GROUP_NAME + "`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin";

   final String insertDataTypeSql1 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('open text', 1, 0, 0)";
   final String insertDataTypeSql2 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('open text', 0, 0, 0)";
   final String insertDataTypeSql3 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('list', 1, 0, 1)";
   final String insertDataTypeSql4 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('list', 1, 1, 1)";
   final String insertDataTypeSql5 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('likert', 1, 0, 0)";
   final String insertDataTypeSql7 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('number', 1, 0, 0)";
   final String insertDataTypeSql8 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('likert_smileys', 0, 0, 0)";
   final String insertDataTypeSql9 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('undefined', 0, 0, 0)";
   final String insertDataTypeSql10 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('photo', 0, 0, 0)";
   
   final String insertDataTypeSql11 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('', 0, 0, 0)";
   final String insertDataTypeSql12 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('location', 0, 0, 0)";
   final String insertDataTypeSql13 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('audio', 0, 0, 0)";
   final String insertDataTypeSql14 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('number', 1, 1, 0)";
   final String insertDataTypeSql15 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('', 1, 1, 0)";
   final String insertDataTypeSql16 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('likert', 1, 1, 0)";
   final String insertDataTypeSql17 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('open text', 0, 1, 0)";
   final String insertDataTypeSql18 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('', 0, 1, 0)";
   final String insertDataTypeSql19 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('graph', 0, 0, 0)";
   final String insertDataTypeSql20 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('likert_smileys', 0, 1, 0)";

   final String insertDataTypeSql21 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('location', 0, 1, 0)";
   final String insertDataTypeSql22 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('photo', 0, 1, 0)";
   final String insertDataTypeSql23 = "INSERT INTO `pacodb`.`"+DataTypeColumns.TABLE_NAME+"` (`"+DataTypeColumns.NAME+"`, `"+DataTypeColumns.IS_NUMERIC+"`, `"+DataTypeColumns.MULTI_SELECT+"`, `"+DataTypeColumns.RESPONSE_MAPPING_REQUIRED+"`) VALUES ('audio', 0, 1, 0)";

       
   final String insertDataTypeSql24 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.SYSTEM+"')";
   final String insertDataTypeSql25 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.SURVEY+"')";
   final String insertDataTypeSql26 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.APPUSAGE_ANDROID+"')";
   final String insertDataTypeSql27 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.NOTIFICATION+"')";
   final String insertDataTypeSql28 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.ACCESSIBILITY+"')";
   final String insertDataTypeSql29 = "INSERT INTO `pacodb`.`"+GroupTypeColumns.TABLE_NAME+"` (`"+GroupTypeColumns.GROUP_TYPE_NAME+"`) VALUES ('"+GroupTypeEnum.PHONESTATUS+"')";
   
   final String addNewColumnsSql1 = "ALTER TABLE `pacodb`.`"+OutputServerColumns.TABLE_NAME+"` " +
           " ADD COLUMN `"+OutputServerColumns.INPUT_ID+"` BIGINT(20) NULL AFTER `archive_flag`," +
           " ADD INDEX `fk_text_input_id_idx` (`input_id` ASC)";
   final String addNewColumnsSql2 = "ALTER TABLE `pacodb`.`"+OutputServerColumns.TABLE_NAME+"`  " + 
                 " ADD CONSTRAINT `fk_text_input_id` " +  
                 "   FOREIGN KEY (`"+OutputServerColumns.INPUT_ID+"`) " + 
                 "  REFERENCES `pacodb`.`"+InputColumns.TABLE_NAME+"` (`"+InputColumns.INPUT_ID+"`) "  +
                 " ON DELETE NO ACTION " + 
                 "  ON UPDATE NO ACTION ";
   final String addNewColumnsSql3 = "ALTER TABLE `pacodb`.`" + EventServerColumns.TABLE_NAME +  "`  " + 
   " ADD COLUMN `"+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID  + "` BIGINT(20) NULL AFTER `sort_date`, " +
   " ADD COLUMN `who_bk` BIGINT(20) NULL AFTER `"+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID  + "`," +
   " ADD INDEX `fk_exp_group_version_mapping_idx` (`"+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID  + "` ASC)";
   final String addNewColumnsSql4 = "ALTER TABLE `pacodb`.`" + EventServerColumns.TABLE_NAME +  "`  " +
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
   " REFERENCES `pacodb`.`"+InputColumns.TABLE_NAME+"` (`" +InputColumns.INPUT_ID+ "`) " + 
   " ON DELETE NO ACTION " +
   " ON UPDATE NO ACTION ";
   final String addNewColumnsSql7 = "ALTER TABLE `pacodb`.`" + ExperimentUserColumns.TABLE_NAME+ "` " + 
   " ADD INDEX `eu_u_userid_fk_idx` (`" + ExperimentUserColumns.USER_ID + "`user_id` ASC)";
   final String addNewColumnsSql8 = "ALTER TABLE `pacodb`.`" + ExperimentUserColumns.TABLE_NAME+ "` " + 
   " ADD CONSTRAINT `eu_u_userid_fk` " +
   " FOREIGN KEY (`user_id`) " + 
   " REFERENCES `pacodb`.`" + UserColumns.TABLE_NAME + "` (`" +UserColumns.USER_ID+ "`) " + 
   " ON DELETE NO ACTION " +
   " ON UPDATE NO ACTION ";
   final String addNewColumnsSql9 = "ALTER TABLE `pacodb`.`"+ ExperimentDetailColumns.TABLE_NAME  + "` " + 
   " ADD INDEX `e_ic_informed_consent_fk_idx` (`"+ ExperimentDetailColumns.INFORMED_CONSENT_ID +"` ASC)";
   final String addNewColumnsSql10 = "ALTER TABLE `pacodb`.`"+ ExperimentDetailColumns.TABLE_NAME  + "`" +
   " ADD CONSTRAINT `e_ic_informed_consent_fk` " +
   " FOREIGN KEY (`"+ InformedConsentColumns.INFORMED_CONSENT_ID  + "`) " + 
   " REFERENCES `pacodb`.`"+ InformedConsentColumns.TABLE_NAME  + "` (`"+ InformedConsentColumns.INFORMED_CONSENT_ID  + "`) " + 
   " ON DELETE NO ACTION " +
   " ON UPDATE NO ACTION ";
   final String addNewColumnsSql11 = "ALTER TABLE `pacodb`.`" + EventServerColumns.TABLE_NAME +  "`  " +
           " ADD INDEX `evg_idx` (`"+EventServerColumns.EXPERIMENT_ID+"`,`"+EventServerColumns.EXPERIMENT_VERSION+"`,`"+EventServerColumns.GROUP_NAME+"`,`"+EventServerColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID +"`)";
   final String SELECT_EVENTS_MATCHING_PREDFINED_INPUT_NAMES = "Select "+Constants.UNDERSCORE_ID+", "+ EventServerColumns.GROUP_NAME +" from "+EventServerColumns.TABLE_NAME +
           " e join "+OutputServerColumns.TABLE_NAME + " o on e."+Constants.UNDERSCORE_ID+ "= o."+OutputServerColumns.EVENT_ID+" where "+EventServerColumns.EXPERIMENT_ID+"=? and "+EventServerColumns.EXPERIMENT_VERSION+"=? and "+EventServerColumns.GROUP_NAME+" = ? and "+ OutputServerColumns.TEXT+ "=? limit 1000 ";
   final String SELECT_EVENTS_MATCHING_PREDFINED_FOR_NOTIF_AND_ACC = "Select " +Constants.UNDERSCORE_ID+ " , " + EventServerColumns.GROUP_NAME + " from "+EventServerColumns.TABLE_NAME +
           " e join "+OutputServerColumns.TABLE_NAME + " o on e."+Constants.UNDERSCORE_ID+ "= o."+OutputServerColumns.EVENT_ID+" where "+EventServerColumns.EXPERIMENT_ID+"=? and "+EventServerColumns.EXPERIMENT_VERSION+"=? and "+EventServerColumns.GROUP_NAME+" = ? ";
   final String UPDATE_EVENTS_QUERY = "update " + EventServerColumns.TABLE_NAME +" e  " + 
           "  set  " + EventServerColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID +  " =?, " + EventServerColumns.WHO +"_bk=? " + 
           " where  "+ Constants.UNDERSCORE_ID+" = ? and " + EventServerColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID + " is null";
   final String UPDATE_OUTPUTS_QUERY = "update  " + OutputServerColumns.TABLE_NAME + " set " + OutputServerColumns.INPUT_ID + " = ?" + 
           " where  " + OutputServerColumns.EVENT_ID + " =? and  " + OutputServerColumns.TEXT + " =?";
   String RENAME_EVENT_TABLE_COLUMNS = "ALTER TABLE `pacodb`.`" + EventServerColumns.TABLE_NAME +  "`  " +
           " CHANGE COLUMN `" + EventServerColumns.EXPERIMENT_ID+ "` `experiment_id_old` BIGINT(20) NULL DEFAULT NULL ," +
           " CHANGE COLUMN `" + EventServerColumns.EXPERIMENT_NAME + "` `experiment_name_old` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL , " + 
           " CHANGE COLUMN `" + EventServerColumns.EXPERIMENT_VERSION + "` `experiment_version_old` INT(11) NULL DEFAULT NULL , " +
           " CHANGE COLUMN `" + EventServerColumns.GROUP_NAME + "` `group_name_old` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL,"
           + "CHANGE COLUMN `" + EventServerColumns.WHO + "` `who_old` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL,"
           + "CHANGE COLUMN `" + EventServerColumns.WHO + "_bk` `who` VARCHAR(500) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL";
   List<String> grpList = Lists.newArrayList();
     
  @Override
  public boolean copyExperimentCreateTables() throws SQLException {
    String[] creationQueries = null;
    
    creationQueries = new String[] { CREATE_TABLE_DATATYPE, CREATE_TABLE_EXTERN_STRING_LIST_LABEL, CREATE_TABLE_EXTERN_STRING_INPUT, CREATE_TABLE_CHOICE_COLLECTION, CREATE_TABLE_EXPERIMENT_DETAIL, 
                         CREATE_TABLE_GROUP_DETAIL, CREATE_TABLE_INPUT, CREATE_TABLE_INPUT_COLLECTION, CREATE_TABLE_EXPERIMENT_VERSION_GROUP_MAPPING, CREATE_TABLE_INFORMED_CONSENT, 
                         CREATE_TABLE_GROUP_TYPE, CREATE_TABLE_GROUP_TYPE_INPUT_MAPPING, CREATE_TABLE_PIVOT_HELPER, CREATE_TABLE_EXPERIMENT_DEFINTION, CREATE_TABLE_EVENT_OLD_GROUP_NAME, 
                         CREATE_TABLE_EXPERIMENT_DEFINITION_BK, CREATE_TABLE_EXP_ID_VERSION_GROUP_NAME};
    
   return executeCreationOrInsertionQuerys(creationQueries);
  }
  

  @Override
  public boolean anonymizeParticipantsCreateTables() throws SQLException {
    String[] creationQueries = new String[2];
    creationQueries = new String[] { CREATE_TABLE_USER, CREATE_TABLE_EXPERIMENT_USER};
    return executeCreationOrInsertionQuerys(creationQueries);
  }
  
  
  @Override
  public boolean insertPredefinedRecords() throws Exception {            
    String[] insertionQueries = new String[] { 
                                  insertDataTypeSql1, insertDataTypeSql2,
                                  insertDataTypeSql3, insertDataTypeSql4,
                                  insertDataTypeSql5,
                                  insertDataTypeSql7, insertDataTypeSql8,
                                  insertDataTypeSql9, insertDataTypeSql10,
                                  insertDataTypeSql11, insertDataTypeSql12,
                                  insertDataTypeSql13, insertDataTypeSql14,
                                  insertDataTypeSql15,
                                  insertDataTypeSql16, insertDataTypeSql17,
                                  insertDataTypeSql18, insertDataTypeSql19,
                                  insertDataTypeSql20, insertDataTypeSql21,
                                  insertDataTypeSql22, insertDataTypeSql23,
                                  insertDataTypeSql24, insertDataTypeSql25,
                                  insertDataTypeSql26, insertDataTypeSql27,
                                  insertDataTypeSql28, insertDataTypeSql29
                                  };
    executeCreationOrInsertionQuerys(insertionQueries);
    // pop grp type input
    populateGroupTypeInput();
    return true;

  }
  
  @Override
  public boolean addModificationsToExistingTables()  throws SQLException {
    String[] modificationQueries = new String[] { 
              addNewColumnsSql1, addNewColumnsSql2 ,
              addNewColumnsSql3, addNewColumnsSql4,
              addNewColumnsSql5, 
              addNewColumnsSql6,
              addNewColumnsSql7, addNewColumnsSql8,
              addNewColumnsSql9, addNewColumnsSql10,
              addNewColumnsSql11
              };
    executeCreationOrInsertionQuerys(modificationQueries);
    return true;
  }
  
  @Override
  public boolean copyExperimentPopulateExperimentBundleTables()  throws SQLException {
    int migrationStatus = 1;
    CSExperimentVersionGroupMappingDao evMappingDaoImpl = new CSExperimentVersionGroupMappingDaoImpl();
    CSExperimentUserDao expUserDaoImpl = new CSExperimentUserDaoImpl();
    CSExperimentDefinitionDao expDefDaoImpl = new CSExperimentDefinitionDaoImpl();
    List<ExperimentDAO> experimentList = readFromExperimentDefinition(migrationStatus);

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
  
  private List<ExperimentDAO> readFromExperimentDefinition(Integer migrationStatus) throws SQLException {
    List<ExperimentDAO> experimentList = Lists.newArrayList();
    Connection conn = null;
    ResultSet rs = null;
    String experimentJson = null;
    ExperimentDAO experimentDao = null;
    PreparedStatement statementSelectExperimentJson = null;
    String query = null;
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      query = QueryConstants.GET_ALL_EXPERIMENT_JSON.toString();
      if (migrationStatus == 1) {
        query = query + " and " + ExperimentDefinitionColumns.ERROR_MESSAGE+ "  is null and  " + ExperimentDefinitionColumns.CONVERTED_JSON + " is not null";
      }
      statementSelectExperimentJson = conn.prepareStatement(query);
      statementSelectExperimentJson.setInt(1, migrationStatus);
      log.info(query);
      rs = statementSelectExperimentJson.executeQuery();
      while(rs.next()) {
        if (migrationStatus == 0) {
          experimentJson = rs.getString(ExperimentDefinitionColumns.SOURCE_JSON);
          experimentDao = JsonConverter.fromSingleEntityJson(experimentJson.substring(1,experimentJson.length()-1));
        } else if (migrationStatus == 1) {
          experimentJson = rs.getString(ExperimentDefinitionColumns.CONVERTED_JSON);
          experimentDao = JsonConverter.fromSingleEntityJson(experimentJson);
        }
        experimentList.add(experimentDao);
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
    if ( expDefDao.getTotalRecordsInExperimentDefinitionBackupTable() == 0) {
      List<ExperimentDAO> experimentList = readFromDataStore();
      if (experimentList == null || experimentList.isEmpty()) {
        return false;
      }
      
      for (ExperimentDAO eachExperiment : experimentList) {
        try {
          expDefDao.insertExperimentDefinitionBackup(eachExperiment.getId(), eachExperiment.getVersion(), JsonConverter.jsonify(Lists.newArrayList(eachExperiment), null, null, null));
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
    String errorMessage = null;
    int migrationStatus = 0 ;
    CSExperimentDefinitionDao expDefDao = new CSExperimentDefinitionDaoImpl();
    ExperimentDAOConverter daoConverter = new ExperimentDAOConverter();
    final ExperimentService experimentService = ExperimentServiceFactory.getExperimentService();
    List<ExperimentDAO> experimentList = readFromExperimentDefinition(migrationStatus);
    
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
  
  @Override
  public boolean removeUnwantedPredefinedExperiments() throws Exception {            
    
    CSExperimentDefinitionDao expDefDaoImpl = new CSExperimentDefinitionDaoImpl();
    List<Long> expIdsToBeDeleted = Lists.newArrayList();
    // Romanian expt
    expIdsToBeDeleted.add(6651607195844608L);
    expDefDaoImpl.deleteExperiment(expIdsToBeDeleted);
    return true;
  }
  
  private boolean populateGroupTypeInput()  throws Exception {
    
    CSInputDao inputDaoImpl = new CSInputDaoImpl();
    CSGroupTypeInputMappingDao predfinedDaoImpl = new CSGroupTypeInputMappingDaoImpl();
    CSGroupTypeDao groupTypeDapImpl = new CSGroupTypeDaoImpl();
    DataType openTextDataType = new DataType("open text", false, false); 
  
    try {
      //  System
      Input openTextJoined = new Input(PredefinedInputNames.JOINED, false, null, new DataType("open text", true, false), PredefinedInputNames.JOINED, 0, null, null, null);
      Input openTextSchedule = new Input(PredefinedInputNames.SCHEDULE, false, null, openTextDataType, PredefinedInputNames.SCHEDULE, 0, null, null, null);
      // record Phone Details
      Input openTextModel = new Input(PredefinedInputNames.MODEL, false, null, openTextDataType, PredefinedInputNames.MODEL, 0, null, null, null);
      Input openTextAndroid = new Input(PredefinedInputNames.ANDROID, false, null, openTextDataType, PredefinedInputNames.ANDROID, 0, null, null, null);
      Input openTextMake = new Input(PredefinedInputNames.MAKE, false, null, openTextDataType, PredefinedInputNames.MAKE, 0, null, null, null);
      Input openTextCarrier = new Input(PredefinedInputNames.CARRIER, false, null, openTextDataType, PredefinedInputNames.CARRIER, 0, null, null, null);
      Input openTextDisplay = new Input(PredefinedInputNames.DISPLAY, false, null, openTextDataType, PredefinedInputNames.DISPLAY, 0, null, null, null);
            
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
      Input openTextAppUsage = new Input(PredefinedInputNames.APPS_USED, false, null, openTextDataType, PredefinedInputNames.APPS_USED, 0, null, null,  null);
      Input openTextAppUsageRaw = new Input(PredefinedInputNames.APPS_USED_RAW, false, null, openTextDataType, PredefinedInputNames.APPS_USED_RAW, 0, null, null, null);
      Input openTextForeGround = new Input(PredefinedInputNames.FOREGROUND, false, null, openTextDataType, PredefinedInputNames.FOREGROUND, 0, null, null, null);
      Integer grpTypeAppUsageId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.APPUSAGE_ANDROID.name());
      inputDaoImpl.insertInput(openTextAppUsage);
      inputDaoImpl.insertInput(openTextAppUsageRaw);
      inputDaoImpl.insertInput(openTextForeGround);
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextAppUsage));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextAppUsageRaw));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypeAppUsageId, openTextForeGround));
      
      // BackGround - phoneEvent
      Input openTextPhoneOn = new Input(PredefinedInputNames.PHONE_ON, false, null, openTextDataType, PredefinedInputNames.PHONE_ON, 0, null, null, null);
      Input openTextPhoneOff = new Input(PredefinedInputNames.PHONE_OFF, false, null, openTextDataType, PredefinedInputNames.PHONE_OFF, 0, null, null, null);
      Integer grpTypePhoneOnId = groupTypeDapImpl.getGroupTypeId(GroupTypeEnum.PHONESTATUS.name());
      inputDaoImpl.insertInput(openTextPhoneOn);
      inputDaoImpl.insertInput(openTextPhoneOff);
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypePhoneOnId, openTextPhoneOn));
      predfinedDaoImpl.insertGroupTypeInputMapping(new GroupTypeInputMapping(grpTypePhoneOnId, openTextPhoneOff));
      
      // BackGround - logAccessibility  accessibilityEventText, accessibilityEventPackage, accessibilityEventText, accessibilityEventType
      Input openTextAccEventText = new Input(PredefinedInputNames.ACCESSIBILITY_EVENT_TEXT, false, null, openTextDataType, PredefinedInputNames.ACCESSIBILITY_EVENT_TEXT, 0, null, null, null);
      Input openTextAccEventPackage = new Input(PredefinedInputNames.ACCESSIBILITY_EVENT_PACKAGE, false, null, openTextDataType, PredefinedInputNames.ACCESSIBILITY_EVENT_PACKAGE, 0, null, null, null);
      Input openTextAccEventClass = new Input(PredefinedInputNames.ACCESSIBILITY_EVENT_CLASS, false, null, openTextDataType, PredefinedInputNames.ACCESSIBILITY_EVENT_CLASS, 0, null, null, null);
      Input openTextAccEventType = new Input(PredefinedInputNames.ACCESSIBILITY_EVENT_TYPE, false, null, openTextDataType, PredefinedInputNames.ACCESSIBILITY_EVENT_TYPE, 0, null, null, null);
      Input openTextAccEventContentDescription = new Input(PredefinedInputNames.ACCESSIBILITY_EVENT_CONTENT_DESCRIPTION, false, null, openTextDataType, PredefinedInputNames.ACCESSIBILITY_EVENT_CONTENT_DESCRIPTION, 0, null, null, null);
      
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
      log.warning("SQLException while adding new group type " + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch (Exception e) {
      log.warning("GException while adding new group type " + ExceptionUtil.getStackTraceAsString(e));
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
  
  private boolean updateQuery(String[] updateQrys) throws SQLException {
    Connection conn = null;
    PreparedStatement statementUpdate = null;
    Integer[] updateCts = new Integer[updateQrys.length];
    
    try {
      int i = 0;
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for (String s : updateQrys) {
        statementUpdate = conn.prepareStatement(s);
        updateCts[i++] = statementUpdate.executeUpdate(); 
      }
       
      log.info("update qry executed" + Arrays.toString(updateCts));
    } finally {
      try {
        if (statementUpdate != null) {
          statementUpdate.close();
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
  
  
  private boolean executeCreationOrInsertionQuerys(String[] creationQrys) throws SQLException {
    boolean isComplete = false;
    
    Connection conn = null;
    PreparedStatement statementCreateOrInsertQuery = null;

    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      for ( int i = 0; i < creationQrys.length; i++) {
        statementCreateOrInsertQuery = conn.prepareStatement(creationQrys[i]);
        log.info(creationQrys[i]);
        statementCreateOrInsertQuery.execute();
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
        if (statementCreateOrInsertQuery != null) {
          statementCreateOrInsertQuery.close();
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
  public boolean updateEventTableExperimentVersionAndGroupNameNull() throws SQLException {
    String[] qry = new String[] { UPDATE_EVENTS_EXPT_VERSION_NULL ,  UPDATE_EVENTS_GROUP_NAME_NULL};
    return updateQuery(qry);
  }
  
  @Override
  public boolean updateEventTableGroupNameNullToUnknown() throws SQLException {
    String[] qry = new String[] { UPDATE_EVENTS_GROUP_NAME_NULL };
    return updateQuery(qry);
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
  public List<EventDAO> getSingleBatchUnprocessedEvent(Connection conn, List<Long> erroredExperimentIds, String unprocessedRecordQuery, Long experimentId, Integer experimentVersion, String groupName) throws SQLException {
   
    List<EventDAO> eventDaoList = Lists.newArrayList();
    EventDAO eventDao = null;
    PreparedStatement statementUnprocessedEventRecord = null;
    ResultSet rsUnprocessedEventQuery = null;
    CSOutputDao outDaoImpl = new  CSOutputDaoImpl();
    int i = 1;
 
    try {
      if (erroredExperimentIds != null && erroredExperimentIds.size() > 0) {
        unprocessedRecordQuery = unprocessedRecordQuery.replace(" limit 1000", " and " + EventServerColumns.EXPERIMENT_ID + "  not in (?) and " + EventServerColumns.EXPERIMENT_ID+ "  = ? and  " + EventServerColumns.EXPERIMENT_VERSION + " = ? and  " + EventServerColumns.GROUP_NAME+ " =? limit 1000");
        unprocessedRecordQuery = unprocessedRecordQuery.replaceFirst("\\?", questionMark(erroredExperimentIds));
        log.info("unproc"+ unprocessedRecordQuery.toString());
      } else {
        unprocessedRecordQuery = unprocessedRecordQuery.replace(" limit 1000", " and " + EventServerColumns.EXPERIMENT_ID+ " = ? and " + EventServerColumns.EXPERIMENT_VERSION+ " = ? and  "+ EventServerColumns.GROUP_NAME +" =? limit 1000");
      }
      statementUnprocessedEventRecord = conn.prepareStatement(unprocessedRecordQuery);
      if (erroredExperimentIds != null && erroredExperimentIds.size() > 0) {
        for (Long expId : erroredExperimentIds) {
          statementUnprocessedEventRecord.setLong(i++, expId);
        }
      }
      statementUnprocessedEventRecord.setLong(i++, experimentId);
      statementUnprocessedEventRecord.setInt(i++, experimentVersion);
      statementUnprocessedEventRecord.setString(i++, groupName);
      
      log.info(unprocessedRecordQuery.toString());
      rsUnprocessedEventQuery = statementUnprocessedEventRecord.executeQuery();
      while (rsUnprocessedEventQuery.next()) {
        eventDao = new EventDAO();
        eventDao.setExperimentId(rsUnprocessedEventQuery.getLong(ExperimentGroupVersionMappingColumns.EXPERIMENT_ID));
        eventDao.setExperimentVersion(rsUnprocessedEventQuery.getInt(ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION));
        eventDao.setWho(rsUnprocessedEventQuery.getString(EventServerColumns.WHO));
        eventDao.setExperimentGroupName(rsUnprocessedEventQuery.getString(EventServerColumns.GROUP_NAME));
        eventDao.setExperimentName(rsUnprocessedEventQuery.getString(EventServerColumns.EXPERIMENT_NAME));
        eventDao.setId(rsUnprocessedEventQuery.getLong(Constants.UNDERSCORE_ID));
        List<WhatDAO> whats = outDaoImpl.getOutputsWithoutInputId(eventDao.getId());
        eventDao.setWhat(whats);
        eventDaoList.add(eventDao);
      } 
      return eventDaoList;
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
  
  private boolean isAllEventOutputsPresentInEVMRecord(ExperimentVersionMapping evm, Set<What> whatSet) {
    Map<String, InputOrderAndChoice> iocsInDB = evm.getInputCollection().getInputOrderAndChoices();
    for (What eachWhat : whatSet) {
      // for scripted inputs
      if (iocsInDB.get(eachWhat.getName()) == null) {
        return false;
      }
    }
    return true;
  }
  
  
  @Override
  public void processOlderVersionsAndAnonUsersInEventTable(Connection conn, List<Long> erroredExperimentIds, List<EventDAO> allEvents)  throws Exception {
    CSExperimentVersionGroupMappingDao daoImpl = new CSExperimentVersionGroupMappingDaoImpl();
    CSExperimentUserDao euImpl = new CSExperimentUserDaoImpl();
    CSFailedEventDao failedDaoImpl = new CSFailedEventDaoImpl();
    Set<Long> eventsPostedForEGVMId = Sets.newHashSet();
    boolean eventsUpdateNeeded = false;
    boolean outputsUpdateNeeded = false;
    boolean migrationFlag = true;
    PreparedStatement statementUpdateEventsTable = null;
    PreparedStatement statementUpdateOutputsTable = null;
    Long firstStepStartTime = System.currentTimeMillis();
    Long firstStepEndTime = null;
    Long secondStepEndTime = 0L;
    Long thirdStepEndTime = null;
    Long fourthStepEndTime = null;
    PacoId anonId = null;
    ExperimentVersionMapping matchingEVM = null;
    Map<String, PacoId> emailAnonIdMap = null;
    String updateEventsQuery = UPDATE_EVENTS_QUERY;
    String updateOutputsQuery = UPDATE_OUTPUTS_QUERY;
    try {
      statementUpdateEventsTable = conn.prepareStatement(updateEventsQuery);
      statementUpdateOutputsTable = conn.prepareStatement(updateOutputsQuery);
      int ct= 0;
      Map<String, ExperimentVersionMapping> allEVMRecords = null;
      if (allEvents != null && allEvents.size() > 0  ) {
        log.info("calling once for a batch");
        allEVMRecords = daoImpl.getAllGroupsInVersion(allEvents.get(0).getExperimentId(), allEvents.get(0).getExperimentVersion());
        if (allEVMRecords.size() == 0) {
          // if we do not hv matching group name in a newer version of experiment with group types
          failedDaoImpl.insertFailedEvent("expId: " + allEvents.get(0).getExperimentId() + "expVersion: "+ allEvents.get(0).getExperimentVersion() + ",who:"+ allEvents.get(0).getWho() + " grp name:" + allEvents.get(0).getExperimentGroupName() , "Event save, does not have matching group ", "Event save does not have matching group");
          return;
        }
        emailAnonIdMap = Maps.newHashMap();
      }
      
      for (EventDAO singleEvent : allEvents) {
        Long eId = singleEvent.getId();
        log.info("choosing eventId "+ eId + "ct" + ct++);
        if (singleEvent != null) {
          matchingEVM = allEVMRecords.get(singleEvent.getExperimentGroupName());
          Set<What> whatSet = convertToWhats(singleEvent.getWhat());
          // find matching evm
          firstStepStartTime = System.currentTimeMillis();
          if ( matchingEVM == null || !isAllEventOutputsPresentInEVMRecord(matchingEVM, whatSet)) {
            daoImpl.ensureEVMRecord(singleEvent.getExperimentId(), singleEvent.getId(), singleEvent.getExperimentName(), singleEvent.getExperimentVersion(), singleEvent.getExperimentGroupName(), singleEvent.getWho(), whatSet, migrationFlag, allEVMRecords);
            matchingEVM = allEVMRecords.get(singleEvent.getExperimentGroupName());
          }
          firstStepEndTime = System.currentTimeMillis();
           
          log.info("matching evm: " + matchingEVM.getExperimentVersionMappingId() + " took " + (firstStepEndTime - firstStepStartTime));
          // update evm in event table, and input id in output table
          anonId = emailAnonIdMap.get(singleEvent.getWho());
          if (anonId == null) { 
            anonId = euImpl.getAnonymousIdAndCreate(singleEvent.getExperimentId(), singleEvent.getWho(), true);
            emailAnonIdMap.put(singleEvent.getWho(), anonId);
          } 
          
          secondStepEndTime = System.currentTimeMillis();
          log.info("anon id step took " + (secondStepEndTime - firstStepEndTime));
          // update events and outputs
          statementUpdateEventsTable.setLong(1, matchingEVM.getExperimentVersionMappingId());
          statementUpdateEventsTable.setInt(2, anonId.getId().intValue());
          statementUpdateEventsTable.setLong(3, singleEvent.getId());
          log.info(statementUpdateEventsTable.toString());
          eventsUpdateNeeded = true;
          statementUpdateEventsTable.addBatch();
          if (singleEvent.getWhat() != null || singleEvent.getWhat().size() > 0)  {
            
            for (WhatDAO singleWhat : singleEvent.getWhat()) {
              // find input id
              InputOrderAndChoice matchingIOC = matchingEVM.getInputCollection().getInputOrderAndChoices().get(singleWhat.getName());
              Input input = matchingIOC != null ? matchingIOC.getInput() : null;
              Long inputId = input != null ? input.getInputId().getId() : 0L;
              if (inputId != 0) {
                statementUpdateOutputsTable.setLong(1, inputId);
                statementUpdateOutputsTable.setLong(2, singleEvent.getId());
                statementUpdateOutputsTable.setString(3, singleWhat.getName());
                log.info(statementUpdateOutputsTable.toString());
                outputsUpdateNeeded = true;
                statementUpdateOutputsTable.addBatch();
              }
            }
          }
          if (!matchingEVM.isEventsPosted()) {
            eventsPostedForEGVMId.add(matchingEVM.getExperimentVersionMappingId());
          }
        }
      }
      if (eventsUpdateNeeded) { 
        statementUpdateEventsTable.executeBatch();
      }
      if (outputsUpdateNeeded) { 
        statementUpdateOutputsTable.executeBatch();
      }
      thirdStepEndTime = System.currentTimeMillis();
      log.info("update events and outputs took "  + ( thirdStepEndTime - secondStepEndTime));
      if (eventsPostedForEGVMId.size() >0 ) {
        daoImpl.updateEventsPosted(eventsPostedForEGVMId);
      }
      fourthStepEndTime = System.currentTimeMillis();
      log.info("update events posted took "  + (fourthStepEndTime - thirdStepEndTime));
      
    } catch (SQLException sqle) {
      log.info("sqlException while performing event udpate" + ExceptionUtil.getStackTraceAsString(sqle));
      throw sqle;
    } catch(Exception e) {
      log.info(ExceptionUtil.getStackTraceAsString(e));
      throw e;
    }finally {
      try {
        if (statementUpdateOutputsTable != null) {
          statementUpdateOutputsTable.close();
        }
        if (statementUpdateEventsTable != null) {
          statementUpdateEventsTable.close();
        }
      } catch (SQLException ex1) {
        log.warning(ErrorMessages.CLOSING_RESOURCE_EXCEPTION.getDescription()+ ex1);
      }
    }      
  }
  
 
  @Override
  public boolean copyExperimentRenameOldEventColumns() throws SQLException {
    executeCreationOrInsertionQuerys(new String[]{RENAME_EVENT_TABLE_COLUMNS});
    return true;
  }

  @Override
  public boolean copyExperimentPopulateDistinctExperimentIdVersionAndGroupName() throws SQLException {
    CSExperimentIdVersionDao daoImpl = new CSExperimentIdVersionDaoImpl();
    daoImpl.insertExperimentIdVersionAndGroupName();
    return true;
  }

  @Override
  public boolean copyExperimentDeleteEventsAndOutputsForDeletedExperiments() throws SQLException {
    CSEventOutputDao daoImpl = new CSEventOutputDaoImpl();
    CSFailedEventDao failedDaoImpl = new CSFailedEventDaoImpl();
    CSExperimentIdVersionDao expIdDaoImpl = new CSExperimentIdVersionDaoImpl();
    List<Long> expIdsToBeDeleted = expIdDaoImpl.getExperimentIdsToBeDeleted();
    for (Long experimentId : expIdsToBeDeleted) {
      // delete all outputs and events for these experiments
      daoImpl.deleteAllEventsAndOutputsData(experimentId, null);
      failedDaoImpl.insertFailedEvent("expId: " + experimentId , "Did not find any experiment definition. ", "Did not find any experiment definition.");
      log.info("expId: " + experimentId + "deleted");
    }
    // delete these experiments from expid version table as well
    if (expIdsToBeDeleted.size() > 0 ) { 
      expIdDaoImpl.deleteExperiments(expIdsToBeDeleted);
    }
    return false;
  }

  @Override
  public boolean copyExperimentCreateEVGMRecordsForAllExperiments() throws SQLException {
    Integer migrationStatus = 0;
    CSExperimentVersionGroupMappingDao evgmDao = new CSExperimentVersionGroupMappingDaoImpl();
    CSExperimentIdVersionDao expIdVersionDao = new CSExperimentIdVersionDaoImpl();
    List<ExperimentLite> expLites = expIdVersionDao.getDistinctExperimentIdAndVersion(migrationStatus);
    for (ExperimentLite expLite : expLites) {
      evgmDao.copyClosestVersion(expLite.getExperimentId(), expLite.getExperimentVersion());
      expIdVersionDao.updateExperimentIdGroupNameStatus(expLite.getExperimentId(), expLite.getExperimentVersion(), null, 1);
    }
    return true;
  }
  
  @Override
  public boolean copyExperimentChangeGroupNameOfEventsWithPredefinedInputs() throws SQLException {
    String featureName = null;
    String finalQryForNotifAcc = null;
    Connection conn = null;
    ResultSet rs = null;
    Set<Long> eventIdsToBeUpdatedWithNewGroupName = null;
    List<String> eventIdsOldGroupName = null;
    PreparedStatement statementSelectEventsWithPredefinedInputs = null;
    PreparedStatement statementAccNotSelectEventsWithPredefinedInputs = null;
    CSGroupTypeInputMappingDao inputMappingDao = new CSGroupTypeInputMappingDaoImpl();
    CSExperimentIdVersionDao expIdVersionDao = new CSExperimentIdVersionDaoImpl();
    CSEventDao eventDaoImpl = new CSEventDaoImpl();
    String qryForNotifAcc = SELECT_EVENTS_MATCHING_PREDFINED_FOR_NOTIF_AND_ACC;
    String finalQryForSys = SELECT_EVENTS_MATCHING_PREDFINED_INPUT_NAMES;
    List<ExperimentLite> expLites = expIdVersionDao.getAllExperimentLiteOfStatus(1);
    try {
      conn = CloudSQLConnectionManager.getInstance().getConnection();
      Long eventId = null;
      Map<String, List<String>>inputMap = inputMappingDao.getAllPredefinedFeatureVariableNames();
     
      int recordCount = 0;
      for (ExperimentLite expLite : expLites) {
        log.info("RecordCount" + (recordCount++) + ":New exp id" + expLite.getExperimentId() + "----------" + expLite.getExperimentVersion());
        Iterator<String> predefinedInputIterator = inputMap.keySet().iterator();
        if (!inputMap.keySet().contains(expLite.getExperimentGroupName())) {
          // for each predefined feature
          while (predefinedInputIterator.hasNext()) {
              featureName = predefinedInputIterator.next();
              // if it is system, phone status or appusage android
              if (!featureName.equals(GroupTypeEnum.ACCESSIBILITY.name()) && !featureName.equals(GroupTypeEnum.NOTIFICATION.name())) {
                try {
                  List<String> eachFeaturesInputVariableNames = inputMap.get(featureName);
                  statementSelectEventsWithPredefinedInputs = conn.prepareStatement(finalQryForSys);
                  statementSelectEventsWithPredefinedInputs.setFetchSize(500);
                  
                  for (String eachFeatureInputVariableName : eachFeaturesInputVariableNames) {
                      while (true) {
                        statementSelectEventsWithPredefinedInputs.setLong(1, expLite.getExperimentId());
                        statementSelectEventsWithPredefinedInputs.setInt(2, expLite.getExperimentVersion());
                        statementSelectEventsWithPredefinedInputs.setString(3, expLite.getExperimentGroupName());
                        statementSelectEventsWithPredefinedInputs.setString(4, eachFeatureInputVariableName);
                        log.info("executing qry " + statementSelectEventsWithPredefinedInputs.toString());
                        rs = statementSelectEventsWithPredefinedInputs.executeQuery();
                        if(!rs.next()) {
                          break;
                        } else {
                          rs.beforeFirst();
                          eventIdsToBeUpdatedWithNewGroupName = Sets.newLinkedHashSet();
                          eventIdsOldGroupName = Lists.newArrayList();
                          while (rs.next()) {
                            eventId = rs.getLong(Constants.UNDERSCORE_ID);
                            if (eventIdsToBeUpdatedWithNewGroupName.add(eventId)) {
                              eventIdsOldGroupName.add(rs.getString(EventServerColumns.GROUP_NAME));
                            }
                          }// 250 records
                          // update events in batch
                          eventDaoImpl.updateGroupName(Lists.newArrayList(eventIdsToBeUpdatedWithNewGroupName), eventIdsOldGroupName, featureName);
                        } // if records present
                        log.info("continue for more records for same feature");
                      }// while loop finish all records
//                    }// for loop on possible group names
                  } // for loop on each variable name in predefined input
                } finally {
                  if ( rs != null) { 
                    rs.close();
                  }
                  if (statementSelectEventsWithPredefinedInputs != null) { 
                    statementSelectEventsWithPredefinedInputs.close();
                  }
                }
                
              } else if (featureName.equals(GroupTypeEnum.ACCESSIBILITY.name())) {
                finalQryForNotifAcc = qryForNotifAcc + " and   " + OutputServerColumns.TEXT + " = 'accessibilityEventType' and  " + OutputServerColumns.ANSWER +" = '21' limit 1000";
              } else  if (featureName.equals(GroupTypeEnum.NOTIFICATION.name())) {
                finalQryForNotifAcc = qryForNotifAcc + " and " + OutputServerColumns.TEXT + " = 'accessibilityEventType' and "+ OutputServerColumns.ANSWER + " != '21' limit 1000";
              }
              if (featureName.equals(GroupTypeEnum.ACCESSIBILITY.name()) || featureName.equals(GroupTypeEnum.NOTIFICATION.name())) { 
                try { 
                  statementAccNotSelectEventsWithPredefinedInputs = conn.prepareStatement(finalQryForNotifAcc);
                  statementAccNotSelectEventsWithPredefinedInputs.setFetchSize(500);
                    while (true) {
                      statementAccNotSelectEventsWithPredefinedInputs.setLong(1, expLite.getExperimentId());
                      statementAccNotSelectEventsWithPredefinedInputs.setInt(2, expLite.getExperimentVersion());
                      statementAccNotSelectEventsWithPredefinedInputs.setString(3, expLite.getExperimentGroupName());
                      
                      log.info("executing qry " + statementAccNotSelectEventsWithPredefinedInputs.toString());
                      rs = statementAccNotSelectEventsWithPredefinedInputs.executeQuery();
                      if(!rs.next()) {
                        break;
                      } else {
                        rs.beforeFirst();
                        eventIdsToBeUpdatedWithNewGroupName = Sets.newLinkedHashSet();
                        eventIdsOldGroupName = Lists.newArrayList();
                        while (rs.next()) {
                          eventId = rs.getLong(Constants.UNDERSCORE_ID);
                          if (eventIdsToBeUpdatedWithNewGroupName.add(eventId)) {
                            eventIdsOldGroupName.add(rs.getString(EventServerColumns.GROUP_NAME));
                          }
                        }// 250 records
                        // update events in batch
                        eventDaoImpl.updateGroupName(Lists.newArrayList(eventIdsToBeUpdatedWithNewGroupName), eventIdsOldGroupName, featureName);
                      }
                      log.info("continue for more records for same feature");
                    }// while finish all recrods
//                  }// for loop on possible group names
                } finally {
                  if ( rs != null) { 
                    rs.close();
                  }
                  if (statementAccNotSelectEventsWithPredefinedInputs != null) { 
                    statementAccNotSelectEventsWithPredefinedInputs.close();
                  }
                }
              } // if predefined grp is acc or notifs
          } // predefined map of all predefined grps
        } // if grp name not in predefined grps
        // finally if there is no exception after checking for all predefined grps, delete this exp id version in expidversion table, since it is processed
        expIdVersionDao.updateExperimentIdGroupNameStatus(expLite.getExperimentId(), expLite.getExperimentVersion(), expLite.getExperimentGroupName(), 2);
      }// for loop on each exp id version combination
    } finally {
      if (conn != null) { 
        conn.close();
      }
    }
    return true;
  }
}
