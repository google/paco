package com.google.sampling.experiential.server;

import com.google.sampling.experiential.cloudsql.columns.DataTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.EventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentUserColumns;
import com.google.sampling.experiential.cloudsql.columns.ExperimentVersionMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringInputColumns;
import com.google.sampling.experiential.cloudsql.columns.ExternStringListLabelColumns;
import com.google.sampling.experiential.cloudsql.columns.FailedEventServerColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeColumns;
import com.google.sampling.experiential.cloudsql.columns.GroupTypeInputMappingColumns;
import com.google.sampling.experiential.cloudsql.columns.InputColumns;
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
  GET_EXPERIMENT_FACET_ID("select * from " + ExperimentVersionMappingColumns.TABLE_NAME + " where experiment_id=? and experiment_version=?"),
  GET_ALL_PRE_DEFINED_INPUTS("select * from " + GroupTypeInputMappingColumns.TABLE_NAME + " gtim join " + InputColumns.TABLE_NAME + " i  on i.input_id=gtim.input_id join extern_String_input esi1 on i.name_id = esi1.extern_string_input_id join extern_String_input esi2 on i.text_id = esi2.extern_string_input_id join data_type dt on i.data_type_id=dt.data_type_id join group_type gt on gt.group_type_id = gtim.group_type_id"),
  GET_LABEL_ID_FOR_STRING("select * from " + ExternStringListLabelColumns.TABLE_NAME + " where "  + ExternStringListLabelColumns.LABEL + "= ?"),
  GET_INPUT_TEXT_ID_FOR_STRING("select * from " + ExternStringInputColumns.TABLE_NAME + " where "  + ExternStringInputColumns.LABEL + "= ?"),
  GET_CLOSEST_VERSION("(SELECT experiment_version FROM pacodb.experiment_version_mapping where experiment_id=? and experiment_version >? order by experiment_version asc limit 1) " +
                      " union (SELECT  experiment_version FROM pacodb.experiment_version_mapping where experiment_id=? and experiment_version <? order by experiment_version desc limit 1 )"),
  GET_ALL_EVM_RECORDS_FOR_VERSION("select * from " + ExperimentVersionMappingColumns.TABLE_NAME + " where "  + ExperimentVersionMappingColumns.EXPERIMENT_ID + "= ? and " + ExperimentVersionMappingColumns.EXPERIMENT_VERSION + "=?");
  

  

  private final String query;
  
  private QueryConstants(String query){
   this.query = query;
  }
  public String toString(){
    return  query;
  }
}
