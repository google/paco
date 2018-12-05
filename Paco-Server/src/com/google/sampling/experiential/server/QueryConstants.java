package com.google.sampling.experiential.server;

import com.google.sampling.experiential.datastore.EventServerColumns;
import com.google.sampling.experiential.datastore.FailedEventServerColumns;
import com.pacoapp.paco.shared.model2.EventBaseColumns;
import com.pacoapp.paco.shared.model2.OutputBaseColumns;
import com.pacoapp.paco.shared.util.Constants;

public enum QueryConstants {
  UPDATE_FAILED_EVENTS_PROCESSED_STATUS_FOR_ID("update "+ FailedEventServerColumns.TABLE_NAME +" set "+ FailedEventServerColumns.REPROCESSED+ " = ? where " + FailedEventServerColumns.ID + "= ?"),
  GET_ALL_UNPROCESSED_FAILED_EVENTS("select * from " + FailedEventServerColumns.TABLE_NAME + " where " + FailedEventServerColumns.REPROCESSED + "='false'"),
  GET_EVENT_FOR_ID("select * from " + EventServerColumns.TABLE_NAME + " where " + Constants.UNDERSCORE_ID+ " =?"),
  GET_ALL_OUTPUTS_FOR_EVENT_ID("select * from " + OutputBaseColumns.TABLE_NAME + " where " + OutputBaseColumns.EVENT_ID + " = ?"),
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
  GET_PARTICIPANTS_QUERY("select "+ EventServerColumns.WHO +" from expwho where " + EventServerColumns.EXPERIMENT_ID+ " =?");
  

  private final String query;
  
  private QueryConstants(String query){
   this.query = query;
  }
  public String toString(){
    return  query;
  }
}
