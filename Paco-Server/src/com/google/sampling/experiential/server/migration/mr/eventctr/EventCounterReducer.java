package com.google.sampling.experiential.server.migration.mr.eventctr;

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

public class EventCounterReducer extends Reducer<String, Integer, Entity> {

  private static final long serialVersionUID = 188147370819557065L;
  private static final Logger log = Logger.getLogger(EventCounterReducer.class.getName());

  @Override
  public void reduce(String key, ReducerInput<Integer> values) {
    int totalEvents = 0;
    int totalOutputs = 0;

    while (values.hasNext()) {
      totalEvents++;
      totalOutputs += values.next();
    }

    List<String> parts = Splitter.on(":").splitToList(key);
    int year = Integer.parseInt(parts.get(0));
    int month = Integer.parseInt(parts.get(1));

    Entity monthYearCount = new Entity("EventCounts");
    monthYearCount.setProperty("year", year);
    monthYearCount.setProperty("month", month);
    monthYearCount.setProperty("date_run", new Date());
    monthYearCount.setUnindexedProperty("event_count", totalEvents);
    monthYearCount.setUnindexedProperty("output_count", totalOutputs);
    emit(monthYearCount);
  }

}
