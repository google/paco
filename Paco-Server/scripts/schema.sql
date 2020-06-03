CREATE DATABASE  IF NOT EXISTS `pacodb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `pacodb`;
-- MySQL dump 10.13  Distrib 5.7.17, for macos10.12 (x86_64)
--
-- Host: 127.0.0.1    Database: pacodb
-- ------------------------------------------------------
-- Server version	5.7.18

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `catchup_failure`
--

DROP TABLE IF EXISTS `catchup_failure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `catchup_failure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `insertion_type` varchar(20) NOT NULL,
  `event_id` bigint(20) DEFAULT NULL,
  `text` varchar(750) DEFAULT NULL,
  `failure_reason` varchar(750) DEFAULT NULL,
  `failure_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=164 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `catchup_failure`
--

LOCK TABLES `catchup_failure` WRITE;
/*!40000 ALTER TABLE `catchup_failure` DISABLE KEYS */;
/*!40000 ALTER TABLE `catchup_failure` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `choice_collection`
--

DROP TABLE IF EXISTS `choice_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `choice_collection` (
  `experiment_ds_id` bigint(20) NOT NULL,
  `choice_collection_id` bigint(20) NOT NULL,
  `choice_id` bigint(20) NOT NULL,
  `choice_order` int(11) NOT NULL,
  PRIMARY KEY (`experiment_ds_id`,`choice_collection_id`,`choice_id`),
  KEY `extern_string_fk` (`choice_id`),
  CONSTRAINT `extern_string_constraint` FOREIGN KEY (`choice_id`) REFERENCES `extern_string_list_label` (`extern_string_list_label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `choice_collection`
--

LOCK TABLES `choice_collection` WRITE;
/*!40000 ALTER TABLE `choice_collection` DISABLE KEYS */;
/*!40000 ALTER TABLE `choice_collection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_type`
--

DROP TABLE IF EXISTS `data_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_type` (
  `data_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `is_numeric` bit(1) DEFAULT NULL,
  `multi_select` bit(1) DEFAULT NULL,
  `response_mapping_required` bit(1) DEFAULT NULL,
  PRIMARY KEY (`data_type_id`),
  UNIQUE KEY `name_type_UNIQUE` (`name`,`is_numeric`,`multi_select`,`response_mapping_required`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_type`
--

LOCK TABLES `data_type` WRITE;
/*!40000 ALTER TABLE `data_type` DISABLE KEYS */;
INSERT INTO `data_type` VALUES (33,'','\0','\0','\0'),(40,'','\0','','\0'),(37,'','','','\0'),(35,'audio','\0','\0','\0'),(45,'audio','\0','','\0'),(41,'graph','\0','\0','\0'),(28,'likert','','\0','\0'),(38,'likert','','','\0'),(30,'likert_smileys','\0','\0','\0'),(42,'likert_smileys','\0','','\0'),(26,'list','','\0',''),(27,'list','','',''),(34,'location','\0','\0','\0'),(43,'location','\0','','\0'),(29,'number','','\0','\0'),(36,'number','','','\0'),(25,'open text','\0','\0','\0'),(39,'open text','\0','','\0'),(24,'open text','','\0','\0'),(32,'photo','\0','\0','\0'),(44,'photo','\0','','\0'),(31,'undefined','\0','\0','\0');
/*!40000 ALTER TABLE `data_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `event_old_group_name`
--

DROP TABLE IF EXISTS `event_old_group_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event_old_group_name` (
  `event_id` bigint(20) NOT NULL,
  `old_group_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `event_old_group_name`
--

LOCK TABLES `event_old_group_name` WRITE;
/*!40000 ALTER TABLE `event_old_group_name` DISABLE KEYS */;
/*!40000 ALTER TABLE `event_old_group_name` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `events`
--

DROP TABLE IF EXISTS `events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `events` (
  `_Id` bigint(20) NOT NULL,
  `schedule_time_utc` datetime DEFAULT NULL,
  `response_time_utc` datetime DEFAULT NULL,
  `action_id` bigint(20) DEFAULT NULL,
  `action_trigger_id` bigint(20) DEFAULT NULL,
  `action_trigger_spec_id` bigint(20) DEFAULT NULL,
  `paco_version` varchar(20) DEFAULT NULL,
  `app_id` varchar(25) DEFAULT NULL,
  `when` datetime DEFAULT NULL,
  `when_fractional_sec` int(11) DEFAULT '0',
  `archive_flag` tinyint(4) NOT NULL DEFAULT '0',
  `joined` tinyint(1) DEFAULT NULL,
  `sort_date_utc` datetime DEFAULT NULL,
  `client_timezone` varchar(20) DEFAULT NULL,
  `schedule_time` datetime DEFAULT NULL,
  `response_time` datetime DEFAULT NULL,
  `sort_date` datetime DEFAULT NULL,
  `experiment_version_group_mapping_id` bigint(20) DEFAULT NULL,
  `who` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`_Id`),
  KEY `fk_exp_version_group_mapping_idx` (`experiment_version_group_mapping_id`),
  KEY `evg_idx` (`experiment_version_group_mapping_id`),
  CONSTRAINT `fk_exp_version_group_mapping` FOREIGN KEY (`experiment_version_group_mapping_id`) REFERENCES `experiment_version_group_mapping` (`experiment_version_group_mapping_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `events`
--

LOCK TABLES `events` WRITE;
/*!40000 ALTER TABLE `events` DISABLE KEYS */;
/*!40000 ALTER TABLE `events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experiment_detail`
--

DROP TABLE IF EXISTS `experiment_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_detail` (
  `experiment_detail_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `experiment_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `description` varchar(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `creator` bigint(20) NOT NULL,
  `contact_email` varchar(200) DEFAULT NULL,
  `organization` varchar(2500) DEFAULT NULL,
  `informed_consent_id` bigint(20) DEFAULT NULL,
  `modified_date` datetime DEFAULT NULL,
  `published` bit(1) DEFAULT b'0',
  `ringtone_uri` varchar(200) DEFAULT NULL,
  `post_install_instructions` mediumtext,
  `deleted` bit(1) DEFAULT b'0',
  PRIMARY KEY (`experiment_detail_id`),
  KEY `e_ic_informed_consent_fk_idx` (`informed_consent_id`),
  CONSTRAINT `e_ic_informed_consent_fk` FOREIGN KEY (`informed_consent_id`) REFERENCES `informed_consent` (`informed_consent_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=10974 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experiment_detail`
--

LOCK TABLES `experiment_detail` WRITE;
/*!40000 ALTER TABLE `experiment_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `experiment_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experiment_user`
--

DROP TABLE IF EXISTS `experiment_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_user` (
  `experiment_id` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `experiment_user_anon_id` int(11) NOT NULL,
  `user_type` char(1) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`experiment_id`,`user_id`),
  UNIQUE KEY `experiment_id_anon_id_UNIQUE` (`experiment_id`,`experiment_user_anon_id`),
  KEY `eu_u_userid_fk_idx` (`user_id`),
  CONSTRAINT `eu_u_userid_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experiment_user`
--

LOCK TABLES `experiment_user` WRITE;
/*!40000 ALTER TABLE `experiment_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `experiment_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experiment_version_group_mapping`
--

DROP TABLE IF EXISTS `experiment_version_group_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `experiment_version_group_mapping` (
  `experiment_version_group_mapping_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `experiment_id` bigint(20) NOT NULL,
  `experiment_version` int(11) NOT NULL,
  `experiment_detail_id` bigint(20) NOT NULL,
  `group_detail_id` bigint(20) NOT NULL,
  `input_collection_id` bigint(20) DEFAULT NULL,
  `events_posted` bit(1) DEFAULT b'0',
  `source` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`experiment_version_group_mapping_id`),
  UNIQUE KEY `experiment_id_version_group_unique` (`experiment_id`,`experiment_version`,`group_detail_id`),
  UNIQUE KEY `experiment_id_version_ic_unique` (`experiment_id`,`experiment_version`,`input_collection_id`),
  KEY `experiment_history_fk_idx` (`experiment_detail_id`),
  KEY `group_history_fk_idx` (`group_detail_id`),
  CONSTRAINT `experiment_history_fk` FOREIGN KEY (`experiment_detail_id`) REFERENCES `experiment_detail` (`experiment_detail_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `group_history_fk` FOREIGN KEY (`group_detail_id`) REFERENCES `group_detail` (`group_detail_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=120664 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experiment_version_group_mapping`
--

LOCK TABLES `experiment_version_group_mapping` WRITE;
/*!40000 ALTER TABLE `experiment_version_group_mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `experiment_version_group_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extern_string_input`
--

DROP TABLE IF EXISTS `extern_string_input`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `extern_string_input` (
  `extern_string_input_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `label` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`extern_string_input_id`),
  UNIQUE KEY `type_UNIQUE` (`label`(500))
) ENGINE=InnoDB AUTO_INCREMENT=547481 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extern_string_input`
--

LOCK TABLES `extern_string_input` WRITE;
/*!40000 ALTER TABLE `extern_string_input` DISABLE KEYS */;
INSERT INTO `extern_string_input` VALUES (547462,'joined'),(547463,'schedule'),(547464,'model'),(547465,'android'),(547466,'make'),(547467,'carrier'),(547468,'display'),(547469,'apps_used'),(547470,'apps_used_raw'),(547471,'foreground'),(547472,'userPresent'),(547473,'userNotPresent'),(547474,'phoneOn'),(547475,'phoneOff'),(547476,'accessibilityEventText'),(547477,'accessibilityEventPackage'),(547478,'accessibilityEventClass'),(547479,'accessibilityEventType'),(547480,'accessibilityEventContentDescription');
/*!40000 ALTER TABLE `extern_string_input` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `extern_string_list_label`
--

DROP TABLE IF EXISTS `extern_string_list_label`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `extern_string_list_label` (
  `extern_string_list_label_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `label` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`extern_string_list_label_id`),
  UNIQUE KEY `type_UNIQUE` (`label`(500))
) ENGINE=InnoDB AUTO_INCREMENT=18052 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `extern_string_list_label`
--

LOCK TABLES `extern_string_list_label` WRITE;
/*!40000 ALTER TABLE `extern_string_list_label` DISABLE KEYS */;
/*!40000 ALTER TABLE `extern_string_list_label` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `failed_events`
--

DROP TABLE IF EXISTS `failed_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `failed_events` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `event_json` varchar(3000) NOT NULL,
  `failed_insert_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `reason` varchar(500) DEFAULT NULL,
  `comments` varchar(1000) DEFAULT NULL,
  `reprocessed` varchar(10) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11807 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `failed_events`
--

LOCK TABLES `failed_events` WRITE;
/*!40000 ALTER TABLE `failed_events` DISABLE KEYS */;
/*!40000 ALTER TABLE `failed_events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_detail`
--

DROP TABLE IF EXISTS `group_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_detail` (
  `group_detail_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(2500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `group_type_id` int(11) DEFAULT NULL,
  `custom_rendering` mediumtext,
  `end_of_day_group` varchar(500) DEFAULT NULL,
  `fixed_duration` bit(1) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_Date` datetime DEFAULT NULL,
  `raw_data_access` bit(1) DEFAULT NULL,
  PRIMARY KEY (`group_detail_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35357 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_detail`
--

LOCK TABLES `group_detail` WRITE;
/*!40000 ALTER TABLE `group_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_type`
--

DROP TABLE IF EXISTS `group_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_type` (
  `group_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `group_type_name` varchar(250) NOT NULL,
  PRIMARY KEY (`group_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_type`
--

LOCK TABLES `group_type` WRITE;
/*!40000 ALTER TABLE `group_type` DISABLE KEYS */;
INSERT INTO `group_type` VALUES (7,'SYSTEM'),(8,'SURVEY'),(9,'APPUSAGE_ANDROID'),(10,'NOTIFICATION'),(11,'ACCESSIBILITY'),(12,'PHONESTATUS'),(13,'APPUSAGE_DESKTOP'),(14,'APPUSAGE_SHELL'),(15,'IDE_IDEA_USAGE');
/*!40000 ALTER TABLE `group_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_type_input_mapping`
--

DROP TABLE IF EXISTS `group_type_input_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_type_input_mapping` (
  `group_type_input_mapping_id` int(11) NOT NULL AUTO_INCREMENT,
  `group_type_id` varchar(45) DEFAULT NULL,
  `input_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`group_type_input_mapping_id`),
  UNIQUE KEY `groupt_type_id_input_id_UNIQUE` (`group_type_id`,`input_id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_type_input_mapping`
--

LOCK TABLES `group_type_input_mapping` WRITE;
/*!40000 ALTER TABLE `group_type_input_mapping` DISABLE KEYS */;
INSERT INTO `group_type_input_mapping` VALUES (44,'10',873385),(45,'10',873386),(46,'10',873387),(47,'10',873388),(48,'10',873389),(39,'11',873385),(40,'11',873386),(41,'11',873387),(42,'11',873388),(43,'11',873389),(37,'12',873383),(38,'12',873384),(49,'13',873390),(50,'13',873391),(51,'13',873392),(52,'13',873393),(53,'13',873394),(54,'14',873395),(55,'14',873396),(56,'14',873397),(57,'14',873398),(58,'14',873399),(59,'15',873400),(60,'15',873401),(61,'15',873402),(62,'15',873403),(63,'15',873404),(25,'7',873371),(26,'7',873372),(27,'7',873373),(28,'7',873374),(29,'7',873375),(30,'7',873376),(31,'7',873377),(32,'9',873378),(33,'9',873379),(34,'9',873380),(35,'9',873381),(36,'9',873382);
/*!40000 ALTER TABLE `group_type_input_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `informed_consent`
--

DROP TABLE IF EXISTS `informed_consent`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `informed_consent` (
  `informed_consent_id` bigint(20) NOT NULL,
  `experiment_id` bigint(20) NOT NULL,
  `informed_consent` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  PRIMARY KEY (`informed_consent_id`,`experiment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `informed_consent`
--

LOCK TABLES `informed_consent` WRITE;
/*!40000 ALTER TABLE `informed_consent` DISABLE KEYS */;
/*!40000 ALTER TABLE `informed_consent` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `input`
--

DROP TABLE IF EXISTS `input`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `input` (
  `input_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name_id` bigint(20) NOT NULL,
  `text_id` bigint(20) NOT NULL,
  `required` bit(1) DEFAULT b'0',
  `conditional` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `response_data_type_id` int(11) DEFAULT NULL,
  `likert_steps` tinyint(4) DEFAULT NULL,
  `left_label` varchar(500) DEFAULT NULL,
  `right_label` varchar(500) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`input_id`),
  KEY `name_extern_string_fk_idx` (`name_id`),
  KEY `text_extern_string_fk_idx` (`text_id`),
  KEY `response_type_fk_idx` (`response_data_type_id`),
  CONSTRAINT `ih_es_name_fk` FOREIGN KEY (`name_id`) REFERENCES `extern_string_input` (`extern_string_input_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `ih_es_response_type_fk` FOREIGN KEY (`response_data_type_id`) REFERENCES `data_type` (`data_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `ih_es_text_fk` FOREIGN KEY (`text_id`) REFERENCES `extern_string_input` (`extern_string_input_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=873405 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `input`
--

LOCK TABLES `input` WRITE;
/*!40000 ALTER TABLE `input` DISABLE KEYS */;
INSERT INTO `input` VALUES (873371,547462,547462,'\0',NULL,24,0,NULL,NULL,NULL),(873372,547463,547463,'\0',NULL,25,0,NULL,NULL,NULL),(873373,547464,547464,'\0',NULL,25,0,NULL,NULL,NULL),(873374,547465,547465,'\0',NULL,25,0,NULL,NULL,NULL),(873375,547466,547466,'\0',NULL,25,0,NULL,NULL,NULL),(873376,547467,547467,'\0',NULL,25,0,NULL,NULL,NULL),(873377,547468,547468,'\0',NULL,25,0,NULL,NULL,NULL),(873378,547469,547469,'\0',NULL,25,0,NULL,NULL,NULL),(873379,547470,547470,'\0',NULL,25,0,NULL,NULL,NULL),(873380,547471,547471,'\0',NULL,25,0,NULL,NULL,NULL),(873381,547472,547472,'\0',NULL,25,0,NULL,NULL,NULL),(873382,547473,547473,'\0',NULL,25,0,NULL,NULL,NULL),(873383,547474,547474,'\0',NULL,25,0,NULL,NULL,NULL),(873384,547475,547475,'\0',NULL,25,0,NULL,NULL,NULL),(873385,547476,547476,'\0',NULL,25,0,NULL,NULL,NULL),(873386,547477,547477,'\0',NULL,25,0,NULL,NULL,NULL),(873387,547478,547478,'\0',NULL,25,0,NULL,NULL,NULL),(873388,547479,547479,'\0',NULL,25,0,NULL,NULL,NULL),(873389,547480,547480,'\0',NULL,25,0,NULL,NULL,NULL),(873390,547469,547469,'\0',NULL,25,0,NULL,NULL,NULL),(873391,547470,547470,'\0',NULL,25,0,NULL,NULL,NULL),(873392,547471,547471,'\0',NULL,25,0,NULL,NULL,NULL),(873393,547472,547472,'\0',NULL,25,0,NULL,NULL,NULL),(873394,547473,547473,'\0',NULL,25,0,NULL,NULL,NULL),(873395,547469,547469,'\0',NULL,25,0,NULL,NULL,NULL),(873396,547470,547470,'\0',NULL,25,0,NULL,NULL,NULL),(873397,547471,547471,'\0',NULL,25,0,NULL,NULL,NULL),(873398,547472,547472,'\0',NULL,25,0,NULL,NULL,NULL),(873399,547473,547473,'\0',NULL,25,0,NULL,NULL,NULL),(873400,547469,547469,'\0',NULL,25,0,NULL,NULL,NULL),(873401,547470,547470,'\0',NULL,25,0,NULL,NULL,NULL),(873402,547471,547471,'\0',NULL,25,0,NULL,NULL,NULL),(873403,547472,547472,'\0',NULL,25,0,NULL,NULL,NULL),(873404,547473,547473,'\0',NULL,25,0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `input` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `input_collection`
--

DROP TABLE IF EXISTS `input_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `input_collection` (
  `experiment_ds_id` bigint(20) NOT NULL,
  `input_collection_id` bigint(20) NOT NULL,
  `input_id` bigint(20) NOT NULL,
  `choice_collection_id` bigint(20) DEFAULT NULL,
  `input_order` int(11) NOT NULL,
  PRIMARY KEY (`input_collection_id`,`experiment_ds_id`,`input_id`),
  KEY `ic_input_id_fk_idx` (`input_id`),
  CONSTRAINT `ic_input_id_fk` FOREIGN KEY (`input_id`) REFERENCES `input` (`input_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `input_collection`
--

LOCK TABLES `input_collection` WRITE;
/*!40000 ALTER TABLE `input_collection` DISABLE KEYS */;
/*!40000 ALTER TABLE `input_collection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `migration_cursor`
--

DROP TABLE IF EXISTS `migration_cursor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `migration_cursor` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cursor` varchar(400) DEFAULT NULL,
  `current_time` datetime DEFAULT NULL,
  `current_time_fractional_sec` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=68535 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `migration_cursor`
--

LOCK TABLES `migration_cursor` WRITE;
/*!40000 ALTER TABLE `migration_cursor` DISABLE KEYS */;
/*!40000 ALTER TABLE `migration_cursor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `missing_event_ids`
--

DROP TABLE IF EXISTS `missing_event_ids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `missing_event_ids` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `output_info` varchar(3000) DEFAULT NULL,
  `current_time` datetime DEFAULT NULL,
  `current_time_fractional_sec` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=91437 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `missing_event_ids`
--

LOCK TABLES `missing_event_ids` WRITE;
/*!40000 ALTER TABLE `missing_event_ids` DISABLE KEYS */;
/*!40000 ALTER TABLE `missing_event_ids` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `outputs`
--

DROP TABLE IF EXISTS `outputs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `outputs` (
  `event_id` bigint(20) NOT NULL,
  `answer` varchar(1000) DEFAULT NULL,
  `archive_flag` tinyint(4) NOT NULL DEFAULT '0',
  `input_id` bigint(20) NOT NULL,
  PRIMARY KEY (`event_id`,`input_id`),
  KEY `fk_text_input_id_idx` (`input_id`),
  KEY `event_id_input_id_idx` (`event_id`,`input_id`),
  CONSTRAINT `events_id_fk` FOREIGN KEY (`event_id`) REFERENCES `events` (`_Id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_text_input_id` FOREIGN KEY (`input_id`) REFERENCES `input` (`input_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `outputs`
--

LOCK TABLES `outputs` WRITE;
/*!40000 ALTER TABLE `outputs` DISABLE KEYS */;
/*!40000 ALTER TABLE `outputs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pivot_helper`
--

DROP TABLE IF EXISTS `pivot_helper`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pivot_helper` (
  `experiment_version_group_mapping_id` bigint(20) NOT NULL,
  `anon_who` int(11) NOT NULL,
  `input_id` bigint(20) NOT NULL,
  `events_posted` bigint(20) NOT NULL DEFAULT '0',
  `processed` bit(1) DEFAULT b'0',
  PRIMARY KEY (`experiment_version_group_mapping_id`,`anon_who`,`input_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pivot_helper`
--

LOCK TABLES `pivot_helper` WRITE;
/*!40000 ALTER TABLE `pivot_helper` DISABLE KEYS */;
/*!40000 ALTER TABLE `pivot_helper` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `streaming`
--

DROP TABLE IF EXISTS `streaming`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `streaming` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `start_time` datetime DEFAULT NULL,
  `start_time_fractional_sec` int(11) DEFAULT '0',
  `current_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `streaming`
--

LOCK TABLES `streaming` WRITE;
/*!40000 ALTER TABLE `streaming` DISABLE KEYS */;
/*!40000 ALTER TABLE `streaming` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `temp_experiment_definition`
--

DROP TABLE IF EXISTS `temp_experiment_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `temp_experiment_definition` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  `source_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `converted_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  `migration_status` int(11) DEFAULT NULL,
  `error_message` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `temp_experiment_definition`
--

LOCK TABLES `temp_experiment_definition` WRITE;
/*!40000 ALTER TABLE `temp_experiment_definition` DISABLE KEYS */;
/*!40000 ALTER TABLE `temp_experiment_definition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `temp_experiment_definition_bk`
--

DROP TABLE IF EXISTS `temp_experiment_definition_bk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `temp_experiment_definition_bk` (
  `id` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  `source_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin,
  PRIMARY KEY (`id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `temp_experiment_definition_bk`
--

LOCK TABLES `temp_experiment_definition_bk` WRITE;
/*!40000 ALTER TABLE `temp_experiment_definition_bk` DISABLE KEYS */;
/*!40000 ALTER TABLE `temp_experiment_definition_bk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `temp_experiment_id_version_group_name`
--

DROP TABLE IF EXISTS `temp_experiment_id_version_group_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `temp_experiment_id_version_group_name` (
  `experiment_id` bigint(20) NOT NULL,
  `experiment_version` int(11) NOT NULL,
  `group_name` varchar(700) COLLATE utf8mb4_bin NOT NULL,
  `status` int(11) DEFAULT '0',
  PRIMARY KEY (`experiment_id`,`experiment_version`,`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `temp_experiment_id_version_group_name`
--

LOCK TABLES `temp_experiment_id_version_group_name` WRITE;
/*!40000 ALTER TABLE `temp_experiment_id_version_group_name` DISABLE KEYS */;
/*!40000 ALTER TABLE `temp_experiment_id_version_group_name` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `who` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `who_unique_index` (`who`)
) ENGINE=InnoDB AUTO_INCREMENT=45466 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'pacodb'
--

--
-- Dumping routines for database 'pacodb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-06-03  2:06:06
