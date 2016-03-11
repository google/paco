package com.google.sampling.experiential.server.migration.mr;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.Reducer;
import com.google.appengine.tools.mapreduce.ReducerInput;
import com.google.common.base.Splitter;
import com.google.sampling.experiential.server.stats.participation.ResponseStatEntityManager;

/**
 * Reduces all rows for each key into a summary stats entity 
 */

public class ResponseStatReducer extends Reducer<String, ArrayList<Integer>, Entity> {

  private static final long serialVersionUID = 188147370819557065L;
  private static final Logger log = Logger.getLogger(ResponseStatReducer.class.getName());

  @Override
  public void reduce(String key, ReducerInput<ArrayList<Integer>> values) {
    int schedRTotal = 0;
    int missedRTotal = 0;
    int selfRTotal = 0;
    
    while (values.hasNext()) {
      List<Integer> val = values.next();
      
      schedRTotal += val.get(0);
      missedRTotal += val.get(1);
      selfRTotal += val.get(2);
    }
    
    List<String> parts = Splitter.on(":").splitToList(key);
    long experimentId = Long.parseLong(parts.get(0));
    String experimentGroupName = parts.get(1);
    long dateMidnightUtcMillis = Long.parseLong(parts.get(2));
    String who = parts.get(3);
    
        
    Entity whoResult = new Entity(ResponseStatEntityManager.KIND);
    whoResult.setProperty(ResponseStatEntityManager.EXPERIMENT_ID_PROPERTY, experimentId);    
    whoResult.setProperty(ResponseStatEntityManager.EXPERIMENT_GROUP_NAME_PROPERTY, experimentGroupName);    
    whoResult.setProperty(ResponseStatEntityManager.WHO_PROPERTY, who);
    whoResult.setProperty(ResponseStatEntityManager.DATE_PROPERTY, dateMidnightUtcMillis);
    whoResult.setUnindexedProperty(ResponseStatEntityManager.SCHED_R_PROPERTY, schedRTotal);
    whoResult.setUnindexedProperty(ResponseStatEntityManager.MISSED_R_PROPERTY, missedRTotal);
    whoResult.setUnindexedProperty(ResponseStatEntityManager.SELF_R_PROPERTY, selfRTotal);
    whoResult.setUnindexedProperty(ResponseStatEntityManager.LAST_CONTACT_DATE_TIME_PROPERTY, new DateTime(dateMidnightUtcMillis).toDate());
    
    
    emit(whoResult);
  }

}
