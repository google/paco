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
  GET_ALL_USERS_FOR_EXPERIMENT("select * from " + ExperimentUserColumns.TABLE_NAME + " join " + UserColumns.TABLE_NAME + " on " + UserColumns.TABLE_NAME +". "+ UserColumns.USER_ID +" = " + ExperimentUserColumns.TABLE_NAME  +  "." + ExperimentUserColumns.USER_ID + " where " + ExperimentUserColumns.EXPERIMENT_ID +" = ? "),
  GET_ALL_GROUP_TYPE("select * from " + GroupTypeColumns.TABLE_NAME ),
  
  GET_EVENT_FOR_ID("select * from " + EventServerColumns.TABLE_NAME + " where " + Constants.UNDERSCORE_ID+ " =?"),
  GET_ALL_OUTPUTS_FOR_EVENT_ID("select * from " + OutputBaseColumns.TABLE_NAME + " where " + OutputBaseColumns.EVENT_ID + " = ?"),
  GET_EXPERIMENT_DEFINITION_RECORD_COUNT("select count(*) from experiment_definition_bk"),
  GET_ALL_UNPROCESSED_PIVOT_HELPER("select * from " + PivotHelperColumns.TABLE_NAME + " where " + PivotHelperColumns.PROCESSED + " = b'0'"),
  GET_ALL_EXPERIMENT_JSON("select * from " + ExperimentDefinitionColumns.TABLE_NAME ),
  GET_ALL_ERRORED_EXPERIMENT_JSON("select id from " + ExperimentDefinitionColumns.TABLE_NAME +  " where error_message is not null" ),
  GET_ALL_EXPERIMENT_JSON_BK("select * from " + ExperimentDefinitionColumns.TABLE_NAME+"_bk where id in(4584120130732032, 4564354635661312, 4577054784749568)" ),
  GET_ANON_ID_FOR_EMAIL("select " +ExperimentUserColumns.EXP_USER_ANON_ID+ " from " + ExperimentUserColumns.TABLE_NAME + " join " + UserColumns.TABLE_NAME + " on " + UserColumns.TABLE_NAME +". "+ UserColumns.USER_ID +" = " + ExperimentUserColumns.TABLE_NAME  +  "." + ExperimentUserColumns.USER_ID + " where " + ExperimentUserColumns.EXPERIMENT_ID +" = ? and " + UserColumns.WHO + " = ?"),
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
  GET_CLOSEST_VERSION("(SELECT "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION +" FROM " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " where "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_ID +"=? and "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION +" >? order by "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION +" asc limit 1) " +
                      " union (SELECT  "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION +" FROM " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " where " + ExperimentGroupVersionMappingColumns.EXPERIMENT_ID +" =? and "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION +" <? order by "+ ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION +" desc limit 1 )"),
  GET_ALL_EVM_RECORDS_FOR_VERSION("select * from " + ExperimentGroupVersionMappingColumns.TABLE_NAME + " where "  + ExperimentGroupVersionMappingColumns.EXPERIMENT_ID + "= ? and " + ExperimentGroupVersionMappingColumns.EXPERIMENT_VERSION + "=?"),
  UNPROCESSED_EVENT_QUERY("select experiment_id, experiment_version, group_name, who, text, experiment_name, _id from events e join outputs o on e._id=o.event_id where (experiment_group_version_mapping_id is null or o.input_id is null) and experiment_id is not null limit 1"),
  INSERT_TO_PIVOT_HELPER("insert into pivot_helper(experiment_group_version_mapping_id, anon_who, input_id)  select evm.experiment_group_version_mapping_id, eu.experiment_user_anon_id, ic.input_id from experiment_group_version_mapping evm " 
          + " join experiment_detail e on evm.experiment_detail_id = e.experiment_detail_id "
          + " join experiment_user eu on evm.experiment_id = eu.experiment_id "
          + " join input_collection ic on ic.experiment_ds_id = evm.experiment_id and  evm.input_collection_id=ic.input_collection_id"),
  DELETE_ALL_OUTPUTS("DELETE outputs FROM events LEFT JOIN outputs ON events._id = outputs.event_id WHERE events.experiment_id=?"),
  DELETE_ALL_EVENTS("DELETE events FROM events LEFT JOIN outputs ON events._id = outputs.event_id WHERE events.experiment_id=?")
  ;
  

  

  private final String query;
  
  private QueryConstants(String query){
   this.query = query;
  }
  public String toString(){
    return  query;
  }
}
