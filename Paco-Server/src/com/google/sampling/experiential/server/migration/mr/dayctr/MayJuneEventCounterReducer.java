package com.google.sampling.experiential.server.migration.mr.dayctr;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.Reducer;
import com.google.appengine.tools.mapreduce.ReducerInput;
import com.google.common.base.Splitter;

/**
 * Reduces all rows for each key into a summary stats entity
 */

public class MayJuneEventCounterReducer extends Reducer<String, Integer, Entity> {

  private static final long serialVersionUID = 188147370819557065L;
  private static final Logger log = Logger.getLogger(MayJuneEventCounterReducer.class.getName());

  @Override
  public void reduce(String key, ReducerInput<Integer> values) {
    Date writeDate = new Date();
    int totalEvents = 0;
    int totalOutputs = 0;

    while (values.hasNext()) {
      totalEvents++;
      totalOutputs += values.next();
    }

    List<String> parts = Splitter.on(":").splitToList(key);
    int month = Integer.parseInt(parts.get(0));
    int day = Integer.parseInt(parts.get(1));

    Entity monthYearCount = new Entity("EventDailyCounts");
    monthYearCount.setProperty("day", day);
    monthYearCount.setProperty("month", month);
    monthYearCount.setProperty("date_run", writeDate);
    monthYearCount.setUnindexedProperty("event_count", totalEvents);
    monthYearCount.setUnindexedProperty("output_count", totalOutputs);
    emit(monthYearCount);
  }

}
