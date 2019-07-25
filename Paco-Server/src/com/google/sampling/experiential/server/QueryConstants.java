package com.google.sampling.experiential.server;

import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.TempExperimentDefinitionColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentVersionGroupMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentUserColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.FailedEventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeInputMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.InputCollectionColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.cloudsql.columns.PivotHelperColumns;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.Constants;

public enum QueryConstants {
  UPDATE_FAILED_EVENTS_PROCESSED_STATUS_FOR_ID("update "+ FailedEventServerColumns.TABLE_NAME +" set "+ FailedEventServerColumns.REPROCESSED+ " = ? where " + FailedEventServerColumns.ID + "= ?"),
  UPDATE_OUTPUT_TEXT("update outputs set text = ?, input_id=null where event_id = ? and text = ?"),
  UPDATE_EVENT_EVGM_ID_AS_NULL("update " + EventServerColumns.TABLE_NAME +  " set " + EventServerColumns.EXPERIMENT_VERSION_GROUP_MAPPING_ID + "  = null  where " + Constants.UNDERSCORE_ID +  "=?"),
  GET_ALL_UNPROCESSED_FAILED_EVENTS("select * from " + FailedEventServerColumns.TABLE_NAME + " where " + FailedEventServerColumns.REPROCESSED + "='false'"),
  GET_ALL_FAILED_EVENTS_STARTING_FROM_ID("select * from " + FailedEventServerColumns.TABLE_NAME + " where " + FailedEventServerColumns.ID + ">= ?"),
  GET_ALL_USERS_FOR_EXPERIMENT("select * from " + ExperimentUserColumns.TABLE_NAME + " join " + UserColumns.TABLE_NAME + " on " + UserColumns.TABLE_NAME +". "+ UserColumns.USER_ID +" = " + ExperimentUserColumns.TABLE_NAME  +  "." + ExperimentUserColumns.USER_ID + " where " + ExperimentUserColumns.EXPERIMENT_ID +" = ? "),
  GET_ALL_USERS_FOR_EVGM("select distinct who_bk from " + EventServerColumns.TABLE_NAME +  " where " + EventServerColumns.EXPERIMENT_VERSION_GROUP_MAPPING_ID +" = ? "),
  GET_ALL_GROUP_TYPE("select * from " + GroupTypeColumns.TABLE_NAME ),
  GET_LABEL_FOR_INPUT_ID("select esi.label from input i join extern_string_input esi on i.name_id=esi.extern_string_input_id where i.input_id=?"),
  GET_ALL_INPUT_IDS_FOR_EVGM_AND_USER("select distinct input_id from events e join outputs o on e._id=o.event_id where e.experiment_version_group_mapping_id=? and e.who_bk=?"),
  GET_EXPERIMENT_IDS_WITH_DUP_INPUTS("select  distinct evgm.experiment_id from " + ExperimentVersionGroupMappingColumns.TABLE_NAME + " evgm " + 
          " join input_collection ic on evgm.input_collection_id=ic.input_collection_id and evgm.experiment_id=ic.experiment_ds_id  " + 
          " join input i on ic.input_id=i.input_id "  +
          " join " + ExternStringInputColumns.TABLE_NAME + " esi  " +  
          " on i.name_id=esi." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID  +    
          " where esi."+ ExternStringInputColumns.LABEL +" like '%-DUP-%' "),
  GET_EXPERIMENT_IDS_FROM_EVENTS_WITH_DUP_INPUTS("select distinct experiment_id from events e join outputs o on e._id=o.event_id where o.text like '%-DUP-%'"),
  GET_INPUT_IDS_WITH_DUP_INPUTS_FOR_EXPERIMENT("select  i.input_id from experiment_version_group_mapping evgm " + 
          " join input_collection ic on evgm.input_collection_id=ic.input_collection_id and evgm.experiment_id=ic.experiment_ds_id  " + 
          " join input i on ic.input_id=i.input_id "  +
          " join " + ExternStringInputColumns.TABLE_NAME + " esi  " +  
          " on i.name_id=esi." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID +  
          " where evgm.experiment_id = ? and esi."+ ExternStringInputColumns.LABEL +" like '%-DUP-%'"),
  GET_ALL_DISTINCT_TEXT_FOR_EXPERIMENT_ID("select distinct text from events e join outputs o on e._id=o.event_id where experiment_id=?"),
  GET_EVENT_FOR_ID("select * from " + EventServerColumns.TABLE_NAME + " where " + Constants.UNDERSCORE_ID+ " =?"),
  GET_EVENT_ID_WITH_DUP_VARIABLE("select distinct _id from events e join outputs o on e._id=o.event_id where e.experiment_id=? and text like'%-DUP-%' and e.experiment_version_group_mapping_id is not null limit 1000"),
  GET_NUMBER_OF_EVENTS_FOR_EXPERIMENT("select count("+ Constants.UNDERSCORE_ID + ") from " + EventServerColumns.TABLE_NAME + " where " + EventServerColumns.EXPERIMENT_ID + " = ?"),
  GET_ALL_OUTPUTS_FOR_EVENT_ID("select * from " + OutputBaseColumns.TABLE_NAME + " where " + OutputBaseColumns.EVENT_ID + " = ?"),
  GET_ALL_OUTPUTS_WITHOUT_INPUTID_FOR_EVENT_ID("select * from " + OutputBaseColumns.TABLE_NAME + " where " + OutputBaseColumns.EVENT_ID + " = ? and input_id is null"),
  GET_DISTINCT_OUTPUTS_FOR_EXPERIMENT_ID("select count(distinct text) from events e join outputs o on e._id=o.event_id where experiment_id=?"),
  GET_EXPERIMENT_DEFINITION_RECORD_COUNT("select count(*) from temp_experiment_definition_bk"),
  GET_EVENTS_COUNT("select count(*) from " + ExperimentVersionGroupMappingColumns.TABLE_NAME + " evgm join events e on evgm.experiment_version_group_mapping_id = e.experiment_version_group_mapping_id " +
                                    " join outputs o on e._id=o.event_id where evgm.experiment_version_group_mapping_id=? and who=? and input_id=?"),
  GET_EGVM_ID_FOR_EXP_ID_AND_VERSION("select "+ ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION_GROUP_MAPPING_ID + " from " + ExperimentVersionGroupMappingColumns.TABLE_NAME + " evm "+
                                    " where " + ExperimentVersionGroupMappingColumns.EXPERIMENT_ID + " = ? and  " + ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION + " = ? " ),
  GET_EVGM_ID("SELECT * FROM " + ExperimentVersionGroupMappingColumns.TABLE_NAME +  " evgm join group_detail gd on evgm.group_detail_id=gd.group_detail_id where experiment_id=? and experiment_version=? and group_name =?"),
  GET_ALL_INPUT_IDS("SELECT  " + InputCollectionColumns.INPUT_ID + " FROM " + InputCollectionColumns.TABLE_NAME +  " where experiment_ds_id=? and input_collection_id =?"),
  GET_ALL_UNPROCESSED_PIVOT_HELPER("select * from " + PivotHelperColumns.TABLE_NAME + " where " + PivotHelperColumns.PROCESSED + " = b'0'"),
  GET_ALL_EXPERIMENT_JSON("select * from " + TempExperimentDefinitionColumns.TABLE_NAME + " where "+TempExperimentDefinitionColumns.MIGRATION_STATUS +" = ?"),
  GET_EXPERIMENT_JSON_FOR_EXP_ID("select * from " + TempExperimentDefinitionColumns.TABLE_NAME + " where "+TempExperimentDefinitionColumns.ID +" = ?"),
  GET_ALL_ERRORED_EXPERIMENT_JSON("select id from " + TempExperimentDefinitionColumns.TABLE_NAME +  " where error_message is not null" ),
  GET_TO_BE_DELETED_EXPERIMENTS("select distinct experiment_id from temp_experiment_id_version_group_name where experiment_id not in (select id from temp_experiment_definition) and experiment_id not in (select distinct experiment_id from experiment_version_group_mapping)" ),
  DELETE_EXPERIMENTS_IN_EXPERIMENT_ID_VERSION("delete from temp_experiment_id_version_group_name where experiment_id = ?" ),
  DELETE_EXPERIMENTS_WITH_VERSION_IN_EXPERIMENT_ID_VERSION("delete from temp_experiment_id_version_group_name where experiment_id = ? and experiment_version = ?" ),
  DELETE_EVGM_EXP_GROUP_DETAILS_INF_CONSENT("delete evgm, ed, infcon, gd " + 
                                        "  from experiment_version_group_mapping evgm  " + 
                                        " join group_detail gd on evgm.group_detail_id=gd.group_detail_id " + 
                                        " join experiment_detail ed on ed.experiment_detail_id=evgm.experiment_detail_id " + 
                                        " left join informed_consent infcon on infcon.experiment_id=evgm.experiment_id and infcon.informed_consent_id=ed.informed_consent_id " + 
                                        " where evgm.experiment_id=? and evgm.experiment_version>0 and evgm.group_detail_id >0"),
  DELETE_INPUT_AND_CHOICE_COLLECTION_FOR_EXPT("delete i,ic, cc " + 
                                                          " from experiment_version_group_mapping evgm  "+  
                                                          " join input_collection ic on evgm.input_collection_id=ic.input_collection_id and evgm.experiment_id=ic.experiment_ds_id " +
                                                          " join input i on ic.input_id=i.input_id " + 
                                                          " left join choice_collection cc on ic.choice_collection_id=cc.choice_collection_id and ic.experiment_ds_id=cc.experiment_ds_id " + 
                                                          " where evgm.experiment_id=?"),
  DELETE_EXPERIMENT_USER_FOR_EXPERIMENT("delete from experiment_user where experiment_id=?"),
  DELELTE_INPUTS_IN_INPUT_COLLECTION_FOR_EXPERIMENT("delete from input_collection where  experiment_ds_id=? and input_id=?"),
  UPDATE_INPUT_COLLECTION_ID_FOR_EVGM_ID("update experiment_version_group_mapping set input_collection_id=? where experiment_version_group_mapping_id= ? "),
  UPDATE_EXPERIMENT_ID_VERSION_GROUP_NAME_STATUS_IN_EXPERIMENT_ID_VERSION("update temp_experiment_id_version_group_name set status=? where experiment_id = ? and experiment_version = ? and group_name=?" ),
  UPDATE_EXPERIMENT_ID_VERSION_STATUS_IN_EXPERIMENT_ID_VERSION("update temp_experiment_id_version_group_name set status=? where experiment_id = ? and experiment_version = ?" ),
  UPDATE_EXPERIMENT_ID_STATUS_IN_EXPERIMENT_ID_VERSION("update temp_experiment_id_version_group_name set status=? where experiment_id = ? " ),
  UPDATE_EVENTS_WITH_NEW_GROUP_NAME("update events set group_name=? where _id=?"),
  INSERT_TO_OLD_GROUP_NAME_TABLE("insert ignore into event_old_group_name(old_group_name,event_id) values (?,?)"),
  INSERT_IGNORE_TO_PIVOT_HELPER("INSERT ignore INTO pivot_helper (experiment_version_group_mapping_id, anon_who, input_id, events_posted) VALUES (?,?,?,?) "),
  INSERT_TO_PIVOT_HELPER_WITH_ON_DUPLICATE_CLAUSE("INSERT INTO pivot_helper (experiment_version_group_mapping_id, anon_who, input_id, events_posted, processed) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE events_posted=events_posted+1"),
  SELECT_PIVOT_HELPER_ZERO_RECORDS("select * from pivot_helper where events_posted=0"),
  SELECT_JULY_AUGUST_EVG("select distinct evgm.experiment_version_group_mapping_id, who_bk, input_id from experiment_version_group_mapping evgm"
          + " join events e on e.experiment_version_group_mapping_id= evgm.experiment_version_group_mapping_id "
          + " join outputs o on e._id=o.event_id where e.response_time between '2018-07-01 01:00:00' and '2018-08-31 11:59:49'"),
  FIND_EVENTS_MISSING_INPUT_IDS("select e.experiment_id, e.experiment_version, e.group_name, e.experiment_version_group_mapping_id, e.who_bk, o.* from events e join outputs o on e._id=o.event_id  where  input_id is null"),
  UPDATE_OUTPUT_WITH_INPUT_ID("update outputs set input_id=? where event_id=? and text=?"),
  UPDATE_PIVOT_HELPER("update pivot_helper set events_posted =? where experiment_version_group_mapping_id=? and anon_who=? and input_id=?"),
  REPLACE_TO_EXPERIMENT_ID_VERSION_GROUP_NAME("REPLACE INTO `pacodb`.`temp_experiment_id_version_group_name` (`experiment_id`, `experiment_version`, `group_name`, `status`) VALUES (?, ?,?,?)"),
  DELETE_FROM_EXPERIMENT_DEFINITION("delete from temp_experiment_definition where id = ? "),
  DELETE_FROM_INPUT("delete from input where input_id=?"),
  UPDATE_SPLIT_JSON_IN_EXPERIMENT_DEFINITION("update temp_experiment_definition set migration_status = 1, converted_json=? where id=? and version=?"),
  UPDATE_MIGRATION_STATUS_IN_EXPERIMENT_DEFINITION("update temp_experiment_definition set migration_status = 2 where id = ? and version = ? "),
  UPDATE_ERROR_MESSAGE_IN_EXPERIMENT_DEFINITION("update temp_experiment_definition set error_message = ? where id = ? and version = ?"),
  UPDATE_EVENTS_POSTED_FOR_EGVM_ID("update experiment_version_group_mapping set events_posted =b'1' where experiment_version_group_mapping_id= ? "),
  GET_ALL_EXPERIMENT_LITE_IN_EXPERIMENT_ID_VERSION("select * from temp_experiment_id_version_group_name where status=? order by experiment_id, experiment_version desc" ),
  GET_DISTINCT_EXPERIMENT_ID_VERSION("select distinct experiment_id, experiment_version from temp_experiment_id_version_group_name where status =? order by experiment_id, experiment_version desc " ),
  GET_ALL_EXPERIMENT_JSON_BK("select * from " + TempExperimentDefinitionColumns.TABLE_NAME+"_bk" ),
  GET_ANON_ID_FOR_EMAIL("select " + ExperimentUserColumns.EXP_USER_ANON_ID+ " from " + ExperimentUserColumns.TABLE_NAME + " join " + UserColumns.TABLE_NAME + " on " + UserColumns.TABLE_NAME +". "+ UserColumns.USER_ID +" = " + ExperimentUserColumns.TABLE_NAME  +  "." + ExperimentUserColumns.USER_ID + " where " + ExperimentUserColumns.EXPERIMENT_ID +" = ? and " + UserColumns.WHO + " = ?"),
  GET_COUNT_INPUT_COLLECTION_ID_IN_EXPERIMENT("select count(*) from experiment_version_group_mapping  where experiment_id= ? and  input_collection_id=?"),
  GET_ALL_EVENTS_WITHOUT_EVGM_IDS("select * from events where experiment_version_group_mapping_id is null "),
  GET_DISTINCT_EVG_OF_EVENTS_WITHOUT_EVGM_IDS("select distinct experiment_id, experiment_version, group_name from events where experiment_version_group_mapping_id is null "),
  GET_QUICK_STATUS_STORED_PROC("call ExpQuickStatus(?,?)"),
  GET_COMPLETE_STATUS_STORED_PROC("call ExpCompleteStatus(?,?)"),
  GET_QUICK_STATUS(" SELECT "+ EventServerColumns.WHO +",count(case when "+ OutputBaseColumns.NAME+" in ('apps_used', 'apps_used_raw') then 1 end) AS appusage, " +
          " count(case when "+ OutputBaseColumns.NAME +" in ('joined') then 1 end) AS joined, " +
          " count(case when "+ OutputBaseColumns.NAME + " not in ('apps_used', 'apps_used_raw','joined') then 1 end) as esm " + 
          " from "+ EventBaseColumns.TABLE_NAME+" e " +
          " join "+ OutputBaseColumns.TABLE_NAME +" o on e."+Constants.UNDERSCORE_ID +" = o."+OutputBaseColumns.EVENT_ID+" where "+ EventServerColumns.EXPERIMENT_ID+"=? and "+ EventServerColumns.WHO+" =? group by " +EventServerColumns.WHO),
  GET_COMPLETE_STATUS(" select "+EventBaseColumns.EXPERIMENT_ID+","+ EventServerColumns.WHO+","+ OutputBaseColumns.NAME+",count(0) noOfRecords from events e join outputs o on e._id = o.event_id " + 
                  " where "+ EventBaseColumns.EXPERIMENT_ID+"=? and "+EventServerColumns.WHO+"=? group by "+ EventServerColumns.EXPERIMENT_ID+", "+EventServerColumns.WHO+","+OutputBaseColumns.NAME),
  GET_TABLES_NAMES_IN_PACODB("select distinct table_name from information_schema.columns where table_schema = 'pacodb'"),
  SHOW_CREATE_TABLE("SHOW CREATE TABLE "),
  SHOW_ALL_STORED_PROCS_IN_PACODB("show procedure status where Db='pacodb'"),
  SHOW_CREATE_PROCEDURE("show create procedure "),
  SET_NAMES("SET NAMES  'utf8mb4'"),
  GET_PARTICIPANTS_QUERY("select "+ EventServerColumns.WHO +" from expwho where " + EventServerColumns.EXPERIMENT_ID+ " =?"),
  GET_ALL_DATATYPES("select * from " + DataTypeColumns.TABLE_NAME),
  GET_EXPERIMENT_DETAIL_ID("select * from " + ExperimentVersionGroupMappingColumns.TABLE_NAME + " where "+ ExperimentVersionGroupMappingColumns.EXPERIMENT_ID +" =? and "+ ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION + "=?"),
  GET_ALL_PRE_DEFINED_INPUTS("select * from " + GroupTypeInputMappingColumns.TABLE_NAME + " gtim join " + InputColumns.TABLE_NAME +
                             " i  on i."+ InputColumns.INPUT_ID +"=gtim." + GroupTypeInputMappingColumns.INPUT_ID + " join " + ExternStringInputColumns.TABLE_NAME +" esi1 on i."+ InputColumns.NAME_ID +" = esi1." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID
                             + " join "+ ExternStringInputColumns.TABLE_NAME +" esi2 on i.text_id = esi2." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID 
                             + " join "+ DataTypeColumns.TABLE_NAME  +" dt on i."+ InputColumns.RESPONSE_DATA_TYPE_ID  +"=dt."+ DataTypeColumns.DATA_TYPE_ID  +" "
                             + " join " + GroupTypeColumns.TABLE_NAME+ " gt on gt." + GroupTypeColumns.GROUP_TYPE_ID+ " = gtim." +GroupTypeInputMappingColumns.GROUP_TYPE_ID),
  GET_LABEL_ID_FOR_STRING("select * from " + ExternStringListLabelColumns.TABLE_NAME + " where "  + ExternStringListLabelColumns.LABEL + "= ?"),
  GET_INPUT_TEXT_ID_FOR_STRING("select * from " + ExternStringInputColumns.TABLE_NAME + " where "  + ExternStringInputColumns.LABEL + "= ?"),
  GET_LATEST_VERSION("SELECT "+ ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION +" FROM " + ExperimentVersionGroupMappingColumns.TABLE_NAME + " where "+ ExperimentVersionGroupMappingColumns.EXPERIMENT_ID +"=? order by "+ ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION +" desc limit 1") ,
  GET_ALL_VERSIONS("SELECT distinct "+ ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION +" FROM " + ExperimentVersionGroupMappingColumns.TABLE_NAME + " where "+ ExperimentVersionGroupMappingColumns.EXPERIMENT_ID +"=? ") ,
  GET_ALL_EVM_RECORDS_FOR_VERSION("select * from " + ExperimentVersionGroupMappingColumns.TABLE_NAME + " where "  + ExperimentVersionGroupMappingColumns.EXPERIMENT_ID + "= ? and " + ExperimentVersionGroupMappingColumns.EXPERIMENT_VERSION + "=?"),
  GET_ALL_GROUPS_IN_VERSION("select * from experiment_version_group_mapping evgm join experiment_detail eh on evgm.experiment_detail_id = eh.experiment_detail_id " + 
          " join group_detail gh on evgm.group_detail_id = gh.group_detail_id " +
          " join `group_type` gt on gh.group_type_id = gt.group_type_id " + 
          " left join input_collection ich on ich.input_collection_id = evgm.input_collection_id and ich.experiment_ds_id = evgm.experiment_id " +
          " left join input ih on ich.input_id = ih.input_id " +
          " join user u on eh.creator = u.user_id " +
          " left join choice_collection cch on ich.choice_collection_id = cch.choice_collection_id and ich.experiment_ds_id = cch.experiment_ds_id " +
          " left join informed_consent ic on eh.informed_consent_id = ic.informed_consent_id and evgm.experiment_id = ic.experiment_id " +
          " left join extern_string_list_label esll on cch.choice_id = esll.extern_string_list_label_id " + 
          " left join " + ExternStringInputColumns.TABLE_NAME + " esi1 on ih.text_id = esi1." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID + 
          " left join " + ExternStringInputColumns.TABLE_NAME + " esi2 on ih.name_id = esi2."+ ExternStringInputColumns.EXTERN_STRING_INPUT_ID +  
          " left join data_type dt on ih.response_data_type_id = dt.data_type_id " +
          " where evgm.experiment_id=? and evgm.experiment_version=? "),
  UNPROCESSED_EVENT_QUERY("select experiment_id, experiment_version, group_name, who, experiment_name, _id from events e  where experiment_version_group_mapping_id is null  limit 1000"),
  INSERT_TO_PIVOT_HELPER("insert into pivot_helper(experiment_version_group_mapping_id, anon_who, input_id)  select evm.experiment_version_group_mapping_id, eu.experiment_user_anon_id, ic.input_id from experiment_version_group_mapping evm " 
          + " join experiment_detail e on evm.experiment_detail_id = e.experiment_detail_id "
          + " join experiment_user eu on evm.experiment_id = eu.experiment_id "
          + " join input_collection ic on ic.experiment_ds_id = evm.experiment_id and  evm.input_collection_id=ic.input_collection_id"),
  DELETE_ALL_OUTPUTS("DELETE outputs FROM events LEFT JOIN outputs ON events._id = outputs.event_id WHERE events._id in (?)"),
  DELETE_ALL_EVENTS("DELETE events FROM events WHERE events._id in (?)"),
  GET_EVENT_IDS_OLD_FORMAT_ORDERED_BY_ID("select _id from events where experiment_id=? order by _id asc limit 250"),
  GET_EVENT_IDS_NEW_FORMAT_ORDERED_BY_ID("select _id from events join experiment_version_group_mapping evgm "  +
          " on evgm.experiment_version_group_mapping_id = events.experiment_version_group_mapping_id where evgm.experiment_id =? order by _id asc limit 250"),
  UPDATE_ALL_EVENTS("update events e join outputs o on e._id=o.event_id set experiment_version_group_mapping_id =1 where o.text like '%-DUP-%' and e.experiment_id=?"),
  INSERT_TEMP_EXPERIMENT_ID_VERSION_GROUP_NAME ("insert into temp_experiment_id_version_group_name(experiment_id, experiment_version, group_name)  select distinct experiment_id, experiment_version, group_name from events where experiment_id is not null"),
  GET_EXPERIMENTS_WITH_HUGE_INPUTSET("select experiment_ds_id, input_collection_id, count(*) from input_collection group by experiment_ds_id, input_collection_id " + 
                                     " having count(*) >150")
  
  ;
  
  private final String query;
  
  private QueryConstants(String query){
   this.query = query;
  }
  public String toString(){
    return  query;
  }
  
  static String q = "select * from group_type_input_mapping gtim join input " + 
  " i on i.input_id=gtim.input_id join extern_string_input " + 
  " esi1 on i.name_id = esi1.extern_string_input_id " + 
  " join extern_string_input esi2 on i.text_id = esi2.extern_string_input_id " +  
  " join data_type dt on i.response_data_type_id = dt.data_type_id " +
  " join group_type gt on gt.group_type_id = gtim.group_type_id";
}
