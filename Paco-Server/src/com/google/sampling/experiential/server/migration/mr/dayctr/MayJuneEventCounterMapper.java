package com.google.sampling.experiential.server.migration.mr.dayctr;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.Mapper;

/**
 * Maps each incoming Event into an intermediate key with a count of the number of outputs/
 * key: <year + ":" + month>
 * count : <n> where n = number of outputs (responses)
 */

public class MayJuneEventCounterMapper extends Mapper<Entity, String, Integer> {

  private static final long serialVersionUID = -3070710020513042698L;

  public static final Logger log = Logger.getLogger(MayJuneEventCounterMapper.class.getName());

  @Override

  public void map(Entity eventEntity) {
    String key = getMonthYearKeyFromWhen(eventEntity);
    if (key == null) {
      return;
    }
    int outputCount = getOutputCount(eventEntity);
    emit(key, outputCount);
  }

  private String getMonthYearKeyFromWhen(Entity eventEntity) {
    String key = null;
    Date when = (Date)eventEntity.getProperty("when");
    if (when == null) {
      key = null;
    } else {
      DateTime whenDateTime = new DateTime(when);
      int day = whenDateTime.getDayOfMonth();
      int month = whenDateTime.getMonthOfYear();
      key = month + ":" + day;
    }
    return key;
  }

  private int getOutputCount(Entity eventEntity) {
    List<String> keysList = (List<String>)eventEntity.getProperty("keysList");
    if (keysList != null) {
      return keysList.size();
    }
    return 0;
  }
}
