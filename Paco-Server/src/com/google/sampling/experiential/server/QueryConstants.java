package com.google.sampling.experiential.server;

import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentDefinitionColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentGroupVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentUserColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.FailedEventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeInputMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
import com.google.sampling.experiential.cloudsql.columns.PivotHelperColumns;
import com.google.sampling.experiential.cloudsql.columns.UserColumns;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.Constants;

public enum QueryConstants {
  UPDATE_FAILED_EVENTS_PROCESSED_STATUS_FOR_ID("update "+ FailedEventServerColumns.TABLE_NAME +" set "+ FailedEventServerColumns.REPROCESSED+ " = ? where " + FailedEventServerColumns.ID + "= ?"),
  GET_ALL_UNPROCESSED_FAILED_EVENTS("select * from " + FailedEventServerColumns.TABLE_NAME + " where " + FailedEventServerColumns.REPROCESSED + "='false'"),
  GET_ALL_FAILED_EVENTS_STARTING_FROM_ID("select * from " + FailedEventServerColumns.TABLE_NAME + " where " + FailedEventServerColumns.ID + ">= ?"),
  GET_ALL_USERS_FOR_EXPERIMENT("select * from " + ExperimentUserColumns.TABLE_NAME + " join " + UserColumns.TABLE_NAME + " on " + UserColumns.TABLE_NAME +". "+ UserColumns.USER_ID +" = " + ExperimentUserColumns.TABLE_NAME  +  "." + ExperimentUserColumns.USER_ID + " where " + ExperimentUserColumns.EXPERIMENT_ID +" = ? "),
  GET_ALL_GROUP_TYPE("select * from " + GroupTypeColumns.TABLE_NAME ),
  GET_EVENT_FOR_ID("select * from " + EventServerColumns.TABLE_NAME + " where " + Constants.UNDERSCORE_ID+ " =?"),
  GET_ALL_OUTPUTS_FOR_EVENT_ID("select * from " + OutputBaseColumns.TABLE_NAME + " where " + OutputBaseColumns.EVENT_ID + " = ?"),
  GET_ALL_OUTPUTS_WITHOUT_INPUTID_FOR_EVENT_ID("select * from " + OutputBaseColumns.TABLE_NAME + " where " + OutputBaseColumns.EVENT_ID + " = ? and input_id is null"),
  GET_DISTINCT_OUTPUTS_FOR_EXPERIMENT_ID("select count(distinct text) from events e join outputs o on e._id=o.event_id where experiment_id=?"),
  GET_EXPERIMENT_DEFINITION_RECORD_COUNT("select count(*) from experiment_definition_bk"),
  GET_EGVM_ID_FOR_EXP_ID_AND_VERSION("select "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_GROUP_VERSION_MAPPING_ID + " from " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " evm "+
                                    " where " + ExperimentGroupVersionMappingColumns.EXPERIMENT_ID + " = ? and  " + ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION + " = ? " ),
  GET_ALL_UNPROCESSED_PIVOT_HELPER("select * from " + PivotHelperColumns.TABLE_NAME + " where " + PivotHelperColumns.PROCESSED + " = b'0'"),
  GET_ALL_EXPERIMENT_JSON("select * from " + ExperimentDefinitionColumns.TABLE_NAME + " where "+ExperimentDefinitionColumns.MIGRATION_STATUS +" = ?"),
  GET_ALL_ERRORED_EXPERIMENT_JSON("select id from " + ExperimentDefinitionColumns.TABLE_NAME +  " where error_message is not null" ),
  GET_TO_BE_DELETED_EXPERIMENTS("select distinct experiment_id from experiment_id_version_group_name where experiment_id not in (select id from experiment_definition)" ),
  DELETE_EXPERIMENTS_IN_EXPERIMENT_ID_VERSION("delete from experiment_id_version_group_name where experiment_id = ?" ),
  DELETE_EXPERIMENTS_WITH_VERSION_IN_EXPERIMENT_ID_VERSION("delete from experiment_id_version_group_name where experiment_id = ? and experiment_version = ?" ),
  UPDATE_INPUT_COLLECTION_ID_FOR_EVGM_ID("update experiment_group_version_mapping set input_collection_id=? where experiment_group_version_mapping_id= ? "),
  UPDATE_EXPERIMENT_ID_VERSION_GROUP_NAME_STATUS_IN_EXPERIMENT_ID_VERSION("update experiment_id_version_group_name set status=? where experiment_id = ? and experiment_version = ? and group_name=?" ),
  UPDATE_EXPERIMENT_ID_VERSION_STATUS_IN_EXPERIMENT_ID_VERSION("update experiment_id_version_group_name set status=? where experiment_id = ? and experiment_version = ?" ),
  UPDATE_EVENTS_WITH_NEW_GROUP_NAME("update events set group_name=? where _id=?"),
  INSERT_TO_OLD_GROUP_NAME_TABLE("insert into event_old_group_name(old_group_name,event_id) values (?,?)"),
  INSERT_TO_PIVOT_HELPER_WITH_ON_DUPLICATE_CLAUSE("INSERT INTO pivot_helper (experiment_group_version_mapping_id, anon_who, input_id, events_posted, processed) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE events_posted=events_posted+1"),
  DELETE_FROM_EXPERIMENT_DEFINITION("delete from experiment_definition where id = ? "),
  UPDATE_SPLIT_JSON_IN_EXPERIMENT_DEFINITION("update experiment_definition set migration_status = 1, converted_json=? where id=? and version=?"),
  UPDATE_MIGRATION_STATUS_IN_EXPERIMENT_DEFINITION("update experiment_definition set migration_status = 2 where id = ? and version = ? "),
  UPDATE_ERROR_MESSAGE_IN_EXPERIMENT_DEFINITION("update experiment_definition set error_message = ? where id = ? and version = ?"),
  UPDATE_EVENTS_POSTED_FOR_EGVM_ID("update experiment_group_version_mapping set events_posted =b'1' where experiment_group_version_mapping_id= ? "),
  GET_ALL_EXPERIMENT_LITE_IN_EXPERIMENT_ID_VERSION("select * from experiment_id_version_group_name where status=? order by experiment_id, experiment_version desc" ),
  GET_DISTINCT_EXPERIMENT_ID_VERSION("select distinct experiment_id, experiment_version from experiment_id_version_group_name where status =? order by experiment_id, experiment_version desc " ),
  GET_ALL_EXPERIMENT_JSON_BK("select * from " + ExperimentDefinitionColumns.TABLE_NAME+"_bk where id in(4584120130732032, 4564354635661312, 4577054784749568)" ),
  GET_ANON_ID_FOR_EMAIL("select " + ExperimentUserColumns.EXP_USER_ANON_ID+ " from " + ExperimentUserColumns.TABLE_NAME + " join " + UserColumns.TABLE_NAME + " on " + UserColumns.TABLE_NAME +". "+ UserColumns.USER_ID +" = " + ExperimentUserColumns.TABLE_NAME  +  "." + ExperimentUserColumns.USER_ID + " where " + ExperimentUserColumns.EXPERIMENT_ID +" = ? and " + UserColumns.WHO + " = ?"),
  GET_COUNT_INPUT_COLLECTION_ID_IN_EXPERIMENT("select count(*) from experiment_group_version_mapping  where experiment_id= ? and  input_collection_id=?"),
  GET_QUICK_STATUS_STORED_PROC("call ExpQuickStatus(?,?)"),
  GET_COMPLETE_STATUS_STORED_PROC("call ExpCompleteStatus(?,?)"),
  GET_QUICK_STATUS(" SELECT "+ EventServerColumns.WHO +",count(case when "+ OutputBaseColumns.NAME+" in ('apps_used', 'apps_used_raw') then 1 end) AS appusage, " +
          " count(case when "+ OutputBaseColumns.NAME +" in ('joined') then 1 end) AS joined, " +
          " count(case when "+ OutputBaseColumns.NAME + " not in ('apps_used', 'apps_used_raw','joined') then 1 end) as esm " + 
          " from "+ EventBaseColumns.TABLE_NAME+" e " +
          " join "+ OutputBaseColumns.TABLE_NAME +" o on e."+Constants.UNDERSCORE_ID +" = o."+OutputBaseColumns.EVENT_ID+" where "+ EventServerColumns.EXPERIMENT_ID+"=? and "+ EventServerColumns.WHO+" =? group by " +EventServerColumns.WHO),
  GET_COMPLETE_STATUS(" select "+EventBaseColumns.EXPERIMENT_ID+","+ EventServerColumns.WHO+","+ OutputBaseColumns.NAME+",count(0) noOfRecords from events e join outputs o on e._id = o.event_id " + 
                  " where "+ EventBaseColumns.EXPERIMENT_ID+"=? and "+EventServerColumns.WHO+"=? group by "+ EventServerColumns.EXPERIMENT_ID+", "+EventServerColumns.WHO+","+OutputBaseColumns.NAME),
  SET_NAMES("SET NAMES  'utf8mb4'"),
  GET_PARTICIPANTS_QUERY("select "+ EventServerColumns.WHO +" from expwho where " + EventServerColumns.EXPERIMENT_ID+ " =?"),
  GET_ALL_DATATYPES("select * from " + DataTypeColumns.TABLE_NAME),
  GET_EXPERIMENT_DETAIL_ID("select * from " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " where "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_ID +" =? and "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION + "=?"),
  GET_ALL_PRE_DEFINED_INPUTS("select * from " + GroupTypeInputMappingColumns.TABLE_NAME + " gtim join " + InputColumns.TABLE_NAME +
                             " i  on i."+ InputColumns.INPUT_ID +"=gtim." + GroupTypeInputMappingColumns.INPUT_ID + " join " + ExternStringInputColumns.TABLE_NAME +" esi1 on i."+ InputColumns.NAME_ID +" = esi1." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID
                             + " join "+ ExternStringInputColumns.TABLE_NAME +" esi2 on i.text_id = esi2." + ExternStringInputColumns.EXTERN_STRING_INPUT_ID 
                             + " join "+ DataTypeColumns.TABLE_NAME  +" dt on i."+ InputColumns.RESPONSE_DATA_TYPE_ID  +"=dt."+ DataTypeColumns.DATA_TYPE_ID  +" "
                             + " join " + GroupTypeColumns.TABLE_NAME+ " gt on gt." + GroupTypeColumns.GROUP_TYPE_ID+ " = gtim." +GroupTypeInputMappingColumns.GROUP_TYPE_ID),
  GET_LABEL_ID_FOR_STRING("select * from " + ExternStringListLabelColumns.TABLE_NAME + " where "  + ExternStringListLabelColumns.LABEL + "= ?"),
  GET_INPUT_TEXT_ID_FOR_STRING("select * from " + ExternStringInputColumns.TABLE_NAME + " where "  + ExternStringInputColumns.LABEL + "= ?"),
  GET_CLOSEST_VERSION("SELECT "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION +" FROM " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " where "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_ID +"=? order by "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION +" desc limit 1") ,
  GET_ALL_EVM_RECORDS_FOR_VERSION("select * from " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " where "  + ExperimentGroupVersionMappingColumns.EXPERIMENT_ID + "= ? and " + ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION + "=?"),
  GET_ALL_GROUPS_IN_VERSION("select * from experiment_group_version_mapping evmh join experiment_detail eh on evmh.experiment_detail_id = eh.experiment_detail_id " + 
          " join group_detail gh on evmh.group_detail_id = gh.group_detail_id " +
          " join `group_type` gt on gh.group_type_id = gt.group_type_id " + 
          " left join input_collection ich on ich.input_collection_id = evmh.input_collection_id and ich.experiment_ds_id = evmh.experiment_id " +
          " left join input ih on ich.input_id = ih.input_id " +
          " join user u on eh.creator = u.user_id " +
          " left join choice_collection cch on ich.choice_collection_id = cch.choice_collection_id and ich.experiment_ds_id = cch.experiment_ds_id " +
          " left join informed_consent ic on eh.informed_consent_id = ic.informed_consent_id and evmh.experiment_id = ic.experiment_id " +
          " left join extern_string_list_label esll on cch.choice_id = esll.extern_string_list_label_id " + 
          " left join extern_string_input esi1 on ih.text_id = esi1.extern_string_input_id " + 
          " left join extern_string_input esi2 on ih.name_id = esi2.extern_string_input_id " +
          " left join data_type dt on ih.response_data_type_id = dt.data_type_id " +
          " where evmh.experiment_id=? and evmh.experiment_version=? "),
  UNPROCESSED_EVENT_QUERY("select experiment_id, experiment_version, group_name, who, experiment_name, _id from events e  where experiment_group_version_mapping_id is null  limit 1000"),
  INSERT_TO_PIVOT_HELPER("insert into pivot_helper(experiment_group_version_mapping_id, anon_who, input_id)  select evm.experiment_group_version_mapping_id, eu.experiment_user_anon_id, ic.input_id from experiment_group_version_mapping evm " 
          + " join experiment_detail e on evm.experiment_detail_id = e.experiment_detail_id "
          + " join experiment_user eu on evm.experiment_id = eu.experiment_id "
          + " join input_collection ic on ic.experiment_ds_id = evm.experiment_id and  evm.input_collection_id=ic.input_collection_id"),
  DELETE_ALL_OUTPUTS("DELETE outputs FROM events LEFT JOIN outputs ON events._id = outputs.event_id WHERE events.experiment_id=?"),
  DELETE_ALL_EVENTS("DELETE events FROM events WHERE events.experiment_id=?"),
  INSERT_EXPERIMENT_ID_VERSION_GROUP_NAME ("insert into experiment_id_version_group_name(experiment_id, experiment_version, group_name)  select distinct experiment_id, experiment_version, group_name from events")
  ;
  

  

  private final String query;
  
  private QueryConstants(String query){
   this.query = query;
  }
  public String toString(){
    return  query;
  }
}
