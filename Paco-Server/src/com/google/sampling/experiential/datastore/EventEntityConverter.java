package com.google.sampling.experiential.datastore;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import com.google.sampling.experiential.model.Event;
import com.google.sampling.experiential.model.PhotoBlob;
import com.google.sampling.experiential.model.What;

public class EventEntityConverter {

  public static Event convertEntityToEvent(Entity entity) {
    Long id = (Long) entity.getProperty("id");
    String who = (String) entity.getProperty("who");
    String lat = (String) entity.getProperty("lat");
    String lon = (String) entity.getProperty("lon");
    Date when = (Date) entity.getProperty("when");
    String appId = (String) entity.getProperty("appId");
    String pacoVersion = (String) entity.getProperty("pacoVersion");
    String experimentName = (String) entity.getProperty("experimentName");
    String experimentId = (String) entity.getProperty("experimentId");
    Integer experimentVersion = (Integer) entity.getProperty("experimentVersion");
    Date scheduledTime = (Date) entity.getProperty("scheduledTime");
    Date responseTime = (Date) entity.getProperty("responseTime");
    boolean shared = (Boolean) entity.getProperty("shared");
    String timeZone = (String) entity.getProperty("timeZone");

    Set<What> what = (Set<What>) entity.getProperty("what");
    List<String> keysList = (List<String>) entity.getProperty("keysList");
    List<String> valuesList = (List<String>) entity.getProperty("valuesList");
    List<PhotoBlob> blobs = (List<PhotoBlob>) entity.getProperty("blobs");

    Event event = new Event(who, lat, lon, when, appId, pacoVersion, what, shared, experimentId, experimentName, experimentVersion,
                           responseTime, scheduledTime, blobs, timeZone);
    event.setId(id);

    return event;
  }


}
