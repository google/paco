package com.google.sampling.experiential.server.migration.mr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.Mapper;
import com.google.common.collect.Lists;
import com.google.sampling.experiential.server.stats.participation.LocalToUTCTimeZoneConverter;

/**
 * Maps each incoming Event into an intermediate key with a count
 * key: <experimentId + ":" + experimentGroupName + ":" + eventDate + ":" + who
 * count : <n,m,k> where n = scheduledResponse, m = missedResponse, k = selfResponse
 */

public class EventMapper extends Mapper<Entity, String, ArrayList<Integer>> {

  private static final long serialVersionUID = -3070710020513042698L;
  
  public static final Logger log = Logger.getLogger(EventMapper.class.getName());

  @Override

  public void map(Entity eventEntity) {
    
    List<String> keysList = (List<String>)eventEntity.getProperty("keysList");
    if (keysList != null && (keysList.contains("joined") || keysList.contains("schedule"))) {
      return;
    }
    
    String experimentIdStr = (String) eventEntity.getProperty("experimentId");
    String experimentGroupName = (String) eventEntity.getProperty("experimentGroupName");
    String who = (String) eventEntity.getProperty("who");

    Date rt = (Date) eventEntity.getProperty("responseTime");
    Date st = (Date) eventEntity.getProperty("scheduledTime");
    String tz = (String) eventEntity.getProperty("tz");

    ArrayList<Integer> stats = null;
    Long localDateNoZoneMillis = null;
    if (st != null && rt != null) {
      localDateNoZoneMillis = adjustTimeToTimezoneIfNecesssary(tz, st);
      stats = Lists.newArrayList(1, 0, 0);
    } else if (st != null && rt == null) {
      localDateNoZoneMillis = adjustTimeToTimezoneIfNecesssary(tz, st);
      stats = Lists.newArrayList(0, 1, 0);
    } else if (st == null && rt != null) {
      localDateNoZoneMillis = adjustTimeToTimezoneIfNecesssary(tz, rt);
      stats = Lists.newArrayList(0, 0, 1);
    }
    String key = experimentIdStr + ":" + experimentGroupName + ":" + localDateNoZoneMillis + ":" + who;
    emit(key, stats);
  }

  public static Long adjustTimeToTimezoneIfNecesssary(String tz, Date responseTime) {
    if (responseTime == null) {
      return null;
    }
    DateTimeZone timezone = null;
    if (tz != null) {
      timezone = DateTimeZone.forID(tz);
    }

    if (timezone != null && responseTime.getTimezoneOffset() != timezone.getOffset(responseTime.getTime())) {
      DateTime withZone = new DateTime(responseTime).withZone(timezone);
      DateTime localInUTCZone = LocalToUTCTimeZoneConverter.changeZoneToUTC(withZone);
      DateMidnight dm = localInUTCZone.toDateMidnight();
      return dm.getMillis();
    } else {
    
    DateTime dateTime = new DateTime(responseTime);
    DateTime localInUTCZone = LocalToUTCTimeZoneConverter.changeZoneToUTC(dateTime);
    DateMidnight dm = localInUTCZone.toDateMidnight();
    return dm.getMillis();
    }
  }

}
